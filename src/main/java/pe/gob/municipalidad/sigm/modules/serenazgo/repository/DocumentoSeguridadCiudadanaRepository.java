package pe.gob.municipalidad.sigm.modules.serenazgo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.serenazgo.entity.DocumentoSeguridadCiudadana;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoDocumentoSeguridad;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.ResultadoVerificacionIntegridad;
import pe.gob.municipalidad.sigm.modules.serenazgo.enums.TipoDocumentoSeguridad;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentoSeguridadCiudadanaRepository extends JpaRepository<DocumentoSeguridadCiudadana, UUID>, JpaSpecificationExecutor<DocumentoSeguridadCiudadana> {

    Optional<DocumentoSeguridadCiudadana> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<DocumentoSeguridadCiudadana> findByDocumentoPadreIdOrderByVersionActualDesc(UUID documentoPadreId);

    List<DocumentoSeguridadCiudadana> findByIncidenciaIdOrderByFechaHoraCargaDesc(UUID incidenciaId);

    List<DocumentoSeguridadCiudadana> findByAlertaIdOrderByFechaHoraCargaDesc(UUID alertaId);

    List<DocumentoSeguridadCiudadana> findByAtencionIdOrderByFechaHoraCargaDesc(UUID atencionId);

    List<DocumentoSeguridadCiudadana> findByIntervencionIdOrderByFechaHoraCargaDesc(UUID intervencionId);

    List<DocumentoSeguridadCiudadana> findByPatrullajeIdOrderByFechaHoraCargaDesc(UUID patrullajeId);

    long countByTipoDocumento(TipoDocumentoSeguridad tipoDocumento);

    long countByEstado(EstadoDocumentoSeguridad estado);

    long countByUltimoResultadoVerificacion(ResultadoVerificacionIntegridad resultado);

    @Query("SELECT MAX(d.codigo) FROM DocumentoSeguridadCiudadana d WHERE d.codigo LIKE :prefijo%")
    String findMaxCodigoConPrefijo(@Param("prefijo") String prefijo);

    /** Bandeja de supervisión: últimos 20 documentos observados o con integridad no coincidente. */
    List<DocumentoSeguridadCiudadana> findTop20ByEstadoOrUltimoResultadoVerificacionOrderByFechaHoraCargaDesc(
            pe.gob.municipalidad.sigm.modules.serenazgo.enums.EstadoDocumentoSeguridad estado, pe.gob.municipalidad.sigm.modules.serenazgo.enums.ResultadoVerificacionIntegridad resultado);
}
