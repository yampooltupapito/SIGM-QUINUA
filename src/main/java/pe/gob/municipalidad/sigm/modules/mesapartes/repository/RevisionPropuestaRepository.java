package pe.gob.municipalidad.sigm.modules.mesapartes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.RevisionPropuesta;

import java.util.List;

@Repository
public interface RevisionPropuestaRepository extends JpaRepository<RevisionPropuesta, Long> {

    List<RevisionPropuesta> findByPropuestaIdOrderByFechaRevisionDesc(Long propuestaId);
}
