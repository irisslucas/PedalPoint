package tqs.ua.pedalpoint.backend.bike;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BikeRepository extends JpaRepository<Bike, Long> {

    Optional<Bike> findByCode(String code);

    List<Bike> findByHubIdAndStateAndBatteryLevelGreaterThanEqualOrderByBatteryLevelDescCodeAsc(
            Long hubId, BikeState state, int minBatteryLevel);

    List<Bike> findByHubIdAndTypeAndStateAndBatteryLevelGreaterThanEqualOrderByBatteryLevelDescCodeAsc(
            Long hubId, BikeType type, BikeState state, int minBatteryLevel);
}
