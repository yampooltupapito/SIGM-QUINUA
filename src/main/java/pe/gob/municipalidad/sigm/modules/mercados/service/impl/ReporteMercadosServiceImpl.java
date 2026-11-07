package pe.gob.municipalidad.sigm.modules.mercados.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.modules.mercados.dto.ComercianteReporteItem;
import pe.gob.municipalidad.sigm.modules.mercados.dto.ContratoReporteItem;
import pe.gob.municipalidad.sigm.modules.mercados.dto.ReporteMercadoFilter;
import pe.gob.municipalidad.sigm.modules.mercados.entity.*;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoAsignacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoAutorizacion;
import pe.gob.municipalidad.sigm.modules.mercados.enums.EstadoContrato;
import pe.gob.municipalidad.sigm.modules.mercados.repository.*;
import pe.gob.municipalidad.sigm.modules.mercados.service.ReporteMercadosService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReporteMercadosServiceImpl implements ReporteMercadosService {

    private final ComercianteRepository comercianteRepository;
    private final AsignacionPuestoRepository asignacionRepository;
    private final ContratoOcupacionRepository contratoRepository;
    private final AutorizacionComercialRepository autorizacionRepository;

    public ReporteMercadosServiceImpl(
            ComercianteRepository comercianteRepository,
            AsignacionPuestoRepository asignacionRepository,
            ContratoOcupacionRepository contratoRepository,
            AutorizacionComercialRepository autorizacionRepository) {
        this.comercianteRepository = comercianteRepository;
        this.asignacionRepository = asignacionRepository;
        this.contratoRepository = contratoRepository;
        this.autorizacionRepository = autorizacionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ComercianteReporteItem> listarComerciantes(ReporteMercadoFilter filtro, Pageable pageable, boolean puedeVerDocumentos) {
        List<ComercianteReporteItem> todos = listarTodosComerciantes(filtro, puedeVerDocumentos);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), todos.size());
        List<ComercianteReporteItem> sublist = start <= end ? todos.subList(start, end) : Collections.emptyList();
        return new PageImpl<>(sublist, pageable, todos.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComercianteReporteItem> listarTodosComerciantes(ReporteMercadoFilter filtro, boolean puedeVerDocumentos) {
        UUID mercadoId = filtro != null ? filtro.getMercadoId() : null;
        String estadoStr = filtro != null ? filtro.getEstado() : null;
        String termino = filtro != null && filtro.getTermino() != null ? filtro.getTermino().trim().toLowerCase() : null;

        List<Comerciante> comerciantes = comercianteRepository.findAll();
        List<AsignacionPuesto> allAsig = asignacionRepository.findAll();

        Map<UUID, List<AsignacionPuesto>> asigPorComerciante = allAsig.stream()
                .filter(a -> a.getComerciante() != null)
                .collect(Collectors.groupingBy(a -> a.getComerciante().getId()));

        return comerciantes.stream()
                .filter(c -> {
                    if (estadoStr != null && !estadoStr.isBlank() && !c.getEstado().name().equalsIgnoreCase(estadoStr)) {
                        return false;
                    }
                    List<AsignacionPuesto> misAsig = asigPorComerciante.getOrDefault(c.getId(), Collections.emptyList());
                    if (mercadoId != null) {
                        boolean tieneEnMercado = misAsig.stream().anyMatch(a -> a.getPuesto() != null && a.getPuesto().getMercado().getId().equals(mercadoId));
                        if (!tieneEnMercado) return false;
                    }
                    if (termino != null) {
                        String doc = c.getNumeroDocumentoTitular() != null ? c.getNumeroDocumentoTitular().toLowerCase() : "";
                        String nom = c.getNombreOTitular().toLowerCase();
                        String cod = c.getCodigoComerciante().toLowerCase();
                        return doc.contains(termino) || nom.contains(termino) || cod.contains(termino);
                    }
                    return true;
                })
                .sorted(Comparator.comparing(Comerciante::getNombreOTitular))
                .map(c -> {
                    List<AsignacionPuesto> misAsig = asigPorComerciante.getOrDefault(c.getId(), Collections.emptyList());
                    int vigentes = (int) misAsig.stream().filter(a -> a.getEstado() == EstadoAsignacion.VIGENTE).count();

                    String mercadosNombres = misAsig.stream()
                            .filter(a -> a.getPuesto() != null && a.getPuesto().getMercado() != null)
                            .map(a -> a.getPuesto().getMercado().getNombre())
                            .distinct()
                            .collect(Collectors.joining(", "));

                    if (mercadosNombres.isBlank()) mercadosNombres = "-";

                    String giros = c.getActividades() != null ? c.getActividades().stream().map(ActividadComercial::getNombre).collect(Collectors.joining(", ")) : "-";
                    if (giros.isBlank()) giros = "-";

                    String numDoc = c.getNumeroDocumentoTitular();
                    if (!puedeVerDocumentos && numDoc != null && numDoc.length() > 4) {
                        numDoc = numDoc.substring(0, 2) + "*".repeat(numDoc.length() - 4) + numDoc.substring(numDoc.length() - 2);
                    }

                    return ComercianteReporteItem.builder()
                            .id(c.getId())
                            .codigoComerciante(c.getCodigoComerciante())
                            .tipoComerciante(c.getTipoComerciante().name())
                            .nombreORazonSocial(c.getNombreOTitular())
                            .tipoDocumento(c.getTipoDocumentoTitular())
                            .numeroDocumento(numDoc)
                            .telefono(c.getTelefonoContacto())
                            .correo(c.getCorreoContacto())
                            .estado(c.getEstado().name())
                            .fechaRegistro(c.getFechaRegistro())
                            .asignacionesVigentes(vigentes)
                            .mercadosRelacionados(mercadosNombres)
                            .girosComerciales(giros)
                            .build();
                }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContratoReporteItem> listarContratos(ReporteMercadoFilter filtro, Pageable pageable) {
        List<ContratoReporteItem> todos = listarTodosContratos(filtro);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), todos.size());
        List<ContratoReporteItem> sublist = start <= end ? todos.subList(start, end) : Collections.emptyList();
        return new PageImpl<>(sublist, pageable, todos.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContratoReporteItem> listarTodosContratos(ReporteMercadoFilter filtro) {
        UUID mercadoId = filtro != null ? filtro.getMercadoId() : null;
        String estadoStr = filtro != null ? filtro.getEstado() : null;
        String termino = filtro != null && filtro.getTermino() != null ? filtro.getTermino().trim().toLowerCase() : null;
        LocalDate hoy = LocalDate.now();

        List<ContratoOcupacion> contratos = contratoRepository.findAll();

        return contratos.stream()
                .filter(c -> {
                    // Criterio 22: No mostrar contratos en borrador como instrumentos vigentes
                    if (c.getEstado() == EstadoContrato.BORRADOR) {
                        return false;
                    }
                    if (mercadoId != null && (c.getPuesto() == null || !c.getPuesto().getMercado().getId().equals(mercadoId))) {
                        return false;
                    }
                    if (estadoStr != null && !estadoStr.isBlank() && !c.getEstado().name().equalsIgnoreCase(estadoStr)) {
                        return false;
                    }
                    if (termino != null) {
                        String cod = c.getCodigoContrato().toLowerCase();
                        String titular = c.getComerciante() != null ? c.getComerciante().getNombreOTitular().toLowerCase() : "";
                        String puesto = c.getPuesto() != null ? c.getPuesto().getCodigo().toLowerCase() : "";
                        return cod.contains(termino) || titular.contains(termino) || puesto.contains(termino);
                    }
                    return true;
                })
                .sorted(Comparator.comparing(ContratoOcupacion::getFechaInicio, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(c -> {
                    long dias = c.getFechaFin() != null ? ChronoUnit.DAYS.between(hoy, c.getFechaFin()) : 999;
                    boolean vencido = c.getFechaFin() != null && c.getFechaFin().isBefore(hoy);
                    boolean proximo = !vencido && dias <= 30;

                    return ContratoReporteItem.builder()
                            .id(c.getId())
                            .codigoContrato(c.getCodigoContrato())
                            .tipoInstrumento("CONTRATO")
                            .mercadoNombre(c.getPuesto() != null ? c.getPuesto().getMercado().getNombre() : "-")
                            .puestoCodigo(c.getPuesto() != null ? c.getPuesto().getCodigo() : "-")
                            .comercianteNombre(c.getComerciante() != null ? c.getComerciante().getNombreOTitular() : "-")
                            .documentoTitular(c.getComerciante() != null ? c.getComerciante().getNumeroDocumentoTitular() : "-")
                            .modalidad(c.getTipoContrato() != null ? c.getTipoContrato().getNombre() : c.getTipoContratoCodigo())
                            .fechaInicio(c.getFechaInicio())
                            .fechaFin(c.getFechaFin())
                            .estado(c.getEstado().name())
                            .diasParaVencer(dias)
                            .vencido(vencido)
                            .proximoAVencer(proximo)
                            .build();
                }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContratoReporteItem> listarAutorizaciones(ReporteMercadoFilter filtro, Pageable pageable) {
        List<ContratoReporteItem> todos = listarTodasAutorizaciones(filtro);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), todos.size());
        List<ContratoReporteItem> sublist = start <= end ? todos.subList(start, end) : Collections.emptyList();
        return new PageImpl<>(sublist, pageable, todos.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContratoReporteItem> listarTodasAutorizaciones(ReporteMercadoFilter filtro) {
        UUID mercadoId = filtro != null ? filtro.getMercadoId() : null;
        String estadoStr = filtro != null ? filtro.getEstado() : null;
        String termino = filtro != null && filtro.getTermino() != null ? filtro.getTermino().trim().toLowerCase() : null;
        LocalDate hoy = LocalDate.now();

        List<AutorizacionComercial> autorizaciones = autorizacionRepository.findAll();

        return autorizaciones.stream()
                .filter(a -> {
                    // Criterio 23: No considerar vigente una autorización suspendida o borrador
                    if (a.getEstado() == EstadoAutorizacion.BORRADOR) {
                        return false;
                    }
                    if (mercadoId != null && (a.getPuesto() == null || !a.getPuesto().getMercado().getId().equals(mercadoId))) {
                        return false;
                    }
                    if (estadoStr != null && !estadoStr.isBlank() && !a.getEstado().name().equalsIgnoreCase(estadoStr)) {
                        return false;
                    }
                    if (termino != null) {
                        String cod = a.getCodigoAutorizacion().toLowerCase();
                        String titular = a.getComerciante() != null ? a.getComerciante().getNombreOTitular().toLowerCase() : "";
                        String puesto = a.getPuesto() != null ? a.getPuesto().getCodigo().toLowerCase() : "";
                        return cod.contains(termino) || titular.contains(termino) || puesto.contains(termino);
                    }
                    return true;
                })
                .sorted(Comparator.comparing(AutorizacionComercial::getFechaInicio, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(a -> {
                    long dias = a.getFechaFin() != null ? ChronoUnit.DAYS.between(hoy, a.getFechaFin()) : 999;
                    boolean vencido = a.getFechaFin() != null && a.getFechaFin().isBefore(hoy);
                    boolean proximo = !vencido && dias <= 30;

                    return ContratoReporteItem.builder()
                            .id(a.getId())
                            .codigoContrato(a.getCodigoAutorizacion())
                            .tipoInstrumento("AUTORIZACION")
                            .mercadoNombre(a.getPuesto() != null ? a.getPuesto().getMercado().getNombre() : "-")
                            .puestoCodigo(a.getPuesto() != null ? a.getPuesto().getCodigo() : "-")
                            .comercianteNombre(a.getComerciante() != null ? a.getComerciante().getNombreOTitular() : "-")
                            .documentoTitular(a.getComerciante() != null ? a.getComerciante().getNumeroDocumentoTitular() : "-")
                            .modalidad(a.getTipoAutorizacionEntity() != null ? a.getTipoAutorizacionEntity().getNombre() : a.getTipoAutorizacion())
                            .fechaInicio(a.getFechaInicio())
                            .fechaFin(a.getFechaFin())
                            .estado(a.getEstado().name())
                            .diasParaVencer(dias)
                            .vencido(vencido)
                            .proximoAVencer(proximo)
                            .build();
                }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContratoReporteItem> listarVencimientos(ReporteMercadoFilter filtro, Pageable pageable) {
        List<ContratoReporteItem> todos = listarTodosVencimientos(filtro);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), todos.size());
        List<ContratoReporteItem> sublist = start <= end ? todos.subList(start, end) : Collections.emptyList();
        return new PageImpl<>(sublist, pageable, todos.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContratoReporteItem> listarTodosVencimientos(ReporteMercadoFilter filtro) {
        List<ContratoReporteItem> contratos = listarTodosContratos(filtro);
        List<ContratoReporteItem> autorizaciones = listarTodasAutorizaciones(filtro);

        List<ContratoReporteItem> consolidado = new ArrayList<>();
        consolidado.addAll(contratos);
        consolidado.addAll(autorizaciones);

        return consolidado.stream()
                .filter(item -> item.isVencido() || item.isProximoAVencer())
                .sorted(Comparator.comparing(ContratoReporteItem::getDiasParaVencer))
                .collect(Collectors.toList());
    }
}
