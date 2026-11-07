package pe.gob.municipalidad.sigm.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pe.gob.municipalidad.sigm.common.constants.SecurityConstants;
import pe.gob.municipalidad.sigm.security.authentication.UserDetailsServiceImpl;
import pe.gob.municipalidad.sigm.security.jwt.JwtTokenProvider;

import java.io.IOException;
import java.util.Collection;

/**
 * Filtro de autenticación JWT para interceptar peticiones entrantes.
 * Extrae y valida tokens tanto desde la cabecera 'Authorization: Bearer' como desde cookies seguras 'SIGM_AUTH_TOKEN'.
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;

    @Autowired
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                   UserDetailsServiceImpl userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this(jwtTokenProvider, null);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = resolveToken(request);

            if (token != null && jwtTokenProvider.validateToken(token)) {
                String username = jwtTokenProvider.getUsernameFromToken(token);
                Collection<? extends GrantedAuthority> authorities = jwtTokenProvider.getAuthoritiesFromToken(token);

                if (jwtTokenProvider.isAuthoritiesCompacted(token) && userDetailsService != null) {
                    try {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        if (userDetails.isEnabled() && userDetails.isAccountNonLocked()) {
                            authorities = userDetails.getAuthorities();
                        }
                    } catch (Exception ex) {
                        log.debug("Conservando autoridades base del token para '{}': {}", username, ex.getMessage());
                    }
                }

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Usuario autenticado vía JWT: '{}' con {} autoridades", username, authorities.size());
            }
        } catch (Exception ex) {
            log.warn("No se pudo establecer la autenticación del usuario en el contexto de seguridad: {}", ex.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        // 1. Prioridad: Cabecera Authorization: Bearer <token>
        String bearerToken = request.getHeader(SecurityConstants.AUTH_HEADER);
        if (bearerToken != null && bearerToken.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            return bearerToken.substring(SecurityConstants.TOKEN_PREFIX.length()).trim();
        }

        // 2. Alternativa segura para clientes web (Thymeleaf): Cookie HTTP-Only
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (SecurityConstants.TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}
