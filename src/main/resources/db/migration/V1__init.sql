CREATE TABLE factorial_result (
    id         BIGSERIAL    PRIMARY KEY,
    status     VARCHAR(50)  NOT NULL,
    number     INTEGER      NOT NULL,
    factorial  NUMERIC,
    worker     VARCHAR(255),
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_factorial_result_pending
    ON factorial_result (status)
    WHERE status = 'PENDING';
