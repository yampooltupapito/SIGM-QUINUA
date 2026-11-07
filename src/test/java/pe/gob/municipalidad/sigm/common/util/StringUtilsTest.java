package pe.gob.municipalidad.sigm.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StringUtilsTest {

    @Test
    @DisplayName("Enmascaramiento de DNI debe proteger los dígitos centrales")
    void maskDni() {
        assertEquals("45****34", StringUtils.maskDni("45892134"));
        assertEquals("123", StringUtils.maskDni("123")); // Menor a 4 caracteres no se altera
    }

    @Test
    @DisplayName("Enmascaramiento de RUC debe proteger los dígitos intermedios")
    void maskRuc() {
        assertEquals("20******958", StringUtils.maskRuc("20177758958"));
    }

    @Test
    @DisplayName("Enmascaramiento de correo electrónico debe proteger el buzón y mostrar dominio")
    void maskEmail() {
        assertEquals("j***z@muniquinua.gob.pe", StringUtils.maskEmail("juan.perez@muniquinua.gob.pe"));
        assertEquals("a***a@dominio.com", StringUtils.maskEmail("alcaldia@dominio.com"));
    }

    @Test
    @DisplayName("Sanitización básica debe remover etiquetas HTML y caracteres peligrosos")
    void sanitizeBasic() {
        assertEquals("alert(1)", StringUtils.sanitizeBasic("<script>alert(1)</script>"));
        assertEquals("Texto seguro", StringUtils.sanitizeBasic("<b>Texto seguro</b>"));
    }
}
