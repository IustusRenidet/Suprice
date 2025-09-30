package com.suprice.suprice.modelo;

import jakarta.validation.constraints.NotBlank;

/**
 * Representa las credenciales utilizadas para el inicio de sesión.
 */
public record CredencialesInicioSesion(
                @NotBlank(message = "El nombre de usuario es obligatorio") String nombreUsuario,
                @NotBlank(message = "La contraseña es obligatoria") String contrasena) {
}
