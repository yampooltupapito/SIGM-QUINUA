package pe.gob.municipalidad.sigm.modules.mesapartes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.ObservacionExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoObservacion;

import java.util.List;
import java.util.Optional;

@Repository
public interface ObservacionExpedienteRepository extends JpaRepository<ObservacionExpediente, Long> {

    List<ObservacionExpediente> findByExpedienteIdOrderByFechaRegistroDesc(Long expedienteId);

    List<ObservacionExpediente> findByExpedienteIdAndVisibilidadOrderByFechaRegistroDesc(Long expedienteId, pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.VisibilidadObservacion visibilidad);

    List<ObservacionExpediente> findByExpedienteIdAndEstado(Long expedienteId, EstadoObservacion estado);

    Optional<ObservacionExpediente> findTopByExpedienteIdAndEstadoOrderByFechaRegistroDesc(Long expedienteId, EstadoObservacion estado);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(o) FROM ObservacionExpediente o WHERE " +
           "(:oficinaId IS NULL OR o.expediente.oficinaResponsable.id = :oficinaId) AND " +
           "o.fechaRegistro >= :fechaDesde AND o.fechaRegistro <= :fechaHasta")
    long countTotalObservaciones(
            @org.springframework.data.repository.query.Param("oficinaId") Long oficinaId,
            @org.springframework.data.repository.query.Param("fechaDesde") java.time.OffsetDateTime fechaDesde,
            @org.springframework.data.repository.query.Param("fechaHasta") java.time.OffsetDateTime fechaHasta
    );

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(DISTINCT o.expediente.id) FROM ObservacionExpediente o WHERE " +
           "(:oficinaId IS NULL OR o.expediente.oficinaResponsable.id = :oficinaId) AND " +
           "o.fechaRegistro >= :fechaDesde AND o.fechaRegistro <= :fechaHasta")
    long countExpedientesObservados(
            @org.springframework.data.repository.query.Param("oficinaId") Long oficinaId,
            @org.springframework.data.repository.query.Param("fechaDesde") java.time.OffsetDateTime fechaDesde,
            @org.springframework.data.repository.query.Param("fechaHasta") java.time.OffsetDateTime fechaHasta
    );
}
