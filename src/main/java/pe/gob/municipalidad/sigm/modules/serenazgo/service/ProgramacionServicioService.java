package pe.gob.municipalidad.sigm.modules.serenazgo.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoProgramacionServicio;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ProgramacionServicioService {

    ProgramacionResponse crearProgramacion(ProgramacionCreateRequest request, String username);

    ProgramacionResponse actualizarProgramacion(UUID id, ProgramacionUpdateRequest request, String username);

    ProgramacionResponse confirmarProgramacion(UUID id, String motivo, String username);

    ProgramacionResponse reprogramarServicio(UUID id, ReprogramarServicioRequest request, String username);

    ProgramacionResponse cancelarServicio(UUID id, CancelarServicioRequest request, String username);

    ProgramacionResponse iniciarServicio(UUID id, String username);

    ProgramacionResponse finalizarServicio(UUID id, String observaciones, String username);

    ProgramacionResponse cambiarEstado(UUID id, CambioEstadoProgramacionRequest request, String username);

    ProgramacionDetailResponse asignarPersonal(UUID id, AsignacionPersonalItemRequest request, String username);

    ProgramacionDetailResponse desasignarPersonal(UUID id, UUID personalId, String motivo, String username);

    ProgramacionDetailResponse cambiarResponsableServicio(UUID id, UUID nuevoResponsableId, String motivo, String username);

    ProgramacionDetailResponse obtenerDetallePorId(UUID id);

    Page<ProgramacionResponse> buscarProgramaciones(String termino,
                                                    LocalDate desde,
                                                    LocalDate hasta,
                                                    EstadoProgramacionServicio estado,
                                                    UUID sectorId,
                                                    UUID turnoId,
                                                    Pageable pageable);

    List<CalendarioEventoResponse> obtenerEventosCalendario(LocalDate desde,
                                                            LocalDate hasta,
                                                            UUID sectorId,
                                                            EstadoProgramacionServicio estado);

    List<ConflictoDisponibilidadResponse> verificarConflictos(LocalDate fecha,
                                                             UUID turnoId,
                                                             List<UUID> personalIds,
                                                             List<UUID> unidadIds,
                                                             List<UUID> equipamientoIds,
                                                             UUID excluirId);

    DashboardProgramacionResponse obtenerDashboardProgramacion();

    byte[] exportarDespachoDiarioPdf(LocalDate fecha);

    byte[] exportarProgramacionExcel(LocalDate desde, LocalDate hasta);
}
