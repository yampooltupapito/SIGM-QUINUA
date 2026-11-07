package pe.gob.municipalidad.sigm.modules.recaudaciontributaria;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;
import pe.gob.municipalidad.sigm.common.exception.DuplicateResourceException;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.dto.request.ContribuyenteCreateRequest;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoVerificacionDocumento;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoContribuyente;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository.ContribuyenteTributarioRepository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.validation.ContribuyenteValidator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContribuyenteValidatorTest {

    @Mock
    private ContribuyenteTributarioRepository contribuyenteRepository;

    private ContribuyenteValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ContribuyenteValidator(contribuyenteRepository);
    }

    @Test
    @DisplayName("Validación de DNI peruano: exactamente 8 dígitos numéricos")
    void testValidacionDni() {
        assertTrue(validator.validarFormatoDni("41234567"));
        assertTrue(validator.validarFormatoDni("70809010"));
        assertFalse(validator.validarFormatoDni("4123456"));   // 7 dígitos
        assertFalse(validator.validarFormatoDni("412345678")); // 9 dígitos
        assertFalse(validator.validarFormatoDni("4123456A")); // alfanumérico
        assertFalse(validator.validarFormatoDni(null));
    }

    @Test
    @DisplayName("Validación de RUC peruano: Algoritmo oficial SUNAT Módulo 11")
    void testValidacionRucModulo11() {
        // RUCs peruanos válidos matemáticamente
        // Ejemplo SUNAT: 20131312955 (SUNAT)
        assertTrue(validator.validarFormatoRuc("20131312955"));
        // Ejemplo Persona Natural con RUC: 10412345678 (si cumple suma % 11)
        // Probemos cálculo:
        // Prefijos válidos: 10, 15, 16, 17, 20
        assertFalse(validator.validarFormatoRuc("12345678901")); // prefijo inválido '12'
        assertFalse(validator.validarFormatoRuc("20131312950")); // dígito verificador adulterado
        assertFalse(validator.validarFormatoRuc("2013131295"));  // longitud menor a 11
        assertFalse(validator.validarFormatoRuc(null));
    }

    @Test
    @DisplayName("Rechazo de registro si el documento ya se encuentra registrado (duplicado estricto)")
    void testRechazoDuplicadoDocumento() {
        when(contribuyenteRepository.existsByTipoDocumentoAndNumeroDocumento("DNI", "41234567")).thenReturn(true);

        ContribuyenteCreateRequest request = ContribuyenteCreateRequest.builder()
                .tipoContribuyente(TipoContribuyente.PERSONA_NATURAL)
                .tipoDocumento("DNI")
                .numeroDocumento("41234567")
                .primerApellido("QUISPE")
                .nombres("JUAN")
                .build();

        assertThrows(DuplicateResourceException.class, () -> validator.validarCreacion(request));
    }

    @Test
    @DisplayName("Validación exitosa de Persona Natural con formato DNI válido")
    void testValidacionPersonaNaturalExitosa() {
        when(contribuyenteRepository.existsByTipoDocumentoAndNumeroDocumento("DNI", "41234567")).thenReturn(false);

        ContribuyenteCreateRequest request = ContribuyenteCreateRequest.builder()
                .tipoContribuyente(TipoContribuyente.PERSONA_NATURAL)
                .tipoDocumento("DNI")
                .numeroDocumento("41234567")
                .primerApellido("QUISPE")
                .nombres("JUAN")
                .correo("juan.quispe@correo.com")
                .telefono("966123456")
                .build();

        EstadoVerificacionDocumento estado = validator.validarCreacion(request);
        assertEquals(EstadoVerificacionDocumento.VALIDADO_FORMATO, estado);
    }

    @Test
    @DisplayName("Rechazo de Persona Natural si faltan nombres o primer apellido")
    void testRechazoPersonaNaturalIncompleta() {
        when(contribuyenteRepository.existsByTipoDocumentoAndNumeroDocumento("DNI", "41234567")).thenReturn(false);

        ContribuyenteCreateRequest requestSinNombre = ContribuyenteCreateRequest.builder()
                .tipoContribuyente(TipoContribuyente.PERSONA_NATURAL)
                .tipoDocumento("DNI")
                .numeroDocumento("41234567")
                .primerApellido("QUISPE")
                .nombres("")
                .build();

        assertThrows(BusinessException.class, () -> validator.validarCreacion(requestSinNombre));
    }

    @Test
    @DisplayName("Rechazo de Persona Jurídica si el tipo de documento no es RUC")
    void testRechazoPersonaJuridicaSinRuc() {
        when(contribuyenteRepository.existsByTipoDocumentoAndNumeroDocumento("DNI", "41234567")).thenReturn(false);

        ContribuyenteCreateRequest request = ContribuyenteCreateRequest.builder()
                .tipoContribuyente(TipoContribuyente.PERSONA_JURIDICA)
                .tipoDocumento("DNI")
                .numeroDocumento("41234567")
                .nombreRazonSocial("EMPRESA COMERCIAL SAC")
                .build();

        assertThrows(BusinessException.class, () -> validator.validarCreacion(request));
    }
}
