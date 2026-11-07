package pe.gob.municipalidad.sigm.modules.mercados.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.administracion.service.AuditoriaAdministrativaService;
import pe.gob.municipalidad.sigm.modules.mercados.dto.ExportacionReporteRequest;
import pe.gob.municipalidad.sigm.modules.mercados.dto.HistorialExportacionResponse;
import pe.gob.municipalidad.sigm.modules.mercados.entity.HistorialExportacionMercado;
import pe.gob.municipalidad.sigm.modules.mercados.entity.MercadoMunicipal;
import pe.gob.municipalidad.sigm.modules.mercados.enums.FormatoExportacion;
import pe.gob.municipalidad.sigm.modules.mercados.repository.HistorialExportacionMercadoRepository;
import pe.gob.municipalidad.sigm.modules.mercados.repository.MercadoMunicipalRepository;
import pe.gob.municipalidad.sigm.modules.mercados.service.HistorialExportacionService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class HistorialExportacionServiceImpl implements HistorialExportacionService {

    private final HistorialExportacionMercadoRepository historialRepository;
    private final UsuarioRepository usuarioRepository;
    private final MercadoMunicipalRepository mercadoRepository;
    private final AuditoriaAdministrativaService auditoriaService;

    public HistorialExportacionServiceImpl(
            HistorialExportacionMercadoRepository historialRepository,
            UsuarioRepository usuarioRepository,
            MercadoMunicipalRepository mercadoRepository,
            AuditoriaAdministrativaService auditoriaService) {
        this.historialRepository = historialRepository;
        this.usuarioRepository = usuarioRepository;
        this.mercadoRepository = mercadoRepository;
        this.auditoriaService = auditoriaService;
    }

    private static final String STORAGE_DIR = "uploads/mercados/exportaciones";
    private static final Map<UUID, byte[]> MEMORY_CACHE = new ConcurrentHashMap<>();

    @Override
    @Transactional
    public HistorialExportacionMercado registrarExportacion(
            ExportacionReporteRequest request,
            byte[] contenido,
            String username,
            String ip,
            String userAgent) {

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElse(null);

        MercadoMunicipal mercado = null;
        if (request.getFiltro() != null && request.getFiltro().getMercadoId() != null) {
            mercado = mercadoRepository.findById(request.getFiltro().getMercadoId()).orElse(null);
        }

        String extension = request.getFormato() == FormatoExportacion.EXCEL ? "xlsx" : "pdf";
        String mimeType = request.getFormato() == FormatoExportacion.EXCEL
                ? "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                : "application/pdf";

        String timestamp = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String nombreArchivo = String.format("REPORTE_%s_%s.%s",
                request.getTipoReporte().name(),
                timestamp,
                extension);

        String filtrosJson = request.getFiltro() != null ? request.getFiltro().toString() : "{}";

        HistorialExportacionMercado historial = HistorialExportacionMercado.builder()
                .usuario(usuario)
                .username(username != null ? username : "sistema")
                .tipoReporte(request.getTipoReporte())
                .formato(request.getFormato())
                .mercado(mercado)
                .filtrosAplicados(filtrosJson)
                .fechaGeneracion(OffsetDateTime.now())
                .estado("COMPLETADO")
                .cantidadRegistros(contenido != null ? contenido.length : 0)
                .nombreArchivo(nombreArchivo)
                .mimeType(mimeType)
                .tamanioBytes(contenido != null ? (long) contenido.length : 0L)
                .ipOrigen(ip)
                .userAgent(userAgent != null && userAgent.length() > 250 ? userAgent.substring(0, 250) : userAgent)
                .build();

        historial = historialRepository.save(historial);

        // Guardar archivo en cache y en disco si es posible
        if (contenido != null && contenido.length > 0) {
            MEMORY_CACHE.put(historial.getId(), contenido);
            guardarEnDisco(historial.getId(), nombreArchivo, contenido);
        }

        auditoriaService.registrar(
                "EXPORTAR_REPORTE_" + request.getFormato().name(),
                "mercados_historial_exportaciones",
                historial.getId(),
                "EXITO",
                String.format("Exportación de %s en formato %s por %s",
                        request.getTipoReporte().getTitulo(), request.getFormato(), username)
        );

        return historial;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HistorialExportacionResponse> listarHistorial(Pageable pageable, String username, boolean esAdmin) {
        Page<HistorialExportacionMercado> page;
        if (esAdmin) {
            page = historialRepository.findAllWithMercado(pageable);
        } else {
            Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);
            if (usuario == null) {
                return Page.empty(pageable);
            }
            page = historialRepository.findByUsuarioIdOrderByFechaGeneracionDesc(usuario.getId(), pageable);
        }

        return page.map(this::convertirAResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] descargarArchivo(UUID historialId, String username, boolean esAdmin) {
        HistorialExportacionMercado historial = historialRepository.findById(historialId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el registro de exportación"));

        if (!esAdmin && !historial.getUsername().equalsIgnoreCase(username)) {
            throw new SecurityException("No tiene permiso para descargar este archivo de exportación");
        }

        // Revisar en memoria
        byte[] enMemoria = MEMORY_CACHE.get(historialId);
        if (enMemoria != null) {
            return enMemoria;
        }

        // Revisar en disco
        byte[] enDisco = leerDeDisco(historialId, historial.getNombreArchivo());
        if (enDisco != null) {
            MEMORY_CACHE.put(historialId, enDisco);
            return enDisco;
        }

        throw new IllegalStateException("El archivo solicitado ya no se encuentra disponible");
    }

    @Override
    @Transactional(readOnly = true)
    public HistorialExportacionMercado obtenerRegistro(UUID historialId) {
        return historialRepository.findById(historialId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el registro de exportación: " + historialId));
    }

    private HistorialExportacionResponse convertirAResponse(HistorialExportacionMercado h) {
        return HistorialExportacionResponse.builder()
                .id(h.getId())
                .username(h.getUsername())
                .tipoReporte(h.getTipoReporte())
                .tipoReporteTitulo(h.getTipoReporte() != null ? h.getTipoReporte().getTitulo() : "-")
                .formato(h.getFormato())
                .mercadoNombre(h.getMercado() != null ? h.getMercado().getNombre() : "TODOS")
                .fechaGeneracion(h.getFechaGeneracion())
                .estado(h.getEstado())
                .cantidadRegistros(h.getCantidadRegistros())
                .nombreArchivo(h.getNombreArchivo())
                .tamanioBytes(h.getTamanioBytes())
                .urlDescarga("/api/mercados/reportes/exportaciones/" + h.getId() + "/descargar")
                .build();
    }

    private void guardarEnDisco(UUID id, String nombreArchivo, byte[] contenido) {
        try {
            Path dir = Paths.get(STORAGE_DIR);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            Path path = dir.resolve(id.toString() + "_" + nombreArchivo);
            Files.write(path, contenido);
        } catch (IOException e) {
            log.warn("No se pudo escribir archivo en disco para exportación {}: {}", id, e.getMessage());
        }
    }

    private byte[] leerDeDisco(UUID id, String nombreArchivo) {
        try {
            Path path = Paths.get(STORAGE_DIR).resolve(id.toString() + "_" + nombreArchivo);
            if (Files.exists(path)) {
                return Files.readAllBytes(path);
            }
        } catch (IOException e) {
            log.warn("Error leyendo archivo de disco para exportación {}: {}", id, e.getMessage());
        }
        return null;
    }
}
