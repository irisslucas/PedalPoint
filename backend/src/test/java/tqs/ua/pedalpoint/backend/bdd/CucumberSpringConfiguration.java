package tqs.ua.pedalpoint.backend.bdd;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;

import io.cucumber.spring.CucumberContextConfiguration;
import tqs.ua.pedalpoint.backend.TestcontainersConfiguration;

/** One Spring context (full app + PostgreSQL in a container) shared by all Cucumber scenarios. */
@CucumberContextConfiguration
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class CucumberSpringConfiguration {
}
