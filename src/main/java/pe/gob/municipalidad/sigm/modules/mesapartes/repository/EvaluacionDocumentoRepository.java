package pe.gob.municipalidad.sigm.modules.mesapartes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.EvaluacionDocumento;

import java.util.List;

@Repository
public interface EvaluacionDocumentoRepository extends JpaRepository<EvaluacionDocumento, Long> {

    List<EvaluacionDocumento> findByEvaluacionId(Long evaluacionId);
}
