package pe.gob.municipalidad.sigm.common.document.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.common.document.entity.DocumentoVerificacion;

import java.util.List;

@Repository
public interface DocumentoVerificacionRepository extends JpaRepository<DocumentoVerificacion, Long> {

    List<DocumentoVerificacion> findByDocumentoIdOrderByFechaVerificacionDesc(Long documentoId);
}
