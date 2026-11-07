package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoVersionPlano;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VersionPlanoResponse {
    private UUID id;
    private UUID planoId;
    private Integer numeroVersion;
    private EstadoVersionPlano estado;
    private String estadoLabel;
    private String descripcionCambios;
    private UUID usuarioCreadorId;
    private String usuarioCreadorNombre;
    private UUID usuarioPublicadorId;
    private String usuarioPublicadorNombre;
    private OffsetDateTime fechaCreacion;
    private OffsetDateTime fechaPublicacion;
    private String hashContenido;
    private Integer totalElementos;
    private Integer totalPuestos;
    private String imagenReferenciaUrl;
    private BigDecimal imagenEscala;
    private BigDecimal imagenPosX;
    private BigDecimal imagenPosY;
    private BigDecimal imagenOpacidad;
    private Boolean imagenVisible;
    private Boolean imagenBloqueada;
    private Long version;
}
