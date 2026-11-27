package tqs.ua.pedalpoint.simulator;

import java.util.List;

/** The demo fleet: the same e-bikes, hubs and batteries as the backend seed data (migration V5). */
public final class DefaultFleet {

    private DefaultFleet() {
    }

    public static List<SimulatedBike> create() {
        return List.of(
                SimulatedBike.dockedAt("PP-E001", "Residências", 92),
                SimulatedBike.dockedAt("PP-E002", "Residências", 64),
                SimulatedBike.dockedAt("PP-E003", "Residências", 12),
                SimulatedBike.dockedAt("PP-C001", "Residências", 80),
                SimulatedBike.dockedAt("PP-E004", "Biblioteca", 45),
                SimulatedBike.dockedAt("PP-E005", "Biblioteca", 70),
                SimulatedBike.dockedAt("PP-E006", "Departamento de Informática", 88),
                SimulatedBike.dockedAt("PP-C002", "Departamento de Informática", 55));
    }
}
