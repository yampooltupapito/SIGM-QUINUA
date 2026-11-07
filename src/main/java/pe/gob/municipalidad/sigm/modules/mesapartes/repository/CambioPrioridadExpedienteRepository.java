package pe.gob.municipalidad.sigm.modules.mesapartes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.CambioPrioridadExpediente;

import java.util.List;

@Repository
public interface CambioPrioridadExpedienteRepository extends JpaRepository<CambioPrioridadExpediente, Long> {

    @Query("SELECT c FROM CambioPrioridadExpediente c " +
           "JOIN FETCH c.usuario u " +
           "WHERE c.expediente.id = :expedienteId " +
           "ORDER BY c.fechaCambio DESC")
    List<CambioPrioridadExpediente> findByExpedienteIdOrderByFechaCambioDesc(@Param("expedienteId") Long expedienteId);
}
