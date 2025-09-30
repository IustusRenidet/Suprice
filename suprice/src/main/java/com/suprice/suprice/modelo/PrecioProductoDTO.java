package com.suprice.suprice.modelo;

import java.math.BigDecimal;

/**
 * Representa un precio por lista del producto consultado.
 */
public record PrecioProductoDTO(int lista, BigDecimal precioSinImpuestos, BigDecimal precioConImpuestos) {
}
