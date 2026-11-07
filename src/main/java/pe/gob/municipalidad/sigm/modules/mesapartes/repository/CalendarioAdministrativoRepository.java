package pe.gob.municipalidad.sigm.modules.mesapartes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.municipalidad.sigm.modules.mesapartes.entity.CalendarioAdministrativo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CalendarioAdministrativoRepository extends JpaRepository<CalendarioAdministrativo, Long> {

    Optional<CalendarioAdministrativo> findByFecha(LocalDate fecha);

    boolean existsByFecha(LocalDate fecha);

    List<CalendarioAdministrativo> findByAnioOrderByFechaAsc(Integer anio);

    List<CalendarioAdministrativo> findByActivoTrueOrderByFechaAsc();

    List<CalendarioAdministrativo> findByFechaBetweenAndActivoTrue(LocalDate fechaInicio, LocalDate fechaFin);
}
