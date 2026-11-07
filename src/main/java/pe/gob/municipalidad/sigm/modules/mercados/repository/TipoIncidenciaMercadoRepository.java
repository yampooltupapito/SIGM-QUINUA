package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.TipoIncidenciaMercado;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TipoIncidenciaMercadoRepository extends JpaRepository<TipoIncidenciaMercado, UUID> {

    Optional<TipoIncidenciaMercado> findByCodigo(String codigo);

    List<TipoIncidenciaMercado> findByActivoTrueOrderByNombreAsc();
}
