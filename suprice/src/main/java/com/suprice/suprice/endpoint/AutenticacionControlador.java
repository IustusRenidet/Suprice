package com.suprice.suprice.endpoint;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.suprice.suprice.modelo.CredencialesInicioSesion;
import com.suprice.suprice.modelo.RespuestaOperacionDTO;
import com.suprice.suprice.modelo.RolUsuario;
import com.suprice.suprice.modelo.UsuarioDTO;
import com.suprice.suprice.modelo.UsuarioEntidad;
import com.suprice.suprice.modelo.UsuarioSesion;
import com.suprice.suprice.servicio.ServicioUsuarios;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

/**
 * Controlador REST responsable de autenticar usuarios y gestionar la sesión activa.
 */
@RestController
@RequestMapping("/api/autenticacion")
@Validated
public class AutenticacionControlador {

        public static final String SESION_USUARIO = "USUARIO_AUTENTICADO";

        private final ServicioUsuarios servicioUsuarios;

        public AutenticacionControlador(ServicioUsuarios servicioUsuarios) {
                this.servicioUsuarios = servicioUsuarios;
        }

        @PostMapping("/iniciar")
        public ResponseEntity<?> iniciarSesion(@Valid @RequestBody CredencialesInicioSesion credenciales,
                        HttpSession session) {
                Optional<UsuarioEntidad> usuarioOpt = servicioUsuarios.buscarUsuario(credenciales.nombreUsuario());
                if (usuarioOpt.isEmpty()) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(new RespuestaOperacionDTO(false, "Credenciales incorrectas"));
                }
                UsuarioEntidad usuario = usuarioOpt.get();
                if (!servicioUsuarios.validarCredenciales(credenciales.nombreUsuario(), credenciales.contrasena())) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(new RespuestaOperacionDTO(false, "Credenciales incorrectas"));
                }
                UsuarioSesion sesion = new UsuarioSesion(usuario.nombreUsuario(), usuario.rol());
                session.setAttribute(SESION_USUARIO, sesion);
                return ResponseEntity.ok(new UsuarioDTO(usuario.nombreUsuario(), usuario.rol()));
        }

        @PostMapping("/cerrar")
        public ResponseEntity<RespuestaOperacionDTO> cerrarSesion(HttpSession session) {
                session.invalidate();
                return ResponseEntity.ok(new RespuestaOperacionDTO(true, "Sesión cerrada"));
        }

        @GetMapping("/usuario-actual")
        public ResponseEntity<?> obtenerUsuarioActual(HttpSession session) {
                UsuarioSesion sesion = (UsuarioSesion) session.getAttribute(SESION_USUARIO);
                if (sesion == null) {
                        return ResponseEntity.ok().body(null);
                }
                return ResponseEntity.ok(new UsuarioDTO(sesion.getNombreUsuario(), sesion.getRol()));
        }

        public static boolean esAdministrador(HttpSession session) {
                UsuarioSesion sesion = (UsuarioSesion) session.getAttribute(SESION_USUARIO);
                return sesion != null && sesion.getRol() == RolUsuario.ADMINISTRADOR;
        }

        public static Optional<UsuarioSesion> obtenerSesion(HttpSession session) {
                return Optional.ofNullable((UsuarioSesion) session.getAttribute(SESION_USUARIO));
        }
}
