CREATE TABLE IF NOT EXISTS runs (
    run_id VARCHAR(36) PRIMARY KEY,
    algorithm_id VARCHAR(64) NOT NULL,
    problem_class VARCHAR(256) NOT NULL,
    genotype_type VARCHAR(64),
    population_size INTEGER,
    max_generations INTEGER,
    config_hash VARCHAR(64),
    seed BIGINT,
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    total_generations INTEGER,
    best_fitness DOUBLE PRECISION,
    best_individual TEXT,
    total_duration_ms BIGINT,
    status VARCHAR(16) DEFAULT 'RUNNING'
);

CREATE TABLE IF NOT EXISTS generation_stats (
    id INTEGER PRIMARY KEY,
    run_id VARCHAR(36) NOT NULL,
    generation INTEGER NOT NULL,
    best_fitness DOUBLE PRECISION,
    worst_fitness DOUBLE PRECISION,
    avg_fitness DOUBLE PRECISION,
    std_fitness DOUBLE PRECISION,
    best_individual TEXT,
    eval_duration_nanos BIGINT,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (run_id) REFERENCES runs(run_id),
    UNIQUE(run_id, generation)
);
