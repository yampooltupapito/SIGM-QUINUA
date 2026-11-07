package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.ActividadComercial;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActividadComercialRepository extends JpaRepository<ActividadComercial, Long> {

    Optional<ActividadComercial> findByCodigo(String codigo);

    List<ActividadComercial> findByEstadoOrderByNombreAsc(String estado);
}
