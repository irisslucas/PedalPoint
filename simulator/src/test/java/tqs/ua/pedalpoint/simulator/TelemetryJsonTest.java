package tqs.ua.pedalpoint.simulator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TelemetryJsonTest {

    @Test
    void writesTheFourFieldsTheBackendReads() {
        SimulatedBike bike = new SimulatedBike("EB-001", "Biblioteca", 55, "LOCKED", "E12");

        assertThat(TelemetryJson.of(bike))
                .isEqualTo("{\"battery\":55,\"hub\":\"Biblioteca\",\"lock\":\"LOCKED\",\"faultCode\":\"E12\"}");
    }

    @Test
    void absentValuesAreJsonNull() {
        SimulatedBike bike = new SimulatedBike("EB-001", null, 5, null, null);

        assertThat(TelemetryJson.of(bike)).isEqualTo("{\"battery\":5,\"hub\":null,\"lock\":null,\"faultCode\":null}");
    }

    @Test
    void keepsAccentsAndEscapesQuotesBackslashesAndControlCharacters() {
        assertThat(TelemetryJson.string("Residências")).isEqualTo("\"Residências\"");
        assertThat(TelemetryJson.string("a\"b\\c\nd\u0001")).isEqualTo("\"a\\\"b\\\\c\\nd\\u0001\"");
    }
}
