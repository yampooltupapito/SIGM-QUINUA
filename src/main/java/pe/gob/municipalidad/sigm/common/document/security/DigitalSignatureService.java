package pe.gob.municipalidad.sigm.common.document.security;

/**
 * Punto de extensión para firma digital criptográfica con certificados X.509.
 *
 * Distinción conceptual:
 * 1. Imagen de firma: Representación gráfica manuscrita escaneada (sin valor legal por sí sola).
 * 2. Firma electrónica simple: Datos en formato electrónico anexos a otros datos.
 * 3. Firma digital criptográfica: Creada con clave privada bajo control exclusivo del firmante
 *    y verificable con certificado digital emitido por una Entidad de Certificación acreditada (INDECOPI/RENIEC)
 *    conforme a la Ley N° 27269 (Ley de Firmas y Certificados Digitales del Perú).
 */
public interface DigitalSignatureService {

    /**
     * Indica si el servicio de firma digital cuenta con certificados y proveedores acreditados configurados.
     */
    boolean isConfigured();

    /**
     * Aplica una firma digital criptográfica sobre los bytes del documento PDF.
     *
     * @param documentBytes contenido binario original del documento
     * @param certificadoAlias alias o identificador del certificado institucional
     * @return documento binario firmado digitalmente
     */
    byte[] signPdf(byte[] documentBytes, String certificadoAlias);
}
