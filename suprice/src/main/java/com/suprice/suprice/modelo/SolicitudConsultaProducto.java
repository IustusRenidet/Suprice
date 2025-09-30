package com.suprice.suprice.modelo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Representa los datos necesarios para realizar la consulta de un producto.
 */
public record SolicitudConsultaProducto(
                @NotNull TipoSistemaAspel sistema,
                @NotBlank String rutaVersion,
                @NotBlank String rutaEmpresa,
                @NotBlank String sufijoTablas,
                @NotBlank String codigoProducto,
                boolean incluirImpuestos) {
}
