package pe.gob.municipalidad.sigm.common.document.validation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Validador de seguridad para carga de archivos documentales.
 * Verifica tamaño, nombre sanitizado, extensiones permitidas, ejecutables y firmas mágicas binarias.
 */
@Slf4j
@Component
public class DocumentFileValidator {

    private final long maxFileSize;
    private final Set<String> allowedExtensions;

    private static final Set<String> DANGEROUS_EXTENSIONS = Set.of(
            "exe", "bat", "cmd", "sh", "vbs", "js", "jsp", "jar", "zip", "rar",
            "tar", "gz", "7z", "msi", "dll", "com", "scr", "pif", "hta", "cpl"
    );

    public DocumentFileValidator(
            @Value("${sigm.document.max-file-size:26214400}") long maxFileSize,
            @Value("${sigm.document.allowed-extensions:pdf,jpeg,jpg,png,webp,docx,xlsx}") String allowedExtensionsStr
    ) {
        this.maxFileSize = maxFileSize;
        this.allowedExtensions = new HashSet<>();
        if (allowedExtensionsStr != null) {
            Arrays.stream(allowedExtensionsStr.split(","))
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .forEach(this.allowedExtensions::add);
        }
    }

    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("El archivo documental está vacío o no fue proporcionado.");
        }

        if (file.getSize() > maxFileSize) {
            throw new BusinessException("El archivo excede el límite máximo permitido de " + (maxFileSize / (1024 * 1024)) + " MB.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new BusinessException("El nombre del archivo no puede ser nulo o vacío.");
        }

        // 1. Detección de Path Traversal y caracteres peligrosos
        if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\") || originalFilename.contains("\0")) {
            log.warn("Intento de nombre de archivo malicioso: {}", originalFilename);
            throw new BusinessException("El nombre del archivo contiene caracteres o secuencias no permitidas.");
        }

        // 2. Extracción de extensión
        String extension = extractExtension(originalFilename);
        if (extension.isEmpty()) {
            throw new BusinessException("El archivo debe contar con una extensión válida.");
        }

        // 3. Bloqueo de ejecutables y extensiones de alto riesgo
        if (DANGEROUS_EXTENSIONS.contains(extension)) {
            log.warn("Intento de carga de archivo peligroso: extension={}", extension);
            throw new BusinessException("La extensión '." + extension + "' está estrictamente bloqueada por políticas de seguridad institucional.");
        }

        // 4. Verificación contra lista blanca permitida
        if (!allowedExtensions.contains(extension)) {
            throw new BusinessException("Formato no admitido. Extensiones autorizadas: " + String.join(", ", allowedExtensions));
        }

        // 5. Verificación de Magic Bytes (firma binaria real)
        validateMagicBytes(file, extension);

        // 6. Registro de política antivirus (documentando cuarentena si no hay escáner activo)
        log.debug("Archivo {} superó validación de firmas y política de contención.", originalFilename);
    }

    public String extractExtension(String filename) {
        if (filename == null) return "";
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1 || lastDot == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDot + 1).toLowerCase().trim();
    }

    private void validateMagicBytes(MultipartFile file, String extension) {
        byte[] header = new byte[8];
        try (InputStream is = file.getInputStream()) {
            int read = is.read(header);
            if (read < 4) {
                throw new BusinessException("El archivo está corrupto o es de longitud insuficiente.");
            }
        } catch (IOException e) {
            throw new BusinessException("Error al inspeccionar la cabecera del archivo.");
        }

        switch (extension) {
            case "pdf":
                // %PDF (0x25, 0x50, 0x44, 0x46)
                if (header[0] != 0x25 || header[1] != 0x50 || header[2] != 0x44 || header[3] != 0x46) {
                    throw new BusinessException("El archivo no corresponde a un documento PDF legítimo.");
                }
                break;
            case "png":
                // 0x89 0x50 0x4E 0x47
                if ((header[0] & 0xFF) != 0x89 || header[1] != 0x50 || header[2] != 0x4E || header[3] != 0x47) {
                    throw new BusinessException("El archivo no corresponde a una imagen PNG válida.");
                }
                break;
            case "jpg":
            case "jpeg":
                // 0xFF 0xD8 0xFF
                if ((header[0] & 0xFF) != 0xFF || (header[1] & 0xFF) != 0xD8 || (header[2] & 0xFF) != 0xFF) {
                    throw new BusinessException("El archivo no corresponde a una imagen JPEG válida.");
                }
                break;
            case "webp":
                // 'R','I','F','F'
                if (header[0] != 0x52 || header[1] != 0x49 || header[2] != 0x46 || header[3] != 0x46) {
                    throw new BusinessException("El archivo no corresponde a una imagen WebP válida.");
                }
                break;
            case "docx":
            case "xlsx":
                // ZIP signature PK.. (0x50, 0x4B)
                if (header[0] != 0x50 || header[1] != 0x4B) {
                    throw new BusinessException("El archivo ofimático no presenta una estructura de contenedor válida.");
                }
                break;
            default:
                break;
        }
    }
}
