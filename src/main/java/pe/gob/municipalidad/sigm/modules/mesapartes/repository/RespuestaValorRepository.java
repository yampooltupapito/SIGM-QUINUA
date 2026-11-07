package pe.gob.municipalidad.sigm.modules.mesapartes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.RespuestaValor;

import java.util.List;

@Repository
public interface RespuestaValorRepository extends JpaRepository<RespuestaValor, Long> {

    List<RespuestaValor> findByRespuestaId(Long respuestaId);
}
