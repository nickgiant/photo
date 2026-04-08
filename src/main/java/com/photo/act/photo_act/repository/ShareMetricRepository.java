package com.photo.act.photo_act.repository;


import com.photo.act.photo_act.model.ShareMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ShareMetricRepository
        extends JpaRepository<ShareMetric, Long> {

    Optional<ShareMetric> findByResourceTypeAndResourceIdAndPlatform(
            String resourceType,
            String resourceId,
            String platform);
}
