package pe.gob.municipalidad.sigm.modules.mesapartes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.ActuacionExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.VisibilidadObservacion;

import java.util.List;

@Repository
public interface ActuacionExpedienteRepository extends JpaRepository<ActuacionExpediente, Long> {

    List<ActuacionExpediente> findByExpedienteIdOrderByFechaActuacionDesc(Long expedienteId);

    List<ActuacionExpediente> findByExpedienteIdAndVisibilidadOrderByFechaActuacionDesc(Long expedienteId, VisibilidadObservacion visibilidad);

    long countByExpedienteId(Long expedienteId);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(a) FROM ActuacionExpediente a WHERE " +
           "(:oficinaId IS NULL OR a.oficina.id = :oficinaId) AND " +
           "a.fechaActuacion >= :fechaDesde AND a.fechaActuacion <= :fechaHasta")
    long countActuaciones(
            @org.springframework.data.repository.query.Param("oficinaId") Long oficinaId,
            @org.springframework.data.repository.query.Param("fechaDesde") java.time.OffsetDateTime fechaDesde,
            @org.springframework.data.repository.query.Param("fechaHasta") java.time.OffsetDateTime fechaHasta
    );
}
