package tqs.ua.pedalpoint.backend.bdd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import tqs.ua.pedalpoint.backend.bike.Bike;
import tqs.ua.pedalpoint.backend.bike.BikeRepository;
import tqs.ua.pedalpoint.backend.bike.BikeState;
import tqs.ua.pedalpoint.backend.bike.BikeType;
import tqs.ua.pedalpoint.backend.hub.Hub;
import tqs.ua.pedalpoint.backend.hub.HubRepository;
import tqs.ua.pedalpoint.backend.telemetry.MqttProperties;
import tqs.ua.pedalpoint.backend.telemetry.MqttTelemetryAdapter;
import tqs.ua.pedalpoint.backend.telemetry.TelemetryService;
import tqs.ua.pedalpoint.backend.telemetry.TelemetrySource;

/**
 * Step definitions for features/us20_telemetry.feature (US-20); also gives the US-21 steps a way to publish.
 * Messages go through a real Mosquitto broker, the MqttTelemetryAdapter and the TelemetryService.
 * After each publish the steps wait until the backend has processed it, so the Then steps see the final state.
 */
public class TelemetrySteps {

    private static final String SYNC_BIKE = "SYNC-MARKER";
    private static final String DEFAULT_HUB = "Residências";

    private final BikeRepository bikeRepository;
    private final HubRepository hubRepository;
    private final TelemetrySource telemetrySource;
    private final MqttProperties mqttProperties;

    private MqttClient publisher;
    private final ListAppender<ILoggingEvent> adapterLog = new ListAppender<>();
    private final ListAppender<ILoggingEvent> serviceLog = new ListAppender<>();
    private boolean published;

    public TelemetrySteps(BikeRepository bikeRepository, HubRepository hubRepository, TelemetrySource telemetrySource,
            MqttProperties mqttProperties) {
        this.bikeRepository = bikeRepository;
        this.hubRepository = hubRepository;
        this.telemetrySource = telemetrySource;
        this.mqttProperties = mqttProperties;
    }

    // order after AvailableBikesSteps.cleanFleet (default order 10000), which deletes every e-bike
    @Before(value = "@US-20 or @US-21", order = 20_000)
    public void startScenario() throws Exception {
        published = false;
        attach(MqttTelemetryAdapter.class, adapterLog);
        attach(TelemetryService.class, serviceLog);
        // the scenarios refer to these two e-bikes; EB-001 is set up again by the Given steps
        parkedBike("EB-001", 100, DEFAULT_HUB);
        parkedBike("EB-002", 100, DEFAULT_HUB);
        publisher = new MqttClient(mqttProperties.brokerUrl(), "bdd-publisher", new MemoryPersistence());
        publisher.connect();
    }

    @After(value = "@US-20 or @US-21", order = 20_000)
    public void endScenario() throws Exception {
        publisher.disconnect();
        publisher.close();
        detach(MqttTelemetryAdapter.class, adapterLog);
        detach(TelemetryService.class, serviceLog);
    }

    // The feature uses the same sentence as a Given (set up) and as a Then (check). Cucumber matches steps by text
    // and not by keyword, so there is one definition: it sets up before anything is published and checks afterwards.

    @Given("the e-bike {string} has battery {int}% at hub {string}")
    public void theEBikeHasBatteryAtHub(String code, int battery, String hubName) {
        if (!published) {
            parkedBike(code, battery, hubName);
            return;
        }
        Bike bike = bike(code);
        assertThat(bike.getBatteryLevel()).isEqualTo(battery);
        assertThat(bike.getHub().getId()).isEqualTo(hub(hubName).getId());
    }

    @Given("the e-bike {string} has battery {int}%")
    public void theEBikeHasBattery(String code, int battery) {
        if (!published) {
            parkedBike(code, battery, DEFAULT_HUB);
            return;
        }
        assertThat(bike(code).getBatteryLevel()).isEqualTo(battery);
    }

    @When("the telemetry source publishes for {string}:")
    public void theTelemetrySourcePublishesFor(String code, DataTable table) throws Exception {
        Map<String, String> row = table.asMaps().get(0);
        publish(code, json(row));
    }

    @When("the telemetry source publishes an invalid message for {string}")
    public void theTelemetrySourcePublishesAnInvalidMessageFor(String code) throws Exception {
        publish(code, "{ this is not valid json");
    }

