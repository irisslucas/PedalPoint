package tqs.ua.pedalpoint.backend.telemetry;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Creates the MQTT adapter only when {@code pedalpoint.telemetry.mqtt.enabled=true}. */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(MqttProperties.class)
public class TelemetryConfiguration {

    @Bean
    TelemetryMessageParser telemetryMessageParser() {
        return new TelemetryMessageParser();
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(prefix = "pedalpoint.telemetry.mqtt", name = "enabled", havingValue = "true")
    TelemetrySource mqttTelemetrySource(MqttProperties properties, TelemetryMessageParser parser) {
        return new MqttTelemetryAdapter(properties, parser);
    }
}
