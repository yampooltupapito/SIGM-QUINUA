package pe.gob.municipalidad.sigm.modules.mesapartes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.ActuacionDocumento;

import java.util.List;

@Repository
public interface ActuacionDocumentoRepository extends JpaRepository<ActuacionDocumento, Long> {

    List<ActuacionDocumento> findByActuacionId(Long actuacionId);
}
