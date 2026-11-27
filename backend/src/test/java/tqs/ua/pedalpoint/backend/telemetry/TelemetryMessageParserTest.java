package tqs.ua.pedalpoint.backend.telemetry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TelemetryMessageParserTest {

    private static final String TOPIC = "pedalpoint/bikes/EB-001/telemetry";

    private final TelemetryMessageParser parser = new TelemetryMessageParser();

    private static byte[] json(String text) {
        return text.getBytes(StandardCharsets.UTF_8);
    }

    @Test
    void parsesAFullMessageAndTakesTheCodeFromTheTopic() {
        TelemetryMessage message = parser.parse(TOPIC,
                json("{\"battery\":55,\"hub\":\"Biblioteca\",\"lock\":\"LOCKED\",\"faultCode\":\"E12\"}"));

        assertThat(message).isEqualTo(new TelemetryMessage("EB-001", 55, "Biblioteca", "LOCKED", "E12"));
    }

    @Test
    void onlyTheBatteryIsRequired() {
        TelemetryMessage message = parser.parse(TOPIC, json("{\"battery\":42}"));

        assertThat(message).isEqualTo(new TelemetryMessage("EB-001", 42, null, null, null));
    }

    @Test
    void nullAndBlankTextFieldsMeanAbsent() {
        TelemetryMessage message = parser.parse(TOPIC,
                json("{\"battery\":10,\"hub\":null,\"lock\":\"  \",\"faultCode\":\"\"}"));

        assertThat(message).isEqualTo(new TelemetryMessage("EB-001", 10, null, null, null));
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 100 })
    void acceptsTheBatteryLimits(int battery) {
        assertThat(parser.parse(TOPIC, json("{\"battery\":" + battery + "}")).battery()).isEqualTo(battery);
    }

    @ParameterizedTest
    @ValueSource(strings = { "{\"battery\":-1}", "{\"battery\":101}", "{\"battery\":50.5}", "{\"battery\":\"50\"}",
            "{\"battery\":null}", "{\"battery\":99999999999}", "{\"hub\":\"Biblioteca\"}" })
    void rejectsAMissingOrOutOfRangeBattery(String payload) {
        assertThatThrownBy(() -> parser.parse(TOPIC, json(payload))).isInstanceOf(InvalidTelemetryException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = { "not json", "", "[1,2]", "42", "{\"battery\":" })
    void rejectsAPayloadThatIsNotAJsonObject(String payload) {
        assertThatThrownBy(() -> parser.parse(TOPIC, json(payload))).isInstanceOf(InvalidTelemetryException.class);
    }

    @Test
    void rejectsATextFieldThatIsNotText() {
        assertThatThrownBy(() -> parser.parse(TOPIC, json("{\"battery\":10,\"hub\":5}")))
                .isInstanceOf(InvalidTelemetryException.class).hasMessageContaining("hub");
    }

    @ParameterizedTest
    @ValueSource(strings = { "pedalpoint/bikes//telemetry", "pedalpoint/bikes/telemetry", "other/bikes/EB-001/telemetry",
            "pedalpoint/bikes/EB-001/status", "pedalpoint/bikes/EB/001/telemetry" })
    void rejectsAnUnexpectedTopic(String topic) {
        assertThatThrownBy(() -> parser.parse(topic, json("{\"battery\":10}")))
                .isInstanceOf(InvalidTelemetryException.class).hasMessageContaining("topic");
    }
}
