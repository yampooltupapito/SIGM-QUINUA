package pe.gob.municipalidad.sigm.modules.mesapartes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.RespuestaFormulario;

import java.util.Optional;

@Repository
public interface RespuestaFormularioRepository extends JpaRepository<RespuestaFormulario, Long> {

    @Query("SELECT r FROM RespuestaFormulario r LEFT JOIN FETCH r.valores v LEFT JOIN FETCH v.campo WHERE r.expediente.id = :expedienteId")
    Optional<RespuestaFormulario> findByExpedienteIdWithValores(@Param("expedienteId") Long expedienteId);

    Optional<RespuestaFormulario> findByExpedienteId(Long expedienteId);
}
