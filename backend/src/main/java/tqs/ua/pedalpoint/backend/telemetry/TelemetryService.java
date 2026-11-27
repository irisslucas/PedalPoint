package tqs.ua.pedalpoint.backend.telemetry;

import java.time.Clock;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tqs.ua.pedalpoint.backend.bike.Bike;
import tqs.ua.pedalpoint.backend.bike.BikeRepository;
import tqs.ua.pedalpoint.backend.hub.Hub;
import tqs.ua.pedalpoint.backend.hub.HubRepository;

/**
 * Updates the e-bike from a telemetry message. It knows nothing about MQTT: messages arrive through
 * {@link TelemetryHandler}, from whatever {@link TelemetrySource} is in use.
 */
@Service
public class TelemetryService implements TelemetryHandler {

    private static final Logger log = LoggerFactory.getLogger(TelemetryService.class);

    private final BikeRepository bikeRepository;
    private final HubRepository hubRepository;
    private final Clock clock;
    private final int minBattery;

    public TelemetryService(BikeRepository bikeRepository, HubRepository hubRepository, Clock clock,
            @Value("${pedalpoint.rules.min-battery-to-book:20}") int minBattery) {
        this.bikeRepository = bikeRepository;
        this.hubRepository = hubRepository;
        this.clock = clock;
        this.minBattery = minBattery;
    }

    /** Messages for an unknown e-bike or hub are logged and ignored, never stored. */
    @Override
    @Transactional
    public void onTelemetry(TelemetryMessage message) {
        Optional<Bike> bike = bikeRepository.findByCode(message.bikeCode());
        if (bike.isEmpty()) {
            log.warn("Telemetry ignored: unknown e-bike {}", message.bikeCode());
            return;
        }
        Hub hub = null;
        if (message.hub() != null) {
            hub = hubRepository.findByName(message.hub()).orElse(null);
            if (hub == null) {
                log.warn("Telemetry of {} ignored: unknown hub {}", message.bikeCode(), message.hub());
                return;
            }
        }
        bike.get().recordTelemetry(message.battery(), hub, clock.instant(), minBattery);
        if (message.faultCode() != null) {
            log.warn("E-bike {} reports fault {}", message.bikeCode(), message.faultCode());
        }
    }
}
