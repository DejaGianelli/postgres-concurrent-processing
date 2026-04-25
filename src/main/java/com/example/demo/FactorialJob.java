package com.example.demo;

import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FactorialJob {

    private static final Logger logger = LoggerFactory.getLogger(FactorialJob.class);

    private final FactorialService service;
    private final EntityManager entityManager;

    public FactorialJob(FactorialService service, EntityManager entityManager) {
        this.service = service;
        this.entityManager = entityManager;
    }

    @Async
    public void execute(int batchSize) {
        String worker = Thread.currentThread().getName();
        int count = 0;
        logger.info("Starting process");
        List<Long> ids = service.lockItemsToProcess(batchSize);
        while (!ids.isEmpty()) {
            for (Long id : ids) {
                var result = service.process(id, worker);
                result.ifPresent(entityManager::detach);
            }
            count = count + ids.size();
            ids = service.lockItemsToProcess(batchSize);
        }
        logger.info("Finished processing {} items. Worker: {}", count,
                Thread.currentThread().getName());
    }
}
