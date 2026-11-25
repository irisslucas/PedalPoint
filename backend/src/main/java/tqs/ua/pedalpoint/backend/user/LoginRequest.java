package tqs.ua.pedalpoint.backend.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(example = "ana@ua.pt") @NotBlank String email,
        @Schema(example = "Secret123!") @NotBlank String password) {
}
