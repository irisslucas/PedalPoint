package tqs.ua.pedalpoint.backend.bike;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import tqs.ua.pedalpoint.backend.hub.Hub;

/** The effect of a telemetry reading on an e-bike (no Spring, no database). */
class BikeTelemetryTest {

    private static final int MIN = 20;
    private static final Instant NOW = Instant.parse("2026-11-27T10:00:00Z");

    private final Hub residencias = new Hub("Residências", 20);
    private final Hub biblioteca = new Hub("Biblioteca", 15);

    private Bike bike(BikeState state, int battery, Hub hub) {
        return new Bike("EB-001", BikeType.E_BIKE, state, battery, hub);
    }

    @ParameterizedTest
    @CsvSource({ "0,CHARGING", "19,CHARGING", "20,AVAILABLE", "80,AVAILABLE" })
    void anAvailableBikeBelowTheMinimumBatteryIsChargingAndAtTheMinimumItIsNot(int battery, BikeState expected) {
        Bike bike = bike(BikeState.AVAILABLE, 90, residencias);

        bike.recordTelemetry(battery, null, NOW, MIN);

        assertThat(bike.getState()).isEqualTo(expected);
        assertThat(bike.getBatteryLevel()).isEqualTo(battery);
    }

    @ParameterizedTest
    @CsvSource({ "19,CHARGING", "20,AVAILABLE", "100,AVAILABLE" })
    void aChargingBikeReturnsToAvailableAtTheMinimumBattery(int battery, BikeState expected) {
        Bike bike = bike(BikeState.CHARGING, 10, residencias);

        bike.recordTelemetry(battery, null, NOW, MIN);

        assertThat(bike.getState()).isEqualTo(expected);
    }

    @ParameterizedTest
    @EnumSource(value = BikeState.class, names = { "RESERVED", "MAINTENANCE", "RETIRED" })
    void telemetryDoesNotChangeTheStateOfOtherStatesEvenWithAFlatBattery(BikeState state) {
        Bike bike = bike(state, 50, residencias);

        bike.recordTelemetry(5, null, NOW, MIN);

        assertThat(bike.getState()).isEqualTo(state);
        assertThat(bike.getBatteryLevel()).isEqualTo(5);
    }

    @Test
    void aTripIsNeverInterruptedForLowBatteryAndTheBikeHasNoHub() {
        Bike bike = bike(BikeState.IN_USE, 50, null);

        bike.recordTelemetry(3, biblioteca, NOW, MIN);

        assertThat(bike.getState()).isEqualTo(BikeState.IN_USE);
        assertThat(bike.getHub()).isNull();
        assertThat(bike.getBatteryLevel()).isEqualTo(3);
    }

    @Test
    void aReportedHubReplacesTheCurrentOneForADockedBike() {
        Bike bike = bike(BikeState.AVAILABLE, 80, residencias);

        bike.recordTelemetry(55, biblioteca, NOW, MIN);

        assertThat(bike.getHub()).isSameAs(biblioteca);
    }

    @Test
    void withoutAReportedHubTheBikeStaysWhereItIs() {
        Bike bike = bike(BikeState.AVAILABLE, 80, residencias);

        bike.recordTelemetry(55, null, NOW, MIN);

        assertThat(bike.getHub()).isSameAs(residencias);
    }

    @Test
    void theTimeOfTheReadingIsRecorded() {
        Bike bike = bike(BikeState.AVAILABLE, 80, residencias);

        bike.recordTelemetry(79, null, NOW, MIN);

        assertThat(bike.getLastTelemetryAt()).isEqualTo(NOW);
    }
}
