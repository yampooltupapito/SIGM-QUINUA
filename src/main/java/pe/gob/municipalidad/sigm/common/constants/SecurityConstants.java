package pe.gob.municipalidad.sigm.common.constants;

/**
 * Constantes de seguridad y autenticación para SIGM-QUINUA.
 */
public final class SecurityConstants {

    private SecurityConstants() {
        // Utility class
    }

    public static final String AUTH_HEADER = "Authorization";
    public static final String AUTH_HEADER_NAME = AUTH_HEADER;
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String BEARER_PREFIX = TOKEN_PREFIX;
    public static final String TOKEN_COOKIE_NAME = "SIGM_AUTH_TOKEN";
    public static final String ACCESS_TOKEN_COOKIE_NAME = TOKEN_COOKIE_NAME;
    public static final String REFRESH_TOKEN_COOKIE_NAME = "SIGM_REFRESH_TOKEN";

    // Duración de token por defecto (8 horas en milisegundos)
    public static final long ACCESS_TOKEN_VALIDITY_MS = 8 * 60 * 60 * 1000L;
    // Duración de refresh token (7 días en milisegundos)
    public static final long REFRESH_TOKEN_VALIDITY_MS = 7 * 24 * 60 * 60 * 1000L;

    public static final String[] PUBLIC_URLS = {
            "/",
            "/servicios",
            "/servicios/**",
            "/nosotros",
            "/institucional",
            "/mesa-partes",
            "/tramites/**",
            "/noticias",
            "/noticias/**",
            "/comunicados",
            "/comunicados/**",
            "/faq",
            "/contacto",
            "/registro",
            "/mesa-partes/seguimiento",
            "/mesa-partes/seguimiento/**",
            "/mesa-partes/consulta-tramite",
            "/consulta-tramite",
            "/verificar-documento",
            "/verificar-documento/**",
            "/verificar/**",
            "/publico/**",
            "/api/mercados/documentos/verificar/**",
            "/api/publico/**",
            "/api/portal/**",
            "/api/perfil/fotografia/**",
            "/ping",
            "/verify",
            "/login",
            "/forgot-password",
            "/reset-password",
            "/logout",
            "/css/**",
            "/js/**",
            "/images/**",
            "/icons/**",
            "/favicon.ico",
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/auth/logout",
            "/api/auth/forgot-password",
            "/api/auth/reset-password",
            "/api/auth/verify-reset-token",
            "/portal/seguridad/reportar",
            "/portal/seguridad/confirmacion",
            "/portal/seguridad/seguimiento",
            "/portal/seguridad/responder-solicitud",
            "/api/portal/seguridad/reportar",
            "/api/portal/seguridad/seguimiento",
            "/api/portal/seguridad/responder-solicitud"
    };
}
