package tqs.ua.pedalpoint.backend.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Schema(example = "ana@ua.pt", description = "Must be an @ua.pt address; case and surrounding spaces are ignored")
        @NotBlank String email,

        @Schema(example = "Secret123!", minLength = 8, maxLength = 72)
        @NotBlank @Size(min = 8, max = 72) String password,

        @Schema(example = "STUDENT")
        @NotNull UserProfile profile) {
}
