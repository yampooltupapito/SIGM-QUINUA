package pe.gob.municipalidad.sigm.modules.serenazgo.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.municipalidad.sigm.modules.serenazgo.dto.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AlertaCiudadanaSeguridadService {

    // Portal Ciudadano
    AlertaConfirmacionResponse registrarAlertaCiudadana(AlertaRegistroCiudadanoRequest request, List<MultipartFile> archivos, String usernameAutenticado, String ipCliente);

    AlertaDetailResponse consultarSeguimiento(String codigo, String pinConsulta, String usernameAutenticado);

    Page<AlertaResponse> listarMisReportes(String username, Pageable pageable);

    AlertaDetailResponse obtenerMiReporte(UUID id, String username);

    void responderSolicitudInformacion(AlertaRespuestaCiudadanoRequest request, String usernameAutenticado);

    // Gestión Administrativa
    AlertaConfirmacionResponse registrarAlertaAdmin(AlertaRegistroAdminRequest request, List<MultipartFile> archivos, String username);

    Page<AlertaResponse> listarConFiltrosAdmin(AlertaFilter filtro, Pageable pageable);

    AlertaDetailResponse obtenerDetalleAdmin(UUID id, boolean puedeVerDatosSensibles);

    AlertaResponse clasificarAlerta(UUID id, AlertaClasificarRequest request, String username);

    AlertaResponse cambiarPrioridad(UUID id, AlertaPrioridadRequest request, String username);

    AlertaResponse cambiarEstado(UUID id, AlertaCambioEstadoRequest request, String username);

    AlertaSolicitudInfoResponse solicitarInformacion(UUID id, AlertaSolicitarInfoRequest request, String username);

    AlertaResponse vincularConIncidencia(UUID id, AlertaVincularIncidenciaRequest request, String username);

    AlertaResponse cerrarAlerta(UUID id, AlertaCierreRequest request, String username);

    AlertaResponse descartarAlerta(UUID id, AlertaDescarteRequest request, String username);

    DashboardAlertasResponse obtenerDashboardAlertas();

    byte[] exportarAlertasExcel(LocalDate desde, LocalDate hasta);

    byte[] exportarFichaPdf(UUID id);
}
