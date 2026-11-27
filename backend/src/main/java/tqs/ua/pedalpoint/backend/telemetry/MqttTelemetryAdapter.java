package tqs.ua.pedalpoint.backend.telemetry;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link TelemetrySource} that reads the telemetry the simulator (or the real e-bikes) publishes to an MQTT broker.
 * Invalid messages are logged and dropped; the next messages are still processed.
 */
public class MqttTelemetryAdapter implements TelemetrySource {

    private static final Logger log = LoggerFactory.getLogger(MqttTelemetryAdapter.class);
    private static final int QOS_AT_LEAST_ONCE = 1;

    private final MqttProperties properties;
    private final TelemetryMessageParser parser;
    private IMqttClient client;

    public MqttTelemetryAdapter(MqttProperties properties, TelemetryMessageParser parser) {
        this.properties = properties;
        this.parser = parser;
    }

    @Override
    public synchronized void subscribe(TelemetryHandler handler) {
        try {
            client = new MqttClient(properties.brokerUrl(), properties.clientId(), new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            client.setCallback(new Callback(handler));
            client.connect(options);
            subscribeToTopic(handler);
            log.info("Subscribed to {} on {}", properties.topic(), properties.brokerUrl());
        } catch (MqttException e) {
            throw new IllegalStateException("Cannot subscribe to telemetry on " + properties.brokerUrl(), e);
        }
    }

    private void subscribeToTopic(TelemetryHandler handler) throws MqttException {
        client.subscribe(properties.topic(), QOS_AT_LEAST_ONCE,
                (topic, message) -> deliver(handler, topic, message.getPayload()));
    }

    void deliver(TelemetryHandler handler, String topic, byte[] payload) {
        TelemetryMessage message;
        try {
            message = parser.parse(topic, payload);
        } catch (InvalidTelemetryException e) {
            log.warn("Invalid telemetry ignored (topic {}): {}", topic, e.getMessage());
            return;
        }
        try {
            handler.onTelemetry(message);
        } catch (RuntimeException e) {
            log.error("Telemetry of {} could not be processed", message.bikeCode(), e);
        }
    }

    @Override
    public synchronized void close() {
        if (client == null) {
            return;
        }
        try {
            client.disconnect();
            client.close();
        } catch (MqttException e) {
            log.warn("Error while closing the MQTT connection", e);
        } finally {
            client = null;
        }
    }

    /** After an automatic reconnect the clean session has lost the subscription, so it is made again. */
    private final class Callback implements MqttCallbackExtended {

        private final TelemetryHandler handler;

        Callback(TelemetryHandler handler) {
            this.handler = handler;
        }

        @Override
        public void connectComplete(boolean reconnect, String serverUri) {
            if (!reconnect) {
                return;
            }
            try {
                subscribeToTopic(handler);
                log.info("Reconnected to {} and subscribed again", serverUri);
            } catch (MqttException e) {
                log.error("Could not subscribe again after reconnecting", e);
            }
        }

        @Override
        public void connectionLost(Throwable cause) {
            log.warn("MQTT connection lost: {}", cause.getMessage());
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            // delivered through the subscription listener
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            // the adapter only receives
        }
    }
}
