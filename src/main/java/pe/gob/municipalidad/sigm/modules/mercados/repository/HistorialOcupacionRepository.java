package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.HistorialOcupacion;

import java.util.List;
import java.util.UUID;

@Repository
public interface HistorialOcupacionRepository extends JpaRepository<HistorialOcupacion, Long> {

    @Query("SELECT h FROM HistorialOcupacion h WHERE h.puesto.id = :puestoId ORDER BY h.fecha DESC, h.id DESC")
    List<HistorialOcupacion> findByPuestoIdOrderByFechaDesc(@Param("puestoId") UUID puestoId);

    @Query("SELECT h FROM HistorialOcupacion h WHERE h.puesto.id = :puestoId ORDER BY h.fecha DESC, h.id DESC")
    Page<HistorialOcupacion> findByPuestoIdOrderByFechaDesc(@Param("puestoId") UUID puestoId, Pageable pageable);

    @Query("SELECT h FROM HistorialOcupacion h WHERE h.comerciante.id = :comercianteId ORDER BY h.fecha DESC, h.id DESC")
    List<HistorialOcupacion> findByComercianteIdOrderByFechaDesc(@Param("comercianteId") UUID comercianteId);

    @Query("SELECT h FROM HistorialOcupacion h WHERE h.asignacion.id = :asignacionId ORDER BY h.fecha DESC, h.id DESC")
    List<HistorialOcupacion> findByAsignacionIdOrderByFechaDesc(@Param("asignacionId") UUID asignacionId);
}
