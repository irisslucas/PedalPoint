package tqs.ua.pedalpoint.simulator;

/** The state of one simulated e-bike. */
public class SimulatedBike {

    public static final int FULL = 100;

    private final String code;
    private final String hub;
    private int battery;
    private final String lock;
    private final String faultCode;

    /** An e-bike docked and locked at a hub, with no fault. */
    public static SimulatedBike dockedAt(String code, String hub, int battery) {
        return new SimulatedBike(code, hub, battery, "LOCKED", null);
    }

    public SimulatedBike(String code, String hub, int battery, String lock, String faultCode) {
        if (battery < 0 || battery > FULL) {
            throw new IllegalArgumentException("Battery must be between 0 and 100: " + battery);
        }
        this.code = code;
        this.hub = hub;
        this.battery = battery;
        this.lock = lock;
        this.faultCode = faultCode;
    }

    /** A docked e-bike charges {@code percent} points, never above 100; an e-bike away from a hub does not charge. */
    public void charge(int percent) {
        if (hub != null) {
            battery = Math.min(FULL, battery + percent);
        }
    }

    public String code() {
        return code;
    }

    /** The hub where the e-bike is docked, or {@code null} while it is away from a hub. */
    public String hub() {
        return hub;
    }

    public int battery() {
        return battery;
    }

    public String lock() {
        return lock;
    }

    public String faultCode() {
        return faultCode;
    }
}
