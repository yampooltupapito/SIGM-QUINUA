package pe.gob.municipalidad.sigm.modules.mercados.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.administracion.service.AuditoriaAdministrativaService;
import pe.gob.municipalidad.sigm.modules.mercados.dto.AperturaCajaRequest;
import pe.gob.municipalidad.sigm.modules.mercados.dto.CierreCajaRequest;
import pe.gob.municipalidad.sigm.modules.mercados.dto.SesionCajaResponse;
import pe.gob.municipalidad.sigm.modules.mercados.entity.CajaMercado;
import pe.gob.municipalidad.sigm.modules.mercados.entity.SesionCajaMercado;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoSesionCaja;
import pe.gob.municipalidad.sigm.modules.mercados.repository.CajaMercadoRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.PagoMercadoRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.SesionCajaMercadoRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CajaMercadoService {

    private final CajaMercadoRepository cajaRepository;
    private final SesionCajaMercadoRepository sesionRepository;
    private final PagoMercadoRepository pagoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaAdministrativaService auditoriaService;

    public CajaMercadoService(CajaMercadoRepository cajaRepository,
                              SesionCajaMercadoRepository sesionRepository,
                              PagoMercadoRepository pagoRepository,
                              UsuarioRepository usuarioRepository,
                              AuditoriaAdministrativaService auditoriaService) {
        this.cajaRepository = cajaRepository;
        this.sesionRepository = sesionRepository;
        this.pagoRepository = pagoRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public SesionCajaResponse abrirSesion(AperturaCajaRequest request, String username, Long usuarioId) {
        CajaMercado caja = cajaRepository.findById(request.getCajaId())
                .orElseThrow(() -> new ResourceNotFoundException("Caja recaudadora no encontrada: " + request.getCajaId()));

        if (!"ACTIVA".equalsIgnoreCase(caja.getEstado())) {
            throw new BusinessException("La caja seleccionada no está en estado ACTIVA.");
        }

        // Validar que la caja no tenga una sesión abierta
        Optional<SesionCajaMercado> abiertaCaja = sesionRepository.findByCajaIdAndEstado(caja.getId(), EstadoSesionCaja.ABIERTA);
        if (abiertaCaja.isPresent()) {
            throw new BusinessException("La caja " + caja.getCodigo() + " ya tiene una sesión abierta por el cajero: " + abiertaCaja.get().getCajeroUsername());
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + usuarioId));

        // Validar que el cajero no tenga otra sesión abierta
        Optional<SesionCajaMercado> abiertaUsuario = sesionRepository.findByUsuarioCajeroIdAndEstado(usuarioId, EstadoSesionCaja.ABIERTA);
        if (abiertaUsuario.isPresent()) {
            throw new BusinessException("El usuario cajero ya tiene una sesión activa abierta en la caja: " + abiertaUsuario.get().getCaja().getCodigo());
        }

        if (request.getMontoInicial() == null || request.getMontoInicial().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("El monto inicial de apertura debe ser mayor o igual a 0.00.");
        }

        SesionCajaMercado sesion = SesionCajaMercado.builder()
                .caja(caja)
                .usuarioCajero(usuario)
                .cajeroUsername(username)
                .fechaApertura(OffsetDateTime.now())
                .montoInicial(request.getMontoInicial())
                .totalIngresos(BigDecimal.ZERO)
                .totalReversos(BigDecimal.ZERO)
                .montoFinalEsperado(request.getMontoInicial())
                .estado(EstadoSesionCaja.ABIERTA)
                .observaciones(request.getObservaciones())
                .build();

        SesionCajaMercado guardada = sesionRepository.save(sesion);

        auditoriaService.registrar("APERTURA_CAJA_MERCADO", "mercados_cajas_sesiones", guardada.getId(),
                "EXITO", "Caja " + caja.getCodigo() + " abierta por cajero " + username + " con saldo inicial S/ " + guardada.getMontoInicial());

        return toResponse(guardada);
    }

    @Transactional
    public SesionCajaResponse cerrarSesion(UUID sesionId, CierreCajaRequest request, String username) {
        SesionCajaMercado sesion = sesionRepository.findById(sesionId)
                .orElseThrow(() -> new ResourceNotFoundException("Sesión de caja no encontrada: " + sesionId));

        if (sesion.getEstado() != EstadoSesionCaja.ABIERTA) {
            throw new BusinessException("Solo se pueden cerrar sesiones que se encuentren en estado ABIERTA.");
        }

        if (request.getMontoFinalDeclarado() == null || request.getMontoFinalDeclarado().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("El monto final declarado debe ser mayor o igual a 0.00.");
        }

        // Calcular recaudación real de la sesión
        BigDecimal totalIngresos = pagoRepository.sumarTotalRecaudadoSesion(sesion.getId());
        if (totalIngresos == null) totalIngresos = BigDecimal.ZERO;
        sesion.setTotalIngresos(totalIngresos);

        BigDecimal montoEsperado = sesion.getMontoInicial().add(totalIngresos).subtract(sesion.getTotalReversos());
        sesion.setMontoFinalEsperado(montoEsperado);
        sesion.setMontoFinalDeclarado(request.getMontoFinalDeclarado());
        sesion.setDiferencia(request.getMontoFinalDeclarado().subtract(montoEsperado));
        sesion.setFechaCierre(OffsetDateTime.now());
        sesion.setEstado(EstadoSesionCaja.CERRADA);
        if (request.getObservaciones() != null && !request.getObservaciones().isBlank()) {
            sesion.setObservaciones((sesion.getObservaciones() != null ? sesion.getObservaciones() + " | " : "") + request.getObservaciones());
        }

        SesionCajaMercado guardada = sesionRepository.save(sesion);

        auditoriaService.registrar("CIERRE_CAJA_MERCADO", "mercados_cajas_sesiones", guardada.getId(),
                "EXITO", "Caja cerrada por " + username + ". Declarado: S/ " + guardada.getMontoFinalDeclarado() +
                        ", Esperado: S/ " + guardada.getMontoFinalEsperado() + ", Dif: S/ " + guardada.getDiferencia());

        return toResponse(guardada);
    }

    @Transactional(readOnly = true)
    public Optional<SesionCajaResponse> obtenerSesionActivaCaja(UUID cajaId) {
        return sesionRepository.findByCajaIdAndEstado(cajaId, EstadoSesionCaja.ABIERTA).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Optional<SesionCajaResponse> obtenerSesionActivaUsuario(Long usuarioId) {
        return sesionRepository.findByUsuarioCajeroIdAndEstado(usuarioId, EstadoSesionCaja.ABIERTA).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public SesionCajaResponse obtenerPorId(UUID sesionId) {
        return sesionRepository.findById(sesionId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Sesión de caja no encontrada: " + sesionId));
    }

    @Transactional(readOnly = true)
    public Page<SesionCajaResponse> buscarSesionesPaginado(UUID cajaId, String estado, Pageable pageable) {
        return sesionRepository.buscarPaginado(cajaId, estado, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<CajaMercado> listarCajasActivas() {
        return cajaRepository.findByEstado("ACTIVA");
    }

    public SesionCajaResponse toResponse(SesionCajaMercado s) {
        return SesionCajaResponse.builder()
                .id(s.getId())
                .cajaId(s.getCaja().getId())
                .cajaCodigo(s.getCaja().getCodigo())
                .cajaNombre(s.getCaja().getNombre())
                .usuarioCajeroId(s.getUsuarioCajero().getId())
                .cajeroUsername(s.getCajeroUsername())
                .fechaApertura(s.getFechaApertura())
                .fechaCierre(s.getFechaCierre())
                .montoInicial(s.getMontoInicial())
                .totalIngresos(s.getTotalIngresos())
                .totalReversos(s.getTotalReversos())
                .montoFinalEsperado(s.getMontoFinalEsperado())
                .montoFinalDeclarado(s.getMontoFinalDeclarado())
                .diferencia(s.getDiferencia())
                .estado(s.getEstado())
                .estadoDescripcion(s.getEstado() != null ? s.getEstado().getDescripcion() : null)
                .observaciones(s.getObservaciones())
                .build();
    }
}
