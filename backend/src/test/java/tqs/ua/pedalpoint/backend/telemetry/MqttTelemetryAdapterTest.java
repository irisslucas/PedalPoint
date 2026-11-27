package tqs.ua.pedalpoint.backend.telemetry;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/** The delivery rules of the adapter, without a broker (the broker itself is covered by MqttTelemetryAdapterIT). */
class MqttTelemetryAdapterTest {

    private final MqttTelemetryAdapter adapter = new MqttTelemetryAdapter(
            new MqttProperties(true, null, null, null), new TelemetryMessageParser());
    private final List<TelemetryMessage> received = new ArrayList<>();

    private static byte[] json(String text) {
        return text.getBytes(StandardCharsets.UTF_8);
    }

    @Test
    void anInvalidMessageIsDroppedAndTheNextOnesAreStillDelivered() {
        adapter.deliver(received::add, "pedalpoint/bikes/EB-001/telemetry", json("garbage"));
        adapter.deliver(received::add, "pedalpoint/bikes/EB-002/telemetry", json("{\"battery\":70}"));

        assertThat(received).extracting(TelemetryMessage::bikeCode).containsExactly("EB-002");
    }

    @Test
    void aFailingHandlerDoesNotStopTheNextMessages() {
        TelemetryHandler failingOnce = new TelemetryHandler() {
            private boolean first = true;

            @Override
            public void onTelemetry(TelemetryMessage message) {
                if (first) {
                    first = false;
                    throw new IllegalStateException("boom");
                }
                received.add(message);
            }
        };

        adapter.deliver(failingOnce, "pedalpoint/bikes/EB-001/telemetry", json("{\"battery\":10}"));
        adapter.deliver(failingOnce, "pedalpoint/bikes/EB-002/telemetry", json("{\"battery\":20}"));

        assertThat(received).extracting(TelemetryMessage::bikeCode).containsExactly("EB-002");
    }

    @Test
    void propertiesFallBackToTheDefaults() {
        MqttProperties properties = new MqttProperties(false, " ", null, "");

        assertThat(properties.brokerUrl()).isEqualTo("tcp://localhost:1883");
        assertThat(properties.topic()).isEqualTo("pedalpoint/bikes/+/telemetry");
        assertThat(properties.clientId()).isEqualTo("pedalpoint-backend");
    }
}
