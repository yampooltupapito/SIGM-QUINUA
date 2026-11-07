package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InspeccionDetailResponse {

    private InspeccionResponse inspeccion;
    private List<ParticipanteDto> participantes;
    private List<RespuestaVerificacionDto> verificaciones;
    private List<HallazgoResponse> hallazgos;
    private List<EvidenciaDto> evidencias;
    private List<MedidaCorrectivaResponse> medidasCorrectivas;
    private ActaInspeccionResponse acta;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipanteDto {
        private Long usuarioId;
        private String usuarioNombre;
        private String usuarioUsername;
        private String funcion;
        private String estado;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EvidenciaDto {
        private String id;
        private String hallazgoId;
        private String tipoEvidencia;
        private String nombreArchivo;
        private String contentType;
        private Long tamanioBytes;
        private String rutaAlmacenamiento;
        private String hashSha256;
        private String descripcion;
        private String fechaSubida;
        private String usuarioSubidaNombre;
    }
}
