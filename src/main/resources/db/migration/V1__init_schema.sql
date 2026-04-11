CREATE TABLE app_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(16) NOT NULL CHECK (role IN ('PLANNER', 'MASTER', 'OPERATOR')),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE production_plan (
    id BIGSERIAL PRIMARY KEY,
    product_code VARCHAR(32) NOT NULL,
    product_name VARCHAR(120) NOT NULL,
    planned_quantity INTEGER NOT NULL CHECK (planned_quantity > 0),
    planned_date DATE NOT NULL,
    status VARCHAR(24) NOT NULL CHECK (status IN ('DRAFT', 'APPROVED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    created_by BIGINT NOT NULL REFERENCES app_user(id),
    updated_by BIGINT NOT NULL REFERENCES app_user(id),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE production_order (
    id BIGSERIAL PRIMARY KEY,
    plan_id BIGINT NOT NULL UNIQUE REFERENCES production_plan(id),
    status VARCHAR(32) NOT NULL CHECK (status IN ('CREATED', 'STARTED', 'OPERATIONS_IN_PROGRESS', 'READY_FOR_RELEASE', 'RELEASED')),
    started_by BIGINT REFERENCES app_user(id),
    started_at TIMESTAMPTZ,
    released_by BIGINT REFERENCES app_user(id),
    released_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE operation_execution (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES production_order(id),
    operation_name VARCHAR(120) NOT NULL,
    completed_quantity INTEGER NOT NULL CHECK (completed_quantity > 0),
    performed_by BIGINT NOT NULL REFERENCES app_user(id),
    performed_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    actor_user_id BIGINT NOT NULL REFERENCES app_user(id),
    action VARCHAR(64) NOT NULL,
    entity_type VARCHAR(64) NOT NULL,
    entity_id BIGINT NOT NULL,
    details VARCHAR(500) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_plan_product_code ON production_plan(product_code);
CREATE INDEX idx_order_status ON production_order(status);
CREATE INDEX idx_operation_order_id ON operation_execution(order_id);
CREATE INDEX idx_audit_actor_created ON audit_log(actor_user_id, created_at);
