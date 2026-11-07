package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.ContratoHistorial;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContratoHistorialRepository extends JpaRepository<ContratoHistorial, Long> {

    List<ContratoHistorial> findByContratoIdOrderByFechaAsc(UUID contratoId);
}
