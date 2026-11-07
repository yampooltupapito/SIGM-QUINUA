package pe.gob.municipalidad.sigm.modules.serenazgo.dto.mapa;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ElementoGeograficoDetalleResponse {

    private String tipo; // INCIDENCIA, ALERTA, INTERVENCION, PATRULLAJE, SECTOR, ZONA, RUTA, PUNTO_CONTROL
    private String id;
    private String codigo;
    private String titulo;
    private String descripcion;
    private String fechaHora;
    private String estado;
    private String estadoDescripcion;
    private String prioridad;
    private String prioridadDescripcion;
    private String sectorNombre;
    private String zonaNombre;
    private String ubicacionReferencia;
    private Double latitud;
    private Double longitud;
    private boolean tieneCoordenadas;
    private String urlDetalle;

    @Builder.Default
    private Map<String, Object> atributos = new LinkedHashMap<>();
}
