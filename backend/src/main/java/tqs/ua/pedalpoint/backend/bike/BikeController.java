package tqs.ua.pedalpoint.backend.bike;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "E-bikes", description = "E-bikes available at the hubs")
public class BikeController {

    private final BikeService bikeService;

    public BikeController(BikeService bikeService) {
        this.bikeService = bikeService;
    }

    /** US-04: e-bikes available at a hub, optionally filtered by type (E_BIKE or CARGO_E_BIKE). */
    @GetMapping("/api/hubs/{hubId}/bikes")
    @Operation(summary = "List the e-bikes available at a hub",
            description = "Returns the AVAILABLE e-bikes at the hub with at least the minimum battery to book, "
                    + "highest battery first. Public: no login needed.")
    @ApiResponse(responseCode = "200", description = "E-bikes available at the hub (can be empty)")
    @ApiResponse(responseCode = "400", description = "Unknown e-bike type", content = @Content)
    @ApiResponse(responseCode = "404", description = "Hub does not exist", content = @Content)
    public List<BikeDto> listAvailableBikes(
            @Parameter(description = "Hub id") @PathVariable Long hubId,
            @Parameter(description = "Optional filter: E_BIKE or CARGO_E_BIKE")
            @RequestParam(required = false) BikeType type) {
        return bikeService.findAvailableAtHub(hubId, type);
    }
}
