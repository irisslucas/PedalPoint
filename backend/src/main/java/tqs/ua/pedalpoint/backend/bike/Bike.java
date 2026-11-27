package tqs.ua.pedalpoint.backend.bike;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import tqs.ua.pedalpoint.backend.hub.Hub;

@Entity
@Table(name = "bike")
public class Bike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BikeType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BikeState state;

    @Column(name = "battery_level", nullable = false)
    private int batteryLevel;

    /** Hub where the e-bike is parked; null while it is in use. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hub_id")
    private Hub hub;

    @Column(name = "last_telemetry_at")
    private Instant lastTelemetryAt;

    protected Bike() {
        // for JPA
    }

    public Bike(String code, BikeType type, BikeState state, int batteryLevel, Hub hub) {
        this.code = code;
        this.type = type;
        this.state = state;
        this.batteryLevel = batteryLevel;
        this.hub = hub;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public BikeType getType() {
        return type;
    }

    public BikeState getState() {
        return state;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public Hub getHub() {
        return hub;
    }

    public Instant getLastTelemetryAt() {
        return lastTelemetryAt;
    }

    /**
     * Applies a telemetry reading: battery, time of the reading and, when the e-bike is docked, the hub.
     * An e-bike in use has no hub, so a hub in the reading is ignored for it (a trip ends through the trip rules).
     * Then the charging rule: docked and available below {@code minBattery} it goes to CHARGING, and it goes back
     * to AVAILABLE when it reaches {@code minBattery}. An e-bike in use, booked, in maintenance or retired never
     * changes state because of its battery.
     */
    public void recordTelemetry(int battery, Hub reportedHub, Instant at, int minBattery) {
        this.batteryLevel = battery;
        this.lastTelemetryAt = at;
        if (state != BikeState.IN_USE && reportedHub != null) {
            this.hub = reportedHub;
        }
        if (state == BikeState.AVAILABLE && battery < minBattery) {
            this.state = BikeState.CHARGING;
        } else if (state == BikeState.CHARGING && battery >= minBattery) {
            this.state = BikeState.AVAILABLE;
        }
    }
}
