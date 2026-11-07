package pe.gob.municipalidad.sigm.modules.serenazgo.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.document.entity.Documento;
import pe.gob.municipalidad.sigm.common.document.repository.DocumentoRepository;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.common.model.Usuario;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.common.repository.PersonaRepository;
import pe.gob.municipalidad.sigm.common.repository.UsuarioRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoPersonalSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoProgramacionServicio;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.PersonalSerenazgoService;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PersonalSerenazgoServiceImpl implements PersonalSerenazgoService {

    private final PersonalSerenazgoRepository personalRepository;
    private final CargoOperativoSerenazgoRepository cargoRepository;
    private final HistorialCargoSerenazgoRepository historialCargoRepository;
    private final HistorialEstadoPersonalSerenazgoRepository historialEstadoRepository;
    private final DocumentoPersonalSerenazgoRepository documentoPersonalRepository;
    private final ProgramacionServicioSerenazgoRepository programacionRepository;
    private final PersonaRepository personaRepository;
    private final UsuarioRepository usuarioRepository;
    private final OficinaRepository oficinaRepository;
    private final DocumentoRepository documentoRepository;

    @Override
    public PersonalSerenazgoResponse registrar(PersonalSerenazgoCreateRequest request, String username) {
        Persona persona = personaRepository.findById(request.getPersonaId())
                .orElseThrow(() -> new NoSuchElementException("Persona institucional no encontrada con ID: " + request.getPersonaId()));

        personalRepository.findByPersonaId(persona.getId()).ifPresent(p -> {
            if (p.getEstado() == EstadoPersonalSerenazgo.ACTIVO) {
                throw new IllegalStateException("La persona ya cuenta con una ficha activa en el cuerpo de Serenazgo: " + p.getCodigoInterno());
            }
        });

        CargoOperativoSerenazgo cargo = cargoRepository.findById(request.getCargoOperativoId())
                .orElseThrow(() -> new NoSuchElementException("Cargo operativo no encontrado con ID: " + request.getCargoOperativoId()));

        Oficina oficina = null;
        if (request.getOficinaId() != null) {
            oficina = oficinaRepository.findById(request.getOficinaId()).orElse(null);
        }

        Usuario usuario = null;
        if (request.getUsuarioId() != null) {
            usuario = usuarioRepository.findById(request.getUsuarioId()).orElse(null);
        }

        Long sec = personalRepository.obtenerSiguienteValorSecuencia();
        String codigoGenerado = String.format("SER-%06d", sec);

        LocalDate fechaIngreso = request.getFechaIngreso() != null ? request.getFechaIngreso() : LocalDate.now();

        PersonalSerenazgo sereno = PersonalSerenazgo.builder()
                .codigoInterno(codigoGenerado)
                .persona(persona)
                .usuario(usuario)
                .oficina(oficina)
                .cargoOperativo(cargo)
                .tipoDocumentoLaboral(request.getTipoDocumentoLaboral())
                .fechaIngreso(fechaIngreso)
                .estado(request.getEstado() != null ? request.getEstado() : EstadoPersonalSerenazgo.ACTIVO)
                .observaciones(request.getObservaciones())
                .createdBy(username)
                .updatedBy(username)
                .build();

        sereno = personalRepository.save(sereno);

        Usuario userAuditoria = usuarioRepository.findByUsername(username).orElse(null);

        // Historial inicial de cargo
        HistorialCargoSerenazgo histCargo = HistorialCargoSerenazgo.builder()
                .personal(sereno)
                .cargoAnterior(null)
                .cargoNuevo(cargo)
                .fechaInicio(fechaIngreso)
                .motivo("Alta e incorporación inicial al cuerpo de serenazgo")
                .usuarioRegistro(userAuditoria)
                .build();
        historialCargoRepository.save(histCargo);

        // Historial inicial de estado
        HistorialEstadoPersonalSerenazgo histEstado = HistorialEstadoPersonalSerenazgo.builder()
                .personal(sereno)
                .estadoAnterior(sereno.getEstado())
                .estadoNuevo(sereno.getEstado())
                .fechaEfectiva(fechaIngreso)
                .motivo("Alta administrativa inicial")
                .usuarioRegistro(userAuditoria)
                .build();
        historialEstadoRepository.save(histEstado);

        log.info("Personal de serenazgo registrado exitosamente: {} por usuario {}", sereno.getCodigoInterno(), username);
        return mapearResponse(sereno);
    }

    @Override
    public PersonalSerenazgoResponse actualizar(UUID id, PersonalSerenazgoUpdateRequest request, String username) {
        PersonalSerenazgo sereno = personalRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Personal de serenazgo no encontrado con ID: " + id));

        if (request.getOficinaId() != null) {
            sereno.setOficina(oficinaRepository.findById(request.getOficinaId()).orElse(null));
        }
        if (request.getUsuarioId() != null) {
            sereno.setUsuario(usuarioRepository.findById(request.getUsuarioId()).orElse(null));
        }
        if (request.getTipoDocumentoLaboral() != null) {
            sereno.setTipoDocumentoLaboral(request.getTipoDocumentoLaboral());
        }
        if (request.getFechaIngreso() != null) {
            sereno.setFechaIngreso(request.getFechaIngreso());
        }
        if (request.getFechaSalida() != null) {
            sereno.setFechaSalida(request.getFechaSalida());
        }
        if (request.getObservaciones() != null) {
            sereno.setObservaciones(request.getObservaciones());
        }

        sereno.setUpdatedBy(username);
        sereno = personalRepository.save(sereno);

        log.info("Personal de serenazgo actualizado: {} por usuario {}", sereno.getCodigoInterno(), username);
        return mapearResponse(sereno);
    }

    @Override
    @Transactional(readOnly = true)
    public PersonalSerenazgoDetailResponse obtenerDetalle(UUID id) {
        PersonalSerenazgo sereno = personalRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Personal de serenazgo no encontrado con ID: " + id));

        DisponibilidadPersonalResponse disp = calcularDisponibilidadInterna(sereno, LocalDate.now());
        List<HistorialCargoResponse> cargos = obtenerHistorialCargos(id);
        List<HistorialEstadoResponse> estados = obtenerHistorialEstados(id);
        List<DocumentoPersonalResponse> docs = obtenerDocumentos(id);

        Persona p = sereno.getPersona();
        String docEnmascarado = enmascararDocumento(p.getTipoDocumento(), p.getNumeroDocumento());

        return PersonalSerenazgoDetailResponse.builder()
                .id(sereno.getId())
                .codigoInterno(sereno.getCodigoInterno())
                .personaId(p.getId())
                .nombreCompleto(p.getNombreCompleto())
                .nombres(p.getNombres())
                .apellidoPaterno(p.getApellidoPaterno())
                .apellidoMaterno(p.getApellidoMaterno())
                .tipoDocumento(p.getTipoDocumento())
                .numeroDocumentoEnmascarado(docEnmascarado)
                .telefono(p.getTelefono())
                .correo(p.getCorreo())
                .direccion(p.getDireccion())
                .usuarioId(sereno.getUsuario() != null ? sereno.getUsuario().getId() : null)
                .username(sereno.getUsuario() != null ? sereno.getUsuario().getUsername() : null)
                .oficinaId(sereno.getOficina() != null ? sereno.getOficina().getId() : null)
                .nombreOficina(sereno.getOficina() != null ? sereno.getOficina().getNombre() : "Sin Oficina Asignada")
                .cargoOperativoId(sereno.getCargoOperativo() != null ? sereno.getCargoOperativo().getId() : null)
                .nombreCargoOperativo(sereno.getCargoOperativo() != null ? sereno.getCargoOperativo().getNombre() : "Sin Cargo")
                .codigoCargoOperativo(sereno.getCargoOperativo() != null ? sereno.getCargoOperativo().getCodigo() : "-")
                .nivelJerarquico(sereno.getCargoOperativo() != null ? sereno.getCargoOperativo().getNivelJerarquico() : 1)
                .tipoDocumentoLaboral(sereno.getTipoDocumentoLaboral())
                .fechaIngreso(sereno.getFechaIngreso())
                .fechaSalida(sereno.getFechaSalida())
                .estado(sereno.getEstado().name())
                .observaciones(sereno.getObservaciones())
                .version(sereno.getVersion())
                .disponible(disp.isDisponible())
                .motivoNoDisponible(disp.getMotivo())
                .historialCargos(cargos)
                .historialEstados(estados)
                .documentos(docs)
                .createdAt(sereno.getCreatedAt())
                .updatedAt(sereno.getUpdatedAt())
                .createdBy(sereno.getCreatedBy())
                .updatedBy(sereno.getUpdatedBy())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PersonalSerenazgoResponse> listar(PersonalSerenazgoFilter filter, Pageable pageable) {
        List<PersonalSerenazgo> todos = personalRepository.findAll();

        List<PersonalSerenazgoResponse> filtrados = todos.stream()
                .filter(p -> {
                    if (filter == null) return true;
                    if (filter.getEstado() != null && p.getEstado() != filter.getEstado()) {
                        return false;
                    }
                    if (filter.getCargoOperativoId() != null &&
                            (p.getCargoOperativo() == null || !p.getCargoOperativo().getId().equals(filter.getCargoOperativoId()))) {
                        return false;
                    }
                    if (filter.getOficinaId() != null &&
                            (p.getOficina() == null || !p.getOficina().getId().equals(filter.getOficinaId()))) {
                        return false;
                    }
                    if (filter.getFechaIngresoDesde() != null &&
                            (p.getFechaIngreso() == null || p.getFechaIngreso().isBefore(filter.getFechaIngresoDesde()))) {
                        return false;
                    }
                    if (filter.getFechaIngresoHasta() != null &&
                            (p.getFechaIngreso() == null || p.getFechaIngreso().isAfter(filter.getFechaIngresoHasta()))) {
                        return false;
                    }
                    if (filter.getTextoBusqueda() != null && !filter.getTextoBusqueda().isBlank()) {
                        String busqueda = filter.getTextoBusqueda().toLowerCase().trim();
                        boolean coincideCodigo = p.getCodigoInterno() != null && p.getCodigoInterno().toLowerCase().contains(busqueda);
                        boolean coincideNombre = p.getPersona() != null && p.getPersona().getNombreCompleto().toLowerCase().contains(busqueda);
                        boolean coincideDoc = p.getPersona() != null && p.getPersona().getNumeroDocumento() != null && p.getPersona().getNumeroDocumento().contains(busqueda);
                        if (!coincideCodigo && !coincideNombre && !coincideDoc) {
                            return false;
                        }
                    }
                    if (Boolean.TRUE.equals(filter.getSoloDisponibles())) {
                        if (p.getEstado() != EstadoPersonalSerenazgo.ACTIVO) {
                            return false;
                        }
                    }
                    return true;
                })
                .map(this::mapearResponse)
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filtrados.size());
        List<PersonalSerenazgoResponse> pageContent = (start < filtrados.size()) ? filtrados.subList(start, end) : List.of();

        return new PageImpl<>(pageContent, pageable, filtrados.size());
    }

    @Override
    public PersonalSerenazgoResponse cambiarEstado(UUID id, CambioEstadoPersonalRequest request, String username) {
        PersonalSerenazgo sereno = personalRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Personal de serenazgo no encontrado con ID: " + id));

        if (sereno.getEstado() == request.getNuevoEstado()) {
            throw new IllegalArgumentException("El personal ya se encuentra en estado: " + request.getNuevoEstado().getDescripcion());
        }

        EstadoPersonalSerenazgo estadoAnterior = sereno.getEstado();
        sereno.setEstado(request.getNuevoEstado());
        if (request.getNuevoEstado() == EstadoPersonalSerenazgo.RETIRADO) {
            sereno.setFechaSalida(request.getFechaEfectiva());
        } else if (estadoAnterior == EstadoPersonalSerenazgo.RETIRADO && request.getNuevoEstado() == EstadoPersonalSerenazgo.ACTIVO) {
            sereno.setFechaSalida(null);
        }

        sereno.setUpdatedBy(username);
        sereno = personalRepository.save(sereno);

        Usuario userAuditoria = usuarioRepository.findByUsername(username).orElse(null);
        Documento docSustento = null;
        if (request.getDocumentoSustentoId() != null) {
            docSustento = documentoRepository.findById(request.getDocumentoSustentoId()).orElse(null);
        }

        HistorialEstadoPersonalSerenazgo hist = HistorialEstadoPersonalSerenazgo.builder()
                .personal(sereno)
                .estadoAnterior(estadoAnterior)
                .estadoNuevo(request.getNuevoEstado())
                .fechaEfectiva(request.getFechaEfectiva())
                .motivo(request.getMotivo())
                .documentoSustento(docSustento)
                .usuarioRegistro(userAuditoria)
                .build();
        historialEstadoRepository.save(hist);

        log.info("Estado de personal {} cambiado de {} a {} por {}", sereno.getCodigoInterno(), estadoAnterior, request.getNuevoEstado(), username);
        return mapearResponse(sereno);
    }

    @Override
    public PersonalSerenazgoResponse cambiarCargo(UUID id, CambioCargoPersonalRequest request, String username) {
        PersonalSerenazgo sereno = personalRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Personal de serenazgo no encontrado con ID: " + id));

        CargoOperativoSerenazgo nuevoCargo = cargoRepository.findById(request.getNuevoCargoId())
                .orElseThrow(() -> new NoSuchElementException("Nuevo cargo operativo no encontrado: " + request.getNuevoCargoId()));

        CargoOperativoSerenazgo cargoAnterior = sereno.getCargoOperativo();
        if (cargoAnterior != null && cargoAnterior.getId().equals(nuevoCargo.getId())) {
            throw new IllegalArgumentException("El personal ya ostenta el cargo: " + nuevoCargo.getNombre());
        }

        // Cerrar vigencia del cargo anterior en historial
        List<HistorialCargoSerenazgo> historiales = historialCargoRepository.findByPersonalIdOrderByFechaInicioDescCreatedAtDesc(id);
        if (!historiales.isEmpty()) {
            HistorialCargoSerenazgo ultimo = historiales.get(0);
            if (ultimo.getFechaFin() == null) {
                ultimo.setFechaFin(request.getFechaInicio());
                historialCargoRepository.save(ultimo);
            }
        }

        sereno.setCargoOperativo(nuevoCargo);
        sereno.setUpdatedBy(username);
        sereno = personalRepository.save(sereno);

        Usuario userAuditoria = usuarioRepository.findByUsername(username).orElse(null);
        Documento docSustento = null;
        if (request.getDocumentoSustentoId() != null) {
            docSustento = documentoRepository.findById(request.getDocumentoSustentoId()).orElse(null);
        }

        HistorialCargoSerenazgo nuevoHist = HistorialCargoSerenazgo.builder()
                .personal(sereno)
                .cargoAnterior(cargoAnterior)
                .cargoNuevo(nuevoCargo)
                .fechaInicio(request.getFechaInicio())
                .motivo(request.getMotivo())
                .documentoSustento(docSustento)
                .usuarioRegistro(userAuditoria)
                .build();
        historialCargoRepository.save(nuevoHist);

        log.info("Cargo de personal {} cambiado a {} por {}", sereno.getCodigoInterno(), nuevoCargo.getNombre(), username);
        return mapearResponse(sereno);
    }

    @Override
    @Transactional(readOnly = true)
    public EstadisticasPersonalResponse obtenerEstadisticas() {
        long total = personalRepository.count();
        long activos = personalRepository.countByEstado(EstadoPersonalSerenazgo.ACTIVO);
        long inactivos = personalRepository.countByEstado(EstadoPersonalSerenazgo.INACTIVO);
        long suspendidos = personalRepository.countByEstado(EstadoPersonalSerenazgo.SUSPENDIDO);
        long retirados = personalRepository.countByEstado(EstadoPersonalSerenazgo.RETIRADO);

        Map<String, Long> porCargo = new HashMap<>();
        List<PersonalSerenazgo> todos = personalRepository.findAll();
        for (PersonalSerenazgo p : todos) {
            if (p.getCargoOperativo() != null) {
                String nombreCargo = p.getCargoOperativo().getNombre();
                porCargo.put(nombreCargo, porCargo.getOrDefault(nombreCargo, 0L) + 1L);
            }
        }

        return EstadisticasPersonalResponse.builder()
                .totalRegistrados(total)
                .totalActivos(activos)
                .totalInactivos(inactivos)
                .totalSuspendidos(suspendidos)
                .totalRetirados(retirados)
                .totalDisponiblesHoy(activos)
                .distribucionPorCargo(porCargo)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DisponibilidadPersonalResponse verificarDisponibilidad(UUID id, LocalDate fecha) {
        PersonalSerenazgo sereno = personalRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Personal de serenazgo no encontrado con ID: " + id));

        LocalDate fechaConsulta = (fecha != null) ? fecha : LocalDate.now();
        return calcularDisponibilidadInterna(sereno, fechaConsulta);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generarFichaPdf(UUID id) {
        PersonalSerenazgo sereno = personalRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Personal de serenazgo no encontrado con ID: " + id));

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 36, 36, 40, 40);
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            writer.setPdfVersion(PdfWriter.PDF_VERSION_1_7);
            doc.open();

            // Estilos
            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, new Color(15, 23, 42));
            Font fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, new Color(51, 65, 85));
            Font fontSeccion = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(30, 41, 59));
            Font fontEtiqueta = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, new Color(71, 85, 105));
            Font fontValor = FontFactory.getFont(FontFactory.HELVETICA, 8, new Color(15, 23, 42));
            Font fontNota = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 7, new Color(100, 116, 139));

            // Encabezado Institucional
            Paragraph pMun = new Paragraph("MUNICIPALIDAD DISTRITAL DE QUINUA", fontTitulo);
            pMun.setAlignment(Element.ALIGN_CENTER);
            doc.add(pMun);

            Paragraph pSub = new Paragraph("PROVINCIA DE HUAMANGA — DEPARTAMENTO DE AYACUCHO\nSUBGERENCIA DE SEGURIDAD CIUDADANA Y SERENAZGO", fontSubtitulo);
            pSub.setAlignment(Element.ALIGN_CENTER);
            pSub.setSpacingAfter(10f);
            doc.add(pSub);

            // Línea separadora
            Paragraph pSep = new Paragraph("_______________________________________________________________________________________________", fontNota);
            pSep.setAlignment(Element.ALIGN_CENTER);
            pSep.setSpacingAfter(12f);
            doc.add(pSep);

            // Título Documental
            Paragraph pFicha = new Paragraph("FICHA TÉCNICA Y OPERATIVA DEL EFECTIVO DE SERENAZGO", fontSeccion);
            pFicha.setAlignment(Element.ALIGN_CENTER);
            pFicha.setSpacingAfter(14f);
            doc.add(pFicha);

            // Tabla Datos Principales
            PdfPTable tabla = new PdfPTable(4);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[]{25, 25, 25, 25});

            agregarCelda(tabla, "CÓDIGO INTERNO:", fontEtiqueta);
            agregarCelda(tabla, sereno.getCodigoInterno(), fontValor);
            agregarCelda(tabla, "ESTADO ACTUAL:", fontEtiqueta);
            agregarCelda(tabla, sereno.getEstado().getDescripcion(), fontValor);

            Persona p = sereno.getPersona();
            agregarCelda(tabla, "APELLIDOS Y NOMBRES:", fontEtiqueta);
            PdfPCell cNom = new PdfPCell(new Phrase(p.getNombreCompleto(), fontValor));
            cNom.setColspan(3);
            cNom.setPadding(4f);
            tabla.addCell(cNom);

            agregarCelda(tabla, "DOC. IDENTIDAD:", fontEtiqueta);
            agregarCelda(tabla, p.getTipoDocumento() + " " + enmascararDocumento(p.getTipoDocumento(), p.getNumeroDocumento()), fontValor);
            agregarCelda(tabla, "TELÉFONO CONTACTO:", fontEtiqueta);
            agregarCelda(tabla, p.getTelefono() != null ? p.getTelefono() : "No registrado", fontValor);

            agregarCelda(tabla, "CARGO OPERATIVO:", fontEtiqueta);
            agregarCelda(tabla, sereno.getCargoOperativo() != null ? sereno.getCargoOperativo().getNombre() : "Sin Cargo", fontValor);
            agregarCelda(tabla, "OFICINA RESPONSABLE:", fontEtiqueta);
            agregarCelda(tabla, sereno.getOficina() != null ? sereno.getOficina().getNombre() : "Seguridad Ciudadana", fontValor);

            agregarCelda(tabla, "FECHA DE INGRESO:", fontEtiqueta);
            agregarCelda(tabla, sereno.getFechaIngreso() != null ? sereno.getFechaIngreso().toString() : "No registrada", fontValor);
            agregarCelda(tabla, "FECHA DE CESE/SALIDA:", fontEtiqueta);
            agregarCelda(tabla, sereno.getFechaSalida() != null ? sereno.getFechaSalida().toString() : "Activo Vigente", fontValor);

            doc.add(tabla);

            // Nota Institucional
            Paragraph pAviso = new Paragraph("\n* Información oficial de uso estrictamente confidencial para fines de coordinación operativa de la Municipalidad Distrital de Quinua conforme a la Ley N° 31297 (Ley del Servicio de Serenazgo Municipal) y Ley N° 29733 (Protección de Datos Personales).", fontNota);
            pAviso.setSpacingBefore(15f);
            doc.add(pAviso);

            Paragraph pFechaGen = new Paragraph("Fecha de emisión: " + OffsetDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), fontNota);
            pFechaGen.setAlignment(Element.ALIGN_RIGHT);
            pFechaGen.setSpacingBefore(8f);
            doc.add(pFechaGen);

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error al generar ficha PDF del sereno {}: {}", id, e.getMessage());
            throw new RuntimeException("Error en generación de ficha técnica PDF", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialCargoResponse> obtenerHistorialCargos(UUID id) {
        return historialCargoRepository.findByPersonalIdOrderByFechaInicioDescCreatedAtDesc(id).stream()
                .map(h -> HistorialCargoResponse.builder()
                        .id(h.getId())
                        .cargoAnterior(h.getCargoAnterior() != null ? h.getCargoAnterior().getNombre() : "Sin Cargo Anterior")
                        .cargoNuevo(h.getCargoNuevo().getNombre())
                        .fechaInicio(h.getFechaInicio())
                        .fechaFin(h.getFechaFin())
                        .motivo(h.getMotivo())
                        .documentoSustentoId(h.getDocumentoSustento() != null ? h.getDocumentoSustento().getId() : null)
                        .documentoNombre(h.getDocumentoSustento() != null ? h.getDocumentoSustento().getNombreOriginal() : null)
                        .usuarioRegistro(h.getUsuarioRegistro() != null ? h.getUsuarioRegistro().getUsername() : "Sistema")
                        .createdAt(h.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialEstadoResponse> obtenerHistorialEstados(UUID id) {
        return historialEstadoRepository.findByPersonalIdOrderByFechaEfectivaDescCreatedAtDesc(id).stream()
                .map(h -> HistorialEstadoResponse.builder()
                        .id(h.getId())
                        .estadoAnterior(h.getEstadoAnterior().getDescripcion())
                        .estadoNuevo(h.getEstadoNuevo().getDescripcion())
                        .fechaEfectiva(h.getFechaEfectiva())
                        .motivo(h.getMotivo())
                        .documentoSustentoId(h.getDocumentoSustento() != null ? h.getDocumentoSustento().getId() : null)
                        .documentoNombre(h.getDocumentoSustento() != null ? h.getDocumentoSustento().getNombreOriginal() : null)
                        .usuarioRegistro(h.getUsuarioRegistro() != null ? h.getUsuarioRegistro().getUsername() : "Sistema")
                        .createdAt(h.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentoPersonalResponse> obtenerDocumentos(UUID id) {
        return documentoPersonalRepository.findByPersonalIdOrderByCreatedAtDesc(id).stream()
                .map(d -> DocumentoPersonalResponse.builder()
                        .id(d.getId())
                        .documentoId(d.getDocumento().getId())
                        .nombreArchivo(d.getDocumento().getNombreOriginal())
                        .tipoDocumento(d.getTipoDocumento().getDescripcion())
                        .numeroDocumento(d.getNumeroDocumento())
                        .descripcion(d.getDescripcion())
                        .fechaEmision(d.getFechaEmision())
                        .fechaVencimiento(d.getFechaVencimiento())
                        .estado(d.getEstado().name())
                        .mimeType(d.getDocumento().getMimeType())
                        .tamanoBytes(d.getDocumento().getTamanoBytes())
                        .createdAt(d.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    private void agregarCelda(PdfPTable tabla, String texto, Font font) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, font));
        celda.setPadding(4f);
        celda.setBorderColor(new Color(226, 232, 240));
        tabla.addCell(celda);
    }

    private DisponibilidadPersonalResponse calcularDisponibilidadInterna(PersonalSerenazgo sereno, LocalDate fecha) {
        if (sereno.getEstado() != EstadoPersonalSerenazgo.ACTIVO) {
            return DisponibilidadPersonalResponse.builder()
                    .personalId(sereno.getId())
                    .codigoInterno(sereno.getCodigoInterno())
                    .nombreCompleto(sereno.getPersona().getNombreCompleto())
                    .fechaConsulta(fecha)
                    .disponible(false)
                    .motivo("Personal no activo (" + sereno.getEstado().getDescripcion() + ")")
                    .build();
        }

        // Consultar si tiene turno asignado en la fecha
        List<ProgramacionServicioSerenazgo> prog = programacionRepository
                .findByPersonalIdAndFechaServicioBetween(sereno.getId(), fecha, fecha);

        if (!prog.isEmpty()) {
            ProgramacionServicioSerenazgo s = prog.get(0);
            String turnoNom = s.getTurno() != null ? s.getTurno().getNombre() : "Turno";
            String sectorNom = s.getSector() != null ? s.getSector().getNombre() : "General";
            return DisponibilidadPersonalResponse.builder()
                    .personalId(sereno.getId())
                    .codigoInterno(sereno.getCodigoInterno())
                    .nombreCompleto(sereno.getPersona().getNombreCompleto())
                    .fechaConsulta(fecha)
                    .disponible(true)
                    .motivo("Programado en servicio de guardia")
                    .turnoAsignado(turnoNom)
                    .sectorAsignado(sectorNom)
                    .build();
        }

        return DisponibilidadPersonalResponse.builder()
                .personalId(sereno.getId())
                .codigoInterno(sereno.getCodigoInterno())
                .nombreCompleto(sereno.getPersona().getNombreCompleto())
                .fechaConsulta(fecha)
                .disponible(true)
                .motivo("Disponible para asignación de guardia")
                .build();
    }

    private PersonalSerenazgoResponse mapearResponse(PersonalSerenazgo s) {
        Persona p = s.getPersona();
        String docEnmascarado = enmascararDocumento(p.getTipoDocumento(), p.getNumeroDocumento());
        boolean disp = s.getEstado() == EstadoPersonalSerenazgo.ACTIVO;

        return PersonalSerenazgoResponse.builder()
                .id(s.getId())
                .codigoInterno(s.getCodigoInterno())
                .personaId(p.getId())
                .nombreCompleto(p.getNombreCompleto())
                .tipoDocumentoIdentidad(p.getTipoDocumento())
                .numeroDocumentoEnmascarado(docEnmascarado)
                .usuarioId(s.getUsuario() != null ? s.getUsuario().getId() : null)
                .username(s.getUsuario() != null ? s.getUsuario().getUsername() : null)
                .oficinaId(s.getOficina() != null ? s.getOficina().getId() : null)
                .nombreOficina(s.getOficina() != null ? s.getOficina().getNombre() : "Seguridad Ciudadana")
                .cargoOperativoId(s.getCargoOperativo() != null ? s.getCargoOperativo().getId() : null)
                .nombreCargoOperativo(s.getCargoOperativo() != null ? s.getCargoOperativo().getNombre() : "Sin Cargo")
                .codigoCargoOperativo(s.getCargoOperativo() != null ? s.getCargoOperativo().getCodigo() : "-")
                .estado(s.getEstado().name())
                .fechaIngreso(s.getFechaIngreso())
                .fechaSalida(s.getFechaSalida())
                .disponible(disp)
                .build();
    }

    private String enmascararDocumento(String tipo, String numero) {
        if (numero == null || numero.length() < 4) return "****";
        return numero.substring(0, 2) + "****" + numero.substring(numero.length() - 2);
    }
}
