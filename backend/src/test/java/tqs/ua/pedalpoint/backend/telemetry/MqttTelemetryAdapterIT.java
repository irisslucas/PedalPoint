package tqs.ua.pedalpoint.backend.telemetry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** The adapter against a real Mosquitto broker. */
@Testcontainers
class MqttTelemetryAdapterIT {

    @Container
    static final GenericContainer<?> MOSQUITTO = new GenericContainer<>("eclipse-mosquitto:2.0.20")
            .withExposedPorts(1883)
            .withCopyToContainer(Transferable.of("listener 1883\nallow_anonymous true\n"),
                    "/mosquitto/config/mosquitto.conf")
            .waitingFor(Wait.forLogMessage(".*mosquitto version .* running.*", 1));

    private final List<TelemetryMessage> received = new CopyOnWriteArrayList<>();
    private MqttTelemetryAdapter adapter;
    private MqttClient publisher;

    @BeforeEach
    void connect() throws Exception {
        String url = "tcp://" + MOSQUITTO.getHost() + ":" + MOSQUITTO.getMappedPort(1883);
        adapter = new MqttTelemetryAdapter(new MqttProperties(true, url, null, "backend-it"),
                new TelemetryMessageParser());
        adapter.subscribe(received::add);
        publisher = new MqttClient(url, "publisher-it", new MemoryPersistence());
        publisher.connect();
    }

    @AfterEach
    void disconnect() throws Exception {
        publisher.disconnect();
        publisher.close();
        adapter.close();
    }

    private void publish(String bikeCode, String payload) throws Exception {
        MqttMessage message = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
        message.setQos(1);
        publisher.publish("pedalpoint/bikes/" + bikeCode + "/telemetry", message);
    }

    @Test
    void readsAMessagePublishedToTheBroker() throws Exception {
        publish("EB-001", "{\"battery\":42,\"hub\":\"Biblioteca\",\"lock\":\"LOCKED\"}");

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> assertThat(received)
                .containsExactly(new TelemetryMessage("EB-001", 42, "Biblioteca", "LOCKED", null)));
    }

    @Test
    void anInvalidMessageDoesNotStopTheNextOnes() throws Exception {
        publish("EB-001", "this is not json");
        publish("EB-002", "{\"battery\":70}");

        await().atMost(Duration.ofSeconds(10)).untilAsserted(
                () -> assertThat(received).extracting(TelemetryMessage::bikeCode).containsExactly("EB-002"));
    }
}
