package pe.gob.municipalidad.sigm.modules.mesapartes.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.CodigoSeguimiento;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.Expediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoCodigoSeguimiento;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.CodigoSeguimientoRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ExpedienteRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.CodigoSeguimientoService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Optional;

@Slf4j
@Service
public class CodigoSeguimientoServiceImpl implements CodigoSeguimientoService {

    private static final String ALFABETO_SEGURO = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final int LONGITUD_CODIGO = 8;
    private static final int MAX_INTENTOS_DEFAULT = 5;
    private static final int MINUTOS_BLOQUEO = 15;

    private final SecureRandom secureRandom = new SecureRandom();
    private final CodigoSeguimientoRepository codigoSeguimientoRepository;
    private final ExpedienteRepository expedienteRepository;

    public CodigoSeguimientoServiceImpl(CodigoSeguimientoRepository codigoSeguimientoRepository,
                                         ExpedienteRepository expedienteRepository) {
        this.codigoSeguimientoRepository = codigoSeguimientoRepository;
        this.expedienteRepository = expedienteRepository;
    }

    @Override
    @Transactional
    public CodigoSeguimiento generarOObtenerCodigo(Expediente expediente) {
        if (expediente == null) {
            throw new IllegalArgumentException("El expediente no puede ser nulo.");
        }

        Optional<CodigoSeguimiento> existenteOpt = codigoSeguimientoRepository
                .findFirstByExpedienteIdOrderByFechaEmisionDesc(expediente.getId());

        if (existenteOpt.isPresent() && existenteOpt.get().getEstado() == EstadoCodigoSeguimiento.ACTIVO) {
            CodigoSeguimiento existente = existenteOpt.get();
            if (expediente.getCodigoSeguimiento() == null) {
                expediente.setCodigoSeguimiento(existente.getCodigo());
                expedienteRepository.save(expediente);
            }
            return existente;
        }

        String nuevoCodigo = generarCadenaAleatoriaSegura();
        String hash = calcularSha256(nuevoCodigo);

        CodigoSeguimiento nuevo = CodigoSeguimiento.builder()
                .expediente(expediente)
                .codigo(nuevoCodigo)
                .tokenHash(hash)
                .fechaEmision(OffsetDateTime.now())
                .fechaExpiracion(OffsetDateTime.now().plusMonths(12)) // Vigencia de 12 meses
                .intentosFallidos(0)
                .maxIntentos(MAX_INTENTOS_DEFAULT)
                .estado(EstadoCodigoSeguimiento.ACTIVO)
                .build();

        nuevo = codigoSeguimientoRepository.save(nuevo);

        expediente.setCodigoSeguimiento(nuevoCodigo);
        expedienteRepository.save(expediente);

        log.info("Código de seguimiento seguro emitido para expediente {}: {}", expediente.getNumeroExpediente(), nuevoCodigo);
        return nuevo;
    }

    @Override
    @Transactional
    public CodigoSeguimiento validarCodigoSeguimiento(String codigo, String ipOrigen) {
        if (codigo == null || codigo.trim().isBlank()) {
            throw new BusinessException("Debe ingresar un código de seguimiento válido.");
        }

        String codigoNormalizado = codigo.trim().toUpperCase();
        CodigoSeguimiento cs = codigoSeguimientoRepository.findByCodigo(codigoNormalizado)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró ningún trámite con el código proporcionado."));

        // Validar si está revocado
        if (cs.getEstado() == EstadoCodigoSeguimiento.REVOCADO) {
            throw new BusinessException("El código de seguimiento ha sido revocado por razones de seguridad.");
        }

        // Validar expiración
        if (cs.estaExpirado() || cs.getEstado() == EstadoCodigoSeguimiento.EXPIRADO) {
            cs.setEstado(EstadoCodigoSeguimiento.EXPIRADO);
            codigoSeguimientoRepository.save(cs);
            throw new BusinessException("El código de seguimiento ha caducado. Comuníquese con Mesa de Partes.");
        }

        // Validar bloqueo temporal por fuerza bruta
        if (cs.estaBloqueado() || cs.getEstado() == EstadoCodigoSeguimiento.BLOQUEADO) {
            if (cs.getBloqueadoHasta() != null && cs.getBloqueadoHasta().isAfter(OffsetDateTime.now())) {
                throw new BusinessException("El código de seguimiento se encuentra temporalmente bloqueado por múltiples intentos. Reintente después de las "
                        + cs.getBloqueadoHasta().toLocalTime().toString().substring(0, 5));
            } else {
                // Desbloqueo automático tras expirar el tiempo de bloqueo
                cs.setEstado(EstadoCodigoSeguimiento.ACTIVO);
                cs.setIntentosFallidos(0);
                cs.setBloqueadoHasta(null);
            }
        }

        // Registrar acceso
        cs.setUltimoAcceso(OffsetDateTime.now());
        cs.setIpUltimoAcceso(ipOrigen);
        return codigoSeguimientoRepository.save(cs);
    }

    @Override
    @Transactional
    public void revocarCodigo(Long expedienteId, String motivo, String username) {
        CodigoSeguimiento cs = codigoSeguimientoRepository.findFirstByExpedienteIdOrderByFechaEmisionDesc(expedienteId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe código de seguimiento activo para este expediente."));

        cs.setEstado(EstadoCodigoSeguimiento.REVOCADO);
        cs.setMotivoRevocacion(motivo != null ? motivo : "Revocación administrativa por el usuario " + username);
        codigoSeguimientoRepository.save(cs);
        log.info("Código de seguimiento revocado para expediente ID {}: {}", expedienteId, cs.getCodigo());
    }

    @Override
    @Transactional
    public void rotarCodigo(Long expedienteId, String username) {
        Expediente exp = expedienteRepository.findById(expedienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Expediente no encontrado: " + expedienteId));

        // Revocar el actual si existe
        codigoSeguimientoRepository.findFirstByExpedienteIdOrderByFechaEmisionDesc(expedienteId)
                .ifPresent(antiguo -> {
                    antiguo.setEstado(EstadoCodigoSeguimiento.REVOCADO);
                    antiguo.setMotivoRevocacion("Rotación de código solicitada por " + username);
                    codigoSeguimientoRepository.save(antiguo);
                });

        // Generar nuevo
        generarOObtenerCodigo(exp);
    }

    private String generarCadenaAleatoriaSegura() {
        StringBuilder sb = new StringBuilder("SEG-");
        for (int i = 0; i < LONGITUD_CODIGO; i++) {
            int idx = secureRandom.nextInt(ALFABETO_SEGURO.length());
            sb.append(ALFABETO_SEGURO.charAt(idx));
        }
        return sb.toString();
    }

    private String calcularSha256(String texto) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(texto.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error en algoritmo de hash SHA-256", e);
        }
    }
}
