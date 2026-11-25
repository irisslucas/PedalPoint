CREATE TABLE app_user (
    id            BIGSERIAL    PRIMARY KEY,
    email         VARCHAR(254) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    role          VARCHAR(20)  NOT NULL
        CHECK (role IN ('USER', 'OPERATOR', 'TECHNICIAN', 'CLIENT_MANAGER', 'ADMIN')),
    profile       VARCHAR(20)  NOT NULL
        CHECK (profile IN ('STUDENT', 'STAFF')),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);
