package tqs.ua.pedalpoint.backend.telemetry;

/**
 * One telemetry reading of an e-bike, independent of the transport that carried it.
 *
 * @param bikeCode  code of the e-bike (for example {@code PP-E001})
 * @param battery   battery level, 0 to 100
 * @param hub       name of the hub where the e-bike is docked; {@code null} while it is in use
 * @param lock      lock state reported by the e-bike, for example {@code LOCKED}; may be {@code null}
 * @param faultCode fault code reported by the e-bike; {@code null} when there is no fault
 */
public record TelemetryMessage(String bikeCode, int battery, String hub, String lock, String faultCode) {
}
