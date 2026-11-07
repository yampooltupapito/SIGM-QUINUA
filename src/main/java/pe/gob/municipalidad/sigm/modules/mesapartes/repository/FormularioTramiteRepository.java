package pe.gob.municipalidad.sigm.modules.mesapartes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.FormularioTramite;

import java.util.Optional;

@Repository
public interface FormularioTramiteRepository extends JpaRepository<FormularioTramite, Long> {

    Optional<FormularioTramite> findByTipoTramiteIdAndActivoTrue(Long tipoTramiteId);

    Optional<FormularioTramite> findByCodigo(String codigo);
}
