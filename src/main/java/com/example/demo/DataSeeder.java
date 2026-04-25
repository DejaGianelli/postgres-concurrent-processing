package com.example.demo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Random;

public class DataSeeder {

    private static final String URL      = "jdbc:postgresql://localhost:5432/demo";
    private static final String USER     = "demo";
    private static final String PASSWORD = "demo";

    private static final int RECORD_COUNT = 1_000_000;
    private static final int MAX_NUMBER   = 100;

    public static void main(String[] args) throws Exception {
        Random random = new Random();
        String sql = "INSERT INTO factorial_result (status, number) VALUES ('PENDING', ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            for (int i = 0; i < RECORD_COUNT; i++) {
                stmt.setInt(1, random.nextInt(MAX_NUMBER) + 1);
                stmt.addBatch();
            }

            stmt.executeBatch();
            conn.commit();
        }

        System.out.printf("✓ %d registros inseridos com status PENDING%n", RECORD_COUNT);
    }
}