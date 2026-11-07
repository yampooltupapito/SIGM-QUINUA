package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RutaPatrullajeResponse {
    private UUID id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private UUID sectorId;
    private String sectorNombre;
    private UUID zonaId;
    private String zonaNombre;
    private BigDecimal distanciaEstimadaKm;
    private Integer tiempoEstimadoMin;
    private String estado;
    private Integer versionRuta;
    private int totalPuntos;

    @Builder.Default
    private List<PuntoControlResponse> puntosControl = new ArrayList<>();
}
