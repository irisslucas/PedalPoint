package tqs.ua.pedalpoint.simulator;

/**
 * The payload the backend reads: {@code {"battery":55,"hub":"Biblioteca","lock":"LOCKED","faultCode":null}}.
 * Built by hand: four fields do not justify a JSON library in the simulator.
 */
public final class TelemetryJson {

    private TelemetryJson() {
    }

    public static String of(SimulatedBike bike) {
        return "{\"battery\":" + bike.battery()
                + ",\"hub\":" + string(bike.hub())
                + ",\"lock\":" + string(bike.lock())
                + ",\"faultCode\":" + string(bike.faultCode()) + "}";
    }

    static String string(String value) {
        if (value == null) {
            return "null";
        }
        StringBuilder out = new StringBuilder("\"");
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        return out.append('"').toString();
    }
}
