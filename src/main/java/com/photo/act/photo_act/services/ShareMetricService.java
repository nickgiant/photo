package com.photo.act.photo_act.services;


import com.photo.act.photo_act.model.ShareMetric;
import com.photo.act.photo_act.model.ShareableResource;
import com.photo.act.photo_act.repository.ShareMetricRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShareMetricService {

    private final ShareMetricRepository repository;

    public ShareMetricService(ShareMetricRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void increment(String platform, ShareableResource resource) {

        ShareMetric metric = repository
                .findByResourceTypeAndResourceIdAndPlatform(
                        resource.type().name(),
                        resource.id(),
                        platform)
                .orElseGet(() ->
                        new ShareMetric(
                                resource.type().name(),
                                resource.id(),
                                platform));

        metric.increment();
        repository.save(metric);
    }

    public long getCount(String platform,
                         ShareableResource resource) {

        return repository
                .findByResourceTypeAndResourceIdAndPlatform(
                        resource.type().name(),
                        resource.id(),
                        platform)
                .map(ShareMetric::getShareCount)
                .orElse(0L);
    }
}
