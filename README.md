# Concurrent Batch Processing with PostgreSQL

An experiment demonstrating how to safely process database records concurrently using PostgreSQL advisory locking mechanisms, Spring Boot, and Java's async processing model.

## The Problem

When multiple workers process records from the same database table concurrently, two classic problems arise:

- **Double processing**: two workers pick up the same record and compute it twice.
- **Stale reads**: a worker reads a record that another worker already claimed.

This project explores how to use PostgreSQL's `SELECT FOR UPDATE SKIP LOCKED` to eliminate both problems without relying on application-level coordination or external queues.

## How It Works

### Locking Strategy

When a worker requests a batch of records to process, the query acquires a pessimistic write lock on the selected rows:

```sql
SELECT id FROM factorial_result WHERE status = 'PENDING'
FOR UPDATE SKIP LOCKED
LIMIT 100
```

`SKIP LOCKED` means that if another transaction already holds a lock on a row, that row is silently skipped rather than causing the query to wait or fail. This allows multiple workers to safely claim disjoint sets of records in parallel.

### Processing Pipeline

```
POST /factorial/process
        │
        ▼
 Lock PENDING rows        ← SELECT FOR UPDATE SKIP LOCKED
        │
        ▼
 Mark as PROCESSING       ← bulk UPDATE within the same transaction
        │
        ▼
 Calculate factorial       ← async, per record
        │
        ▼
 Save result + DONE       ← UPDATE with result and worker identity
```

Each batch goes through a single coordinating transaction that atomically locks and marks rows as `PROCESSING`. This ensures no other worker can claim the same rows even after the lock is released.

### Status Lifecycle

```
PENDING → PROCESSING → DONE
                     ↘ ERROR
```

| Status       | Meaning                                      |
|--------------|----------------------------------------------|
| `PENDING`    | Waiting to be processed                      |
| `PROCESSING` | Claimed by a worker, calculation in progress |
| `DONE`       | Factorial computed and persisted             |
| `ERROR`      | Calculation failed                           |

## Stack

| Layer       | Technology                        |
|-------------|-----------------------------------|
| Runtime     | Java 21                           |
| Framework   | Spring Boot 4                     |
| Database    | PostgreSQL 16                     |
| ORM         | Hibernate 7 / Spring Data JPA     |
| Migrations  | Flyway                            |
| Async       | Spring `@Async`                   |

## Running Locally

**Start the database:**
```bash
docker compose up -d
```

**Run the application:**
```bash
./mvnw spring-boot:run
```

**Seed the table with random PENDING records:**
```bash
mvn compile exec:java -Dexec.mainClass="com.example.demo.DataSeeder"
```
This inserts 1,000,000 records with numbers between 1 and 100.

**Trigger processing:**
```bash
curl -X POST http://localhost:8080/factorial/process
```

Returns `202 Accepted` immediately. Processing runs in the background. Call it 
multiple times to start multiple workers concurrently.

## Key Files

| File | Role |
|------|------|
| `FactorialResultRepository` | JPQL queries with `FOR UPDATE SKIP LOCKED` |
| `FactorialService` | Batch lock + calculate + persist logic |
| `FactorialJob` | Async orchestration of the processing loop |
| `FactorialController` | HTTP entry point |
| `Factorial` | Pure `BigInteger` factorial computation |
| `DataSeeder` | JDBC bulk insert for test data |
| `V1__init.sql` | Table schema |