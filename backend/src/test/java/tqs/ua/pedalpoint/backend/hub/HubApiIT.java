package tqs.ua.pedalpoint.backend.hub;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import tqs.ua.pedalpoint.backend.TestcontainersConfiguration;

/**
 * Integration test: full app + real PostgreSQL in a container (Testcontainers).
 * Flyway migrations run against it, so this also checks the schema and seed data.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class HubApiIT {

    @Autowired
    private MockMvc mvc;

    @Test
    void getHubs_returnsSeededHubsOrderedByName() throws Exception {
        mvc.perform(get("/api/hubs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Biblioteca"))
                .andExpect(jsonPath("$[1].name").value("Departamento de Informática"))
                .andExpect(jsonPath("$[2].name").value("Residências"));
    }

    @Test
    void healthEndpoint_isUp() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
