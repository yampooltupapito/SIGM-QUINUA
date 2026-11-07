package pe.gob.municipalidad.sigm.modules.serenazgo.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.repository.OficinaRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.TurnoCreateRequest;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.TurnoResponse;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.TurnoUpdateRequest;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.TurnoSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoTurnoSerenazgo;
import pe.gob.municipalidad.sigm.modules.serenazgo.repository.TurnoSerenazgoRepository;
import pe.gob.municipalidad.sigm.modules.serenazgo.service.TurnoSerenazgoService;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TurnoSerenazgoServiceImpl implements TurnoSerenazgoService {

    private final TurnoSerenazgoRepository turnoRepository;
    private final OficinaRepository oficinaRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public TurnoResponse crearTurno(TurnoCreateRequest request, String username) {
        log.info("Creando nuevo turno de serenazgo por usuario: {}", username);

        // 1. Validar nombre único
        if (turnoRepository.existsByNombreIgnoreCase(request.getNombre().trim())) {
            throw new IllegalArgumentException("Ya existe un turno registrado con el nombre: " + request.getNombre().trim());
        }

        // 2. Determinar o generar código correlativo
        String codigoFinal = request.getCodigo();
        if (codigoFinal == null || codigoFinal.trim().isEmpty()) {
            Long correlativo = jdbcTemplate.queryForObject("SELECT nextval('seq_serenazgo_turno')", Long.class);
            codigoFinal = String.format("TUR-%d-%04d", LocalDate.now().getYear(), correlativo != null ? correlativo : 1);
        } else {
            codigoFinal = codigoFinal.trim().toUpperCase();
            if (turnoRepository.existsByCodigoIgnoreCase(codigoFinal)) {
                throw new IllegalArgumentException("Ya existe un turno con el código: " + codigoFinal);
            }
        }

        // 3. Validar oficina si se especifica
        Oficina oficina = null;
        if (request.getOficinaResponsableId() != null) {
            oficina = oficinaRepository.findById(request.getOficinaResponsableId())
                    .orElseThrow(() -> new NoSuchElementException("Oficina responsable no encontrada: " + request.getOficinaResponsableId()));
        }

        // 4. Evaluar cruce de medianoche automático si horaFin < horaInicio
        boolean cruzaMedianoche = Boolean.TRUE.equals(request.getCruzaMedianoche()) ||
                request.getHoraFin().isBefore(request.getHoraInicio());

        TurnoSerenazgo turno = TurnoSerenazgo.builder()
                .codigo(codigoFinal)
                .nombre(request.getNombre().trim())
                .descripcion(request.getDescripcion() != null ? request.getDescripcion().trim() : null)
                .tipoTurno(request.getTipoTurno())
                .horaInicio(request.getHoraInicio())
                .horaFin(request.getHoraFin())
                .cruzaMedianoche(cruzaMedianoche)
                .colorHex(request.getColorHex() != null ? request.getColorHex() : "#2563eb")
                .estado(request.getEstado() != null ? request.getEstado() : EstadoTurnoSerenazgo.ACTIVO)
                .oficinaResponsable(oficina)
                .observaciones(request.getObservaciones())
                .createdBy(username)
                .build();

        turno.setDuracionReferencialMinutos(turno.calcularDuracionMinutos());
        turno = turnoRepository.save(turno);

        log.info("Turno de serenazgo creado exitosamente: {} [{}]", turno.getCodigo(), turno.getNombre());
        return mapearResponse(turno);
    }

    @Override
    @Transactional
    public TurnoResponse actualizarTurno(UUID id, TurnoUpdateRequest request, String username) {
        log.info("Actualizando turno ID: {} por usuario: {}", id, username);

        TurnoSerenazgo turno = turnoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Turno no encontrado con ID: " + id));

        if (turnoRepository.existsByNombreIgnoreCaseAndIdNot(request.getNombre().trim(), id)) {
            throw new IllegalArgumentException("Ya existe otro turno registrado con el nombre: " + request.getNombre().trim());
        }

        Oficina oficina = null;
        if (request.getOficinaResponsableId() != null) {
            oficina = oficinaRepository.findById(request.getOficinaResponsableId())
                    .orElseThrow(() -> new NoSuchElementException("Oficina responsable no encontrada: " + request.getOficinaResponsableId()));
        }

        boolean cruzaMedianoche = Boolean.TRUE.equals(request.getCruzaMedianoche()) ||
                request.getHoraFin().isBefore(request.getHoraInicio());

        turno.setNombre(request.getNombre().trim());
        turno.setDescripcion(request.getDescripcion() != null ? request.getDescripcion().trim() : null);
        turno.setTipoTurno(request.getTipoTurno());
        turno.setHoraInicio(request.getHoraInicio());
        turno.setHoraFin(request.getHoraFin());
        turno.setCruzaMedianoche(cruzaMedianoche);
        turno.setDuracionReferencialMinutos(turno.calcularDuracionMinutos());
        turno.setOficinaResponsable(oficina);
        turno.setObservaciones(request.getObservaciones());
        if (request.getColorHex() != null) {
            turno.setColorHex(request.getColorHex());
        }
        if (request.getEstado() != null) {
            turno.setEstado(request.getEstado());
        }
        turno.setUpdatedBy(username);

        turno = turnoRepository.save(turno);
        return mapearResponse(turno);
    }

    @Override
    @Transactional
    public TurnoResponse cambiarEstadoTurno(UUID id, EstadoTurnoSerenazgo nuevoEstado, String username) {
        log.info("Cambiando estado de turno ID: {} a {} por {}", id, nuevoEstado, username);

        TurnoSerenazgo turno = turnoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Turno no encontrado con ID: " + id));

        turno.setEstado(nuevoEstado);
        turno.setUpdatedBy(username);
        turno = turnoRepository.save(turno);

        return mapearResponse(turno);
    }

    @Override
    @Transactional(readOnly = true)
    public TurnoResponse obtenerTurnoPorId(UUID id) {
        TurnoSerenazgo turno = turnoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Turno no encontrado con ID: " + id));
        return mapearResponse(turno);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TurnoResponse> listarTurnosActivos() {
        return turnoRepository.findByEstadoOrderByHoraInicioAsc(EstadoTurnoSerenazgo.ACTIVO)
                .stream()
                .map(this::mapearResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TurnoResponse> buscarTurnos(String termino, EstadoTurnoSerenazgo estado, Pageable pageable) {
        Page<TurnoSerenazgo> pagina;
        if (termino != null && !termino.trim().isEmpty()) {
            pagina = turnoRepository.buscarConFiltrosConTermino(termino.trim(), estado, pageable);
        } else {
            pagina = turnoRepository.buscarConFiltrosSinTermino(estado, pageable);
        }
        List<TurnoResponse> dtos = pagina.getContent().stream()
                .map(this::mapearResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, pagina.getTotalElements());
    }

    private TurnoResponse mapearResponse(TurnoSerenazgo t) {
        int minutos = t.getDuracionReferencialMinutos() != null ? t.getDuracionReferencialMinutos() : t.calcularDuracionMinutos();
        int hrs = minutos / 60;
        int mins = minutos % 60;
        String duracionFormateada = String.format("%d h %02d m", hrs, mins);

        return TurnoResponse.builder()
                .id(t.getId())
                .codigo(t.getCodigo())
                .nombre(t.getNombre())
                .descripcion(t.getDescripcion())
                .tipoTurno(t.getTipoTurno())
                .tipoTurnoDesc(t.getTipoTurno() != null ? t.getTipoTurno().getDescripcion() : "Diurno")
                .colorHex(t.getColorHex())
                .horaInicio(t.getHoraInicio())
                .horaFin(t.getHoraFin())
                .cruzaMedianoche(t.getCruzaMedianoche())
                .duracionReferencialMinutos(minutos)
                .duracionFormateada(duracionFormateada)
                .estado(t.getEstado())
                .estadoDesc(t.getEstado() != null ? t.getEstado().name() : "ACTIVO")
                .oficinaResponsableId(t.getOficinaResponsable() != null ? t.getOficinaResponsable().getId() : null)
                .oficinaResponsableNombre(t.getOficinaResponsable() != null ? t.getOficinaResponsable().getNombre() : null)
                .observaciones(t.getObservaciones())
                .version(t.getVersion())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
