package tqs.ua.pedalpoint.backend.bdd;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import tqs.ua.pedalpoint.backend.bike.Bike;
import tqs.ua.pedalpoint.backend.bike.BikeRepository;
import tqs.ua.pedalpoint.backend.bike.BikeService;
import tqs.ua.pedalpoint.backend.bike.BikeState;

/**
 * Step definitions for features/us21_charging.feature (US-21).
 * The backend does not charge e-bikes: the chargers at the hubs (the simulator) do, and the backend reacts to the
 * telemetry. Here "minutes pass" is played by a stand-in charger that publishes one reading per minute, at the rate
 * given in the Background. The 1% per minute of the real simulator is checked in the simulator's own tests.
 */
public class ChargingSteps {

    private final TelemetrySteps telemetry;
    private final BikeRepository bikeRepository;
    private final BikeService bikeService;

    private int chargePerMinute;
    private final Map<String, Integer> batteryAtArrival = new HashMap<>();

    public ChargingSteps(TelemetrySteps telemetry, BikeRepository bikeRepository, BikeService bikeService) {
        this.telemetry = telemetry;
        this.bikeRepository = bikeRepository;
        this.bikeService = bikeService;
    }

    @Given("the minimum battery to use an e-bike is {int}%")
    public void theMinimumBatteryToUseAnEBikeIs(int minBattery) {
        assertThat(bikeService.getMinBatteryToBook()).isEqualTo(minBattery);
    }

    @Given("every hub charges e-bikes at {int}% per minute")
    public void everyHubChargesEBikesAtPerMinute(int percent) {
        chargePerMinute = percent;
    }

    @Given("the e-bike {string} is parked at hub {string}")
    public void theEBikeIsParkedAtHub(String code, String hub) {
        telemetry.dockedBike(code, 100, hub, BikeState.AVAILABLE);
    }

    /** A trip that ends at a hub leaves the e-bike CHARGING when it is below the minimum, otherwise AVAILABLE. */
    @Given("the e-bike {string} arrived at hub {string} with battery {int}%")
    public void theEBikeArrivedAtHubWithBattery(String code, String hub, int battery) {
        BikeState state = battery < bikeService.getMinBatteryToBook() ? BikeState.CHARGING : BikeState.AVAILABLE;
        telemetry.dockedBike(code, battery, hub, state);
        batteryAtArrival.put(code, battery);
    }

    @When("{int} minutes pass")
    public void minutesPass(int minutes) throws Exception {
        for (String code : batteryAtArrival.keySet()) {
            int battery = batteryAtArrival.get(code);
            for (int minute = 0; minute < minutes; minute++) {
                battery = Math.min(100, battery + chargePerMinute);
                telemetry.publishBattery(code, battery);
            }
            batteryAtArrival.put(code, battery);
        }
    }

    @Then("the e-bike {string} has state {string}")
    public void theEBikeHasState(String code, String state) {
        assertThat(bike(code).getState()).isEqualTo(BikeState.valueOf(state));
    }

    @Then("the e-bike {string} has battery {int}% and state {string}")
    public void theEBikeHasBatteryAndState(String code, int battery, String state) {
        Bike bike = bike(code);
        assertThat(bike.getBatteryLevel()).isEqualTo(battery);
        assertThat(bike.getState()).isEqualTo(BikeState.valueOf(state));
    }

    private Bike bike(String code) {
        return bikeRepository.findByCode(code).orElseThrow();
    }
}
