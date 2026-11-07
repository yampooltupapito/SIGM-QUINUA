package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.TipoContrato;

import java.util.List;
import java.util.Optional;

@Repository
public interface TipoContratoRepository extends JpaRepository<TipoContrato, Long> {

    Optional<TipoContrato> findByCodigo(String codigo);

    List<TipoContrato> findByEstadoOrderByNombreAsc(String estado);
}
