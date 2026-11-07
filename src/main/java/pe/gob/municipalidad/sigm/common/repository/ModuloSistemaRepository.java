package pe.gob.municipalidad.sigm.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.common.model.ModuloSistema;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModuloSistemaRepository extends JpaRepository<ModuloSistema, Long> {

    Optional<ModuloSistema> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<ModuloSistema> findByEstadoOrderByOrdenAsc(String estado);
}
