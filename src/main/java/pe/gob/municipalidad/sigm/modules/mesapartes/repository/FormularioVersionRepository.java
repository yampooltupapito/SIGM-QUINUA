package pe.gob.municipalidad.sigm.modules.mesapartes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.FormularioVersion;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.enums.EstadoFormularioVersion;

import java.util.Optional;

@Repository
public interface FormularioVersionRepository extends JpaRepository<FormularioVersion, Long> {

    Optional<FormularioVersion> findByFormularioIdAndVersion(Long formularioId, Integer version);

    Optional<FormularioVersion> findFirstByFormularioIdAndEstadoOrderByVersionDesc(Long formularioId, EstadoFormularioVersion estado);

    Optional<FormularioVersion> findFirstByFormularioIdOrderByVersionDesc(Long formularioId);
}
