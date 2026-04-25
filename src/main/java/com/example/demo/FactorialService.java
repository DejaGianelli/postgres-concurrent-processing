package com.example.demo;

import jakarta.persistence.EntityManager;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Service
public class FactorialService {

    private static final Logger logger = LoggerFactory.getLogger(FactorialService.class);

    private final FactorialResultRepository repository;
    private final EntityManager entityManager;

    public FactorialService(FactorialResultRepository repository,
                            EntityManager entityManager) {
        this.repository = repository;
        this.entityManager = entityManager;
    }

    @Transactional
    public List<Long> lockItemsToProcess(int size) {
        List<Long> ids = repository.nextIdsToProcess(PageRequest.of(0, size));
        repository.markAsProcessing(ids);
        return ids;
    }

    public void process(List<Long> ids, String worker) {
        for (Long id : ids) {
            try {
                FactorialResult factorialResult = getFactorialResult(id);
                logger.info("Processing factorial {}", factorialResult.getId());
                BigInteger result = Factorial.calculate(factorialResult.getNumber());
                factorialResult.setFactorial(result);
                factorialResult.setStatus("DONE");
                factorialResult.setWorker(worker);
                factorialResult = repository.save(factorialResult);
                entityManager.detach(factorialResult);
                logger.info("Factorial {} processed: {}", factorialResult.getId(), result);
            } catch (Exception e) {
                repository.markAsError(id);
                logger.info("Error processing factorial {}. Error: {}", id,
                        e.getMessage());
            }
        }
    }

    private @NonNull FactorialResult getFactorialResult(Long id) {
        Optional<FactorialResult> optional = repository.findById(id);
        if (optional.isEmpty()) {
            throw new IllegalArgumentException("Factorial Result not found");
        }
        return optional.get();
    }
}