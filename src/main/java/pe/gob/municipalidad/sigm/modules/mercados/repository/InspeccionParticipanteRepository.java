package pe.gob.municipalidad.sigm.modules.mercados.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mercados.entity.InspeccionParticipante;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InspeccionParticipanteRepository extends JpaRepository<InspeccionParticipante, UUID> {

    List<InspeccionParticipante> findByInspeccionId(UUID inspeccionId);

    Optional<InspeccionParticipante> findByInspeccionIdAndUsuarioId(UUID inspeccionId, Long usuarioId);

    boolean existsByInspeccionIdAndUsuarioId(UUID inspeccionId, Long usuarioId);

    void deleteByInspeccionIdAndUsuarioId(UUID inspeccionId, Long usuarioId);
}
