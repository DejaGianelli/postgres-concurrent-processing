package com.example.demo;

import java.math.BigInteger;

public class Factorial {

    public static BigInteger calculate(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n must be non-negative");
        }
        BigInteger result = BigInteger.ONE;
        for (int i = 2; i <= n; i++) {
            result = result.multiply(BigInteger.valueOf(i));
        }
        return result;
    }
}