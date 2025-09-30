package com.suprice.suprice.modelo;

import java.math.BigDecimal;
import java.util.List;

/**
 * Resultado completo de una consulta de producto.
 */
public record ProductoConsultadoDTO(
                String codigo,
                String descripcion,
                List<String> clavesAlternas,
                String esquemaImpuestos,
                BigDecimal existenciaTotal,
                List<ExistenciaDetalleDTO> existencias,
                List<PrecioProductoDTO> precios,
                String imagenBase64,
                boolean impuestosIncluidos) {
}
