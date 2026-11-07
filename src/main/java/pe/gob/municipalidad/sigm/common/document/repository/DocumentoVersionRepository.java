package pe.gob.municipalidad.sigm.common.document.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.common.document.entity.DocumentoVersion;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentoVersionRepository extends JpaRepository<DocumentoVersion, Long> {

    List<DocumentoVersion> findByDocumentoIdOrderByNumeroVersionDesc(Long documentoId);

    Optional<DocumentoVersion> findByDocumentoIdAndNumeroVersion(Long documentoId, Integer numeroVersion);

    long countByDocumentoId(Long documentoId);
}
