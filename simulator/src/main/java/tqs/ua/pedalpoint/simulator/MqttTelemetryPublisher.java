package tqs.ua.pedalpoint.simulator;

import java.nio.charset.StandardCharsets;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/** Publishes to {@code pedalpoint/bikes/<code>/telemetry}. A failed publish is logged; the next tick tries again. */
public class MqttTelemetryPublisher implements TelemetryPublisher {

    private static final System.Logger LOG = System.getLogger(MqttTelemetryPublisher.class.getName());

    private final MqttClient client;

    public MqttTelemetryPublisher(String brokerUrl, String clientId) throws MqttException {
        client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        client.connect(options);
    }

    static String topic(String bikeCode) {
        return "pedalpoint/bikes/" + bikeCode + "/telemetry";
    }

    @Override
    public void publish(SimulatedBike bike) {
        MqttMessage message = new MqttMessage(TelemetryJson.of(bike).getBytes(StandardCharsets.UTF_8));
        message.setQos(1);
        try {
            client.publish(topic(bike.code()), message);
        } catch (MqttException e) {
            LOG.log(System.Logger.Level.WARNING, "Could not publish telemetry of " + bike.code(), e);
        }
    }

    @Override
    public void close() {
        try {
            client.disconnect();
            client.close();
        } catch (MqttException e) {
            LOG.log(System.Logger.Level.WARNING, "Error while closing the MQTT connection", e);
        }
    }
}
