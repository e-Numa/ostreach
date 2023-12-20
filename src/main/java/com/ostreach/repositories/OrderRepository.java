package com.ostreach.repositories;

import com.ostreach.entities.enums.OrderStatus;
import com.ostreach.entities.enums.StatusConstant;
import com.ostreach.entities.model.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    List<OrderEntity> findAllByStatusAndTaskEntityIsNull(OrderStatus status);
    Page<OrderEntity> findOrderEntitiesByRecordStatusOrderByDateCreatedDesc(StatusConstant statusConstant, Pageable pageable);
    Page<OrderEntity> findAllByUserEntity_EmailOrderByDateCreated(String userEntity_email, Pageable pageable);
    Page<OrderEntity> findAllByUserEntityEmailOrderByIdDesc(String email, Pageable pageable);

    Optional<OrderEntity> findByTrackingNumber(String trackingNumber);
    Optional<OrderEntity> findOrderEntityById(Long id);
    Optional<OrderEntity> findFirstByUserEntityEmailOrderByDateCreatedDesc(String email);
    Slice<OrderEntity> findAllByStatusOrStatusOrStatusOrderByIdAsc(OrderStatus status, OrderStatus status2, OrderStatus status3, Pageable pageable);
}
