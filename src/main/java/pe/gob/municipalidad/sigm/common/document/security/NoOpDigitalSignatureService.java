package pe.gob.municipalidad.sigm.common.document.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.gob.municipalidad.sigm.common.exception.BusinessException;

/**
 * Implementación base que declara de forma transparente que la infraestructura de PKI /
 * certificados digitales acreditados aún no ha sido provista con claves criptográficas reales.
 */
@Slf4j
@Service
public class NoOpDigitalSignatureService implements DigitalSignatureService {

    @Override
    public boolean isConfigured() {
        return false;
    }

    @Override
    public byte[] signPdf(byte[] documentBytes, String certificadoAlias) {
        log.warn("Se intentó invocar la firma digital pero no existe un proveedor de certificados X.509 configurado.");
        throw new BusinessException(
                "El módulo de firma digital con certificado digital oficial no está configurado en este entorno. " +
                "El documento mantiene su integridad mediante huella criptográfica SHA-256."
        );
    }
}
