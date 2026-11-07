package pe.gob.municipalidad.sigm.modules.mercados.service.verification;

import lombok.Builder;
import lombok.Getter;

/**
 * Resultado formal de verificación de identidad o registro tributario.
 */
@Getter
@Builder
public class VerificationResult {
    private final boolean formatoValido;
    private final boolean verificadoExternamente;
    private final String proveedor;
    private final String mensaje;
    private final String documento;
    private final String nombreRazonSocial;

    public static VerificationResult formatoValido(String proveedor, String documento, String mensaje) {
        return VerificationResult.builder()
                .formatoValido(true)
                .verificadoExternamente(false)
                .proveedor(proveedor)
                .documento(documento)
                .mensaje(mensaje)
                .build();
    }

    public static VerificationResult formatoInvalido(String proveedor, String documento, String mensaje) {
        return VerificationResult.builder()
                .formatoValido(false)
                .verificadoExternamente(false)
                .proveedor(proveedor)
                .documento(documento)
                .mensaje(mensaje)
                .build();
    }
}
