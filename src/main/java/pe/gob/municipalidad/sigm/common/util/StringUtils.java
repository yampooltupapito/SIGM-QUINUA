package pe.gob.municipalidad.sigm.common.util;

/**
 * Utilidades de manipulación, saneamiento y enmascaramiento seguro de texto para SIGM-QUINUA.
 */
public final class StringUtils {

    private StringUtils() {
        // Utility class
    }

    /**
     * Limpia y normaliza cadenas de entrada eliminando espacios redundantes y caracteres de control.
     */
    public static String cleanAndNormalize(String input) {
        if (input == null) {
            return null;
        }
        String cleaned = input.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "").trim();
        return cleaned.isEmpty() ? null : cleaned;
    }

    /**
     * Sanitiza cadenas eliminando etiquetas HTML y scripts potencialmente maliciosos.
     */
    public static String sanitizeBasic(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("<[^>]*>", "").trim();
    }

    /**
     * Enmascara un DNI para visualización segura en logs o interfaces (ej. 45****34).
     */
    public static String maskDni(String dni) {
        if (dni == null || dni.length() < 4) {
            return dni != null ? dni : "****";
        }
        if (dni.length() == 8) {
            return dni.substring(0, 2) + "****" + dni.substring(6);
        }
        return "****" + dni.substring(dni.length() - 4);
    }

    /**
     * Enmascara un RUC para visualización segura (ej. 20******958).
     */
    public static String maskRuc(String ruc) {
        if (ruc == null || ruc.length() < 7) {
            return "***********";
        }
        return ruc.substring(0, 2) + "******" + ruc.substring(ruc.length() - 3);
    }

    /**
     * Enmascara un correo electrónico para privacidad de ciudadanos (ej. j***z@dominio.com).
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***@***";
        }
        int atIndex = email.indexOf('@');
        String name = email.substring(0, atIndex);
        String domain = email.substring(atIndex);

        if (name.length() <= 2) {
            return name.charAt(0) + "***" + domain;
        }
        return name.charAt(0) + "***" + name.charAt(name.length() - 1) + domain;
    }
}
