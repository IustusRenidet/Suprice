package com.suprice.suprice.modelo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Datos requeridos para crear un usuario desde el panel administrativo.
 */
public record PeticionUsuarioAdmin(
                @NotBlank String nombreUsuario,
                @NotBlank String contrasena,
                @NotNull RolUsuario rol) {
}
