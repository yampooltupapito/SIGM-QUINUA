package pe.gob.municipalidad.sigm.modules.mercados.service.verification;

/**
 * Proveedor de integración con servicios oficiales de RENIEC.
 */
public interface ReniecVerificationProvider {
    boolean estaConfigurado();
    VerificationResult verificarDni(String dni);
}
