package tqs.ua.pedalpoint.backend.bike;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BikeRepository extends JpaRepository<Bike, Long> {

    List<Bike> findByHubIdAndStateAndBatteryLevelGreaterThanEqualOrderByBatteryLevelDescCodeAsc(
            Long hubId, BikeState state, int minBatteryLevel);

    List<Bike> findByHubIdAndTypeAndStateAndBatteryLevelGreaterThanEqualOrderByBatteryLevelDescCodeAsc(
            Long hubId, BikeType type, BikeState state, int minBatteryLevel);
}
