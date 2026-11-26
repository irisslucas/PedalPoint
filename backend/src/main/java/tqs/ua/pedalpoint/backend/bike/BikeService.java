package tqs.ua.pedalpoint.backend.bike;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tqs.ua.pedalpoint.backend.hub.HubRepository;

@Service
public class BikeService {

    private final BikeRepository bikeRepository;
    private final HubRepository hubRepository;
    private final int minBatteryToBook;

    public BikeService(BikeRepository bikeRepository, HubRepository hubRepository,
            @Value("${pedalpoint.rules.min-battery-to-book:20}") int minBatteryToBook) {
        this.bikeRepository = bikeRepository;
        this.hubRepository = hubRepository;
        this.minBatteryToBook = minBatteryToBook;
    }

    /**
     * E-bikes a user can take at a hub: state AVAILABLE and battery at or above the minimum,
     * optionally of one type, with the most charged first.
     */
    @Transactional(readOnly = true)
    public List<BikeDto> findAvailableAtHub(Long hubId, BikeType type) {
        if (!hubRepository.existsById(hubId)) {
            throw new HubNotFoundException(hubId);
        }
        List<Bike> bikes = type == null
                ? bikeRepository.findByHubIdAndStateAndBatteryLevelGreaterThanEqualOrderByBatteryLevelDescCodeAsc(
                        hubId, BikeState.AVAILABLE, minBatteryToBook)
                : bikeRepository.findByHubIdAndTypeAndStateAndBatteryLevelGreaterThanEqualOrderByBatteryLevelDescCodeAsc(
                        hubId, type, BikeState.AVAILABLE, minBatteryToBook);
        return bikes.stream().map(BikeDto::from).toList();
    }

    public int getMinBatteryToBook() {
        return minBatteryToBook;
    }
}
