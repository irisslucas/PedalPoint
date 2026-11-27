package tqs.ua.pedalpoint.simulator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class FleetSimulatorTest {

    /** Records what was published as "code:battery". */
    private static final class RecordingPublisher implements TelemetryPublisher {
        final List<String> published = new ArrayList<>();

        @Override
        public void publish(SimulatedBike bike) {
            published.add(bike.code() + ":" + bike.battery());
        }

        @Override
        public void close() {
            // nothing to release
        }
    }

    private final RecordingPublisher publisher = new RecordingPublisher();

    @Test
    void aTickChargesEveryDockedBikeAndThenPublishesThem() {
        FleetSimulator simulator = new FleetSimulator(List.of(
                SimulatedBike.dockedAt("EB-001", "Biblioteca", 10),
                SimulatedBike.dockedAt("EB-002", "Residências", 99)), 1, publisher);

        simulator.tick();

        assertThat(publisher.published).containsExactly("EB-001:11", "EB-002:100");
    }

    @Test
    void publishAllReportsTheCurrentStateWithoutCharging() {
        FleetSimulator simulator = new FleetSimulator(
                List.of(SimulatedBike.dockedAt("EB-001", "Biblioteca", 10)), 1, publisher);

        simulator.publishAll();

        assertThat(publisher.published).containsExactly("EB-001:10");
    }

    @Test
    void anEmptyBikeNeedsTwentyTicksToReachTheMinimumBattery() {
        SimulatedBike bike = SimulatedBike.dockedAt("EB-001", "Biblioteca", 0);
        FleetSimulator simulator = new FleetSimulator(List.of(bike), 1, publisher);

        for (int tick = 0; tick < 19; tick++) {
            simulator.tick();
        }
        assertThat(bike.battery()).isEqualTo(19);
        simulator.tick();
        assertThat(bike.battery()).isEqualTo(20);
    }

    @Test
    void rejectsANegativeCharge() {
        assertThatThrownBy(() -> new FleetSimulator(List.of(), -1, publisher))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
