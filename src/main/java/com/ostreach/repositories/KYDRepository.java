package com.ostreach.repositories;

import com.ostreach.entities.model.KYDEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KYDRepository extends JpaRepository<KYDEntity, Long> {
}
