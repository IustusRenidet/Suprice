package com.suprice.suprice.modelo;

/**
 * Representa el resultado estándar de una operación ejecutada en el backend.
 */
public record RespuestaOperacionDTO(boolean exito, String mensaje) {
}
