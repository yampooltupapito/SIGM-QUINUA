package pe.gob.municipalidad.sigm.modules.mercados.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoComerciante;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoComerciante;
import pe.gob.municipalidad.sigm.modules.mercados.enums.TipoVinculoComerciante;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortalComerciantePerfilResponse {
    private UUID comercianteId;
    private String codigoComerciante;
    private String nombreCompletoORazonSocial;
    private TipoComerciante tipoComerciante;
    private String documentoEnmascarado;
    private String telefono;
    private String correoElectronico;
    private String direccion;
    private EstadoComerciante estadoRegistro;
    private LocalDate fechaRegistro;
    private TipoVinculoComerciante tipoVinculo;
    private String facultadesRepresentacion;
    private List<PortalComercianteContextoResponse> otrosContextosDisponibles;
}
