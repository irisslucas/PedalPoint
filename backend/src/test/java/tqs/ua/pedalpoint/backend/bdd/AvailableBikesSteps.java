package tqs.ua.pedalpoint.backend.bdd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.Arrays;
import java.util.List;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import tqs.ua.pedalpoint.backend.bike.Bike;
import tqs.ua.pedalpoint.backend.bike.BikeRepository;
import tqs.ua.pedalpoint.backend.bike.BikeService;
import tqs.ua.pedalpoint.backend.bike.BikeState;
import tqs.ua.pedalpoint.backend.bike.BikeType;
import tqs.ua.pedalpoint.backend.hub.Hub;
import tqs.ua.pedalpoint.backend.hub.HubRepository;

/** Step definitions for features/us04_available_ebikes.feature (US-04). */
public class AvailableBikesSteps {

    private static final long UNKNOWN_HUB_ID = 999_999L;

    private final MockMvc mvc;
    private final BikeRepository bikeRepository;
    private final HubRepository hubRepository;
    private final BikeService bikeService;

    private MvcResult result;

    public AvailableBikesSteps(MockMvc mvc, BikeRepository bikeRepository, HubRepository hubRepository,
            BikeService bikeService) {
        this.mvc = mvc;
        this.bikeRepository = bikeRepository;
        this.hubRepository = hubRepository;
        this.bikeService = bikeService;
    }

    @Before
    public void cleanFleet() {
        // each scenario starts with no e-bikes; hubs from the seed data are reused
        bikeRepository.deleteAll();
    }

    @Given("the minimum battery to book is {int}%")
    public void theMinimumBatteryToBookIs(int minBattery) {
        assertThat(bikeService.getMinBatteryToBook()).isEqualTo(minBattery);
    }

    @Given("the hub {string} exists")
    public void theHubExists(String name) {
        hub(name);
    }

    @Given("the e-bike {string} of type {string} is at hub {string} with state {string} and battery {int}%")
    public void theEBikeIsAtHub(String code, String type, String hubName, String state, int battery) {
        bikeRepository.save(new Bike(code, BikeType.valueOf(type), BikeState.valueOf(state), battery, hub(hubName)));
    }

    @When("a user lists the e-bikes at hub {string}")
    public void aUserListsTheEBikesAtHub(String hubName) throws Exception {
        result = mvc.perform(get("/api/hubs/{id}/bikes", hub(hubName).getId())).andReturn();
    }

    @When("a user lists the e-bikes of type {string} at hub {string}")
    public void aUserListsTheEBikesOfTypeAtHub(String type, String hubName) throws Exception {
        result = mvc.perform(get("/api/hubs/{id}/bikes", hub(hubName).getId()).param("type", type)).andReturn();
    }

    @When("a user lists the e-bikes at a hub that does not exist")
    public void aUserListsTheEBikesAtAnUnknownHub() throws Exception {
        assertThat(hubRepository.existsById(UNKNOWN_HUB_ID)).isFalse();
        result = mvc.perform(get("/api/hubs/{id}/bikes", UNKNOWN_HUB_ID)).andReturn();
    }

    @Then("the e-bike {string} is listed")
    public void theEBikeIsListed(String code) throws Exception {
        assertThat(listedCodes()).contains(code);
    }

    @Then("the e-bike {string} is not listed")
    public void theEBikeIsNotListed(String code) throws Exception {
        assertThat(listedCodes()).doesNotContain(code);
    }

    @Then("only the e-bikes {string} are listed")
    public void onlyTheEBikesAreListed(String codes) throws Exception {
        List<String> expected = Arrays.stream(codes.split(",")).map(String::trim).toList();
        assertThat(listedCodes()).containsExactlyElementsOf(expected);
    }

    @Then("no e-bikes are listed")
    public void noEBikesAreListed() throws Exception {
        assertThat(listedCodes()).isEmpty();
    }

    @Then("the response status is {int}")
    public void theResponseStatusIs(int status) {
        assertThat(result.getResponse().getStatus()).isEqualTo(status);
    }

    private List<String> listedCodes() throws Exception {
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        return JsonPath.read(result.getResponse().getContentAsString(), "$[*].code");
    }

    private Hub hub(String name) {
        return hubRepository.findByName(name).orElseGet(() -> hubRepository.save(new Hub(name, 20)));
    }
}
