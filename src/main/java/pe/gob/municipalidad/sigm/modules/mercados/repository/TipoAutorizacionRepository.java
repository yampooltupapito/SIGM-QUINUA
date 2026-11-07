package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.TipoAutorizacion;

import java.util.List;
import java.util.Optional;

@Repository
public interface TipoAutorizacionRepository extends JpaRepository<TipoAutorizacion, Long> {

    Optional<TipoAutorizacion> findByCodigo(String codigo);

    List<TipoAutorizacion> findByEstadoOrderByNombreAsc(String estado);
}
