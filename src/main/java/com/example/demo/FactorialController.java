package com.example.demo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/factorial")
public class FactorialController {

    private final FactorialJob job;

    public FactorialController(FactorialJob job) {
        this.job = job;
    }

    @PostMapping("/process")
    public ResponseEntity<Void> process() {
        int batchSize = 100;
        job.execute(batchSize);
        return ResponseEntity.accepted().build();
    }
}
