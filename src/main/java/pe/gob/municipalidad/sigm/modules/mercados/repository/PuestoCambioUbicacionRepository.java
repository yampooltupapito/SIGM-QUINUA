package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.PuestoCambioUbicacion;

import java.util.List;
import java.util.UUID;

@Repository
public interface PuestoCambioUbicacionRepository extends JpaRepository<PuestoCambioUbicacion, Long> {

    List<PuestoCambioUbicacion> findByPuestoIdOrderByFechaCambioDesc(UUID puestoId);
}
