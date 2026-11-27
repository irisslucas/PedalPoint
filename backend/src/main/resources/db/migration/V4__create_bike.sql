CREATE TABLE bike (
    id                BIGSERIAL   PRIMARY KEY,
    code              VARCHAR(20) NOT NULL UNIQUE,
    type              VARCHAR(20) NOT NULL CHECK (type IN ('E_BIKE', 'CARGO_E_BIKE')),
    state             VARCHAR(20) NOT NULL CHECK (state IN ('AVAILABLE', 'RESERVED', 'IN_USE', 'CHARGING', 'MAINTENANCE', 'RETIRED')),
    battery_level     INTEGER     NOT NULL CHECK (battery_level BETWEEN 0 AND 100),
    hub_id            BIGINT      REFERENCES hub (id),
    last_telemetry_at TIMESTAMPTZ,
    -- an e-bike in use is not parked at any hub; in every other state it is
    CHECK ((state = 'IN_USE') = (hub_id IS NULL))
);

CREATE INDEX idx_bike_hub_state ON bike (hub_id, state);
