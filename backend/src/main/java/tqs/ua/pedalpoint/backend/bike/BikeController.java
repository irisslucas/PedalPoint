package tqs.ua.pedalpoint.backend.bike;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BikeController {

    private final BikeService bikeService;

    public BikeController(BikeService bikeService) {
        this.bikeService = bikeService;
    }

    /** US-04: e-bikes available at a hub, optionally filtered by type (E_BIKE or CARGO_E_BIKE). */
    @GetMapping("/api/hubs/{hubId}/bikes")
    public List<BikeDto> listAvailableBikes(@PathVariable Long hubId,
            @RequestParam(required = false) BikeType type) {
        return bikeService.findAvailableAtHub(hubId, type);
    }
}
