package pe.gob.municipalidad.sigm.common.document.storage;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.UUID;

/**
 * Implementación local en sistema de archivos privado con protección contra Path Traversal.
 */
@Slf4j
@Service
public class FileSystemDocumentStorageService implements DocumentStorageService {

    private final Path rootLocation;

    public FileSystemDocumentStorageService(@Value("${sigm.document.storage.path:./storage/documentos}") String storagePath) {
        this.rootLocation = Paths.get(storagePath).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            if (!Files.exists(rootLocation)) {
                Files.createDirectories(rootLocation);
                log.info("Directorio de almacenamiento documental creado en: {}", rootLocation);
            }
        } catch (IOException e) {
            log.error("No se pudo inicializar el directorio de almacenamiento documental: {}", rootLocation, e);
            throw new BusinessException("Error al inicializar el repositorio de almacenamiento documental.");
        }
    }

    @Override
    public String store(InputStream inputStream, String extension) {
        String cleanExt = extension != null ? extension.replaceAll("[^a-zA-Z0-9]", "").toLowerCase() : "bin";
        String internalFilename = UUID.randomUUID().toString() + "." + cleanExt;
        Path destination = resolveSafePath(internalFilename);

        try {
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
            log.debug("Archivo almacenado exitosamente en: {}", destination);
            return internalFilename;
        } catch (IOException e) {
            log.error("Error al persistir archivo en {}", destination, e);
            throw new BusinessException("Error de entrada/salida al guardar el archivo documental.");
        }
    }

    @Override
    public InputStream read(String nombreInterno) {
        Path path = resolveSafePath(nombreInterno);
        if (!Files.exists(path) || !Files.isReadable(path)) {
            log.warn("Archivo documental no encontrado o inaccesible: {}", path);
            throw new BusinessException("El archivo físico solicitado no se encuentra disponible.");
        }
        try {
            return Files.newInputStream(path);
        } catch (IOException e) {
            log.error("Error al abrir flujo de lectura para: {}", path, e);
            throw new BusinessException("Error al acceder al archivo documental.");
        }
    }

    @Override
    public boolean delete(String nombreInterno) {
        Path path = resolveSafePath(nombreInterno);
        try {
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("No se pudo eliminar el archivo físico: {}", path, e);
            return false;
        }
    }

    @Override
    public boolean exists(String nombreInterno) {
        if (nombreInterno == null || nombreInterno.isBlank()) {
            return false;
        }
        try {
            Path path = resolveSafePath(nombreInterno);
            return Files.exists(path) && Files.isRegularFile(path);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public long getSizeBytes(String nombreInterno) {
        Path path = resolveSafePath(nombreInterno);
        try {
            return Files.size(path);
        } catch (IOException e) {
            log.warn("No se pudo determinar el tamaño del archivo: {}", path, e);
            return 0L;
        }
    }

    private Path resolveSafePath(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new BusinessException("El nombre del archivo no puede ser nulo ni vacío.");
        }
        Path resolved = rootLocation.resolve(filename).normalize();
        if (!resolved.startsWith(rootLocation)) {
            log.error("Intento de Path Traversal detectado con nombre: {}", filename);
            throw new BusinessException("Nombre de archivo inválido o violación de ruta de acceso.");
        }
        return resolved;
    }
}
