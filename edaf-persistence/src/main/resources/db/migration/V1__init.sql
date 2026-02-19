CREATE TABLE IF NOT EXISTS experiments (
    experiment_id TEXT PRIMARY KEY,
    config_hash TEXT NOT NULL UNIQUE,
    schema_version TEXT NOT NULL,
    run_name TEXT,
    algorithm_type TEXT NOT NULL,
    model_type TEXT NOT NULL,
    problem_type TEXT NOT NULL,
    representation_type TEXT NOT NULL,
    selection_type TEXT NOT NULL,
    replacement_type TEXT NOT NULL,
    stopping_type TEXT NOT NULL,
    max_iterations INTEGER,
    config_yaml TEXT NOT NULL,
    config_json TEXT NOT NULL,
    created_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS experiment_params (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    experiment_id TEXT NOT NULL,
    section TEXT NOT NULL,
    param_path TEXT NOT NULL,
    leaf_key TEXT NOT NULL,
    value_type TEXT NOT NULL CHECK(value_type IN ('string','number','boolean','null','json')),
    value_text TEXT,
    value_number DOUBLE,
    value_boolean INTEGER,
    value_json TEXT,
    FOREIGN KEY(experiment_id) REFERENCES experiments(experiment_id),
    UNIQUE(experiment_id, param_path)
);

CREATE TABLE IF NOT EXISTS runs (
    run_id TEXT PRIMARY KEY,
    experiment_id TEXT NOT NULL,
    seed BIGINT NOT NULL,
    status TEXT NOT NULL,
    start_time TEXT NOT NULL,
    end_time TEXT,
    iterations INTEGER,
    evaluations BIGINT,
    best_fitness DOUBLE,
    best_summary TEXT,
    runtime_millis BIGINT,
    artifacts_json TEXT,
    resumed_from TEXT,
    error_message TEXT,
    FOREIGN KEY(experiment_id) REFERENCES experiments(experiment_id)
);

CREATE TABLE IF NOT EXISTS run_objectives (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    run_id TEXT NOT NULL,
    objective_name TEXT NOT NULL,
    objective_value DOUBLE NOT NULL,
    UNIQUE(run_id, objective_name),
    FOREIGN KEY(run_id) REFERENCES runs(run_id)
);

CREATE TABLE IF NOT EXISTS iterations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    run_id TEXT NOT NULL,
    iteration INTEGER NOT NULL,
    evaluations BIGINT NOT NULL,
    best_fitness DOUBLE NOT NULL,
    mean_fitness DOUBLE NOT NULL,
    std_fitness DOUBLE NOT NULL,
    metrics_json TEXT,
    diagnostics_json TEXT,
    created_at TEXT NOT NULL,
    UNIQUE(run_id, iteration),
    FOREIGN KEY(run_id) REFERENCES runs(run_id)
);

CREATE TABLE IF NOT EXISTS checkpoints (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    run_id TEXT NOT NULL,
    iteration INTEGER NOT NULL,
    checkpoint_path TEXT NOT NULL,
    created_at TEXT NOT NULL,
    FOREIGN KEY(run_id) REFERENCES runs(run_id)
);

CREATE TABLE IF NOT EXISTS events (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    run_id TEXT,
    event_type TEXT NOT NULL,
    payload_json TEXT NOT NULL,
    created_at TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_runs_start_time ON runs(start_time DESC);
CREATE INDEX IF NOT EXISTS idx_runs_status ON runs(status);
CREATE INDEX IF NOT EXISTS idx_runs_experiment_id ON runs(experiment_id);
CREATE INDEX IF NOT EXISTS idx_runs_best_fitness ON runs(best_fitness);
CREATE INDEX IF NOT EXISTS idx_experiments_types ON experiments(algorithm_type, problem_type, model_type);
CREATE INDEX IF NOT EXISTS idx_experiment_params_lookup ON experiment_params(experiment_id, section, leaf_key);
CREATE INDEX IF NOT EXISTS idx_experiment_params_value_text ON experiment_params(value_text);
CREATE INDEX IF NOT EXISTS idx_iterations_run_iteration ON iterations(run_id, iteration);
CREATE INDEX IF NOT EXISTS idx_events_run_type_created ON events(run_id, event_type, created_at);
CREATE INDEX IF NOT EXISTS idx_checkpoints_run_iteration ON checkpoints(run_id, iteration);
