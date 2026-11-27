package tqs.ua.pedalpoint.backend.telemetry;

/** A telemetry message that cannot be used: malformed payload or values out of range. */
public class InvalidTelemetryException extends RuntimeException {

    public InvalidTelemetryException(String message) {
        super(message);
    }

    public InvalidTelemetryException(String message, Throwable cause) {
        super(message, cause);
    }
}
