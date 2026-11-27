package tqs.ua.pedalpoint.backend.telemetry;

/** Receives the valid telemetry messages produced by a {@link TelemetrySource}. */
@FunctionalInterface
public interface TelemetryHandler {

    void onTelemetry(TelemetryMessage message);
}
