package com.ostreach.repositories;

import com.ostreach.entities.enums.TaskStatus;
import com.ostreach.entities.model.TaskEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
    Slice<TaskEntity> findByDriverEntityId(Long driverId, Pageable pageable);
    Slice<TaskEntity> findByDriverEntityEmailAndTaskStatusIsNotNullOrderByIdDesc(String email, Pageable pageable);
    Slice<TaskEntity> findAllByDriverEntityEmailAndTaskStatusIsNull(String email, Pageable pageable);
    Slice<TaskEntity> findAllByTaskStatusOrderById(TaskStatus taskStatus, Pageable pageable);
}
