CREATE TABLE hub (
    id       BIGSERIAL    PRIMARY KEY,
    name     VARCHAR(100) NOT NULL UNIQUE,
    capacity INTEGER      NOT NULL CHECK (capacity > 0)
);
