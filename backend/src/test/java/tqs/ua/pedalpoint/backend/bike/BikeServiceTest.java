package tqs.ua.pedalpoint.backend.bike;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tqs.ua.pedalpoint.backend.hub.Hub;
import tqs.ua.pedalpoint.backend.hub.HubRepository;

/** Unit test: no Spring context, no database. */
@ExtendWith(MockitoExtension.class)
class BikeServiceTest {

    private static final long HUB_ID = 1L;
    private static final int MIN_BATTERY = 20;

    @Mock
    private BikeRepository bikeRepository;

    @Mock
    private HubRepository hubRepository;

    private BikeService bikeService;

    @BeforeEach
    void setUp() {
        bikeService = new BikeService(bikeRepository, hubRepository, MIN_BATTERY);
    }

    @Test
    void findAvailableAtHub_withoutType_asksForAvailableBikesAboveMinimumBattery() {
        Hub hub = new Hub("Residências", 20);
        when(hubRepository.existsById(HUB_ID)).thenReturn(true);
        when(bikeRepository.findByHubIdAndStateAndBatteryLevelGreaterThanEqualOrderByBatteryLevelDescCodeAsc(
                HUB_ID, BikeState.AVAILABLE, MIN_BATTERY))
                .thenReturn(List.of(new Bike("EB-001", BikeType.E_BIKE, BikeState.AVAILABLE, 80, hub)));

        List<BikeDto> result = bikeService.findAvailableAtHub(HUB_ID, null);

        assertThat(result).extracting(BikeDto::code, BikeDto::type, BikeDto::batteryLevel)
                .containsExactly(tuple("EB-001", BikeType.E_BIKE, 80));
        verify(bikeRepository, never())
                .findByHubIdAndTypeAndStateAndBatteryLevelGreaterThanEqualOrderByBatteryLevelDescCodeAsc(
                        anyLong(), any(), any(), anyInt());
    }

    @Test
    void findAvailableAtHub_withType_filtersByThatType() {
        when(hubRepository.existsById(HUB_ID)).thenReturn(true);
        when(bikeRepository.findByHubIdAndTypeAndStateAndBatteryLevelGreaterThanEqualOrderByBatteryLevelDescCodeAsc(
                HUB_ID, BikeType.CARGO_E_BIKE, BikeState.AVAILABLE, MIN_BATTERY))
                .thenReturn(List.of());

        assertThat(bikeService.findAvailableAtHub(HUB_ID, BikeType.CARGO_E_BIKE)).isEmpty();

        verify(bikeRepository)
                .findByHubIdAndTypeAndStateAndBatteryLevelGreaterThanEqualOrderByBatteryLevelDescCodeAsc(
                        HUB_ID, BikeType.CARGO_E_BIKE, BikeState.AVAILABLE, MIN_BATTERY);
    }

    @Test
    void findAvailableAtHub_unknownHub_throwsHubNotFound() {
        when(hubRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> bikeService.findAvailableAtHub(99L, null))
                .isInstanceOf(HubNotFoundException.class)
                .hasMessageContaining("99");
        verifyNoInteractions(bikeRepository);
    }
}
