package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.document.repository.DocumentoRepository;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.EvidenciaNotificacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.IntentoNotificacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.NotificacionTributaria;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoEvidenciaNotificacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoEventoNotificacion;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.EvidenciaNotificacionTributariaRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.IntentoNotificacionTributariaRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.NotificacionTributariaRepository;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvidenciaNotificacionService {

    private final EvidenciaNotificacionTributariaRepository evidenciaRepository;
    private final NotificacionTributariaRepository notificacionRepository;
    private final IntentoNotificacionTributariaRepository intentoRepository;
    private final DocumentoRepository documentoRepository;
    private final HistorialNotificacionService historialService;

    private static final long MAX_FILE_SIZE = 15 * 1024 * 1024; // 15 MB

    @Transactional
    public EvidenciaNotificacionTributaria adjuntarEvidencia(UUID notificacionId,
                                                             UUID intentoId,
                                                             TipoEvidenciaNotificacion tipoEvidencia,
                                                             MultipartFile file,
                                                             Long documentoId,
                                                             String observaciones,
                                                             Usuario usuario) {
        NotificacionTributaria notif = notificacionRepository.findById(notificacionId)
                .orElseThrow(() -> new BusinessException("Notificación no encontrada con ID: " + notificacionId));

        IntentoNotificacionTributaria intento = null;
        if (intentoId != null) {
            intento = intentoRepository.findById(intentoId).orElse(null);
        }

        Documento documento = null;
        if (documentoId != null) {
            documento = documentoRepository.findById(documentoId).orElse(null);
        }

        String nombreArchivo;
        long tamanoBytes;
        String mimeType;
        String hashArchivo;

        if (file != null && !file.isEmpty()) {
            if (file.getSize() > MAX_FILE_SIZE) {
                throw new BusinessException("El archivo excede el tamaño máximo permitido de 15 MB.");
            }
            nombreArchivo = file.getOriginalFilename() != null ? file.getOriginalFilename() : "evidencia_" + System.currentTimeMillis();
            tamanoBytes = file.getSize();
            mimeType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
            hashArchivo = calcularHash(file);
        } else if (documento != null) {
            nombreArchivo = documento.getNombreOriginal() != null ? documento.getNombreOriginal() : "documento_" + documento.getId();
            tamanoBytes = documento.getTamanoBytes() != null ? documento.getTamanoBytes() : 0L;
            mimeType = documento.getMimeType() != null ? documento.getMimeType() : "application/pdf";
            hashArchivo = documento.getHashSha256() != null ? documento.getHashSha256() : "HASH-REF-" + documento.getId();
        } else {
            throw new BusinessException("Debe proporcionar un archivo adjunto o vincular un documento existente.");
        }

        EvidenciaNotificacionTributaria evidencia = EvidenciaNotificacionTributaria.builder()
                .notificacion(notif)
                .intento(intento)
                .tipoEvidencia(tipoEvidencia)
                .documento(documento)
                .nombreArchivo(nombreArchivo)
                .tamanoBytes(tamanoBytes)
                .mimeType(mimeType)
                .hashArchivo(hashArchivo)
                .observaciones(observaciones)
                .build();
        evidencia.setCreatedBy(usuario != null ? usuario.getUsername() : "SISTEMA");

        EvidenciaNotificacionTributaria guardada = evidenciaRepository.save(evidencia);

        historialService.registrarEvento(
                notif,
                TipoEventoNotificacion.EVIDENCIA_ADJUNTADA,
                "Evidencia adjuntada: " + tipoEvidencia.getEtiqueta() + " - " + nombreArchivo + " (" + hashArchivo + ")",
                null,
                nombreArchivo,
                usuario,
                null
        );

        return guardada;
    }

    @Transactional(readOnly = true)
    public List<EvidenciaNotificacionTributaria> listarPorNotificacion(UUID notificacionId) {
        return evidenciaRepository.findByNotificacionIdOrderByCreatedAtDesc(notificacionId);
    }

    private String calcularHash(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] hash = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) sb.append('0');
                sb.append(hex);
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("Error al calcular hash de archivo", e);
            return "HASH-CALCULATION-FAILED";
        }
    }
}

