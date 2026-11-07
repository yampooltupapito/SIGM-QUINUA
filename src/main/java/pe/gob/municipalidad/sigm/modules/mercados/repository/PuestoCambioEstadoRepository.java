package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.PuestoCambioEstado;

import java.util.List;
import java.util.UUID;

@Repository
public interface PuestoCambioEstadoRepository extends JpaRepository<PuestoCambioEstado, Long> {

    List<PuestoCambioEstado> findByPuestoIdOrderByFechaCambioDesc(UUID puestoId);
}
