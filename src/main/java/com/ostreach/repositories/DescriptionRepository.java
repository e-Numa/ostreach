package com.ostreach.repositories;

import com.ostreach.entities.model.DescriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DescriptionRepository extends JpaRepository<DescriptionEntity, Long> {
    Optional<DescriptionEntity> findByOrderEntityId(Long id);
    Optional<DescriptionEntity> findByOrderEntityTrackingNum(String trackingNum);
}
