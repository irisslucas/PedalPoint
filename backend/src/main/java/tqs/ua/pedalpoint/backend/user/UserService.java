package tqs.ua.pedalpoint.backend.user;

import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tqs.ua.pedalpoint.backend.security.JwtTokenService;

@Service
public class UserService {

    /** One non-empty local part, no spaces, and exactly the ua.pt domain (no subdomains). */
    private static final Pattern UA_EMAIL = Pattern.compile("^[^@\\s]+@ua\\.pt$");

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService tokenService;

    public UserService(AppUserRepository userRepository, PasswordEncoder passwordEncoder,
            JwtTokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional
    public UserDto register(RegisterRequest request) {
        String email = normalize(request.email());
        if (!UA_EMAIL.matcher(email).matches()) {
            throw new InvalidEmailDomainException();
        }
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException();
        }
        AppUser user = new AppUser(email, passwordEncoder.encode(request.password()), Role.USER, request.profile());
        return UserDto.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        AppUser user = userRepository.findByEmail(normalize(request.email()))
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(InvalidCredentialsException::new);
        return tokenService.issueToken(user);
    }

    static String normalize(String email) {
        return email == null ? "" : email.strip().toLowerCase(Locale.ROOT);
    }
}
