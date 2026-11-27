package tqs.ua.pedalpoint.simulator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** The simulator against a real Mosquitto broker. */
@Testcontainers
class MqttTelemetryPublisherIT {

    @Container
    static final GenericContainer<?> MOSQUITTO = new GenericContainer<>("eclipse-mosquitto:2.0.20")
            .withExposedPorts(1883)
            .withCopyToContainer(Transferable.of("listener 1883\nallow_anonymous true\n"),
                    "/mosquitto/config/mosquitto.conf")
            .waitingFor(Wait.forLogMessage(".*mosquitto version .* running.*", 1));

    private final List<String> received = new CopyOnWriteArrayList<>();
    private String url;
    private MqttClient subscriber;
    private MqttTelemetryPublisher publisher;

    @BeforeEach
    void connect() throws Exception {
        url = "tcp://" + MOSQUITTO.getHost() + ":" + MOSQUITTO.getMappedPort(1883);
        subscriber = new MqttClient(url, "subscriber-it", new MemoryPersistence());
        subscriber.connect();
        subscriber.subscribe("pedalpoint/bikes/+/telemetry", 1, (topic, message) -> received
                .add(topic + " " + new String(message.getPayload(), StandardCharsets.UTF_8)));
        publisher = new MqttTelemetryPublisher(url, "simulator-it");
    }

    @AfterEach
    void disconnect() throws Exception {
        publisher.close();
        subscriber.disconnect();
        subscriber.close();
    }

    @Test
    void publishesTheTelemetryOfEachBikeToItsTopic() {
        FleetSimulator simulator = new FleetSimulator(List.of(
                SimulatedBike.dockedAt("EB-001", "Biblioteca", 10)), 1, publisher);

        simulator.tick();
        simulator.tick();

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> assertThat(received).containsExactly(
                "pedalpoint/bikes/EB-001/telemetry {\"battery\":11,\"hub\":\"Biblioteca\",\"lock\":\"LOCKED\",\"faultCode\":null}",
                "pedalpoint/bikes/EB-001/telemetry {\"battery\":12,\"hub\":\"Biblioteca\",\"lock\":\"LOCKED\",\"faultCode\":null}"));
    }
}
