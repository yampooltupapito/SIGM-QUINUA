package pe.gob.municipalidad.sigm.modules.mesapartes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.TramiteVersion;

import java.util.List;
import java.util.Optional;

@Repository
public interface TramiteVersionRepository extends JpaRepository<TramiteVersion, Long> {

    List<TramiteVersion> findByTipoTramiteIdOrderByNumeroVersionDesc(Long tipoTramiteId);

    Optional<TramiteVersion> findByTipoTramiteIdAndNumeroVersion(Long tipoTramiteId, Integer numeroVersion);

    Optional<TramiteVersion> findFirstByTipoTramiteIdOrderByNumeroVersionDesc(Long tipoTramiteId);
}
