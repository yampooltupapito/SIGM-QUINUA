package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.portal.PredioCiudadanoDto;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ContribuyenteTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.PredioTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.RelacionContribuyentePredio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoRelacionContribuyentePredio;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.RelacionContribuyentePredioRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio de consulta de predios autorizados para el ciudadano autenticado.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultaPrediosCiudadanoService {

    private final PortalTributarioContextService contextService;
    private final RelacionContribuyentePredioRepository relacionPredioRepository;

    @Transactional(readOnly = true)
    public List<PredioCiudadanoDto> listarMisPredios() {
        Optional<ContribuyenteTributario> contribOpt = contextService.obtenerContribuyenteActivo();
        if (contribOpt.isEmpty()) {
            return Collections.emptyList();
        }

        List<RelacionContribuyentePredio> relaciones = relacionPredioRepository
                .findByContribuyenteId(contribOpt.get().getId());

        return relaciones.stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PredioCiudadanoDto obtenerDetallePredio(UUID predioId) {
        // Valida estrictamente que el predio pertenezca al contribuyente activo
        RelacionContribuyentePredio relacion = contextService.validarAccesoPredio(predioId);
        return convertirADto(relacion);
    }

    private PredioCiudadanoDto convertirADto(RelacionContribuyentePredio r) {
        PredioTributario p = r.getPredio();
        return PredioCiudadanoDto.builder()
                .id(p.getId())
                .codigoPredio(p.getCodigoPredio())
                .codigoCatastral(p.getCodigoCatastral())
                .direccionCompleta(p.getDireccionCompleta())
                .referencia(p.getReferenciaUbicacion())
                .tipoPredio(p.getTipoPredio() != null ? p.getTipoPredio().name() : "URBANO")
                .estadoPredio(p.getEstado() != null ? p.getEstado().name() : "ACTIVO")
                .tipoRelacion(r.getTipoRelacion() != null ? r.getTipoRelacion().name() : "PROPIETARIO")
                .porcentajeParticipacion(r.getPorcentajeParticipacion())
                .esPrincipal(r.getEsPrincipal())
                .areaTerreno(p.getAreaTerreno())
                .areaConstruidaTotal(p.getAreaConstruida())
                .autoavaluoTotal(p.getValorAutovaluoReferencial())
                .fechaRegistro(p.getFechaRegistro())
                .build();
    }
}

