package tqs.ua.pedalpoint.backend.hub;

public record HubDto(Long id, String name, int capacity) {

    static HubDto from(Hub hub) {
        return new HubDto(hub.getId(), hub.getName(), hub.getCapacity());
    }
}
