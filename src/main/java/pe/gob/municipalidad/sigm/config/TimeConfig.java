package pe.gob.municipalidad.sigm.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;
import java.util.TimeZone;

/**
 * Configuración de Zona Horaria institucional para SIGM-QUINUA.
 * Garantiza que la aplicación, logs y transacciones operen en la zona horaria oficial del Perú (America/Lima, UTC-5).
 * Expone un bean Clock inyectable para facilitar pruebas unitarias reproducibles.
 */
@Configuration(proxyBeanMethods = false)
public class TimeConfig {

    public static final String TIMEZONE_PERU = "America/Lima";
    public static final ZoneId ZONE_ID_PERU = ZoneId.of(TIMEZONE_PERU);

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZONE_ID_PERU));
    }

    @Bean
    public Clock clock() {
        return Clock.system(ZONE_ID_PERU);
    }
}
