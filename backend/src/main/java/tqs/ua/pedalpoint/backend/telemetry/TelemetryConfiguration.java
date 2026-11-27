package tqs.ua.pedalpoint.backend.telemetry;

import java.time.Clock;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Creates the MQTT adapter only when {@code pedalpoint.telemetry.mqtt.enabled=true} and, once the application
 * has started, connects whatever {@link TelemetrySource} exists to the {@link TelemetryService}.
 */
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

    @Bean
    @ConditionalOnMissingBean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    ApplicationRunner telemetryIngestion(ObjectProvider<TelemetrySource> source, TelemetryService service) {
        return args -> source.ifAvailable(telemetrySource -> telemetrySource.subscribe(service));
    }
}
