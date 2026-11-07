package pe.gob.municipalidad.sigm.modules.mesapartes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.ConsultaSeguimientoAuditoria;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.TipoConsultaSeguimiento;

import java.time.OffsetDateTime;

@Repository
public interface ConsultaSeguimientoAuditoriaRepository extends JpaRepository<ConsultaSeguimientoAuditoria, Long> {

    @Query("SELECT COUNT(c) FROM ConsultaSeguimientoAuditoria c WHERE c.ipOrigen = :ip AND c.fechaConsulta >= :desde")
    long countByIpOrigenAndFechaConsultaAfter(@Param("ip") String ip, @Param("desde") OffsetDateTime desde);

    @Query("SELECT COUNT(c) FROM ConsultaSeguimientoAuditoria c WHERE c.ipOrigen = :ip AND c.tipoConsulta = :tipo AND c.fechaConsulta >= :desde")
    long countByIpAndTipoAndFechaAfter(@Param("ip") String ip, @Param("tipo") TipoConsultaSeguimiento tipo, @Param("desde") OffsetDateTime desde);
}
