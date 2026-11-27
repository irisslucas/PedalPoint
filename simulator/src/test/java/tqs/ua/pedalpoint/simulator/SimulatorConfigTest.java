package tqs.ua.pedalpoint.simulator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SimulatorConfigTest {

    @Test
    void usesTheDefaultsWhenNothingIsSet() {
        SimulatorConfig config = SimulatorConfig.fromEnvironment(Map.of());

        assertThat(config).isEqualTo(new SimulatorConfig("tcp://localhost:1883", 60, 1));
    }

    @Test
    void readsTheEnvironment() {
        SimulatorConfig config = SimulatorConfig.fromEnvironment(Map.of("MQTT_BROKER_URL", "tcp://mosquitto:1883",
                "TICK_SECONDS", " 5 ", "CHARGE_PERCENT_PER_TICK", "2"));

        assertThat(config).isEqualTo(new SimulatorConfig("tcp://mosquitto:1883", 5, 2));
    }

    @ParameterizedTest
    @ValueSource(strings = { "0", "-3", "abc", "1.5" })
    void rejectsATickThatIsNotAPositiveWholeNumber(String value) {
        assertThatThrownBy(() -> SimulatorConfig.fromEnvironment(Map.of("TICK_SECONDS", value)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("TICK_SECONDS");
    }

    @Test
    void aBlankValueMeansDefault() {
        assertThat(SimulatorConfig.fromEnvironment(Map.of("TICK_SECONDS", "  ")).tickSeconds()).isEqualTo(60);
    }
}
