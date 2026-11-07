package pe.gob.municipalidad.sigm.modules.mesapartes.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.AsignacionExpediente;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoAsignacion;

import java.util.List;
import java.util.Optional;

@Repository
public interface AsignacionExpedienteRepository extends JpaRepository<AsignacionExpediente, Long> {

    List<AsignacionExpediente> findByExpedienteIdOrderByFechaAsignacionDesc(Long expedienteId);

    Optional<AsignacionExpediente> findFirstByExpedienteIdAndEstado(Long expedienteId, EstadoAsignacion estado);

    List<AsignacionExpediente> findByUsuarioAsignadoIdAndEstado(Long usuarioId, EstadoAsignacion estado);

    Page<AsignacionExpediente> findByUsuarioAsignadoIdAndEstado(Long usuarioId, EstadoAsignacion estado, Pageable pageable);

    @Query("SELECT COUNT(a) FROM AsignacionExpediente a WHERE a.oficina.id = :oficinaId AND a.estado = 'ACTIVO'")
    long countActivosByOficinaId(@Param("oficinaId") Long oficinaId);

    @Query("SELECT COUNT(a) FROM AsignacionExpediente a WHERE a.usuarioAsignado.id = :usuarioId AND a.estado = 'ACTIVO'")
    long countActivosByUsuarioId(@Param("usuarioId") Long usuarioId);
}
