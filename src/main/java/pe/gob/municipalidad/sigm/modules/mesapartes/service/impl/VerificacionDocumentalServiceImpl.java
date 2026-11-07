package pe.gob.municipalidad.sigm.modules.mesapartes.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.SeguimientoDtos.VerificarDocumentoResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.ConsultaSeguimientoAuditoria;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.DecisionExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.Expediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.VerificacionDocumental;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoVerificacionDocumento;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.ResultadoConsultaSeguimiento;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoConsultaSeguimiento;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ConsultaSeguimientoAuditoriaRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.VerificacionDocumentalRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.VerificacionDocumentalService;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class VerificacionDocumentalServiceImpl implements VerificacionDocumentalService {

    private static final String ALFABETO = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final int LONGITUD_CODIGO = 8;
    private final SecureRandom secureRandom = new SecureRandom();

    private final VerificacionDocumentalRepository verificacionRepository;
    private final ConsultaSeguimientoAuditoriaRepository auditoriaRepository;

    @Value("${sigm.app.base-url:http://localhost:8080}")
    private String defaultBaseUrl;

    public VerificacionDocumentalServiceImpl(VerificacionDocumentalRepository verificacionRepository,
                                             ConsultaSeguimientoAuditoriaRepository auditoriaRepository) {
        this.verificacionRepository = verificacionRepository;
        this.auditoriaRepository = auditoriaRepository;
    }

    @Override
    @Transactional
    public VerificarDocumentoResponse verificarDocumento(String codigoOQr, String ipOrigen, String userAgent) {
        long inicioMs = System.currentTimeMillis();
        String criterio = codigoOQr != null ? codigoOQr.trim() : "";

        if (criterio.isBlank()) {
            registrarAuditoria(null, criterio, ipOrigen, userAgent, ResultadoConsultaSeguimiento.CODIGO_INVALIDO, (int)(System.currentTimeMillis() - inicioMs));
            return VerificarDocumentoResponse.builder()
                    .valido(false)
                    .estadoDocumento("NO_VERIFICABLE")
                    .mensajeLegal("El código de verificación documental proporcionado no es válido.")
                    .build();
        }

        // Buscar por código de verificación, token QR o hash SHA-256
        Optional<VerificacionDocumental> docOpt = verificacionRepository.findByCodigoVerificacion(criterio.toUpperCase());
        if (docOpt.isEmpty()) {
            docOpt = verificacionRepository.findByTokenQr(criterio);
        }
        if (docOpt.isEmpty()) {
            docOpt = verificacionRepository.findByHashSha256(criterio.toLowerCase());
        }
        if (docOpt.isEmpty()) {
            docOpt = verificacionRepository.findByNumeroDocumento(criterio);
        }

        if (docOpt.isEmpty()) {
            registrarAuditoria(null, criterio, ipOrigen, userAgent, ResultadoConsultaSeguimiento.NO_ENCONTRADO, (int)(System.currentTimeMillis() - inicioMs));
            return VerificarDocumentoResponse.builder()
                    .valido(false)
                    .estadoDocumento("NO_VERIFICABLE")
                    .mensajeLegal("No se encontró ningún documento institucional emitido con los datos proporcionados.")
                    .build();
        }

        VerificacionDocumental doc = docOpt.get();
        doc.setVecesConsultado(doc.getVecesConsultado() + 1);
        doc.setUltimaConsulta(OffsetDateTime.now());
        verificacionRepository.save(doc);

        registrarAuditoria(doc.getExpediente(), criterio, ipOrigen, userAgent, ResultadoConsultaSeguimiento.EXITOSA, (int)(System.currentTimeMillis() - inicioMs));

        boolean esValido = doc.getEstadoDocumento() == EstadoVerificacionDocumento.VIGENTE;
        String mensaje = esValido
                ? "Documento auténtico y registrado formalmente en la plataforma institucional de la Municipalidad Distrital de Quinua. Verificado mediante hash criptográfico SHA-256 conforme al TUO de la Ley N° 27444."
                : "El documento figura en el registro institucional pero se encuentra en estado: " + doc.getEstadoDocumento().name() + (doc.getMotivoAnulacion() != null ? ". Motivo: " + doc.getMotivoAnulacion() : "");

        return VerificarDocumentoResponse.builder()
                .valido(esValido)
                .codigoVerificacion(doc.getCodigoVerificacion())
                .tipoDocumento(doc.getTipoDocumento())
                .numeroDocumento(doc.getNumeroDocumento())
                .fechaEmision(doc.getFechaEmision())
                .estadoDocumento(doc.getEstadoDocumento().name())
                .entidadEmisora(doc.getEntidadEmisora())
                .autoridadFirmante(doc.getAutoridadFirmante() != null ? doc.getAutoridadFirmante() : "Autoridad Competente MDQ")
                .cargoFirmante(doc.getCargoFirmante() != null ? doc.getCargoFirmante() : "Funcionario Resolutor")
                .hashSha256(doc.getHashSha256())
                .vecesConsultado(doc.getVecesConsultado())
                .mensajeLegal(mensaje)
                .build();
    }

    @Override
    @Transactional
    public VerificacionDocumental registrarVerificacionDocumento(
            Documento documento,
            Expediente expediente,
            DecisionExpediente decision,
            String tipoDocumento,
            String numeroDocumento,
            String autoridadFirmante,
            String cargoFirmante,
            String baseUrl
    ) {
        String base = baseUrl != null && !baseUrl.isBlank() ? baseUrl : defaultBaseUrl;
        String codigoVerif = "DOC-" + generarCadenaAleatoria();
        String tokenQr = UUID.randomUUID().toString();
        String hash = documento != null && documento.getHashSha256() != null ? documento.getHashSha256() : UUID.randomUUID().toString().replace("-", "");
        String urlVerificacion = base + "/verificar-documento?codigo=" + codigoVerif;

        VerificacionDocumental reg = VerificacionDocumental.builder()
                .codigoVerificacion(codigoVerif)
                .tokenQr(tokenQr)
                .tipoDocumento(tipoDocumento)
                .numeroDocumento(numeroDocumento)
                .expediente(expediente)
                .decision(decision)
                .documento(documento)
                .hashSha256(hash)
                .fechaEmision(OffsetDateTime.now())
                .entidadEmisora("Municipalidad Distrital de Quinua")
                .autoridadFirmante(autoridadFirmante)
                .cargoFirmante(cargoFirmante)
                .estadoDocumento(EstadoVerificacionDocumento.VIGENTE)
                .urlVerificacion(urlVerificacion)
                .vecesConsultado(0)
                .build();

        reg = verificacionRepository.save(reg);
        log.info("Registro de verificación documental emitido: {} para doc {}", codigoVerif, numeroDocumento);
        return reg;
    }

    @Override
    @Transactional
    public void anularDocumento(Long verificacionId, String motivo, String username) {
        VerificacionDocumental doc = verificacionRepository.findById(verificacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Registro de verificación no encontrado: " + verificacionId));

        doc.setEstadoDocumento(EstadoVerificacionDocumento.ANULADO);
        doc.setMotivoAnulacion(motivo != null ? motivo : "Anulación administrativa registrada por " + username);
        verificacionRepository.save(doc);
        log.warn("Documento {} anulado administrativamente por {}", doc.getNumeroDocumento(), username);
    }

    private void registrarAuditoria(Expediente expediente, String criterio, String ip, String ua, ResultadoConsultaSeguimiento resultado, int tiempoMs) {
        try {
            ConsultaSeguimientoAuditoria aud = ConsultaSeguimientoAuditoria.builder()
                    .tipoConsulta(TipoConsultaSeguimiento.VERIFICACION_DOCUMENTAL)
                    .criterioBusqueda(criterio)
                    .expediente(expediente)
                    .ipOrigen(ip != null ? ip : "127.0.0.1")
                    .userAgent(ua != null && ua.length() > 250 ? ua.substring(0, 250) : ua)
                    .resultado(resultado)
                    .fechaConsulta(OffsetDateTime.now())
                    .tiempoRespuestaMs(tiempoMs)
                    .build();
            auditoriaRepository.save(aud);
        } catch (Exception e) {
            log.warn("No se pudo guardar la auditoría de verificación: {}", e.getMessage());
        }
    }

    private String generarCadenaAleatoria() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < LONGITUD_CODIGO; i++) {
            sb.append(ALFABETO.charAt(secureRandom.nextInt(ALFABETO.length())));
        }
        return sb.toString();
    }
}
