package tqs.ua.pedalpoint.backend.telemetry;

/**
 * Where e-bike telemetry comes from. The business logic depends only on this interface, so the MQTT adapter
 * can be replaced (by another broker, a test double or a vendor API) without changing it.
 * An implementation delivers only valid messages: invalid ones are logged and dropped, and delivery continues.
 */
public interface TelemetrySource {

    /** Starts delivering messages to the handler. */
    void subscribe(TelemetryHandler handler);

    /** Stops delivering messages and releases the connection. */
    void close();
}
