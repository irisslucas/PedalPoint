package tqs.ua.pedalpoint.backend.bike;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import tqs.ua.pedalpoint.backend.TestcontainersConfiguration;
import tqs.ua.pedalpoint.backend.hub.Hub;
import tqs.ua.pedalpoint.backend.hub.HubRepository;

/**
 * Integration test: full app + real PostgreSQL (Testcontainers), so the repository query,
 * the V3 schema and the HTTP mapping are checked together. The business rules themselves are
 * covered by the Cucumber scenarios in features/us04_available_ebikes.feature.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class BikeApiIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private BikeRepository bikeRepository;

    @Autowired
    private HubRepository hubRepository;

    private Hub hub;

    @BeforeEach
    void setUp() {
        bikeRepository.deleteAll();
        hub = hubRepository.findByName("Biblioteca").orElseThrow();
    }

    @Test
    void listBikes_returnsOnlyAvailableBikesWithEnoughBattery_mostChargedFirst() throws Exception {
        bikeRepository.save(new Bike("IT-001", BikeType.E_BIKE, BikeState.AVAILABLE, 40, hub));
        bikeRepository.save(new Bike("IT-002", BikeType.CARGO_E_BIKE, BikeState.AVAILABLE, 90, hub));
        bikeRepository.save(new Bike("IT-003", BikeType.E_BIKE, BikeState.CHARGING, 10, hub));
        bikeRepository.save(new Bike("IT-004", BikeType.E_BIKE, BikeState.AVAILABLE, 19, hub));

        mvc.perform(get("/api/hubs/{id}/bikes", hub.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].code").value("IT-002"))
                .andExpect(jsonPath("$[0].type").value("CARGO_E_BIKE"))
                .andExpect(jsonPath("$[0].batteryLevel").value(90))
                .andExpect(jsonPath("$[1].code").value("IT-001"));
    }

    @Test
    void listBikes_filteredByType() throws Exception {
        bikeRepository.save(new Bike("IT-010", BikeType.E_BIKE, BikeState.AVAILABLE, 80, hub));
        bikeRepository.save(new Bike("IT-011", BikeType.CARGO_E_BIKE, BikeState.AVAILABLE, 80, hub));

        mvc.perform(get("/api/hubs/{id}/bikes", hub.getId()).param("type", "E_BIKE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code").value("IT-010"));
    }

    @Test
    void listBikes_unknownHub_returns404() throws Exception {
        mvc.perform(get("/api/hubs/{id}/bikes", 999_999))
                .andExpect(status().isNotFound());
    }

    @Test
    void listBikes_unknownType_returns400() throws Exception {
        mvc.perform(get("/api/hubs/{id}/bikes", hub.getId()).param("type", "SCOOTER"))
                .andExpect(status().isBadRequest());
    }
}
