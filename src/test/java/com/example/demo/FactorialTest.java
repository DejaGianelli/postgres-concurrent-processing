package com.example.demo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

class FactorialTest {

    @ParameterizedTest
    @CsvSource({
        "0,  1",
        "1,  1",
        "2,  2",
        "5,  120",
        "10, 3628800",
        "20, 2432902008176640000"
    })
    void calculate_returnsCorrectValue(int n, long expected) {
        assertEquals(BigInteger.valueOf(expected), Factorial.calculate(n));
    }

    @Test
    void calculate_largeNumber_doesNotOverflow() {
        BigInteger result = Factorial.calculate(100);
        assertTrue(result.bitLength() > 64);
    }

    @Test
    void calculate_negativeInput_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Factorial.calculate(-1));
    }
}