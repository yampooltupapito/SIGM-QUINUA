package pe.gob.municipalidad.sigm.modules.portal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.ConflictException;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.modules.portal.dto.PortalFaqDtos;
import pe.gob.municipalidad.sigm.modules.portal.dto.PortalNoticiaDtos;
import pe.gob.municipalidad.sigm.modules.portal.entity.PortalConfiguracion;
import pe.gob.municipalidad.sigm.modules.portal.entity.PortalFaq;
import pe.gob.municipalidad.sigm.modules.portal.entity.PortalNoticia;
import pe.gob.municipalidad.sigm.modules.portal.repository.PortalConfiguracionRepository;
import pe.gob.municipalidad.sigm.modules.portal.repository.PortalFaqRepository;
import pe.gob.municipalidad.sigm.modules.portal.repository.PortalNoticiaRepository;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortalContenidoService {

    private final PortalNoticiaRepository noticiaRepository;
    private final PortalFaqRepository faqRepository;
    private final PortalConfiguracionRepository configuracionRepository;

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern XSS_DANGEROUS = Pattern.compile("(?i)<script.*?>.*?</script>|<.*?javascript:.*?>|<.*?\\bon\\w+=[\\s\\S]*?>", Pattern.DOTALL);

    // ==========================================
    // NOTICIAS - PÚBLICO
    // ==========================================

    @Transactional(readOnly = true)
    public Page<PortalNoticiaDtos.NoticiaResponse> listarNoticiasPublicas(String tipo, String q, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, Math.min(50, size)));
        String query = (q != null && !q.trim().isEmpty()) ? q.trim() : null;
        String filterTipo = (tipo != null && !tipo.trim().isEmpty() && !"TODOS".equalsIgnoreCase(tipo)) ? tipo.trim() : null;

        if (query != null) {
            String pattern = "%" + query.toLowerCase() + "%";
            return noticiaRepository.buscarNoticiasConTermino("PUBLICADO", filterTipo, pattern, pageable)
                    .map(this::toNoticiaResponse);
        } else {
            return noticiaRepository.buscarNoticiasSinTermino("PUBLICADO", filterTipo, pageable)
                    .map(this::toNoticiaResponse);
        }
    }

    @Transactional
    public PortalNoticiaDtos.NoticiaDetailResponse obtenerDetalleNoticiaPublica(String slug) {
        PortalNoticia noticia = noticiaRepository.findBySlugAndEstado(slug, "PUBLICADO")
                .orElseThrow(() -> new ResourceNotFoundException("La publicación solicitada no se encuentra disponible o no existe."));

        // Incrementar visitas de forma segura
        noticia.setVisitas(noticia.getVisitas() + 1);
        noticiaRepository.save(noticia);

        return toNoticiaDetailResponse(noticia);
    }

    @Transactional(readOnly = true)
    public List<PortalNoticiaDtos.NoticiaResponse> obtenerNoticiasDestacadas() {
        List<PortalNoticia> destacadas = noticiaRepository.findTop3ByEstadoAndDestacadaTrueOrderByFechaPublicacionDesc("PUBLICADO");
        if (destacadas.isEmpty()) {
            destacadas = noticiaRepository.findTop5ByEstadoOrderByFechaPublicacionDesc("PUBLICADO");
        }
        return destacadas.stream().map(this::toNoticiaResponse).collect(Collectors.toList());
    }

    // ==========================================
    // NOTICIAS - ADMINISTRATIVO
    // ==========================================

    @Transactional(readOnly = true)
    public Page<PortalNoticiaDtos.NoticiaResponse> listarNoticiasAdmin(String estado, String tipo, String q, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, Math.min(50, size)));
        String query = (q != null && !q.trim().isEmpty()) ? q.trim() : null;
        String filterEstado = (estado != null && !estado.trim().isEmpty() && !"TODOS".equalsIgnoreCase(estado)) ? estado.trim() : null;
        String filterTipo = (tipo != null && !tipo.trim().isEmpty() && !"TODOS".equalsIgnoreCase(tipo)) ? tipo.trim() : null;

        if (query != null) {
            String pattern = "%" + query.toLowerCase() + "%";
            return noticiaRepository.buscarNoticiasConTermino(filterEstado, filterTipo, pattern, pageable)
                    .map(this::toNoticiaResponse);
        } else {
            return noticiaRepository.buscarNoticiasSinTermino(filterEstado, filterTipo, pageable)
                    .map(this::toNoticiaResponse);
        }
    }

    @Transactional(readOnly = true)
    public PortalNoticiaDtos.NoticiaDetailResponse obtenerNoticiaPorId(Long id) {
        PortalNoticia noticia = noticiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Noticia no encontrada con ID: " + id));
        return toNoticiaDetailResponse(noticia);
    }

    @Transactional
    public PortalNoticiaDtos.NoticiaDetailResponse crearNoticia(PortalNoticiaDtos.NoticiaCreateRequest request) {
        String slug = generarSlugUnico(request.getTitulo(), null);
        String contenidoSanitizado = sanitizarContenido(request.getContenido());

        String estadoInicial = Boolean.TRUE.equals(request.getPublicarInmediatamente()) ? "PUBLICADO" : "BORRADOR";
        LocalDateTime fechaPub = "PUBLICADO".equals(estadoInicial) ? LocalDateTime.now() : null;

        PortalNoticia noticia = PortalNoticia.builder()
                .titulo(request.getTitulo().trim())
                .slug(slug)
                .resumen(request.getResumen().trim())
                .contenido(contenidoSanitizado)
                .tipo(request.getTipo() != null ? request.getTipo().toUpperCase() : "NOTICIA")
                .categoria(request.getCategoria().trim().toUpperCase())
                .imagenUrl(request.getImagenUrl() != null ? request.getImagenUrl().trim() : null)
                .autor(request.getAutor().trim())
                .estado(estadoInicial)
                .fechaPublicacion(fechaPub)
                .destacada(Boolean.TRUE.equals(request.getDestacada()))
                .visitas(0)
                .build();

        PortalNoticia guardada = noticiaRepository.save(noticia);
        log.info("Nueva noticia creada con ID: {} y slug: '{}'", guardada.getId(), guardada.getSlug());
        return toNoticiaDetailResponse(guardada);
    }

    @Transactional
    public PortalNoticiaDtos.NoticiaDetailResponse actualizarNoticia(Long id, PortalNoticiaDtos.NoticiaUpdateRequest request) {
        PortalNoticia noticia = noticiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Noticia no encontrada con ID: " + id));

        // Si cambió el título, actualizar el slug
        if (!noticia.getTitulo().equalsIgnoreCase(request.getTitulo().trim())) {
            noticia.setSlug(generarSlugUnico(request.getTitulo(), id));
        }

        String contenidoSanitizado = sanitizarContenido(request.getContenido());

        // Manejo de fecha de publicación al cambiar a PUBLICADO
        if ("PUBLICADO".equalsIgnoreCase(request.getEstado()) && noticia.getFechaPublicacion() == null) {
            noticia.setFechaPublicacion(LocalDateTime.now());
        }

        noticia.setTitulo(request.getTitulo().trim());
        noticia.setResumen(request.getResumen().trim());
        noticia.setContenido(contenidoSanitizado);
        noticia.setTipo(request.getTipo().toUpperCase());
        noticia.setCategoria(request.getCategoria().trim().toUpperCase());
        noticia.setImagenUrl(request.getImagenUrl() != null ? request.getImagenUrl().trim() : null);
        noticia.setAutor(request.getAutor().trim());
        noticia.setEstado(request.getEstado().toUpperCase());
        noticia.setDestacada(Boolean.TRUE.equals(request.getDestacada()));

        PortalNoticia actualizada = noticiaRepository.save(noticia);
        log.info("Noticia actualizada con ID: {}", actualizada.getId());
        return toNoticiaDetailResponse(actualizada);
    }

    @Transactional
    public void cambiarEstadoNoticia(Long id, String nuevoEstado) {
        PortalNoticia noticia = noticiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Noticia no encontrada con ID: " + id));

        String estadoNormalizado = nuevoEstado.trim().toUpperCase();
        if ("PUBLICADO".equals(estadoNormalizado) && noticia.getFechaPublicacion() == null) {
            noticia.setFechaPublicacion(LocalDateTime.now());
        }
        noticia.setEstado(estadoNormalizado);
        noticiaRepository.save(noticia);
        log.info("Estado de noticia ID {} cambiado a {}", id, estadoNormalizado);
    }

    @Transactional
    public void eliminarNoticia(Long id) {
        if (!noticiaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Noticia no encontrada con ID: " + id);
        }
        noticiaRepository.deleteById(id);
        log.info("Noticia eliminada con ID: {}", id);
    }

    // ==========================================
    // PREGUNTAS FRECUENTES (FAQ)
    // ==========================================

    @Transactional(readOnly = true)
    public List<PortalFaqDtos.FaqResponse> listarFaqPublicas() {
        return faqRepository.findByEstadoOrderByCategoriaAscOrdenAsc("ACTIVO")
                .stream().map(this::toFaqResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PortalFaqDtos.FaqResponse> buscarFaqPublicas(String q) {
        if (q == null || q.trim().isEmpty()) {
            return listarFaqPublicas();
        }
        String pattern = "%" + q.trim().toLowerCase() + "%";
        return faqRepository.buscarFaqActivas(pattern)
                .stream().map(this::toFaqResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> obtenerCategoriasFaq() {
        return faqRepository.findDistinctCategoriasActivas();
    }

    @Transactional(readOnly = true)
    public List<PortalFaqDtos.FaqResponse> listarTodasFaqAdmin() {
        return faqRepository.findAllByOrderByOrdenAsc()
                .stream().map(this::toFaqResponse).collect(Collectors.toList());
    }

    @Transactional
    public PortalFaqDtos.FaqResponse crearFaq(PortalFaqDtos.FaqCreateRequest request) {
        PortalFaq faq = PortalFaq.builder()
                .pregunta(request.getPregunta().trim())
                .respuesta(request.getRespuesta().trim())
                .categoria(request.getCategoria().trim().toUpperCase())
                .orden(request.getOrden() != null ? request.getOrden() : 1)
                .estado(request.getEstado() != null ? request.getEstado().toUpperCase() : "ACTIVO")
                .build();
        PortalFaq guardada = faqRepository.save(faq);
        return toFaqResponse(guardada);
    }

    @Transactional
    public PortalFaqDtos.FaqResponse actualizarFaq(Long id, PortalFaqDtos.FaqUpdateRequest request) {
        PortalFaq faq = faqRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pregunta frecuente no encontrada con ID: " + id));

        faq.setPregunta(request.getPregunta().trim());
        faq.setRespuesta(request.getRespuesta().trim());
        faq.setCategoria(request.getCategoria().trim().toUpperCase());
        faq.setOrden(request.getOrden());
        faq.setEstado(request.getEstado().toUpperCase());

        return toFaqResponse(faqRepository.save(faq));
    }

    @Transactional
    public void eliminarFaq(Long id) {
        if (!faqRepository.existsById(id)) {
            throw new ResourceNotFoundException("Pregunta frecuente no encontrada con ID: " + id);
        }
        faqRepository.deleteById(id);
    }

    // ==========================================
    // CONFIGURACIÓN INSTITUCIONAL
    // ==========================================

    @Transactional(readOnly = true)
    public Map<String, String> obtenerMapaConfiguracion() {
        List<PortalConfiguracion> configs = configuracionRepository.findAll();
        Map<String, String> mapa = new HashMap<>();
        for (PortalConfiguracion c : configs) {
            mapa.put(c.getClave(), c.getValor());
        }
        return mapa;
    }

    @Transactional
    public void guardarConfiguracion(String clave, String valor, String descripcion, String grupo) {
        PortalConfiguracion config = configuracionRepository.findByClave(clave)
                .orElse(PortalConfiguracion.builder().clave(clave).build());
        config.setValor(valor);
        if (descripcion != null) config.setDescripcion(descripcion);
        if (grupo != null) config.setGrupo(grupo);
        configuracionRepository.save(config);
    }

    // ==========================================
    // UTILIDADES DE MAPEO Y SEGURIDAD
    // ==========================================

    private String generarSlugUnico(String titulo, Long excludeId) {
        String base = toSlug(titulo);
        String slug = base;
        int contador = 1;

        while ((excludeId == null && noticiaRepository.existsBySlug(slug)) ||
               (excludeId != null && noticiaRepository.existsBySlugAndIdNot(slug, excludeId))) {
            slug = base + "-" + contador;
            contador++;
        }
        return slug;
    }

    public static String toSlug(String input) {
        if (input == null) return "";
        String nowhitespace = WHITESPACE.matcher(input.trim()).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }

    public static String sanitizarContenido(String contenido) {
        if (contenido == null) return "";
        // Eliminar scripts, javascript handlers e inyecciones peligrosas
        return XSS_DANGEROUS.matcher(contenido).replaceAll("");
    }

    private PortalNoticiaDtos.NoticiaResponse toNoticiaResponse(PortalNoticia n) {
        return PortalNoticiaDtos.NoticiaResponse.builder()
                .id(n.getId())
                .titulo(n.getTitulo())
                .slug(n.getSlug())
                .resumen(n.getResumen())
                .tipo(n.getTipo())
                .categoria(n.getCategoria())
                .imagenUrl(n.getImagenUrl())
                .autor(n.getAutor())
                .estado(n.getEstado())
                .fechaPublicacion(n.getFechaPublicacion())
                .destacada(n.getDestacada())
                .visitas(n.getVisitas())
                .createdAt(n.getCreatedAt())
                .build();
    }

    private PortalNoticiaDtos.NoticiaDetailResponse toNoticiaDetailResponse(PortalNoticia n) {
        return PortalNoticiaDtos.NoticiaDetailResponse.builder()
                .id(n.getId())
                .titulo(n.getTitulo())
                .slug(n.getSlug())
                .resumen(n.getResumen())
                .contenido(n.getContenido())
                .tipo(n.getTipo())
                .categoria(n.getCategoria())
                .imagenUrl(n.getImagenUrl())
                .autor(n.getAutor())
                .estado(n.getEstado())
                .fechaPublicacion(n.getFechaPublicacion())
                .destacada(n.getDestacada())
                .visitas(n.getVisitas())
                .createdAt(n.getCreatedAt())
                .updatedAt(n.getUpdatedAt())
                .build();
    }

    private PortalFaqDtos.FaqResponse toFaqResponse(PortalFaq f) {
        return PortalFaqDtos.FaqResponse.builder()
                .id(f.getId())
                .pregunta(f.getPregunta())
                .respuesta(f.getRespuesta())
                .categoria(f.getCategoria())
                .orden(f.getOrden())
                .estado(f.getEstado())
                .build();
    }
}
