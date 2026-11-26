package tqs.ua.pedalpoint.backend.bike;

public record BikeDto(Long id, String code, BikeType type, int batteryLevel) {

    static BikeDto from(Bike bike) {
        return new BikeDto(bike.getId(), bike.getCode(), bike.getType(), bike.getBatteryLevel());
    }
}
