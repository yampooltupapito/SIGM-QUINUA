package pe.gob.municipalidad.sigm.modules.serenazgo.dto.mapa;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemBusquedaMapaResponse {

    private String tipo; // INCIDENCIA, ALERTA, INTERVENCION, PATRULLAJE, SECTOR, ZONA, RUTA, PUNTO_CONTROL
    private String id;
    private String codigo;
    private String titulo;
    private String subtitulo;
    private Double latitud;
    private Double longitud;
    private boolean tieneCoordenadas;
    private String urlDetalle;
    private String referencia;
}
