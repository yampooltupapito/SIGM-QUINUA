package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectorPatrullajeResponse {
    private UUID id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private String colorHex;
    private String estado;
    private int totalZonas;
    private List<ZonaPatrullajeResponse> zonas;
}
