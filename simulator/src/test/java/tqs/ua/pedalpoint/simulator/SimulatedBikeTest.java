package tqs.ua.pedalpoint.simulator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class SimulatedBikeTest {

    /** The cases of features/us21_charging.feature: 1% per minute, up to 100%. */
    @ParameterizedTest
    @CsvSource({ "0,19,19", "0,20,20", "95,10,100", "100,5,100", "50,0,50" })
    void aDockedBikeChargesOnePercentPerMinuteUpToFull(int start, int minutes, int expected) {
        SimulatedBike bike = SimulatedBike.dockedAt("EB-001", "Biblioteca", start);

        for (int minute = 0; minute < minutes; minute++) {
            bike.charge(1);
        }

        assertThat(bike.battery()).isEqualTo(expected);
    }

    @Test
    void aBikeAwayFromAHubDoesNotCharge() {
        SimulatedBike bike = new SimulatedBike("EB-001", null, 30, "UNLOCKED", null);

        bike.charge(1);

        assertThat(bike.battery()).isEqualTo(30);
    }

    @Test
    void aLargeChargeStepStillStopsAtFull() {
        SimulatedBike bike = SimulatedBike.dockedAt("EB-001", "Biblioteca", 98);

        bike.charge(5);

        assertThat(bike.battery()).isEqualTo(100);
    }

    @ParameterizedTest
    @ValueSource(ints = { -1, 101 })
    void rejectsABatteryOutOfRange(int battery) {
        assertThatThrownBy(() -> SimulatedBike.dockedAt("EB-001", "Biblioteca", battery))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
