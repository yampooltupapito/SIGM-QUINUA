package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.HistorialTitularidadPuesto;

import java.util.List;
import java.util.UUID;

@Repository
public interface HistorialTitularidadPuestoRepository extends JpaRepository<HistorialTitularidadPuesto, Long>, JpaSpecificationExecutor<HistorialTitularidadPuesto> {

    List<HistorialTitularidadPuesto> findByPuestoIdOrderByFechaEfectivaDesc(UUID puestoId);

    Page<HistorialTitularidadPuesto> findByPuestoId(UUID puestoId, Pageable pageable);

    List<HistorialTitularidadPuesto> findByTitularAnteriorIdOrTitularNuevoIdOrderByFechaEfectivaDesc(UUID titularAnteriorId, UUID titularNuevoId);
}
