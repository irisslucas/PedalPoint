package tqs.ua.pedalpoint.simulator;

import java.util.List;

/** Charges the docked e-bikes and publishes the telemetry of the whole fleet. Time is driven from outside. */
public class FleetSimulator {

    private final List<SimulatedBike> fleet;
    private final int chargePerTick;
    private final TelemetryPublisher publisher;

    public FleetSimulator(List<SimulatedBike> fleet, int chargePerTick, TelemetryPublisher publisher) {
        if (chargePerTick < 0) {
            throw new IllegalArgumentException("The charge per tick cannot be negative: " + chargePerTick);
        }
        this.fleet = List.copyOf(fleet);
        this.chargePerTick = chargePerTick;
        this.publisher = publisher;
    }

    /** Publishes the current state without charging, for example when the simulator starts. */
    public void publishAll() {
        fleet.forEach(publisher::publish);
    }

    /** One tick of simulated time (one minute by default): charge, then publish. */
    public void tick() {
        fleet.forEach(bike -> bike.charge(chargePerTick));
        publishAll();
    }

    public List<SimulatedBike> fleet() {
        return fleet;
    }
}
