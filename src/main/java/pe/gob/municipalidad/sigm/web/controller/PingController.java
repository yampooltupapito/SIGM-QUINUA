package pe.gob.municipalidad.sigm.web.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador técnico para comprobación de operatividad (Health Ping).
 * Endpoint público, ligero y de solo lectura que no realiza consultas a la base de datos.
 */
@RestController
public class PingController {

    public static final String PING_RESPONSE = "SIGM-QUINUA OK";

    @GetMapping(value = "/ping", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok(PING_RESPONSE);
    }
}
