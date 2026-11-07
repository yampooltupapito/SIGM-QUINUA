package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoUnidadMovil;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoUnidadMovil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnidadMovilResponse {

    private UUID id;
    private String codigoInterno;
    private TipoUnidadMovil tipoUnidad;
    private String tipoUnidadDesc;
    private String placa;
    private String marca;
    private String modelo;
    private Integer anio;
    private String color;
    private String codigoPatrimonial;
    private EstadoUnidadMovil estadoOperativo;
    private String estadoOperativoDesc;
    private Long oficinaId;
    private String oficinaNombre;
    private BigDecimal kilometrajeActual;
    private LocalDate fechaIncorporacion;
    private String observaciones;
    private String responsableActualNombre;
    private UUID asignacionActivaId;
    private Boolean disponibleParaAsignacion;
}
