package pe.gob.municipalidad.sigm.modules.mesapartes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.gob.municipalidad.sigm.common.document.pdf.QrCodeGenerator;
import pe.gob.municipalidad.sigm.common.model.Oficina;
import pe.gob.municipalidad.sigm.common.model.Persona;
import pe.gob.municipalidad.sigm.modules.mesapartes.dto.MpvSolicitudDtos.ConstanciaPresentacionResponse;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.Expediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.TipoTramite;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.CanalIngreso;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.repository.ExpedienteDocumentoRepository;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.ConstanciaPresentacionServiceImpl;
import pe.gob.municipalidad.sigm.modules.mesapartes.service.FormularioDinamicoService;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias - Generación de Constancia de Presentación PDF")
class ConstanciaPdfUnitTest {

    @Mock
    private QrCodeGenerator qrCodeGenerator;

    @Mock
    private ExpedienteDocumentoRepository expedienteDocumentoRepository;

    @Mock
    private FormularioDinamicoService formularioDinamicoService;

    @InjectMocks
    private ConstanciaPresentacionServiceImpl constanciaService;

    @Test
    @DisplayName("Debe generar un archivo PDF de constancia con encabezados y estructura oficial")
    void testGenerarConstanciaPdfExitoso() {
        Persona solicitante = Persona.builder()
                .id(1L)
                .nombres("Juan")
                .apellidoPaterno("Pérez")
                .apellidoMaterno("Quispe")
                .tipoDocumento("DNI")
                .numeroDocumento("44556677")
                .correo("juan.perez@quinua.gob.pe")
                .telefono("966123456")
                .direccion("Jr. Sucre s/n, Quinua")
                .build();

        Oficina oficina = Oficina.builder()
                .id(1L)
                .nombre("Subgerencia de Catastro y Desarrollo Urbano")
                .build();

        TipoTramite tramite = TipoTramite.builder()
                .id(1L)
                .nombre("Constancia de Posesión de Predio")
                .oficinaResponsable(oficina)
                .build();

        Expediente expediente = Expediente.builder()
                .id(100L)
                .uuid(UUID.randomUUID())
                .numeroExpediente("MPV-2026-000001")
                .anio(2026)
                .asunto("Solicito constancia de posesión pacífica")
                .descripcion("Antecedentes y solicitud para servicio de energía eléctrica domiciliaria")
                .tipoTramite(tramite)
                .solicitante(solicitante)
                .oficinaResponsable(oficina)
                .estadoActual(EstadoExpediente.PRESENTADO)
                .canalIngreso(CanalIngreso.VIRTUAL)
                .fechaPresentacion(OffsetDateTime.now())
                .build();

        when(expedienteDocumentoRepository.findByExpedienteIdWithDocumento(100L)).thenReturn(Collections.emptyList());
        when(formularioDinamicoService.obtenerMapaRespuestas(100L)).thenReturn(Collections.singletonMap("Área del Predio", "200 m²"));
        byte[] validQrPng = new QrCodeGenerator().generateQrCodePng("http://localhost:8080/consulta", 110, 110);
        when(qrCodeGenerator.generateQrCodePng(anyString(), anyInt(), anyInt()))
                .thenReturn(validQrPng);

        byte[] pdfBytes = constanciaService.generarConstanciaPdf(expediente, "http://localhost:8080");

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 500, "El PDF generado debe contener contenido estructurado");
        String cabecera = new String(pdfBytes, 0, Math.min(pdfBytes.length, 5));
        assertEquals("%PDF-", cabecera, "El archivo debe iniciar con la firma mágica %PDF-");
    }

    @Test
    @DisplayName("Debe obtener el DTO estructurado con los datos de la constancia")
    void testObtenerDatosConstancia() {
        Persona solicitante = Persona.builder()
                .id(1L)
                .nombres("Rosa")
                .apellidoPaterno("Gutiérrez")
                .tipoDocumento("DNI")
                .numeroDocumento("77889900")
                .correo("rosa@quinua.pe")
                .build();

        Expediente expediente = Expediente.builder()
                .id(200L)
                .uuid(UUID.randomUUID())
                .numeroExpediente("MPV-2026-000002")
                .asunto("Acceso a la información pública")
                .solicitante(solicitante)
                .estadoActual(EstadoExpediente.PRESENTADO)
                .fechaPresentacion(OffsetDateTime.now())
                .build();

        when(expedienteDocumentoRepository.findByExpedienteIdWithDocumento(200L)).thenReturn(Collections.emptyList());
        when(formularioDinamicoService.obtenerMapaRespuestas(200L)).thenReturn(Collections.emptyMap());

        ConstanciaPresentacionResponse datos = constanciaService.obtenerDatosConstancia(expediente, "http://muniquinua.gob.pe");

        assertNotNull(datos);
        assertEquals("MPV-2026-000002", datos.getNumeroExpediente());
        assertEquals("Rosa Gutiérrez", datos.getSolicitanteNombreCompleto());
        assertTrue(datos.getUrlVerificacion().contains("MPV-2026-000002"));
    }
}
