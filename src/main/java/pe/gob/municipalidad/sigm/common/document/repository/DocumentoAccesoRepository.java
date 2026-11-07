package pe.gob.municipalidad.sigm.common.document.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.common.document.entity.DocumentoAcceso;

import java.util.List;

@Repository
public interface DocumentoAccesoRepository extends JpaRepository<DocumentoAcceso, Long> {

    List<DocumentoAcceso> findByDocumentoIdOrderByFechaAccesoDesc(Long documentoId);
}
