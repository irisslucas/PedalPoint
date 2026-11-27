package tqs.ua.pedalpoint.simulator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class DefaultFleetTest {

    private static final Path BACKEND_SEED = Path.of("../backend/src/main/resources/db/migration/V5__seed_bikes.sql");

    @Test
    void codesAreUnique() {
        assertThat(DefaultFleet.create()).extracting(SimulatedBike::code).doesNotHaveDuplicates();
    }

    /** The backend ignores telemetry of e-bikes it does not know, so the demo fleet must follow the seed data. */
    @Test
    void everyBikeExistsInTheBackendSeedData() throws IOException {
        assumeTrue(Files.exists(BACKEND_SEED), "the backend is not next to the simulator");
        String seed = Files.readString(BACKEND_SEED);

        for (SimulatedBike bike : DefaultFleet.create()) {
            assertThat(seed).contains("'" + bike.code() + "'").contains("'" + bike.hub() + "'");
        }
    }
}
