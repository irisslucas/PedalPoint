package tqs.ua.pedalpoint.backend.bike;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class HubNotFoundException extends RuntimeException {

    public HubNotFoundException(Long hubId) {
        super("Hub " + hubId + " not found");
    }
}
