package pe.gob.municipalidad.sigm.modules.serenazgo.config;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Mantiene abierta la sesión JPA durante el renderizado de las vistas del módulo
 * Seguridad Ciudadana / Serenazgo únicamente.
 *
 * <p>La aplicación mantiene {@code spring.jpa.open-in-view=false} de forma global,
 * pero las plantillas Thymeleaf de Serenazgo recorren relaciones LAZY de las entidades
 * (persona, sector, tipo de incidencia, etc.). Sin sesión abierta esas pantallas fallaban
 * con {@code LazyInitializationException}. Registrar el interceptor solo para las rutas
 * de este módulo corrige el problema sin afectar al resto de módulos.</p>
 */
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class SerenazgoOpenSessionConfig implements WebMvcConfigurer {

    private final EntityManagerFactory entityManagerFactory;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        OpenEntityManagerInViewInterceptor interceptor = new OpenEntityManagerInViewInterceptor();
        interceptor.setEntityManagerFactory(entityManagerFactory);
        registry.addWebRequestInterceptor(interceptor)
                .addPathPatterns("/dashboard/serenazgo", "/dashboard/serenazgo/**");
    }
}
