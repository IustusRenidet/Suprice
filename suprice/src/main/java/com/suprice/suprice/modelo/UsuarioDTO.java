package com.suprice.suprice.modelo;

/**
 * Datos expuestos hacia el frontend relativos a un usuario.
 */
public record UsuarioDTO(String nombreUsuario, RolUsuario rol) {
}
