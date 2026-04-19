package com.photo.act.photo_act.services;

import com.photo.act.photo_act.dto.NewsCategoryDto;
import com.photo.act.photo_act.dto.NewsCreateDto;
import com.photo.act.photo_act.dto.NewsDto;
import com.photo.act.photo_act.dto.NewsItemDto;
import com.photo.act.photo_act.model.*;
import com.photo.act.photo_act.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Core service for the News section.
 *
 * Cache names:
 *   news-item        – single NewsDto by id            TTL 30 min
 *   news-list        – paged news by category / latest TTL 10 min
 *   news-categories  – all categories with stats       TTL 5 min
 *
 * Redis atomic counters (StringRedisTemplate):
 *   news:views:{id}  – incremented on every accepted view event
 *   news:likes:{id}  – set to DB count, incremented on new like
 */
@Service
public class NewsService {

    private static final Logger log = LoggerFactory.getLogger(NewsService.class);

    /** View dedup window: same IP counts as one view within this many hours. */
    private static final int VIEW_DEDUP_HOURS = 24;

    private static final String REDIS_VIEWS_KEY = "news:views:";
    private static final String REDIS_LIKES_KEY = "news:likes:";

    private final NewsRepository          newsRepo;
    private final NewsItemRepository      itemRepo;
    private final NewsCategoryRepository  categoryRepo;
    private final NewsViewRepository      viewRepo;
    private final NewsLikeRepository      likeRepo;
    private final StringRedisTemplate     redis;

    public NewsService(NewsRepository newsRepo,
                       NewsItemRepository itemRepo,
                       NewsCategoryRepository categoryRepo,
                       NewsViewRepository viewRepo,
                       NewsLikeRepository likeRepo,
                       StringRedisTemplate redis) {
        this.newsRepo     = newsRepo;
        this.itemRepo     = itemRepo;
        this.categoryRepo = categoryRepo;
        this.viewRepo     = viewRepo;
        this.likeRepo     = likeRepo;
        this.redis        = redis;
    }

    // ──────────────────────────── Categories ────────────────────────────────

    /** All categories with computed stats (count, last date, views, likes, authors). */
    @Cacheable("news-categories")
    public List<NewsCategoryDto> getAllCategories() {
        return categoryRepo.findAllByOrderByTitleAsc().stream()
                .map(this::buildCategoryDto)
                .toList();
    }

    @Cacheable(value = "news-categories", key = "#id")
    public Optional<NewsCategoryDto> getCategoryById(Long id) {
        return categoryRepo.findById(id).map(this::buildCategoryDto);
    }

    @Transactional
    @CacheEvict(value = "news-categories", allEntries = true)
    public NewsCategoryDto createCategory(String title, String description) {
        if (categoryRepo.existsByTitle(title)) {
            throw new IllegalArgumentException("Category already exists: " + title);
        }
        NewsCategoryEntity saved = categoryRepo.save(new NewsCategoryEntity(title, description));
        return buildCategoryDto(saved);
    }

    @Transactional
    @CacheEvict(value = "news-categories", allEntries = true)
    public Optional<NewsCategoryDto> updateCategory(Long id, String title, String description) {
        return categoryRepo.findById(id).map(cat -> {
            cat.setTitle(title);
            cat.setDescription(description);
            return buildCategoryDto(categoryRepo.save(cat));
        });
    }

    private NewsCategoryDto buildCategoryDto(NewsCategoryEntity cat) {
        long          count     = categoryRepo.countNewsByCategoryId(cat.getId());
        LocalDateTime lastAt    = categoryRepo.findLastNewDateByCategoryId(cat.getId()).orElse(null);
        long          views     = categoryRepo.countViewsByCategoryId(cat.getId());
        long          likes     = categoryRepo.countLikesByCategoryId(cat.getId());
        long          authors   = categoryRepo.countDistinctAuthorsByCategoryId(cat.getId());
        String        timeAgo   = lastAt != null ? timeAgo(lastAt) : "—";
        return NewsCategoryDto.from(cat, count, lastAt, timeAgo, views, likes, authors);
    }

    // ──────────────────────────────── News ──────────────────────────────────

    /** Full news entry with items, view count, and like count — cached by id. */
    @Cacheable(value = "news-item", key = "#id")
    public Optional<NewsDto> getNewsById(Long id) {
        return newsRepo.findById(id).map(this::buildNewsDto);
    }

