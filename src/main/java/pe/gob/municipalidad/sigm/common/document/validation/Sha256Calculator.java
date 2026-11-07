package pe.gob.municipalidad.sigm.common.document.validation;

import pe.gob.municipalidad.sigm.common.exception.BusinessException;

import java.io.InputStream;
import java.security.MessageDigest;

/**
 * Calculador de huella criptográfica SHA-256 para verificación de integridad.
 * Aclaración: El hash SHA-256 garantiza integridad física contra alteraciones,
 * pero no reemplaza una firma digital con certificado digital X.509 de autoría.
 */
public final class Sha256Calculator {

    private Sha256Calculator() {
    }

    public static String calculate(byte[] data) {
        if (data == null) {
            throw new BusinessException("No se pueden calcular datos nulos.");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new BusinessException("Error al calcular la huella SHA-256.");
        }
    }

    public static String calculate(InputStream inputStream) {
        if (inputStream == null) {
            throw new BusinessException("Flujo de entrada nulo.");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
            return bytesToHex(digest.digest());
        } catch (Exception e) {
            throw new BusinessException("Error al calcular la huella SHA-256 del flujo de datos.");
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
