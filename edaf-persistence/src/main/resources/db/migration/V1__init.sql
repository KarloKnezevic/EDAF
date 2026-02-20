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

CREATE TABLE IF NOT EXISTS coco_campaigns (
    campaign_id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    suite TEXT NOT NULL,
    dimensions_json TEXT NOT NULL,
    instances_json TEXT NOT NULL,
    functions_json TEXT NOT NULL,
    status TEXT NOT NULL,
    created_at TEXT NOT NULL,
    started_at TEXT,
    finished_at TEXT,
    notes TEXT
);

CREATE TABLE IF NOT EXISTS coco_optimizer_configs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    campaign_id TEXT NOT NULL,
    optimizer_id TEXT NOT NULL,
    config_path TEXT NOT NULL,
    algorithm_type TEXT NOT NULL,
    model_type TEXT NOT NULL,
    representation_type TEXT NOT NULL,
    config_yaml TEXT NOT NULL,
    created_at TEXT NOT NULL,
    FOREIGN KEY(campaign_id) REFERENCES coco_campaigns(campaign_id),
    UNIQUE(campaign_id, optimizer_id)
);

CREATE TABLE IF NOT EXISTS coco_trials (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    campaign_id TEXT NOT NULL,
    optimizer_id TEXT NOT NULL,
    run_id TEXT NOT NULL,
    function_id INTEGER NOT NULL,
    instance_id INTEGER NOT NULL,
    dimension INTEGER NOT NULL,
    repetition INTEGER NOT NULL,
    budget_evals BIGINT NOT NULL,
    evaluations BIGINT,
    best_fitness DOUBLE,
    runtime_millis BIGINT,
    status TEXT NOT NULL,
    reached_target INTEGER NOT NULL,
    evals_to_target BIGINT,
    target_value DOUBLE NOT NULL,
    created_at TEXT NOT NULL,
    FOREIGN KEY(campaign_id) REFERENCES coco_campaigns(campaign_id),
    UNIQUE(campaign_id, optimizer_id, function_id, instance_id, dimension, repetition)
);

CREATE TABLE IF NOT EXISTS coco_reference_results (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    suite TEXT NOT NULL,
    optimizer_name TEXT NOT NULL,
    function_id INTEGER NOT NULL,
    dimension INTEGER NOT NULL,
    target_value DOUBLE NOT NULL,
    ert DOUBLE NOT NULL,
    success_rate DOUBLE,
    source_url TEXT,
    imported_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS coco_aggregates (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    campaign_id TEXT NOT NULL,
    optimizer_id TEXT NOT NULL,
    dimension INTEGER NOT NULL,
    target_value DOUBLE NOT NULL,
    mean_evals_to_target DOUBLE,
    success_rate DOUBLE NOT NULL,
    median_best_fitness DOUBLE,
    compared_reference_optimizer TEXT,
    reference_ert DOUBLE,
    edaf_ert DOUBLE,
    ert_ratio DOUBLE,
    created_at TEXT NOT NULL,
    FOREIGN KEY(campaign_id) REFERENCES coco_campaigns(campaign_id),
    UNIQUE(campaign_id, optimizer_id, dimension, target_value)
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
CREATE INDEX IF NOT EXISTS idx_coco_campaign_status ON coco_campaigns(status);
CREATE INDEX IF NOT EXISTS idx_coco_trials_campaign ON coco_trials(campaign_id, optimizer_id, dimension, function_id);
CREATE INDEX IF NOT EXISTS idx_coco_trials_run_id ON coco_trials(run_id);
CREATE INDEX IF NOT EXISTS idx_coco_aggregates_campaign ON coco_aggregates(campaign_id, optimizer_id, dimension);
CREATE INDEX IF NOT EXISTS idx_coco_reference_lookup ON coco_reference_results(suite, optimizer_name, function_id, dimension, target_value);
