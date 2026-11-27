package tqs.ua.pedalpoint.backend.telemetry;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Turns a topic and a JSON payload into a {@link TelemetryMessage}.
 * Topic: {@code pedalpoint/bikes/<code>/telemetry}. Payload:
 * {@code {"battery":55,"hub":"Biblioteca","lock":"LOCKED","faultCode":null}}; only {@code battery} is required.
 */
public class TelemetryMessageParser {

    private static final String PREFIX = "pedalpoint/bikes/";
    private static final String SUFFIX = "/telemetry";

    private final JsonMapper mapper = JsonMapper.builder().build();

    public TelemetryMessage parse(String topic, byte[] payload) {
        String code = bikeCode(topic);
        JsonNode json = readJson(payload);
        return new TelemetryMessage(code, battery(json), text(json, "hub"), text(json, "lock"),
                text(json, "faultCode"));
    }

    private String bikeCode(String topic) {
        if (topic == null || !topic.startsWith(PREFIX) || !topic.endsWith(SUFFIX)
                || topic.length() <= PREFIX.length() + SUFFIX.length()) {
            throw new InvalidTelemetryException("Unexpected topic: " + topic);
        }
        String code = topic.substring(PREFIX.length(), topic.length() - SUFFIX.length());
        if (code.contains("/")) {
            throw new InvalidTelemetryException("Unexpected topic: " + topic);
        }
        return code;
    }

    private JsonNode readJson(byte[] payload) {
        JsonNode json;
        try {
            json = mapper.readTree(payload);
        } catch (JacksonException e) {
            throw new InvalidTelemetryException("The payload is not valid JSON", e);
        }
        if (json == null || !json.isObject()) {
            throw new InvalidTelemetryException("The payload is not a JSON object");
        }
        return json;
    }

    private int battery(JsonNode json) {
        JsonNode node = json.get("battery");
        if (node == null || !node.isIntegralNumber() || !node.canConvertToInt()) {
            throw new InvalidTelemetryException("The battery is missing or not a whole number");
        }
        int battery = node.intValue();
        if (battery < 0 || battery > 100) {
            throw new InvalidTelemetryException("The battery must be between 0 and 100: " + battery);
        }
        return battery;
    }

    private String text(JsonNode json, String field) {
        JsonNode node = json.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isString()) {
            throw new InvalidTelemetryException("The field " + field + " must be text");
        }
        String value = node.stringValue();
        return value.isBlank() ? null : value;
    }
}
