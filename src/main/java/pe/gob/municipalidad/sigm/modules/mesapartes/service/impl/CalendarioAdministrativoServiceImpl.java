package pe.gob.municipalidad.sigm.modules.mesapartes.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.municipalidad.sigm.common.exception.ResourceNotFoundException;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.ConfiguracionMpvDtos.*;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.CalendarioAdministrativo;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoCambioConfiguracion;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoEntidadConfiguracion;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.CalendarioAdministrativoRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.CalendarioAdministrativoService;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.ConfiguracionHistorialService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CalendarioAdministrativoServiceImpl implements CalendarioAdministrativoService {

    private static final Logger log = LoggerFactory.getLogger(CalendarioAdministrativoServiceImpl.class);

    private final CalendarioAdministrativoRepository calendarioRepository;
    private final ConfiguracionHistorialService historialService;

    public CalendarioAdministrativoServiceImpl(
            CalendarioAdministrativoRepository calendarioRepository,
            ConfiguracionHistorialService historialService
    ) {
        this.calendarioRepository = calendarioRepository;
        this.historialService = historialService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CalendarioAdminResponse> listarPorAnio(Integer anio) {
        int anioConsulta = (anio != null && anio > 1900) ? anio : LocalDate.now().getYear();
        return calendarioRepository.findByAnioOrderByFechaAsc(anioConsulta)
                .stream()
                .map(this::mapearResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CalendarioAdminResponse> listarTodosActivos() {
        return calendarioRepository.findByActivoTrueOrderByFechaAsc()
                .stream()
                .map(this::mapearResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CalendarioAdminResponse guardarFeriado(GuardarCalendarioRequest request, String username, String ipOrigen) {
        if (request.getFecha() == null) {
            throw new IllegalArgumentException("La fecha es obligatoria.");
        }

        Optional<CalendarioAdministrativo> existente = calendarioRepository.findByFecha(request.getFecha());
        CalendarioAdministrativo cal;
        TipoCambioConfiguracion tipoCambio;

        if (existente.isPresent()) {
            cal = existente.get();
            cal.setNombre(request.getNombre().trim());
            cal.setTipo(request.getTipo());
            cal.setEsLaborable(Boolean.TRUE.equals(request.getEsLaborable()));
            cal.setAplicaTodosTramites(Boolean.TRUE.equals(request.getAplicaTodosTramites()));
            cal.setBaseNormativa(request.getBaseNormativa());
            cal.setActivo(Boolean.TRUE.equals(request.getActivo()));
            cal.setUpdatedBy(username);
            tipoCambio = TipoCambioConfiguracion.MODIFICACION;
        } else {
            cal = CalendarioAdministrativo.builder()
                    .anio(request.getFecha().getYear())
                    .fecha(request.getFecha())
                    .nombre(request.getNombre().trim())
                    .tipo(request.getTipo())
                    .esLaborable(Boolean.TRUE.equals(request.getEsLaborable()))
                    .aplicaTodosTramites(Boolean.TRUE.equals(request.getAplicaTodosTramites()))
                    .baseNormativa(request.getBaseNormativa())
                    .activo(Boolean.TRUE.equals(request.getActivo()))
                    .createdBy(username)
                    .build();
            tipoCambio = TipoCambioConfiguracion.CREACION;
        }

        CalendarioAdministrativo guardado = calendarioRepository.save(cal);

        historialService.registrarCambio(
                TipoEntidadConfiguracion.CALENDARIO,
                guardado.getId(),
                guardado.getFecha().toString(),
                tipoCambio,
                null,
                null,
                "Configuración de día no laborable: " + guardado.getNombre(),
                null,
                null,
                username,
                ipOrigen
        );

        return mapearResponse(guardado);
    }

    @Override
    @Transactional
    public void cambiarEstado(Long id, boolean activo, String username, String ipOrigen) {
        CalendarioAdministrativo cal = calendarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fecha no encontrada en el calendario con ID: " + id));

        cal.setActivo(activo);
        cal.setUpdatedBy(username);
        calendarioRepository.save(cal);

        historialService.registrarCambio(
                TipoEntidadConfiguracion.CALENDARIO,
                cal.getId(),
                cal.getFecha().toString(),
                TipoCambioConfiguracion.MODIFICACION,
                null,
                null,
                (activo ? "Activación" : "Desactivación") + " de fecha no laborable: " + cal.getNombre(),
                null,
                null,
                username,
                ipOrigen
        );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean esDiaNoLaborable(LocalDate fecha) {
        if (fecha == null) {
            return false;
        }
        Optional<CalendarioAdministrativo> opt = calendarioRepository.findByFecha(fecha);
        return opt.isPresent() && Boolean.TRUE.equals(opt.get().getActivo()) && !Boolean.TRUE.equals(opt.get().getEsLaborable());
    }

    private CalendarioAdminResponse mapearResponse(CalendarioAdministrativo c) {
        return CalendarioAdminResponse.builder()
                .id(c.getId())
                .anio(c.getAnio())
                .fecha(c.getFecha())
                .nombre(c.getNombre())
                .tipo(c.getTipo())
                .esLaborable(c.getEsLaborable())
                .aplicaTodosTramites(c.getAplicaTodosTramites())
                .baseNormativa(c.getBaseNormativa())
                .activo(c.getActivo())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
