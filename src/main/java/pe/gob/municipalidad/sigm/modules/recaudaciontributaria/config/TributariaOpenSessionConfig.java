package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.config;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Mantiene abierta la sesión JPA durante el renderizado de ciertas vistas de Recaudación
 * Tributaria únicamente: Recibos y Constancias (documentos), Caja Municipal (cajas),
 * Notificaciones Tributarias (notificaciones) y Arbitrios Municipales (arbitrios).
 *
 * <p>La aplicación mantiene {@code spring.jpa.open-in-view=false} de forma global. Estas
 * plantillas recorren relaciones LAZY directamente (pago -> contribuyente -> persona,
 * usuario -> persona), lo que producía {@code LazyInitializationException} y dejaba la
 * pantalla incompleta. Registrar el interceptor solo para estas rutas corrige el problema
 * sin afectar a otros módulos ni al resto de pantallas tributarias.</p>
 */
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class TributariaOpenSessionConfig implements WebMvcConfigurer {

    private final EntityManagerFactory entityManagerFactory;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        OpenEntityManagerInViewInterceptor interceptor = new OpenEntityManagerInViewInterceptor();
        interceptor.setEntityManagerFactory(entityManagerFactory);
        registry.addWebRequestInterceptor(interceptor)
                .addPathPatterns(
                        "/dashboard/tributaria/documentos", "/dashboard/tributaria/documentos/**",
                        "/dashboard/tributaria/cajas", "/dashboard/tributaria/cajas/**",
                        "/dashboard/tributaria/notificaciones", "/dashboard/tributaria/notificaciones/**",
                        "/dashboard/tributaria/arbitrios", "/dashboard/tributaria/arbitrios/**");
    }
}
