package tqs.ua.pedalpoint.backend.cucumber;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import tqs.ua.pedalpoint.backend.user.AppUserRepository;

/** Step definitions for features/register_login.feature (PDP-19). */
public class AuthSteps {

    private static final String DEFAULT_PASSWORD = "Secret123!";

    private final MockMvc mvc;
    private final AppUserRepository userRepository;
    private final JwtDecoder jwtDecoder;

    private MvcResult result;

    public AuthSteps(MockMvc mvc, AppUserRepository userRepository, JwtDecoder jwtDecoder) {
        this.mvc = mvc;
        this.userRepository = userRepository;
        this.jwtDecoder = jwtDecoder;
    }

    @Before
    public void clearAccounts() {
        userRepository.deleteAll();
    }

    // --- Given ---

    @Given("no account exists for {string}")
    public void noAccountExistsFor(String email) {
        userRepository.findByEmail(email).ifPresent(userRepository::delete);
        assertThat(userRepository.existsByEmail(email)).isFalse();
    }

    @Given("an account exists for {string}")
    public void anAccountExistsFor(String email) throws Exception {
        anAccountExistsWithPassword(email, DEFAULT_PASSWORD);
    }

    @Given("an account exists for {string} with password {string}")
    public void anAccountExistsWithPassword(String email, String password) throws Exception {
        assertThat(register(email, password, "STUDENT").getResponse().getStatus()).isEqualTo(201);
    }

    // --- When ---

    @When("I register with email {string}, password {string} and profile {string}")
    public void iRegister(String email, String password, String profile) throws Exception {
        result = register(email, password, profile);
    }

    @When("I log in with {string} and {string}")
    public void iLogIn(String email, String password) throws Exception {
        result = login(email, password);
    }

    // --- Then ---

    @Then("the account is created with role {string} and profile {string}")
    public void theAccountIsCreated(String role, String profile) throws Exception {
        assertThat(status()).isEqualTo(201);
        assertThat(json("$.role")).isEqualTo(role);
        assertThat(json("$.profile")).isEqualTo(profile);
        assertThat(userRepository.existsByEmail(json("$.email"))).isTrue();
    }

    @Then("the registration is rejected with the message {string}")
    public void theRegistrationIsRejected(String message) throws Exception {
        assertThat(status()).isBetween(400, 499);
        assertThat(json("$.detail")).isEqualTo(message);
    }

    @Then("I receive an access token")
    public void iReceiveAnAccessToken() throws Exception {
        assertThat(status()).isEqualTo(200);
        assertThat(json("$.tokenType")).isEqualTo("Bearer");
        Jwt jwt = jwtDecoder.decode(json("$.accessToken"));
        assertThat(jwt.getSubject()).isNotBlank();
    }

    @Then("the login is rejected")
    public void theLoginIsRejected() throws Exception {
        assertThat(status()).isEqualTo(401);
        assertThat(body()).doesNotContain("accessToken");
    }

    // --- helpers ---

    private MvcResult register(String email, String password, String profile) throws Exception {
        String body = """
                {"email": "%s", "password": "%s", "profile": "%s"}""".formatted(email, password, profile);
        return mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andReturn();
    }

    private MvcResult login(String email, String password) throws Exception {
        String body = """
                {"email": "%s", "password": "%s"}""".formatted(email, password);
        return mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andReturn();
    }

    private int status() {
        return result.getResponse().getStatus();
    }

    private String body() throws Exception {
        return result.getResponse().getContentAsString();
    }

    private String json(String path) throws Exception {
        return JsonPath.read(body(), path);
    }
}