package tqs.ua.pedalpoint.backend;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.Transferable;

/**
 * One Mosquitto broker shared by the tests that need the telemetry adapter. A test class that needs it calls
 * {@link #register} from a {@code @DynamicPropertySource} method: that turns the adapter on and points it at the
 * broker before the application context is created (the adapter bean is conditional on those properties).
 * Other tests do not call it and run without MQTT. The container is removed by Testcontainers when the JVM ends.
 */
public final class MosquittoContainer {

    private static final int MQTT_PORT = 1883;

    private static final GenericContainer<?> BROKER = new GenericContainer<>("eclipse-mosquitto:2.0.20")
            .withExposedPorts(MQTT_PORT)
            .withCopyToContainer(Transferable.of("listener 1883\nallow_anonymous true\n"),
                    "/mosquitto/config/mosquitto.conf")
            .waitingFor(Wait.forLogMessage(".*mosquitto version .* running.*", 1));

    private MosquittoContainer() {
    }

    public static void register(DynamicPropertyRegistry registry) {
        if (!BROKER.isRunning()) {
            BROKER.start();
        }
        registry.add("pedalpoint.telemetry.mqtt.enabled", () -> "true");
        registry.add("pedalpoint.telemetry.mqtt.broker-url",
                () -> "tcp://" + BROKER.getHost() + ":" + BROKER.getMappedPort(MQTT_PORT));
    }
}
