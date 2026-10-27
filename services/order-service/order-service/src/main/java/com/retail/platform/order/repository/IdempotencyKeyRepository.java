package com.retail.platform.order.repository;

import com.retail.platform.order.entity.IdempotencyKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyKeyRepository
        extends JpaRepository<IdempotencyKeyEntity, Long> {

    Optional<IdempotencyKeyEntity> findByIdempotencyKey(String idempotencyKey);
}
