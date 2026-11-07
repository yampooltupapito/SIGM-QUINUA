package pe.gob.municipalidad.sigm.modules.administracion;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class FlywayMigrationV66Test {

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUser;

    @Value("${spring.datasource.password}")
    private String datasourcePassword;

    @Test
    @DisplayName("Migración V66: Consolidación oficial de los 4 servicios, roles, permisos e índices")
    void testFlywayMigrationV66Execution() throws Exception {
        Flyway flyway = Flyway.configure()
                .dataSource(datasourceUrl, datasourceUser, datasourcePassword)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load();

        var result = flyway.migrate();
        assertThat(result.success).isTrue();
        assertThat(Integer.parseInt(flyway.info().current().getVersion().getVersion())).isGreaterThanOrEqualTo(66);

        // Validar en base de datos
        try (Connection conn = DriverManager.getConnection(datasourceUrl, datasourceUser, datasourcePassword);
             Statement st = conn.createStatement()) {

            // Validar que MERCADOS_MUNICIPALES existe y está ACTIVO
            try (ResultSet rs = st.executeQuery("SELECT estado, orden FROM modulos_sistema WHERE codigo = 'MERCADOS_MUNICIPALES'")) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString("estado")).isEqualTo("ACTIVO");
                assertThat(rs.getInt("orden")).isEqualTo(2);
            }

            // Validar que los 151 permisos de mercados fueron reasignados al módulo oficial
            try (ResultSet rs = st.executeQuery(
                    "SELECT count(*) FROM permisos p " +
                    "JOIN modulos_sistema m ON p.modulo_id = m.id " +
                    "WHERE m.codigo = 'MERCADOS_MUNICIPALES'")) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getLong(1)).isGreaterThanOrEqualTo(151L);
            }

            // Validar que los módulos descartados quedaron INACTIVOS
            try (ResultSet rs = st.executeQuery(
                    "SELECT count(*) FROM modulos_sistema " +
                    "WHERE codigo IN ('CATASTRO_URBANO', 'REGISTRO_CIVIL', 'SERVICIOS_PUBLICOS', 'PROGRAMAS_SOCIALES', 'DESARROLLO_ECONOMICO', 'FISCALIZACION_CONTROL') " +
                    "AND estado = 'INACTIVO'")) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getLong(1)).isEqualTo(6L);
            }

            // Validar que ROLE_FISCALIZADOR está INACTIVO
            try (ResultSet rs = st.executeQuery("SELECT estado FROM roles WHERE codigo = 'ROLE_FISCALIZADOR'")) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString("estado")).isEqualTo("INACTIVO");
            }
        }
    }
}

