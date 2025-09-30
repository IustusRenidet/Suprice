package com.suprice.suprice.endpoint;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.suprice.suprice.modelo.PeticionUsuarioAdmin;
import com.suprice.suprice.modelo.RespuestaOperacionDTO;
import com.suprice.suprice.modelo.UsuarioDTO;
import com.suprice.suprice.servicio.ServicioUsuarios;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

/**
 * Permite al administrador gestionar los usuarios del sistema.
 */
@RestController
@RequestMapping("/api/usuarios")
@Validated
public class UsuariosControlador {

        private final ServicioUsuarios servicioUsuarios;

        public UsuariosControlador(ServicioUsuarios servicioUsuarios) {
                this.servicioUsuarios = servicioUsuarios;
        }

        @GetMapping
        public ResponseEntity<?> listarUsuarios(HttpSession session) {
                if (!AutenticacionControlador.esAdministrador(session)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body(new RespuestaOperacionDTO(false, "No cuenta con permisos"));
                }
                List<UsuarioDTO> usuarios = servicioUsuarios.listarUsuarios();
                return ResponseEntity.ok(usuarios);
        }

        @PostMapping
        public ResponseEntity<RespuestaOperacionDTO> crearUsuario(@Valid @RequestBody PeticionUsuarioAdmin peticion,
                        HttpSession session) {
                if (!AutenticacionControlador.esAdministrador(session)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body(new RespuestaOperacionDTO(false, "No cuenta con permisos"));
                }
                RespuestaOperacionDTO respuesta = servicioUsuarios.crearUsuario(peticion);
                HttpStatus status = respuesta.exito() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST;
                return ResponseEntity.status(status).body(respuesta);
        }

        @DeleteMapping("/{nombreUsuario}")
        public ResponseEntity<RespuestaOperacionDTO> eliminarUsuario(@PathVariable String nombreUsuario,
                        HttpSession session) {
                if (!AutenticacionControlador.esAdministrador(session)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body(new RespuestaOperacionDTO(false, "No cuenta con permisos"));
                }
                RespuestaOperacionDTO respuesta = servicioUsuarios.eliminarUsuario(nombreUsuario);
                HttpStatus status = respuesta.exito() ? HttpStatus.OK : HttpStatus.NOT_FOUND;
                return ResponseEntity.status(status).body(respuesta);
        }
}
