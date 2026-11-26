package tqs.ua.pedalpoint.backend.hub;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HubRepository extends JpaRepository<Hub, Long> {

    List<Hub> findAllByOrderByNameAsc();

    Optional<Hub> findByName(String name);
}
