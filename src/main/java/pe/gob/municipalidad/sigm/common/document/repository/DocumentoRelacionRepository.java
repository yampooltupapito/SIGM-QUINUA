package pe.gob.municipalidad.sigm.common.document.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.common.document.entity.DocumentoRelacion;

import java.util.List;

@Repository
public interface DocumentoRelacionRepository extends JpaRepository<DocumentoRelacion, Long> {

    List<DocumentoRelacion> findByDocumentoId(Long documentoId);

    List<DocumentoRelacion> findByModuloAndTipoEntidadAndEntidadId(String modulo, String tipoEntidad, String entidadId);
}