    /** Paged news for a specific category. */
    @Cacheable(value = "news-list", key = "'cat-' + #categoryId + '-p' + #page")
    public Page<NewsDto> getNewsByCategory(Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return newsRepo.findByCategoryIdOrderByCreatedAtDesc(categoryId, pageable)
                       .map(this::buildNewsDto);
    }

    /** Latest news across all categories. */
    @Cacheable(value = "news-list", key = "'latest-p' + #page")
    public Page<NewsDto> getLatestNews(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return newsRepo.findAllByOrderByCreatedAtDesc(pageable)
                       .map(this::buildNewsDto);
    }

    /** News created by a specific user (no caching — personal feed). */
    public List<NewsDto> getNewsByUser(Integer userId) {
        return newsRepo.findByUserIdOrderByCreatedAtDesc(userId).stream()
                       .map(this::buildNewsDto)
                       .toList();
    }

    /** Full-text keyword search across title, description, original_author. */
    public Page<NewsDto> searchNews(String keyword, int page, int size) {
        return newsRepo.searchByKeyword(keyword, PageRequest.of(page, size))
                       .map(this::buildNewsDto);
    }

    /** Create a news entry with optional items. Requires an authenticated user. */
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "news-list",       allEntries = true),
        @CacheEvict(value = "news-categories", allEntries = true)
    })
    public NewsDto createNews(NewsCreateDto dto, Integer userId) {
        NewsEntity news = newsRepo.save(new NewsEntity(
                dto.getTitle(), dto.getDescription(), dto.getPhotoId(),
                userId, dto.getOriginalAuthor(), dto.getOriginalUrl(), dto.getCategoryId()));

        int order = 0;
        for (NewsCreateDto.NewsItemCreateDto itemDto : dto.getItems()) {
            itemRepo.save(new NewsItemEntity(
                    news.getId(), itemDto.getTitle(), itemDto.getDescription(),
                    itemDto.getPhotoId(), itemDto.getVideo(),
                    itemDto.getUrlMore1(), itemDto.getUrlMore2(),
                    itemDto.getUrlMore3(), itemDto.getUrlMore4(),
                    itemDto.getSortOrder() != null ? itemDto.getSortOrder() : order));
            order++;
        }
        return buildNewsDto(news);
    }

    /** Update headline fields of an existing news entry. */
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "news-item",       key = "#id"),
        @CacheEvict(value = "news-list",       allEntries = true),
        @CacheEvict(value = "news-categories", allEntries = true)
    })
    public Optional<NewsDto> updateNews(Long id, NewsCreateDto dto) {
        return newsRepo.findById(id).map(news -> {
            news.setTitle(dto.getTitle());
            news.setDescription(dto.getDescription());
            news.setPhotoId(dto.getPhotoId());
            news.setOriginalAuthor(dto.getOriginalAuthor());
            news.setOriginalUrl(dto.getOriginalUrl());
            news.setCategoryId(dto.getCategoryId());
            return buildNewsDto(newsRepo.save(news));
        });
    }

    /** Replace all items for a news entry (delete existing, insert new). */
    @Transactional
    @CacheEvict(value = "news-item", key = "#newsId")
    public void replaceItems(Long newsId, List<NewsCreateDto.NewsItemCreateDto> itemDtos) {
        itemRepo.deleteByNewsId(newsId);
        int order = 0;
        for (NewsCreateDto.NewsItemCreateDto dto : itemDtos) {
            itemRepo.save(new NewsItemEntity(
                    newsId, dto.getTitle(), dto.getDescription(),
                    dto.getPhotoId(), dto.getVideo(),
                    dto.getUrlMore1(), dto.getUrlMore2(),
                    dto.getUrlMore3(), dto.getUrlMore4(),
                    dto.getSortOrder() != null ? dto.getSortOrder() : order));
            order++;
        }
    }

    private NewsDto buildNewsDto(NewsEntity news) {
        List<NewsItemDto> items = itemRepo.findByNewsIdOrderBySortOrderAsc(news.getId())
                                          .stream().map(NewsItemDto::from).toList();
        String categoryTitle = news.getCategoryId() != null
                ? categoryRepo.findById(news.getCategoryId())
                              .map(NewsCategoryEntity::getTitle).orElse(null)
                : null;
        long views = getViewCount(news.getId());
        long likes = getLikeCount(news.getId());
        return NewsDto.from(news, categoryTitle, items, views, likes);
    }

    // ──────────────────────────────── Views ─────────────────────────────────

    /**
     * Records a view. Deduplication: same IP within {@value #VIEW_DEDUP_HOURS}h is skipped.
     * Also increments the Redis counter for fast reads.
     */
    @Transactional
    public void recordView(Long newsId, Integer userId, String ip,
                           String sessionId, LocalDateTime sessionDateTime) {
        if (ip == null || ip.isBlank()) ip = "unknown";
        try {
            LocalDateTime since = LocalDateTime.now().minusHours(VIEW_DEDUP_HOURS);
            if (!viewRepo.existsRecentView(newsId, ip, since)) {
                viewRepo.save(new NewsViewEntity(newsId, userId, ip, sessionId, sessionDateTime));
                redis.opsForValue().increment(REDIS_VIEWS_KEY + newsId);
                log.debug("Recorded view for news {} from {}", newsId, ip);
            }
        } catch (Exception e) {
            log.error("Error recording view for news {}: {}", newsId, e.getMessage());
        }
    }

    // ──────────────────────────────── Likes ─────────────────────────────────

    /**
     * Toggles like for a news entry by IP. Returns true if the like was added,
     * false if already liked (no duplicate likes per IP).
     */
    @Transactional
    public boolean toggleLike(Long newsId, Integer userId, String ip,
                               String sessionId, LocalDateTime sessionDateTime) {
        if (ip == null || ip.isBlank()) ip = "unknown";
        try {
            if (likeRepo.existsByNewsIdAndIpAddress(newsId, ip)) {
                return false;
            }
            likeRepo.save(new NewsLikeEntity(newsId, userId, ip, sessionId, sessionDateTime));
            redis.opsForValue().increment(REDIS_LIKES_KEY + newsId);
            log.debug("Recorded like for news {} from {}", newsId, ip);
            return true;
        } catch (Exception e) {
            log.error("Error recording like for news {}: {}", newsId, e.getMessage());
            return false;
        }
    }

    public boolean hasLiked(Long newsId, String ip) {
        if (ip == null || ip.isBlank()) return false;
        try {
            return likeRepo.existsByNewsIdAndIpAddress(newsId, ip);
        } catch (Exception e) {
            return false;
        }
    }

    // ─────────────────────── Count helpers (Redis-first) ────────────────────

    public long getViewCount(Long newsId) {
        try {
            String key   = REDIS_VIEWS_KEY + newsId;
            String value = redis.opsForValue().get(key);
            if (value != null) return Long.parseLong(value);
            long count = viewRepo.countByNewsId(newsId);
            redis.opsForValue().set(key, String.valueOf(count), Duration.ofMinutes(30));
            return count;
        } catch (Exception e) {
            log.error("Error fetching view count for news {}: {}", newsId, e.getMessage());
            return fallbackViewCount(newsId);
        }
    }

    public long getLikeCount(Long newsId) {
        try {
            String key   = REDIS_LIKES_KEY + newsId;
            String value = redis.opsForValue().get(key);
            if (value != null) return Long.parseLong(value);
            long count = likeRepo.countDistinctLikersByNewsId(newsId);
            redis.opsForValue().set(key, String.valueOf(count), Duration.ofMinutes(30));
            return count;
        } catch (Exception e) {
            log.error("Error fetching like count for news {}: {}", newsId, e.getMessage());
            return fallbackLikeCount(newsId);
        }
    }

    private long fallbackViewCount(Long newsId) {
        try { return viewRepo.countByNewsId(newsId); } catch (Exception ex) { return 0; }
    }

    private long fallbackLikeCount(Long newsId) {
        try { return likeRepo.countDistinctLikersByNewsId(newsId); } catch (Exception ex) { return 0; }
    }

    // ─────────────────────────────── Util ───────────────────────────────────

    private String timeAgo(LocalDateTime dateTime) {
        LocalDateTime now     = LocalDateTime.now();
        long          minutes = ChronoUnit.MINUTES.between(dateTime, now);
        if (minutes < 1)   return "just now";
        if (minutes < 60)  return minutes + " min" + (minutes == 1 ? "" : "s") + " ago";
        long hours = ChronoUnit.HOURS.between(dateTime, now);
        if (hours < 24)    return hours + " hr" + (hours == 1 ? "" : "s") + " ago";
        long days = ChronoUnit.DAYS.between(dateTime, now);
        if (days < 30)     return days + " day" + (days == 1 ? "" : "s") + " ago";
        long months = ChronoUnit.MONTHS.between(dateTime, now);
        if (months < 12)   return months + " mo" + (months == 1 ? "" : "s") + " ago";
        long years = ChronoUnit.YEARS.between(dateTime, now);
        return years + " yr" + (years == 1 ? "" : "s") + " ago";
    }
}
