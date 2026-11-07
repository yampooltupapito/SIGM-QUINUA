package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.TipoPuestoComercial;

import java.util.List;
import java.util.Optional;

@Repository
public interface TipoPuestoComercialRepository extends JpaRepository<TipoPuestoComercial, Long> {

    Optional<TipoPuestoComercial> findByCodigo(String codigo);

    List<TipoPuestoComercial> findByEstado(String estado);

    List<TipoPuestoComercial> findByEstadoOrderByNombreAsc(String estado);
}
