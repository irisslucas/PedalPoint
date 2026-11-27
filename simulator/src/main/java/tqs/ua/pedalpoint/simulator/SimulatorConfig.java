package tqs.ua.pedalpoint.simulator;

import java.util.Map;

/**
 * Settings, read from environment variables.
 *
 * @param brokerUrl     {@code MQTT_BROKER_URL}, default {@code tcp://localhost:1883}
 * @param tickSeconds   {@code TICK_SECONDS}: real seconds between two ticks, default 60 (one simulated minute);
 *                      use a small value to see charging quickly in a demo
 * @param chargePerTick {@code CHARGE_PERCENT_PER_TICK}: battery points a docked e-bike gains per tick, default 1
 *                      (the rule is 1% per minute)
 */
public record SimulatorConfig(String brokerUrl, int tickSeconds, int chargePerTick) {

    public static SimulatorConfig fromEnvironment(Map<String, String> env) {
        return new SimulatorConfig(
                env.getOrDefault("MQTT_BROKER_URL", "tcp://localhost:1883"),
                positive(env, "TICK_SECONDS", 60),
                positive(env, "CHARGE_PERCENT_PER_TICK", 1));
    }

    private static int positive(Map<String, String> env, String name, int fallback) {
        String text = env.get(name);
        if (text == null || text.isBlank()) {
            return fallback;
        }
        int value;
        try {
            value = Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(name + " must be a whole number: " + text, e);
        }
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be greater than zero: " + text);
        }
        return value;
    }
}
