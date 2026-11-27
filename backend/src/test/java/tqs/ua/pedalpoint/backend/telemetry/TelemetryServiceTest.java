package tqs.ua.pedalpoint.backend.telemetry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tqs.ua.pedalpoint.backend.bike.Bike;
import tqs.ua.pedalpoint.backend.bike.BikeRepository;
import tqs.ua.pedalpoint.backend.bike.BikeState;
import tqs.ua.pedalpoint.backend.bike.BikeType;
import tqs.ua.pedalpoint.backend.hub.Hub;
import tqs.ua.pedalpoint.backend.hub.HubRepository;

@ExtendWith(MockitoExtension.class)
class TelemetryServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-27T10:00:00Z");

    @Mock
    private BikeRepository bikeRepository;

    @Mock
    private HubRepository hubRepository;

    private TelemetryService service;
    private final Hub residencias = new Hub("Residências", 20);
    private final Hub biblioteca = new Hub("Biblioteca", 15);
    private Bike bike;

    @BeforeEach
    void setUp() {
        service = new TelemetryService(bikeRepository, hubRepository, Clock.fixed(NOW, ZoneOffset.UTC), 20);
        bike = new Bike("EB-001", BikeType.E_BIKE, BikeState.AVAILABLE, 80, residencias);
    }

    @Test
    void updatesBatteryHubAndTimeOfTheBike() {
        when(bikeRepository.findByCode("EB-001")).thenReturn(Optional.of(bike));
        when(hubRepository.findByName("Biblioteca")).thenReturn(Optional.of(biblioteca));

        service.onTelemetry(new TelemetryMessage("EB-001", 55, "Biblioteca", "LOCKED", null));

        assertThat(bike.getBatteryLevel()).isEqualTo(55);
        assertThat(bike.getHub()).isSameAs(biblioteca);
        assertThat(bike.getLastTelemetryAt()).isEqualTo(NOW);
        assertThat(bike.getState()).isEqualTo(BikeState.AVAILABLE);
    }

    @Test
    void aLowBatteryAtAHubStartsCharging() {
        when(bikeRepository.findByCode("EB-001")).thenReturn(Optional.of(bike));

        service.onTelemetry(new TelemetryMessage("EB-001", 19, null, null, null));

        assertThat(bike.getState()).isEqualTo(BikeState.CHARGING);
    }

    @Test
    void anUnknownBikeIsIgnored() {
        when(bikeRepository.findByCode("EB-404")).thenReturn(Optional.empty());

        service.onTelemetry(new TelemetryMessage("EB-404", 55, "Biblioteca", null, null));

        assertThat(bike.getBatteryLevel()).isEqualTo(80);
    }

    @Test
    void anUnknownHubLeavesTheBikeUntouched() {
        when(bikeRepository.findByCode("EB-001")).thenReturn(Optional.of(bike));
        when(hubRepository.findByName("Marte")).thenReturn(Optional.empty());

        service.onTelemetry(new TelemetryMessage("EB-001", 10, "Marte", null, null));

        assertThat(bike.getBatteryLevel()).isEqualTo(80);
        assertThat(bike.getState()).isEqualTo(BikeState.AVAILABLE);
        assertThat(bike.getLastTelemetryAt()).isNull();
    }
}
