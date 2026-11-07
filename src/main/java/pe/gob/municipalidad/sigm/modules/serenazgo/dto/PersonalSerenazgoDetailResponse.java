package pe.gob.municipalidad.sigm.modules.serenazgo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonalSerenazgoDetailResponse {
    private UUID id;
    private String codigoInterno;

    // Datos Persona
    private Long personaId;
    private String nombreCompleto;
    private String nombres;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String tipoDocumento;
    private String numeroDocumentoEnmascarado;
    private String telefono;
    private String correo;
    private String direccion;

    // Datos Usuario
    private Long usuarioId;
    private String username;

    // Datos Laborales y Cargo
    private Long oficinaId;
    private String nombreOficina;
    private UUID cargoOperativoId;
    private String nombreCargoOperativo;
    private String codigoCargoOperativo;
    private Integer nivelJerarquico;
    private String tipoDocumentoLaboral;
    private LocalDate fechaIngreso;
    private LocalDate fechaSalida;
    private String estado;
    private String observaciones;
    private Long version;
    private boolean disponible;
    private String motivoNoDisponible;

    // Historiales y Documentos
    private List<HistorialCargoResponse> historialCargos;
    private List<HistorialEstadoResponse> historialEstados;
    private List<DocumentoPersonalResponse> documentos;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
