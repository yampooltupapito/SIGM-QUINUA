package pe.gob.municipalidad.sigm.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import pe.gob.municipalidad.sigm.common.constants.SecurityConstants;
import pe.gob.municipalidad.sigm.security.authentication.UserDetailsServiceImpl;
import pe.gob.municipalidad.sigm.security.authorization.OfficeSecurityService;
import pe.gob.municipalidad.sigm.security.filter.JwtAccessDeniedHandler;
import pe.gob.municipalidad.sigm.security.filter.JwtAuthenticationEntryPoint;
import pe.gob.municipalidad.sigm.security.filter.JwtAuthenticationFilter;

/**
 * Configuración de seguridad empresarial con Spring Security 7 y JWT para SIGM-QUINUA.
 * Implementa autenticación sin estado (Stateless), filtrado JWT, control de acceso RBAC
 * y protección integral contra ataques de autenticación.
 */
@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final UserDetailsServiceImpl userDetailsService;
    private final OfficeSecurityService officeSecurityService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                          JwtAccessDeniedHandler jwtAccessDeniedHandler,
                          UserDetailsServiceImpl userDetailsService,
                          OfficeSecurityService officeSecurityService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
        this.userDetailsService = userDetailsService;
        this.officeSecurityService = officeSecurityService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationProvider authenticationProvider) throws Exception {
        http
            .csrf(csrf -> csrf
                // Los endpoints REST que operan vía token JWT y el cierre de sesión no requieren token CSRF de sesión
                .ignoringRequestMatchers(
                    "/ping",
                    "/logout",
                    "/api/auth/**",
                    "/api/admin/**",
                    "/api/v1/admin/**",
                    "/api/perfil/**",
                    "/api/mercados/**",
                    "/api/dashboard/**",
                    "/dashboard/mercados/**",
                    "/api/notificaciones/**",
                    "/api/portal/**",
                    "/api/documentos/**",
                    "/api/mesa-partes/**",
                    "/api/serenazgo/**",
                    "/api/publico/**",
                    "/api/v1/tributaria/**",
                    "/actuator/**"
                )
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            )
            .logout(logout -> logout.disable())
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)
            )
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/api/portal/comerciante/**", "/portal/comerciante/**").authenticated()
                .requestMatchers("/portal/seguridad/mis-reportes/**", "/api/portal/seguridad/mis-reportes/**").authenticated()
                .requestMatchers(SecurityConstants.PUBLIC_URLS).permitAll()
                .requestMatchers(
                    "/error",
                    "/api/auth/login",
                    "/api/auth/refresh",
                    "/api/auth/logout",
                    "/api/v1/tributaria/documentos/verificar/**",
                    "/actuator/health",
                    "/actuator/info"
                ).permitAll()
                .requestMatchers(
                    "/dashboard/mesa-partes/**"
                ).access((auth, ctx) -> new AuthorizationDecision(officeSecurityService.canAccessModule(auth.get(), "MESAPARTES")))
                .requestMatchers(
                    "/dashboard/mercados", "/dashboard/mercados/**", "/api/mercados/**", "/mercados", "/mercados/**"
                ).access((auth, ctx) -> new AuthorizationDecision(officeSecurityService.canAccessModule(auth.get(), "MERCADOS")))
                .requestMatchers(
                    "/dashboard/serenazgo", "/dashboard/serenazgo/**", "/api/serenazgo/**", "/serenazgo", "/serenazgo/**"
                ).access((auth, ctx) -> new AuthorizationDecision(officeSecurityService.canAccessModule(auth.get(), "SERENAZGO")))
                .requestMatchers(
                    "/dashboard/tributaria", "/dashboard/tributaria/**", "/api/v1/tributaria/**", "/tributaria", "/tributaria/**"
                ).access((auth, ctx) -> new AuthorizationDecision(officeSecurityService.canAccessModule(auth.get(), "TRIBUTARIA")))
                .requestMatchers(
                    "/api/auth/me", "/perfil", "/admin/perfil", "/api/perfil/**",
                    "/dashboard/documentos", "/admin/documentos", "/api/documentos/**",
                    "/api/mesa-partes/**", "/mesa-partes/nueva-solicitud", "/mesa-partes/mis-solicitudes",
                    "/mesa-partes/confirmacion/**", "/mesa-partes/solicitud/**", "/mesa-partes/expedientes/**"
                ).authenticated()
                .requestMatchers("/api/admin/**", "/admin/**", "/api/v1/admin/**")
                    .hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPERADMIN", "ADMINISTRACION_MANAGE", "PORTAL_MANAGE")
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        authProvider.setHideUserNotFoundExceptions(true);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
