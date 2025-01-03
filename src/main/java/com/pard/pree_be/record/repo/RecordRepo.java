package com.pard.pree_be.record.repo;

import com.pard.pree_be.record.entity.Record;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecordRepo extends JpaRepository<Record, UUID> {
    Optional<Record> findByUserId(UUID userId);
}