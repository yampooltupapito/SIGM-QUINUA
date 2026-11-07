package pe.gob.municipalidad.sigm.modules.mercados.service.verification;

/**
 * Servicio unificado de validación matemática y verificación de identidad/RUC.
 */
public interface IdentidadVerificationService {

    /**
     * Valida la estructura matemática de un DNI peruano (8 dígitos numéricos).
     */
    boolean validarFormatoDni(String dni);

    /**
     * Valida la estructura matemática y dígito verificador módulo 11 de un RUC peruano (11 dígitos).
     */
    boolean validarFormatoRuc(String ruc);

    /**
     * Realiza validación de formato y verificación formal ante RENIEC si está configurado.
     */
    VerificationResult verificarDni(String dni);

    /**
     * Realiza validación de formato y verificación formal ante SUNAT si está configurado.
     */
    VerificationResult verificarRuc(String ruc);
}
