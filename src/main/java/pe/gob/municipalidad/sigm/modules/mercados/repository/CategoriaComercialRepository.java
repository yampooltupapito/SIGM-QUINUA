package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.CategoriaComercial;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaComercialRepository extends JpaRepository<CategoriaComercial, Long> {

    List<CategoriaComercial> findByEstadoOrderByOrdenVisualAsc(String estado);

    Optional<CategoriaComercial> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);
}
