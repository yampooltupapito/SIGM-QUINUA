package pe.gob.municipalidad.sigm.modules.mesapartes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.SubsanacionExpediente;

import java.util.List;

@Repository
public interface SubsanacionExpedienteRepository extends JpaRepository<SubsanacionExpediente, Long> {

    List<SubsanacionExpediente> findByExpedienteIdOrderByFechaSubsanacionDesc(Long expedienteId);

    List<SubsanacionExpediente> findByObservacionId(Long observacionId);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(s) FROM SubsanacionExpediente s WHERE " +
           "(:oficinaId IS NULL OR s.expediente.oficinaResponsable.id = :oficinaId) AND " +
           "s.fechaSubsanacion >= :fechaDesde AND s.fechaSubsanacion <= :fechaHasta")
    long countSubsanaciones(
            @org.springframework.data.repository.query.Param("oficinaId") Long oficinaId,
            @org.springframework.data.repository.query.Param("fechaDesde") java.time.OffsetDateTime fechaDesde,
            @org.springframework.data.repository.query.Param("fechaHasta") java.time.OffsetDateTime fechaHasta
    );
}
