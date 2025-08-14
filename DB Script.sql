-- Cleaned PostgreSQL CREATE TABLE Script for dbdiagram or migration tools

BEGIN;

CREATE TABLE IF NOT EXISTS logincredentials (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    current_pwd VARCHAR(100) NOT NULL,
    prev_pwd VARCHAR(100),
    role VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS machines (
    serial_number VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    type VARCHAR(50),
    location VARCHAR(100),
    status VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS machinereadings (
    reading_id SERIAL PRIMARY KEY,
    timestamp TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    machine_id VARCHAR(50),
    water_flow_rate DOUBLE PRECISION,
    pressure_stability_index DOUBLE PRECISION,
    detergent_level DOUBLE PRECISION,
    hydraulic_pressure DOUBLE PRECISION,
    temperature_fluctuation_index DOUBLE PRECISION,
    hydraulic_oil_temperature DOUBLE PRECISION,
    coolant_temperature DOUBLE PRECISION,
    machine_failure BOOLEAN,
    failure_type VARCHAR(50),
    FOREIGN KEY (machine_id) REFERENCES machines (serial_number) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS machinereports (
    report_id SERIAL PRIMARY KEY,
    serial_number VARCHAR(50) NOT NULL,
    title VARCHAR(100) NOT NULL,
    created_by VARCHAR(50),
    date_range_start TIMESTAMPTZ,
    date_range_end TIMESTAMPTZ,
    total_readings INTEGER DEFAULT 0,
    failure_count INTEGER DEFAULT 0,
    no_failure_count INTEGER DEFAULT 0,
    detergent_level_low_count INTEGER DEFAULT 0,
    pressure_drop_count INTEGER DEFAULT 0,
    temperature_anomaly_count INTEGER DEFAULT 0,
    water_flow_issue_count INTEGER DEFAULT 0,
    avg_water_flow_rate DOUBLE PRECISION,
    avg_pressure_stability_index DOUBLE PRECISION,
    avg_detergent_level DOUBLE PRECISION,
    avg_hydraulic_pressure DOUBLE PRECISION,
    avg_temperature_fluctuation_index DOUBLE PRECISION,
    avg_hydraulic_oil_temperature DOUBLE PRECISION,
    avg_coolant_temperature DOUBLE PRECISION,
    failure_prediction_rate DOUBLE PRECISION,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (serial_number) REFERENCES machines (serial_number) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (created_by) REFERENCES logincredentials (username) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS maintenancetasks (
    task_id SERIAL PRIMARY KEY,
    machine_id VARCHAR(50),
    title VARCHAR(100) NOT NULL,
    assigned_engineer VARCHAR(50),
    status VARCHAR(50) DEFAULT 'Upcoming (Not Started)',
    FOREIGN KEY (assigned_engineer) REFERENCES logincredentials (username) ON DELETE SET NULL,
    FOREIGN KEY (machine_id) REFERENCES machines (serial_number) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS taskdates (
    date_id SERIAL PRIMARY KEY,
    task_id INTEGER UNIQUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ,
    scheduled_at TIMESTAMPTZ,
    started_at TIMESTAMPTZ,
    ended_at TIMESTAMPTZ,
    FOREIGN KEY (task_id) REFERENCES maintenancetasks (task_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS tasknotes (
    note_id SERIAL PRIMARY KEY,
    task_id INTEGER UNIQUE,
    notes TEXT,
    comments TEXT,
    FOREIGN KEY (task_id) REFERENCES maintenancetasks (task_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS workorderreports (
    report_id SERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    created_by VARCHAR(50),
    date_range_start TIMESTAMPTZ,
    date_range_end TIMESTAMPTZ,
    total_work_orders INTEGER DEFAULT 0,
    completed_work_orders INTEGER DEFAULT 0,
    in_progress_work_orders INTEGER DEFAULT 0,
    cancelled_work_orders INTEGER DEFAULT 0,
    overdue_work_orders INTEGER DEFAULT 0,
    average_completion_time INTERVAL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES logincredentials (username) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS otpstore (
    username VARCHAR(50) PRIMARY KEY,
    otp VARCHAR(6) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    FOREIGN KEY (username) REFERENCES logincredentials (username) ON DELETE CASCADE
);

END;
