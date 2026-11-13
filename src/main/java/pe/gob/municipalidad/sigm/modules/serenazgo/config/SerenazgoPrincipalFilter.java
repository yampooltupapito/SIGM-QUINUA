package pe.gob.municipalidad.sigm.modules.serenazgo.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pe.gob.municipalidad.sigm.security.authentication.CustomUserDetails;
import pe.gob.municipalidad.sigm.security.authentication.UserDetailsServiceImpl;

import java.io.IOException;

/**
 * Adaptador de principal exclusivo del módulo Seguridad Ciudadana / Serenazgo.
 *
 * <p>El filtro JWT global autentica las peticiones web con el <em>username</em> (String) como
 * principal. Los controladores y servicios de Serenazgo, en cambio, reciben el usuario mediante
 * {@code @AuthenticationPrincipal CustomUserDetails}, que con ese principal llegaba siempre
 * {@code null} y provocaba {@code NullPointerException} (exportes, supervisión, reportes, etc.).</p>
 *
 * <p>Este filtro se ejecuta después de la cadena de seguridad y, únicamente para las rutas del
 * módulo, sustituye el principal por el {@link CustomUserDetails} del mismo usuario, conservando
 * sus autoridades y detalles. No modifica el filtro JWT ni el comportamiento de otros módulos.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SerenazgoPrincipalFilter extends OncePerRequestFilter {

    private static final String PREFIJO_WEB = "/dashboard/serenazgo";
    private static final String PREFIJO_API = "/api/serenazgo";
    private static final String PREFIJO_API_V1 = "/api/v1/serenazgo";

    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String ruta = request.getRequestURI();
        String contexto = request.getContextPath();
        if (contexto != null && !contexto.isEmpty() && ruta.startsWith(contexto)) {
            ruta = ruta.substring(contexto.length());
        }
        return !(ruta.startsWith(PREFIJO_WEB) || ruta.startsWith(PREFIJO_API) || ruta.startsWith(PREFIJO_API_V1));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication actual = SecurityContextHolder.getContext().getAuthentication();

        if (actual != null
                && actual.isAuthenticated()
                && !(actual instanceof AnonymousAuthenticationToken)
                && !(actual.getPrincipal() instanceof CustomUserDetails)) {
            try {
                UserDetails detalles = userDetailsService.loadUserByUsername(actual.getName());
                if (detalles instanceof CustomUserDetails custom) {
                    UsernamePasswordAuthenticationToken adaptada =
                            new UsernamePasswordAuthenticationToken(custom, actual.getCredentials(), actual.getAuthorities());
                    adaptada.setDetails(actual.getDetails());
                    SecurityContextHolder.getContext().setAuthentication(adaptada);
                }
            } catch (Exception ex) {
                log.debug("No se pudo adaptar el principal de '{}' a CustomUserDetails: {}", actual.getName(), ex.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
