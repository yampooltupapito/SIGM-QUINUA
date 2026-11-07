package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.ComercianteCambioEstado;

import java.util.List;
import java.util.UUID;

@Repository
public interface ComercianteCambioEstadoRepository extends JpaRepository<ComercianteCambioEstado, Long> {

    List<ComercianteCambioEstado> findByComercianteIdOrderByFechaCambioDesc(UUID comercianteId);
}
