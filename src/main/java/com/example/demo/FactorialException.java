package com.example.demo;

public class FactorialException extends RuntimeException {
    private final FactorialResult factorialResult;

    public FactorialException(FactorialResult factorialResult) {
        this.factorialResult = factorialResult;
    }

    public FactorialResult getFactorialResult() {
        return factorialResult;
    }
}
