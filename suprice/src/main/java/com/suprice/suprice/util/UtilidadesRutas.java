package com.suprice.suprice.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Conjunto de utilidades para trabajar con rutas de archivos en el sistema operativo.
 */
public final class UtilidadesRutas {

        private UtilidadesRutas() {
        }

        /**
         * Normaliza una ruta entregada por el sistema operativo para ser utilizada en JDBC.
         *
         * @param ruta ruta original.
         * @return cadena con separadores compatibles con JDBC.
         */
        public static String normalizarParaConexion(Path ruta) {
                String rutaNormalizada = ruta.toAbsolutePath().toString().replace("\\", "/");
                if (rutaNormalizada.startsWith("/")) {
                        return rutaNormalizada;
                }
                return "/" + rutaNormalizada;
        }

        /**
         * Busca recursivamente un archivo con la extensión proporcionada.
         *
         * @param carpeta   carpeta base.
         * @param extension extensión deseada, incluyendo el punto.
         * @return ruta encontrada o vacía si no existe.
         */
        public static Optional<Path> buscarArchivoPorExtension(Path carpeta, String extension) {
                if (!Files.isDirectory(carpeta)) {
                        return Optional.empty();
                }
                try {
                        return Files.walk(carpeta, 4)
                                        .filter(Files::isRegularFile)
                                        .filter(path -> path.getFileName().toString().toLowerCase().endsWith(extension.toLowerCase()))
                                        .findFirst();
                } catch (Exception ex) {
                        return Optional.empty();
                }
        }

        /**
         * Convierte una ruta textual a {@link Path} asegurando su validez.
         *
         * @param ruta cadena a convertir.
         * @return ruta como {@link Path} o {@code null} cuando no es válida.
         */
        public static Path aPath(String ruta) {
                if (ruta == null || ruta.isBlank()) {
                        return null;
                }
                return Paths.get(ruta).toAbsolutePath().normalize();
        }
}
