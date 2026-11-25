package tqs.ua.pedalpoint.backend.user;

import java.time.Instant;

public record UserDto(Long id, String email, Role role, UserProfile profile, Instant createdAt) {

    static UserDto from(AppUser user) {
        return new UserDto(user.getId(), user.getEmail(), user.getRole(), user.getProfile(), user.getCreatedAt());
    }
}
