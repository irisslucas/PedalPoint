package tqs.ua.pedalpoint.simulator;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Starts the fleet simulator: publishes the fleet at once, then charges and publishes at every tick. */
public final class SimulatorApplication {

    private static final System.Logger LOG = System.getLogger(SimulatorApplication.class.getName());

    private SimulatorApplication() {
    }

    public static void main(String[] args) throws Exception {
        SimulatorConfig config = SimulatorConfig.fromEnvironment(System.getenv());
        MqttTelemetryPublisher publisher = new MqttTelemetryPublisher(config.brokerUrl(), "pedalpoint-simulator");
        FleetSimulator simulator = new FleetSimulator(DefaultFleet.create(), config.chargePerTick(), publisher);

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            scheduler.shutdownNow();
            publisher.close();
        }));

        LOG.log(System.Logger.Level.INFO, "Simulating {0} e-bikes on {1}: +{2}% every {3} s",
                simulator.fleet().size(), config.brokerUrl(), config.chargePerTick(), config.tickSeconds());
        simulator.publishAll();
        scheduler.scheduleAtFixedRate(guarded(simulator), config.tickSeconds(), config.tickSeconds(),
                TimeUnit.SECONDS);
    }

    /** An exception must not cancel the schedule: a failed tick is logged and the next one runs. */
    private static Runnable guarded(FleetSimulator simulator) {
        return () -> {
            try {
                simulator.tick();
            } catch (RuntimeException e) {
                LOG.log(System.Logger.Level.ERROR, "Simulation tick failed", e);
            }
        };
    }
}
