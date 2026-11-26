package tqs.ua.pedalpoint.backend.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.jayway.jsonpath.JsonPath;

import tqs.ua.pedalpoint.backend.TestcontainersConfiguration;

/**
 * Integration test: full app + real PostgreSQL (Testcontainers).
 * One test per scenario of src/test/resources/features/register_login.feature (PDP-19).
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class AuthApiIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void noAccounts() {
        userRepository.deleteAll();
    }

    private ResultActions register(String email, String password, String profile) throws Exception {
        String body = """
                {"email": "%s", "password": "%s", "profile": "%s"}""".formatted(email, password, profile);
        return mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private ResultActions login(String email, String password) throws Exception {
        String body = """
                {"email": "%s", "password": "%s"}""".formatted(email, password);
        return mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private void anAccountExists(String email, String password) throws Exception {
        register(email, password, "STUDENT").andExpect(status().isCreated());
    }

    @Test
    void scenario_registerWithUaEmail() throws Exception {
        register("ana@ua.pt", "Secret123!", "STUDENT")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("ana@ua.pt"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.profile").value("STUDENT"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        AppUser stored = userRepository.findByEmail("ana@ua.pt").orElseThrow();
        assertThat(stored.getRole()).isEqualTo(Role.USER);
        assertThat(stored.getProfile()).isEqualTo(UserProfile.STUDENT);
        assertThat(stored.getPasswordHash()).isNotEqualTo("Secret123!");
    }

    @Test
    void scenario_rejectEmailOutsideUaDomain() throws Exception {
        register("ana@gmail.com", "Secret123!", "STUDENT")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Only @ua.pt emails can register"));

        assertThat(userRepository.count()).isZero();
    }

    @Test
    void scenario_rejectDuplicateEmail() throws Exception {
        anAccountExists("ana@ua.pt", "Secret123!");

        register("ana@ua.pt", "Other123!", "STUDENT")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Email already registered"));

        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void scenario_logInWithValidCredentials() throws Exception {
        anAccountExists("ana@ua.pt", "Secret123!");

        String response = login("ana@ua.pt", "Secret123!")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn().getResponse().getContentAsString();

        Jwt jwt = jwtDecoder.decode(JsonPath.read(response, "$.accessToken"));
        assertThat(jwt.getSubject()).isEqualTo("ana@ua.pt");
        assertThat(jwt.getClaimAsStringList("roles")).containsExactly("USER");
    }

    @Test
    void scenario_rejectWrongCredentials() throws Exception {
        anAccountExists("ana@ua.pt", "Secret123!");

        login("ana@ua.pt", "Wrong123!")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.accessToken").doesNotExist());
    }
}
