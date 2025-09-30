package com.suprice.suprice.endpoint;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.suprice.suprice.modelo.EmpresaSistemaDTO;
import com.suprice.suprice.modelo.TipoSistemaAspel;
import com.suprice.suprice.modelo.VersionSistemaDTO;
import com.suprice.suprice.servicio.ServicioConfiguracionAspel;

import jakarta.servlet.http.HttpSession;

/**
 * Expone servicios REST para obtener versiones y empresas disponibles de los sistemas Aspel.
 */
@RestController
@RequestMapping("/api/configuracion")
public class ConfiguracionControlador {

        private final ServicioConfiguracionAspel servicioConfiguracionAspel;

        public ConfiguracionControlador(ServicioConfiguracionAspel servicioConfiguracionAspel) {
                this.servicioConfiguracionAspel = servicioConfiguracionAspel;
        }

        @GetMapping("/sistemas")
        public ResponseEntity<List<TipoSistemaAspel>> obtenerSistemas(HttpSession session) {
                if (AutenticacionControlador.obtenerSesion(session).isEmpty()) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                }
                return ResponseEntity.ok(List.of(TipoSistemaAspel.SAE, TipoSistemaAspel.CAJA));
        }

        @GetMapping("/versiones")
        public ResponseEntity<?> obtenerVersiones(HttpSession session, @RequestParam TipoSistemaAspel sistema) {
                if (AutenticacionControlador.obtenerSesion(session).isEmpty()) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                }
                List<VersionSistemaDTO> versiones = servicioConfiguracionAspel.listarVersiones(sistema);
                return ResponseEntity.ok(versiones);
        }

        @GetMapping("/empresas")
        public ResponseEntity<?> obtenerEmpresas(HttpSession session, @RequestParam TipoSistemaAspel sistema,
                        @RequestParam String rutaVersion) {
                if (AutenticacionControlador.obtenerSesion(session).isEmpty()) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                }
                List<EmpresaSistemaDTO> empresas = servicioConfiguracionAspel.listarEmpresas(sistema, rutaVersion);
                return ResponseEntity.ok(empresas);
        }
}
