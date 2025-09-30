package com.suprice.suprice.servicio;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.suprice.suprice.modelo.PeticionUsuarioAdmin;
import com.suprice.suprice.modelo.RespuestaOperacionDTO;
import com.suprice.suprice.modelo.RolUsuario;
import com.suprice.suprice.modelo.UsuarioDTO;
import com.suprice.suprice.modelo.UsuarioEntidad;

import jakarta.annotation.PostConstruct;

/**
 * Gestiona la persistencia de usuarios en SQLite.
 */
@Service
public class ServicioUsuarios {

        private static final Logger LOGGER = LoggerFactory.getLogger(ServicioUsuarios.class);

        private final JdbcTemplate jdbcTemplate;

        private final RowMapper<UsuarioEntidad> mapeadorUsuarios = this::mapearUsuario;

        public ServicioUsuarios(JdbcTemplate jdbcTemplate) {
                this.jdbcTemplate = jdbcTemplate;
        }

        @PostConstruct
        public void inicializar() {
                jdbcTemplate.execute(
                                "CREATE TABLE IF NOT EXISTS usuarios (nombre_usuario TEXT PRIMARY KEY, contrasena TEXT NOT NULL, rol TEXT NOT NULL)");
                crearAdminPorDefecto();
        }

        private void crearAdminPorDefecto() {
                Optional<UsuarioEntidad> admin = buscarUsuario("admin");
                if (admin.isPresent()) {
                        return;
                }
                String hash = BCrypt.hashpw("]mYMI&Rep711", BCrypt.gensalt(10));
                jdbcTemplate.update("INSERT INTO usuarios(nombre_usuario, contrasena, rol) VALUES(?,?,?)", "admin", hash,
                                RolUsuario.ADMINISTRADOR.name());
                LOGGER.info("Usuario administrador inicial creado en {}", LocalDateTime.now());
        }

        public Optional<UsuarioEntidad> buscarUsuario(String nombreUsuario) {
                try {
                        return jdbcTemplate.query("SELECT nombre_usuario, contrasena, rol FROM usuarios WHERE nombre_usuario = ?",
                                        mapeadorUsuarios, nombreUsuario).stream().findFirst();
                } catch (Exception ex) {
                        LOGGER.error("Error al buscar usuario {}: {}", nombreUsuario, ex.getMessage());
                        return Optional.empty();
                }
        }

        public boolean validarCredenciales(String nombreUsuario, String contrasenaPlano) {
                Optional<UsuarioEntidad> usuarioOpt = buscarUsuario(nombreUsuario);
                if (usuarioOpt.isEmpty()) {
                        return false;
                }
                UsuarioEntidad usuario = usuarioOpt.get();
                return BCrypt.checkpw(contrasenaPlano, usuario.contrasena());
        }

        public List<UsuarioDTO> listarUsuarios() {
                return jdbcTemplate
                                .query("SELECT nombre_usuario, contrasena, rol FROM usuarios ORDER BY nombre_usuario",
                                                mapeadorUsuarios)
                                .stream().map(this::convertirADTO).toList();
        }

        @Transactional
        public RespuestaOperacionDTO crearUsuario(PeticionUsuarioAdmin peticion) {
                if (peticion.nombreUsuario().equalsIgnoreCase("admin")) {
                        return new RespuestaOperacionDTO(false, "El usuario admin no puede ser reemplazado");
                }
                if (buscarUsuario(peticion.nombreUsuario()).isPresent()) {
                        return new RespuestaOperacionDTO(false, "El usuario ya existe");
                }
                String hash = BCrypt.hashpw(peticion.contrasena(), BCrypt.gensalt(10));
                int filas = jdbcTemplate.update(
                                "INSERT INTO usuarios(nombre_usuario, contrasena, rol) VALUES(?,?,?)",
                                peticion.nombreUsuario(), hash, peticion.rol().name());
                return new RespuestaOperacionDTO(filas > 0,
                                filas > 0 ? "Usuario creado" : "No fue posible crear el usuario");
        }

        @Transactional
        public RespuestaOperacionDTO eliminarUsuario(String nombreUsuario) {
                if (nombreUsuario.equalsIgnoreCase("admin")) {
                        return new RespuestaOperacionDTO(false, "El usuario administrador no puede eliminarse");
                }
                int filas = jdbcTemplate.update("DELETE FROM usuarios WHERE nombre_usuario = ?", nombreUsuario);
                return new RespuestaOperacionDTO(filas > 0, filas > 0 ? "Usuario eliminado" : "Usuario no encontrado");
        }

        private UsuarioDTO convertirADTO(UsuarioEntidad entidad) {
                return new UsuarioDTO(entidad.nombreUsuario(), entidad.rol());
        }

        private UsuarioEntidad mapearUsuario(ResultSet rs, int rowNum) throws SQLException {
                RolUsuario rol = RolUsuario.valueOf(rs.getString("rol"));
                return new UsuarioEntidad(rs.getString("nombre_usuario"), rs.getString("contrasena"), rol);
        }
}
