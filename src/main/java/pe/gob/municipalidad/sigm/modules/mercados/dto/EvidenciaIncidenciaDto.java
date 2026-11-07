package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvidenciaIncidenciaDto {
    private UUID id;
    private String tipoEvidencia;
    private String nombreArchivo;
    private String rutaArchivo;
    private String contentType;
    private Long tamanoBytes;
    private String hashSha256;
    private String descripcion;
    private OffsetDateTime fechaRegistro;
    private Long subidoPorId;
    private String subidoPorNombre;
}
