CREATE TABLE IF NOT EXISTS mbeans(
    name     TEXT,
    property TEXT,
    value    TEXT,
    actual   BOOLEAN not null default true,
    PRIMARY KEY (name, property)
);

CREATE TABLE IF NOT EXISTS mbeans_audit_log(
    session_user_name   TEXT,
    client_addr         INET,
    application_name    TEXT,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    name                TEXT,
    property            TEXT,
    old_value           TEXT,
    new_value           TEXT,
    PRIMARY KEY (name, property, updated_at)
);
