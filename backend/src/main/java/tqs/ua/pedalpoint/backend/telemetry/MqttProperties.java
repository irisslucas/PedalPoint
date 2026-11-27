package tqs.ua.pedalpoint.backend.telemetry;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Settings of the MQTT telemetry adapter ({@code pedalpoint.telemetry.mqtt.*}). */
@ConfigurationProperties(prefix = "pedalpoint.telemetry.mqtt")
public record MqttProperties(boolean enabled, String brokerUrl, String topic, String clientId) {

    public MqttProperties {
        if (brokerUrl == null || brokerUrl.isBlank()) {
            brokerUrl = "tcp://localhost:1883";
        }
        if (topic == null || topic.isBlank()) {
            topic = "pedalpoint/bikes/+/telemetry";
        }
        if (clientId == null || clientId.isBlank()) {
            clientId = "pedalpoint-backend";
        }
    }
}
