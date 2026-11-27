package tqs.ua.pedalpoint.simulator;

/** Where the simulator sends telemetry. The MQTT implementation is the only one used outside tests. */
public interface TelemetryPublisher {

    void publish(SimulatedBike bike);

    void close();
}
