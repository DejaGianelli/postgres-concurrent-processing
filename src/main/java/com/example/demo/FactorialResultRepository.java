package com.example.demo;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import java.math.BigInteger;
import java.util.List;

public interface FactorialResultRepository extends JpaRepository<FactorialResult, Long> {

    @Query("""
            SELECT fr.id
            FROM FactorialResult fr
            WHERE fr.status = 'PENDING'
            """)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    List<Long> nextIdsToProcess(Pageable pageable);

    @Modifying
    @Query("""
            UPDATE FactorialResult fr
            SET fr.status = 'PROCESSING'
            WHERE fr.id IN :ids
            """)
    void markAsProcessing(List<Long> ids);

    @Modifying
    @Query("""
            UPDATE FactorialResult fr SET fr.status = 'ERROR' WHERE fr.id = :id
            """)
    void markAsError(Long id);

    @Modifying
    @Query("""
            UPDATE FactorialResult fr
            SET fr.status = 'DONE', fr.factorial = :result, fr.worker = :worker
            WHERE fr.id = :id
            """)
    void markAsDone(Long id, BigInteger result, String worker);
}
