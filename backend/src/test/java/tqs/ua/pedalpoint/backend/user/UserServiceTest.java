package tqs.ua.pedalpoint.backend.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import tqs.ua.pedalpoint.backend.security.JwtTokenService;

/** Unit test: no Spring context, no database. Uses a real (low-cost) BCrypt encoder. */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String PASSWORD = "Secret123!";

    @Mock
    private AppUserRepository userRepository;

    @Mock
    private JwtTokenService tokenService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder, tokenService);
    }

    private AppUser savedUser() {
        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository).save(captor.capture());
        return captor.getValue();
    }

    private void saveReturnsArgument() {
        when(userRepository.save(any(AppUser.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    // --- register ---

    @Test
    void register_uaEmail_createsUserWithRoleUserAndProfile() {
        saveReturnsArgument();

        UserDto result = userService.register(new RegisterRequest("ana@ua.pt", PASSWORD, UserProfile.STAFF));

        assertThat(result.email()).isEqualTo("ana@ua.pt");
        assertThat(result.role()).isEqualTo(Role.USER);
        assertThat(result.profile()).isEqualTo(UserProfile.STAFF);
        assertThat(savedUser().getCreatedAt()).isNotNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"ana@gmail.com", "ana@ua.pt.com", "ana@fakeua.pt", "ana@uapt", "@ua.pt", "ana ua.pt", "ana smith@ua.pt", ""})
    void register_emailOutsideUaDomain_isRejected(String email) {
        RegisterRequest request = new RegisterRequest(email, PASSWORD, UserProfile.STUDENT);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(InvalidEmailDomainException.class)
                .hasMessage("Only @ua.pt emails can register");
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_uppercaseAndWhitespace_areNormalized() {
        saveReturnsArgument();

        UserDto result = userService.register(new RegisterRequest("  Ana@UA.PT \t", PASSWORD, UserProfile.STUDENT));

        assertThat(result.email()).isEqualTo("ana@ua.pt");
        verify(userRepository).existsByEmail("ana@ua.pt");
        assertThat(savedUser().getEmail()).isEqualTo("ana@ua.pt");
    }

    @Test
    void register_duplicateEmail_isRejected() {
        when(userRepository.existsByEmail("ana@ua.pt")).thenReturn(true);
        RegisterRequest request = new RegisterRequest("ANA@ua.pt", PASSWORD, UserProfile.STUDENT);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(EmailAlreadyRegisteredException.class)
                .hasMessage("Email already registered");
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_storesBcryptHashNotThePlainPassword() {
        saveReturnsArgument();

        userService.register(new RegisterRequest("ana@ua.pt", PASSWORD, UserProfile.STUDENT));

        String hash = savedUser().getPasswordHash();
        assertThat(hash).isNotEqualTo(PASSWORD).startsWith("$2");
        assertThat(passwordEncoder.matches(PASSWORD, hash)).isTrue();
    }

    // --- login ---

    private AppUser existingUser() {
        return new AppUser("ana@ua.pt", passwordEncoder.encode(PASSWORD), Role.USER, UserProfile.STUDENT);
    }

    @Test
    void login_correctCredentials_returnsToken() {
        AppUser user = existingUser();
        TokenResponse token = TokenResponse.bearer("jwt", 3600);
        when(userRepository.findByEmail("ana@ua.pt")).thenReturn(Optional.of(user));
        when(tokenService.issueToken(user)).thenReturn(token);

        assertThat(userService.login(new LoginRequest(" Ana@UA.pt ", PASSWORD))).isEqualTo(token);
    }

    @Test
    void login_wrongPassword_isRejected() {
        when(userRepository.findByEmail("ana@ua.pt")).thenReturn(Optional.of(existingUser()));
        LoginRequest request = new LoginRequest("ana@ua.pt", "Wrong123!");

        assertThatThrownBy(() -> userService.login(request)).isInstanceOf(InvalidCredentialsException.class);
        verifyNoInteractions(tokenService);
    }

    @Test
    void login_unknownEmail_isRejectedWithSameError() {
        when(userRepository.findByEmail("bruno@ua.pt")).thenReturn(Optional.empty());
        LoginRequest request = new LoginRequest("bruno@ua.pt", PASSWORD);

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");
        verifyNoInteractions(tokenService);
    }
}
