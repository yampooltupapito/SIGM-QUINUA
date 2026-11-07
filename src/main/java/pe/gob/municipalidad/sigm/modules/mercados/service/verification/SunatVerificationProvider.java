package pe.gob.municipalidad.sigm.modules.mercados.service.verification;

/**
 * Proveedor de integración con servicios oficiales de SUNAT.
 */
public interface SunatVerificationProvider {
    boolean estaConfigurado();
    VerificationResult verificarRuc(String ruc);
}
