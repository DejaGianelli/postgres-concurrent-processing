package com.example.demo;

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

    public FactorialService(FactorialResultRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public List<Long> lockItemsToProcess(int size) {
        List<Long> ids = repository.nextIdsToProcess(PageRequest.of(0, size));
        repository.markAsProcessing(ids);
        return ids;
    }

    @Transactional
    public Optional<FactorialResult> process(Long id, String worker) {
        try {
            FactorialResult factorialResult = getFactorialResult(id);
            logger.info("Processing factorial {}", factorialResult.getId());
            BigInteger result = Factorial.calculate(factorialResult.getNumber());
            repository.markAsDone(id, result, worker);
            logger.info("Factorial {} processed: {}", factorialResult.getId(), result);
            return Optional.of(factorialResult);
        } catch (Exception e) {
            repository.markAsError(id);
            logger.info("Error processing factorial {}. Error: {}", id,
                    e.getMessage());
            return Optional.empty();
        }
    }

    private @NonNull FactorialResult getFactorialResult(Long id) {
        Optional<FactorialResult> result = repository.findById(id);
        if (result.isEmpty()) {
            throw new IllegalArgumentException("Factorial Result not found");
        }
        return result.get();
    }
}