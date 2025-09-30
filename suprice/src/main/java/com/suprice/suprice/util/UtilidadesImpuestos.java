package com.suprice.suprice.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilidades para el cálculo de impuestos compuestos.
 */
public final class UtilidadesImpuestos {

        private UtilidadesImpuestos() {
        }

        /**
         * Calcula los impuestos acumulados sobre un precio.
         *
         * @param precioBase  precio base.
         * @param porcentajes porcentajes de impuestos.
         * @param reglas      reglas de aplicación (0 = base, 1 = base + impuesto1, etc.).
         * @return suma total de impuestos.
         */
        public static BigDecimal calcularTotalImpuestos(BigDecimal precioBase, List<BigDecimal> porcentajes,
                        List<Integer> reglas) {
                if (precioBase == null || porcentajes == null || reglas == null) {
                        return BigDecimal.ZERO;
                }
                BigDecimal base = precioBase.setScale(4, RoundingMode.HALF_UP);
                List<BigDecimal> acumulados = new ArrayList<>();
                BigDecimal total = BigDecimal.ZERO;
                for (int i = 0; i < porcentajes.size(); i++) {
                        BigDecimal porcentaje = porcentajes.get(i);
                        if (porcentaje == null) {
                                porcentaje = BigDecimal.ZERO;
                        }
                        if (BigDecimal.ZERO.compareTo(porcentaje) == 0) {
                                acumulados.add(BigDecimal.ZERO);
                                continue;
                        }
                        BigDecimal baseAplicacion = base;
                        int regla = reglas.size() > i ? reglas.get(i) : 0;
                        if (regla > 0) {
                                for (int j = 0; j < regla && j < acumulados.size(); j++) {
                                        baseAplicacion = baseAplicacion.add(acumulados.get(j));
                                }
                        }
                        BigDecimal impuesto = baseAplicacion.multiply(porcentaje)
                                        .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                        acumulados.add(impuesto);
                        total = total.add(impuesto);
                }
                return total.setScale(4, RoundingMode.HALF_UP);
        }
}
