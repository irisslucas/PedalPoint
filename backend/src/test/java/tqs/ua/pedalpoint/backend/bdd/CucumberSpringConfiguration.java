package tqs.ua.pedalpoint.backend.bdd;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import io.cucumber.spring.CucumberContextConfiguration;
import tqs.ua.pedalpoint.backend.MosquittoContainer;
import tqs.ua.pedalpoint.backend.TestcontainersConfiguration;

/** One Spring context (full app + PostgreSQL and Mosquitto in containers) shared by all Cucumber scenarios. */
@CucumberContextConfiguration
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class CucumberSpringConfiguration {

    @DynamicPropertySource
    static void mosquitto(DynamicPropertyRegistry registry) {
        MosquittoContainer.register(registry);
    }
}
