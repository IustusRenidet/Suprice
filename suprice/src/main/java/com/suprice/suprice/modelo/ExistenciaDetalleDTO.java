package com.suprice.suprice.modelo;

import java.math.BigDecimal;

/**
 * Indica la existencia de un producto en un almacén o tienda específica.
 */
public record ExistenciaDetalleDTO(String almacen, BigDecimal existencia) {
}
