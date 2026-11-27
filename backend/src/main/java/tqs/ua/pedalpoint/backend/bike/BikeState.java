package tqs.ua.pedalpoint.backend.bike;

/**
 * Life cycle of an e-bike. See the state diagram in the product specification.
 * CHARGING: parked at a hub with less than the minimum battery; not available until it reaches it.
 */
public enum BikeState {
    AVAILABLE,
    RESERVED,
    IN_USE,
    CHARGING,
    MAINTENANCE,
    RETIRED
}
