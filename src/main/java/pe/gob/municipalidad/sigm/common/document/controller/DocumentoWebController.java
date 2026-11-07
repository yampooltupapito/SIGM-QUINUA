package pe.gob.municipalidad.sigm.common.document.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pe.gob.municipalidad.sigm.common.document.dto.DocumentDtos.DocumentoEstadisticasResponse;
import pe.gob.municipalidad.sigm.common.document.dto.DocumentDtos.DocumentoResponse;
import pe.gob.municipalidad.sigm.common.document.entity.CategoriaDocumento;
import pe.gob.municipalidad.sigm.common.document.entity.ClasificacionAcceso;
import pe.gob.municipalidad.sigm.common.document.entity.EstadoDocumento;
import pe.gob.municipalidad.sigm.common.document.service.DocumentoService;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;

@Slf4j
@Controller
public class DocumentoWebController {

    private final DocumentoService documentoService;
    private final OficinaRepository oficinaRepository;

    public DocumentoWebController(DocumentoService documentoService, OficinaRepository oficinaRepository) {
        this.documentoService = documentoService;
        this.oficinaRepository = oficinaRepository;
    }

    @GetMapping({"/dashboard/documentos", "/admin/documentos"})
    @PreAuthorize("isAuthenticated()")
    public String index(
            @RequestParam(value = "estado", required = false) EstadoDocumento estado,
            @RequestParam(value = "categoria", required = false) CategoriaDocumento categoria,
            @RequestParam(value = "clasificacion", required = false) ClasificacionAcceso clasificacion,
            @RequestParam(value = "termino", required = false) String termino,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Authentication authentication,
            Model model
    ) {
        String username = authentication != null ? authentication.getName() : null;
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(size, 50));

        Page<DocumentoResponse> documentos = documentoService.listarDocumentos(
                estado, categoria, clasificacion, termino, pageable, username
        );
        DocumentoEstadisticasResponse estadisticas = documentoService.obtenerEstadisticas();

        model.addAttribute("moduloActivo", "documentos");
        model.addAttribute("tituloPagina", "Gestión Documental Centralizada | SIGM-QUINUA");
        model.addAttribute("documentos", documentos);
        model.addAttribute("estadisticas", estadisticas);
        model.addAttribute("categorias", CategoriaDocumento.values());
        model.addAttribute("clasificaciones", ClasificacionAcceso.values());
        model.addAttribute("estados", EstadoDocumento.values());
        model.addAttribute("oficinas", oficinaRepository.findAll());

        // Parámetros de filtro seleccionados
        model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("categoriaSeleccionada", categoria);
        model.addAttribute("clasificacionSeleccionada", clasificacion);
        model.addAttribute("terminoBusqueda", termino);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);

        return "admin/documentos/index";
    }
}
