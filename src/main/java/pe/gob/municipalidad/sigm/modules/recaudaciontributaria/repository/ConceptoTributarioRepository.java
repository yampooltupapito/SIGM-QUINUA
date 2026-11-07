package pe.gob.municipalidad.sigm.modules.recaudaciontributaria.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.entity.ConceptoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.EstadoConceptoTributario;
import pe.gob.municipalidad.sigm.modules.recaudaciontributaria.enums.TipoConceptoTributario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConceptoTributarioRepository extends JpaRepository<ConceptoTributario, UUID>, JpaSpecificationExecutor<ConceptoTributario> {

    Optional<ConceptoTributario> findByCodigo(String codigo);

    Optional<ConceptoTributario> findByCodigoIgnoreCase(String codigo);

    List<ConceptoTributario> findByTipoConcepto(TipoConceptoTributario tipoConcepto);

    List<ConceptoTributario> findByEstado(EstadoConceptoTributario estado);

    Page<ConceptoTributario> findAllByOrderByCodigoAsc(Pageable pageable);

    boolean existsByCodigo(String codigo);

    boolean existsByCodigoIgnoreCase(String codigo);

    long countByEstado(EstadoConceptoTributario estado);
}