    @When("the telemetry source publishes battery {int}% for {string}")
    public void theTelemetrySourcePublishesBatteryFor(int battery, String code) throws Exception {
        publishBattery(code, battery);
    }

    @Then("the last telemetry time of {string} is updated")
    public void theLastTelemetryTimeIsUpdated(String code) {
        assertThat(bike(code).getLastTelemetryAt()).isNotNull();
    }

    @Then("the e-bike {string} still has battery {int}%")
    public void theEBikeStillHasBattery(String code, int battery) {
        assertThat(bike(code).getBatteryLevel()).isEqualTo(battery);
    }

    @Then("the invalid message is logged")
    public void theInvalidMessageIsLogged() {
        assertThat(adapterLog.list).anySatisfy(event -> {
            assertThat(event.getLevel()).isEqualTo(Level.WARN);
            assertThat(event.getFormattedMessage()).contains("Invalid telemetry ignored");
        });
    }

    @Given("a Mosquitto broker is running")
    public void aMosquittoBrokerIsRunning() {
        assertThat(publisher.isConnected()).isTrue();
    }

    @Given("the MqttTelemetryAdapter is subscribed to {string}")
    public void theMqttTelemetryAdapterIsSubscribedTo(String topic) {
        assertThat(telemetrySource).isInstanceOf(MqttTelemetryAdapter.class);
        assertThat(mqttProperties.topic()).isEqualTo(topic);
    }

    @When("a message with battery {int} is published to {string}")
    public void aMessageWithBatteryIsPublishedTo(int battery, String topic) throws Exception {
        send(topic, "{\"battery\":" + battery + "}");
        awaitProcessed();
    }

    /** Publishes a reading with only the battery and waits until the backend has processed it. */
    void publishBattery(String code, int battery) throws Exception {
        publish(code, "{\"battery\":" + battery + "}");
    }

    private void publish(String code, String payload) throws Exception {
        send("pedalpoint/bikes/" + code + "/telemetry", payload);
        awaitProcessed();
    }

    private void send(String topic, String payload) throws Exception {
        published = true;
        MqttMessage message = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
        message.setQos(1);
        publisher.publish(topic, message);
    }

    /**
     * Messages of one publisher are handled in order, so once a message for an unknown e-bike has been
     * logged by the service, everything published before it has been processed.
     */
    private void awaitProcessed() throws Exception {
        int seen = serviceLog.list.size();
        send("pedalpoint/bikes/" + SYNC_BIKE + "/telemetry", "{\"battery\":1}");
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> assertThat(
                List.copyOf(serviceLog.list).subList(seen, serviceLog.list.size()))
                .anyMatch(event -> event.getFormattedMessage().contains(SYNC_BIKE)));
    }

    private static String json(Map<String, String> row) {
        StringBuilder json = new StringBuilder("{");
        row.forEach((key, value) -> {
            if (value == null || value.isBlank()) {
                return;
            }
            String quoted = "battery".equals(key) ? value : "\"" + value + "\"";
            json.append(json.length() > 1 ? "," : "").append('"').append(key).append("\":").append(quoted);
        });
        return json.append('}').toString();
    }

    private void parkedBike(String code, int battery, String hubName) {
        dockedBike(code, battery, hubName, BikeState.AVAILABLE);
    }

    void dockedBike(String code, int battery, String hubName, BikeState state) {
        bikeRepository.findByCode(code).ifPresent(bikeRepository::delete);
        bikeRepository.save(new Bike(code, BikeType.E_BIKE, state, battery, hub(hubName)));
    }

    private Bike bike(String code) {
        return bikeRepository.findByCode(code).orElseThrow();
    }

    private Hub hub(String name) {
        return hubRepository.findByName(name).orElseGet(() -> hubRepository.save(new Hub(name, 20)));
    }

    private void attach(Class<?> type, ListAppender<ILoggingEvent> appender) {
        appender.start();
        ((Logger) LoggerFactory.getLogger(type)).addAppender(appender);
    }

    private void detach(Class<?> type, ListAppender<ILoggingEvent> appender) {
        ((Logger) LoggerFactory.getLogger(type)).detachAppender(appender);
        appender.stop();
    }
}
