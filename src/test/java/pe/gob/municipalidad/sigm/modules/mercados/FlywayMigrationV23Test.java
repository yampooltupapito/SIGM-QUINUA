package pe.gob.municipalidad.sigm.modules.mercados;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class FlywayMigrationV23Test {

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUser;

    @Value("${spring.datasource.password}")
    private String datasourcePassword;

    @Test
    @DisplayName("22. Verificar Flyway: Aplicación exitosa de la migración V23 en PostgreSQL")
    void testFlywayMigrationV23Execution() {
        Flyway flyway = Flyway.configure()
                .dataSource(datasourceUrl, datasourceUser, datasourcePassword)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load();

        var result = flyway.migrate();
        assertThat(result.success).isTrue();
        assertThat(Integer.parseInt(flyway.info().current().getVersion().getVersion())).isGreaterThanOrEqualTo(23);
    }
}
