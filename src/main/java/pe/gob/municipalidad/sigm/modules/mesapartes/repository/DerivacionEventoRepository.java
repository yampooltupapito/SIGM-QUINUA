package pe.gob.municipalidad.sigm.modules.mesapartes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.DerivacionEvento;

import java.util.List;

@Repository
public interface DerivacionEventoRepository extends JpaRepository<DerivacionEvento, Long> {

    List<DerivacionEvento> findByDerivacionIdOrderByFechaEventoAsc(Long derivacionId);
}
