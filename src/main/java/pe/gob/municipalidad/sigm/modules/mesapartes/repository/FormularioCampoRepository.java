package pe.gob.municipalidad.sigm.modules.mesapartes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.FormularioCampo;

import java.util.List;
import java.util.Optional;

@Repository
public interface FormularioCampoRepository extends JpaRepository<FormularioCampo, Long> {

    @Query("SELECT c FROM FormularioCampo c LEFT JOIN FETCH c.opciones WHERE c.version.id = :versionId ORDER BY c.orden ASC")
    List<FormularioCampo> findByVersionIdWithOpciones(@Param("versionId") Long versionId);

    Optional<FormularioCampo> findByVersionIdAndNombreCampo(Long versionId, String nombreCampo);
}
