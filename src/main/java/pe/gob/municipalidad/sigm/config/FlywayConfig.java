package pe.gob.municipalidad.sigm.config;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Configuración explícita y segura de Flyway para SIGM-QUINUA.
 * Garantiza que las migraciones de esquema se ejecuten ordenadamente antes
 * de que Hibernate inicialice y valide el EntityManagerFactory.
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "spring.flyway.enabled", havingValue = "true", matchIfMissing = true)
public class FlywayConfig {

    @Value("${spring.flyway.locations:classpath:db/migration}")
    private String locations;

    @Value("${spring.flyway.baseline-on-migrate:false}")
    private boolean baselineOnMigrate;

    @Bean(initMethod = "migrate")
    public Flyway flyway(DataSource dataSource) {
        log.info("Iniciando ejecución de migraciones Flyway desde: {}", locations);

        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations(locations)
                .baselineOnMigrate(baselineOnMigrate)
                .baselineVersion("0")
                .load();

        try {
            flyway.repair();
        } catch (Exception e) {
            log.warn("Flyway repair aviso: {}", e.getMessage());
        }

        log.info("Instancia de Flyway configurada para SIGM-QUINUA.");
        return flyway;
    }

    /**
     * Asegura que entityManagerFactory espere la finalización de flyway.migrate().
     */
    @Bean
    public static BeanFactoryPostProcessor entityManagerFactoryDependsOnFlyway() {
        return beanFactory -> {
            if (beanFactory.containsBeanDefinition("entityManagerFactory")) {
                BeanDefinition bd = beanFactory.getBeanDefinition("entityManagerFactory");
                String[] existingDependsOn = bd.getDependsOn();
                if (existingDependsOn == null || existingDependsOn.length == 0) {
                    bd.setDependsOn("flyway");
                } else {
                    String[] newDependsOn = new String[existingDependsOn.length + 1];
                    System.arraycopy(existingDependsOn, 0, newDependsOn, 0, existingDependsOn.length);
                    newDependsOn[existingDependsOn.length] = "flyway";
                    bd.setDependsOn(newDependsOn);
                }
            }
        };
    }
}
