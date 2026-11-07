package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.PatrullajeHistorial;

import java.util.List;
import java.util.UUID;

@Repository
public interface PatrullajeHistorialRepository extends JpaRepository<PatrullajeHistorial, UUID> {

    List<PatrullajeHistorial> findByPatrullajeIdOrderByFechaDesc(UUID patrullajeId);
}
