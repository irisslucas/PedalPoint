package tqs.ua.pedalpoint.backend.cucumber;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;

import io.cucumber.spring.CucumberContextConfiguration;
import tqs.ua.pedalpoint.backend.TestcontainersConfiguration;

/** Spring context shared by all step definitions: full app + real PostgreSQL (Testcontainers). */
@CucumberContextConfiguration
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class CucumberSpringConfiguration {
}