package com.suprice.suprice.modelo;

/**
 * Representa los datos almacenados en la base SQLite para un usuario.
 */
public record UsuarioEntidad(String nombreUsuario, String contrasena, RolUsuario rol) {
}
