package pe.gob.municipalidad.sigm.modules.serenazgo.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.EquipamientoSerenazgoService;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.RecursosSeguridadService;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.UnidadesMovilesService;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecursosSeguridadServiceImpl implements RecursosSeguridadService {

    private final UnidadMovilSerenazgoRepository unidadRepository;
    private final EquipamientoSerenazgoRepository equipamientoRepository;
    private final MantenimientoRecursoRepository mantenimientoRepository;
    private final IncidenciaTecnicaRecursoRepository incidenciaRepository;
    private final HistorialEstadoUnidadMovilRepository historialEstadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UnidadesMovilesService unidadesService;
    private final EquipamientoSerenazgoService equipamientoService;

    @Override
    @Transactional(readOnly = true)
    public DashboardRecursosResponse obtenerDashboardRecursos() {
        long totalUnidades = unidadRepository.count();
        long dispUnidades = unidadRepository.countByEstadoOperativo(EstadoUnidadMovil.DISPONIBLE) +
                unidadRepository.countByEstadoOperativo(EstadoUnidadMovil.OPERATIVA);
        long asigUnidades = unidadRepository.countByEstadoOperativo(EstadoUnidadMovil.ASIGNADA);
        long mntUnidades = unidadRepository.countByEstadoOperativo(EstadoUnidadMovil.EN_MANTENIMIENTO);
        long fueraUnidades = unidadRepository.countByEstadoOperativo(EstadoUnidadMovil.FUERA_DE_SERVICIO) +
                unidadRepository.countByEstadoOperativo(EstadoUnidadMovil.INACTIVA);

        long totalEquipos = equipamientoRepository.count();
        long dispEquipos = equipamientoRepository.countByEstado(EstadoEquipamientoSerenazgo.DISPONIBLE);
        long asigEquipos = equipamientoRepository.countByEstado(EstadoEquipamientoSerenazgo.ASIGNADO);
        long repEquipos = equipamientoRepository.countByEstado(EstadoEquipamientoSerenazgo.EN_REPARACION) +
                equipamientoRepository.countByEstado(EstadoEquipamientoSerenazgo.EN_MANTENIMIENTO);
        long bajaEquipos = equipamientoRepository.countByEstado(EstadoEquipamientoSerenazgo.DE_BAJA);

        long incAbiertas = incidenciaRepository.countIncidenciasAbiertas();
        long mntProgramados = mantenimientoRepository.countByEstado(EstadoMantenimientoRecurso.PROGRAMADO) +
                mantenimientoRepository.countByEstado(EstadoMantenimientoRecurso.EN_PROCESO);

        return DashboardRecursosResponse.builder()
                .totalUnidades(totalUnidades)
                .unidadesDisponibles(dispUnidades)
                .unidadesAsignadas(asigUnidades)
                .unidadesEnMantenimiento(mntUnidades)
                .unidadesFueraServicio(fueraUnidades)
                .totalEquipos(totalEquipos)
                .equiposDisponibles(dispEquipos)
                .equiposAsignados(asigEquipos)
                .equiposEnReparacion(repEquipos)
                .equiposDeBaja(bajaEquipos)
                .incidenciasAbiertas(incAbiertas)
                .mantenimientosProgramados(mntProgramados)
                .build();
    }

    @Override
    @Transactional
    public MantenimientoResponse finalizarMantenimiento(UUID mantenimientoId, FinalizarMantenimientoRequest request, String username) {
        MantenimientoRecurso mnt = mantenimientoRepository.findById(mantenimientoId)
                .orElseThrow(() -> new NoSuchElementException("El mantenimiento especificado no existe"));

        mnt.setEstado(EstadoMantenimientoRecurso.FINALIZADO);
        mnt.setFechaRealizada(request.getFechaRealizada() != null ? request.getFechaRealizada() : LocalDate.now());
        mnt.setResultadoTecnico(request.getResultadoTecnico());
        if (request.getCostoFinal() != null) {
            mnt.setCosto(request.getCostoFinal());
        }
        mnt = mantenimientoRepository.save(mnt);

        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);

        // Si era unidad móvil y estaba en mantenimiento, restaurar a DISPONIBLE
        if (mnt.getUnidad() != null) {
            UnidadMovilSerenazgo u = mnt.getUnidad();
            if (u.getEstadoOperativo() == EstadoUnidadMovil.EN_MANTENIMIENTO) {
                u.setEstadoOperativo(EstadoUnidadMovil.DISPONIBLE);
                unidadRepository.save(u);

                HistorialEstadoUnidadMovil hist = HistorialEstadoUnidadMovil.builder()
                        .unidad(u)
                        .estadoAnterior(EstadoUnidadMovil.EN_MANTENIMIENTO)
                        .estadoNuevo(EstadoUnidadMovil.DISPONIBLE)
                        .fechaEfectiva(OffsetDateTime.now())
                        .motivo("Mantenimiento finalizado con conformidad: " + mnt.getCodigo())
                        .usuarioRegistro(usuario)
                        .build();
                historialEstadoRepository.save(hist);
            }
        }

        // Si era equipamiento y estaba en mantenimiento, restaurar
        if (mnt.getEquipamiento() != null) {
            EquipamientoSerenazgo eq = mnt.getEquipamiento();
            if (eq.getEstado() == EstadoEquipamientoSerenazgo.EN_MANTENIMIENTO || eq.getEstado() == EstadoEquipamientoSerenazgo.EN_REPARACION) {
                eq.setEstado(EstadoEquipamientoSerenazgo.DISPONIBLE);
                equipamientoRepository.save(eq);
            }
        }

        return mapearMantenimientoResponse(mnt);
    }

    @Override
    @Transactional
    public IncidenciaTecnicaResponse resolverIncidencia(UUID incidenciaId, ResolverIncidenciaTecnicaRequest request, String username) {
        IncidenciaTecnicaRecurso inc = incidenciaRepository.findById(incidenciaId)
                .orElseThrow(() -> new NoSuchElementException("La incidencia técnica especificada no existe"));

        EstadoIncidenciaTecnica nuevoEst = request.getNuevoEstado() != null ? request.getNuevoEstado() : EstadoIncidenciaTecnica.RESUELTA;
        inc.setEstado(nuevoEst);
        inc.setAccionesTomadas(request.getAccionesTomadas());
        inc = incidenciaRepository.save(inc);

        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);

        if (inc.getUnidad() != null) {
            UnidadMovilSerenazgo u = inc.getUnidad();
            if (u.getEstadoOperativo() == EstadoUnidadMovil.FUERA_DE_SERVICIO) {
                u.setEstadoOperativo(EstadoUnidadMovil.DISPONIBLE);
                unidadRepository.save(u);

                HistorialEstadoUnidadMovil hist = HistorialEstadoUnidadMovil.builder()
                        .unidad(u)
                        .estadoAnterior(EstadoUnidadMovil.FUERA_DE_SERVICIO)
                        .estadoNuevo(EstadoUnidadMovil.DISPONIBLE)
                        .fechaEfectiva(OffsetDateTime.now())
                        .motivo("Incidencia técnica resuelta: " + inc.getCodigo() + " - " + request.getAccionesTomadas())
                        .usuarioRegistro(usuario)
                        .build();
                historialEstadoRepository.save(hist);
            }
        }

        if (inc.getEquipamiento() != null) {
            EquipamientoSerenazgo eq = inc.getEquipamiento();
            if (eq.getEstado() == EstadoEquipamientoSerenazgo.EN_REPARACION) {
                eq.setEstado(EstadoEquipamientoSerenazgo.DISPONIBLE);
                equipamientoRepository.save(eq);
            }
        }

        return mapearIncidenciaResponse(inc);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarExcel(UnidadMovilFilter unidadFilter, EquipamientoFilter equipFilter) {
        List<UnidadMovilResponse> unidades = unidadesService.listar(unidadFilter, Pageable.unpaged()).getContent();
        List<EquipamientoResponse> equipos = equipamientoService.listar(equipFilter, Pageable.unpaged()).getContent();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // Estilos
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Hoja 1: Unidades Móviles
            Sheet sheet1 = workbook.createSheet("Unidades Móviles");
            String[] cols1 = {"CÓDIGO", "TIPO", "PLACA", "MARCA", "MODELO", "AÑO", "ESTADO OPERATIVO", "KILOMETRAJE", "OFICINA", "RESPONSABLE ACTUAL"};
            Row r0 = sheet1.createRow(0);
            for (int i = 0; i < cols1.length; i++) {
                Cell cell = r0.createCell(i);
                cell.setCellValue(cols1[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx1 = 1;
            for (UnidadMovilResponse u : unidades) {
                Row r = sheet1.createRow(rowIdx1++);
                r.createCell(0).setCellValue(sanitizar(u.getCodigoInterno()));
                r.createCell(1).setCellValue(sanitizar(u.getTipoUnidadDesc()));
                r.createCell(2).setCellValue(sanitizar(u.getPlaca() != null ? u.getPlaca() : "N/A"));
                r.createCell(3).setCellValue(sanitizar(u.getMarca() != null ? u.getMarca() : ""));
                r.createCell(4).setCellValue(sanitizar(u.getModelo() != null ? u.getModelo() : ""));
                r.createCell(5).setCellValue(u.getAnio() != null ? u.getAnio().toString() : "");
                r.createCell(6).setCellValue(sanitizar(u.getEstadoOperativoDesc()));
                r.createCell(7).setCellValue(u.getKilometrajeActual() != null ? u.getKilometrajeActual().doubleValue() : 0.0);
                r.createCell(8).setCellValue(sanitizar(u.getOficinaNombre() != null ? u.getOficinaNombre() : "BASE CENTRAL"));
                r.createCell(9).setCellValue(sanitizar(u.getResponsableActualNombre() != null ? u.getResponsableActualNombre() : "DISPONIBLE"));
            }

            for (int i = 0; i < cols1.length; i++) {
                sheet1.autoSizeColumn(i);
            }

            // Hoja 2: Equipamiento
            Sheet sheet2 = workbook.createSheet("Equipamiento Operativo");
            String[] cols2 = {"CÓDIGO", "TIPO", "DESCRIPCIÓN", "MARCA / MODELO", "N° SERIE", "CÓD. PATRIMONIAL", "ESTADO", "STOCK", "OFICINA", "ASIGNADO A"};
            Row r0_2 = sheet2.createRow(0);
            for (int i = 0; i < cols2.length; i++) {
                Cell cell = r0_2.createCell(i);
                cell.setCellValue(cols2[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx2 = 1;
            for (EquipamientoResponse e : equipos) {
                Row r = sheet2.createRow(rowIdx2++);
                r.createCell(0).setCellValue(sanitizar(e.getCodigoInterno()));
                r.createCell(1).setCellValue(sanitizar(e.getTipoEquipoDesc()));
                r.createCell(2).setCellValue(sanitizar(e.getDescripcion()));
                r.createCell(3).setCellValue(sanitizar((e.getMarca() != null ? e.getMarca() : "") + " " + (e.getModelo() != null ? e.getModelo() : "")));
                r.createCell(4).setCellValue(sanitizar(e.getNumeroSerie() != null ? e.getNumeroSerie() : "N/A"));
                r.createCell(5).setCellValue(sanitizar(e.getCodigoPatrimonial() != null ? e.getCodigoPatrimonial() : "N/A"));
                r.createCell(6).setCellValue(sanitizar(e.getEstadoDesc()));
                r.createCell(7).setCellValue(Boolean.TRUE.equals(e.getEsControladoPorCantidad()) ? (e.getStockDisponible() + "/" + e.getStockTotal()) : "1/1");
                r.createCell(8).setCellValue(sanitizar(e.getOficinaResponsableNombre() != null ? e.getOficinaResponsableNombre() : "BASE CENTRAL"));
                r.createCell(9).setCellValue(sanitizar(e.getResponsableNombre() != null ? e.getResponsableNombre() : (e.getUnidadMovilInfo() != null ? e.getUnidadMovilInfo() : "DISPONIBLE")));
            }

            for (int i = 0; i < cols2.length; i++) {
                sheet2.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error al exportar inventario a Excel", e);
            throw new RuntimeException("Error en exportación a Excel", e);
        }
    }

    private String sanitizar(String val) {
        if (val == null) return "";
        String s = val.trim();
        if (s.startsWith("=") || s.startsWith("+") || s.startsWith("-") || s.startsWith("@")) {
            return "'" + s;
        }
        return s;
    }

    private MantenimientoResponse mapearMantenimientoResponse(MantenimientoRecurso m) {
        String infoUnidad = m.getUnidad() != null ? m.getUnidad().getCodigoInterno() : null;
        String infoEquipo = m.getEquipamiento() != null ? m.getEquipamiento().getCodigoInterno() + " (" + m.getEquipamiento().getDescripcion() + ")" : null;

        return MantenimientoResponse.builder()
                .id(m.getId())
                .codigo(m.getCodigo())
                .tipoRecurso(m.getTipoRecurso())
                .tipoRecursoDesc(m.getTipoRecurso().getDescripcion())
                .unidadId(m.getUnidad() != null ? m.getUnidad().getId() : null)
                .unidadInfo(infoUnidad)
                .equipamientoId(m.getEquipamiento() != null ? m.getEquipamiento().getId() : null)
                .equipamientoInfo(infoEquipo)
                .tipoMantenimiento(m.getTipoMantenimiento())
                .tipoMantenimientoDesc(m.getTipoMantenimiento().getDescripcion())
                .fechaProgramada(m.getFechaProgramada())
                .fechaRealizada(m.getFechaRealizada())
                .kilometrajeMantenimiento(m.getKilometrajeMantenimiento())
                .descripcion(m.getDescripcion())
                .tallerProveedor(m.getTallerProveedor())
                .costo(m.getCosto())
                .estado(m.getEstado())
                .estadoDesc(m.getEstado().getDescripcion())
                .motivoCancelacion(m.getMotivoCancelacion())
                .resultadoTecnico(m.getResultadoTecnico())
                .usuarioRegistro(m.getUsuarioRegistro() != null ? m.getUsuarioRegistro().getUsername() : null)
                .createdAt(m.getCreatedAt())
                .build();
    }

    private IncidenciaTecnicaResponse mapearIncidenciaResponse(IncidenciaTecnicaRecurso i) {
        String persNombre = null;
        if (i.getPersonalReporta() != null && i.getPersonalReporta().getPersona() != null) {
            persNombre = i.getPersonalReporta().getPersona().getNombreCompleto();
        }

        return IncidenciaTecnicaResponse.builder()
                .id(i.getId())
                .codigo(i.getCodigo())
                .tipoRecurso(i.getTipoRecurso())
                .tipoRecursoDesc(i.getTipoRecurso().getDescripcion())
                .unidadId(i.getUnidad() != null ? i.getUnidad().getId() : null)
                .unidadInfo(i.getUnidad() != null ? i.getUnidad().getCodigoInterno() : null)
                .equipamientoId(i.getEquipamiento() != null ? i.getEquipamiento().getId() : null)
                .equipamientoInfo(i.getEquipamiento() != null ? i.getEquipamiento().getCodigoInterno() : null)
                .tipoIncidencia(i.getTipoIncidencia())
                .tipoIncidenciaDesc(i.getTipoIncidencia().getDescripcion())
                .fechaReporte(i.getFechaReporte())
                .descripcion(i.getDescripcion())
                .personalReportaId(i.getPersonalReporta() != null ? i.getPersonalReporta().getId() : null)
                .personalReportaNombre(persNombre)
                .nivelGravedad(i.getNivelGravedad())
                .nivelGravedadDesc(i.getNivelGravedad().getDescripcion())
                .estado(i.getEstado())
                .estadoDesc(i.getEstado().getDescripcion())
                .accionesTomadas(i.getAccionesTomadas())
                .usuarioRegistro(i.getUsuarioRegistro() != null ? i.getUsuarioRegistro().getUsername() : null)
                .createdAt(i.getCreatedAt())
                .build();
    }
}
