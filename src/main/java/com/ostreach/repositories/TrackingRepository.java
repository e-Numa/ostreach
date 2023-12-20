package com.ostreach.repositories;

import com.ostreach.entities.model.TrackingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrackingRepository extends JpaRepository<TrackingEntity, Long> {
    TrackingEntity findByOrderEntityTrackingNum(String trackingNum);
}