package pe.gob.municipalidad.sigm.modules.serenazgo.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PersonalSerenazgoService {

    PersonalSerenazgoResponse registrar(PersonalSerenazgoCreateRequest request, String username);

    PersonalSerenazgoResponse actualizar(UUID id, PersonalSerenazgoUpdateRequest request, String username);

    PersonalSerenazgoDetailResponse obtenerDetalle(UUID id);

    Page<PersonalSerenazgoResponse> listar(PersonalSerenazgoFilter filter, Pageable pageable);

    PersonalSerenazgoResponse cambiarEstado(UUID id, CambioEstadoPersonalRequest request, String username);

    PersonalSerenazgoResponse cambiarCargo(UUID id, CambioCargoPersonalRequest request, String username);

    EstadisticasPersonalResponse obtenerEstadisticas();

    DisponibilidadPersonalResponse verificarDisponibilidad(UUID id, LocalDate fecha);

    byte[] generarFichaPdf(UUID id);

    List<HistorialCargoResponse> obtenerHistorialCargos(UUID id);

    List<HistorialEstadoResponse> obtenerHistorialEstados(UUID id);

    List<DocumentoPersonalResponse> obtenerDocumentos(UUID id);
}
