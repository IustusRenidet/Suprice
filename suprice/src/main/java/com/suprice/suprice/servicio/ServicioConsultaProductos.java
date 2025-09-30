package com.suprice.suprice.servicio;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.suprice.suprice.modelo.ExistenciaDetalleDTO;
import com.suprice.suprice.modelo.PrecioProductoDTO;
import com.suprice.suprice.modelo.ProductoConsultadoDTO;
import com.suprice.suprice.modelo.SolicitudConsultaProducto;
import com.suprice.suprice.modelo.TipoSistemaAspel;
import com.suprice.suprice.util.UtilidadesImpuestos;
import com.suprice.suprice.util.UtilidadesRutas;

/**
 * Encapsula la lógica de conexión hacia las bases de datos Firebird para consultar productos.
 */
@Service
public class ServicioConsultaProductos {

        private static final Logger LOGGER = LoggerFactory.getLogger(ServicioConsultaProductos.class);

        private final String host;
        private final String puerto;
        private final String usuario;
        private final String contrasena;

        public ServicioConsultaProductos() {
                this.host = System.getenv().getOrDefault("FIREBIRD_HOST", "localhost");
                this.puerto = System.getenv().getOrDefault("FIREBIRD_PORT", "3050");
                this.usuario = System.getenv().getOrDefault("FIREBIRD_USUARIO", "SYSDBA");
                this.contrasena = System.getenv().getOrDefault("FIREBIRD_CONTRASENA", "masterkey");
        }

        public Optional<ProductoConsultadoDTO> consultarProducto(SolicitudConsultaProducto solicitud) {
                try {
                        return switch (solicitud.sistema()) {
                        case SAE -> consultarProductoSae(solicitud);
                        case CAJA -> consultarProductoCaja(solicitud);
                        };
                } catch (Exception ex) {
                        LOGGER.error("Error inesperado consultando producto {}: {}", solicitud.codigoProducto(),
                                        ex.getMessage(), ex);
                        return Optional.empty();
                }
        }

        private Optional<ProductoConsultadoDTO> consultarProductoSae(SolicitudConsultaProducto solicitud)
                        throws SQLException {
                Path rutaEmpresa = UtilidadesRutas.aPath(solicitud.rutaEmpresa());
                if (rutaEmpresa == null) {
                        return Optional.empty();
                }
                Path rutaBd = localizarBaseDatos(rutaEmpresa).orElse(null);
                if (rutaBd == null) {
                        LOGGER.warn("No se localizó base de datos Firebird en {}", rutaEmpresa);
                        return Optional.empty();
                }
                String sufijo = solicitud.sufijoTablas().toUpperCase();
                String tablaProductos = "INVE" + sufijo;
                String tablaAlternos = "CVES_ALTER" + sufijo;
                String tablaImpuestos = "IMPU" + sufijo;
                String tablaExistencias = "MULT" + sufijo;
                String tablaPrecios = "PRECIO_X_PROD" + sufijo;

                try (Connection conexion = abrirConexion(rutaBd)) {
                        ProductoBasico producto = obtenerProductoBasico(conexion, tablaProductos,
                                        solicitud.codigoProducto());
                        if (producto == null) {
                                return Optional.empty();
                        }
                        List<String> alternos = obtenerClavesAlternas(conexion, tablaAlternos, solicitud.codigoProducto());
                        EsquemaImpuestos esquema = obtenerEsquemaImpuestos(conexion, tablaImpuestos,
                                        producto.claveImpuestos());
                        List<PrecioProductoDTO> precios = obtenerPrecios(conexion, tablaPrecios, solicitud.codigoProducto(),
                                        esquema, solicitud.incluirImpuestos());
                        List<ExistenciaDetalleDTO> existencias = obtenerExistencias(conexion, tablaExistencias,
                                        solicitud.codigoProducto());
                        BigDecimal existenciaTotal = producto.existenciaTotal();
                        String imagen = cargarImagenProducto(rutaEmpresa, producto.claveImagen());
                        return Optional.of(new ProductoConsultadoDTO(producto.codigo(), producto.descripcion(), alternos,
                                        producto.claveImpuestos(), existenciaTotal, existencias, precios, imagen,
                                        solicitud.incluirImpuestos()));
                }
        }

        private Optional<ProductoConsultadoDTO> consultarProductoCaja(SolicitudConsultaProducto solicitud)
                        throws SQLException {
                Path rutaEmpresa = UtilidadesRutas.aPath(solicitud.rutaEmpresa());
                if (rutaEmpresa == null) {
                        return Optional.empty();
                }
                Path rutaBd = localizarBaseDatos(rutaEmpresa).orElse(null);
                if (rutaBd == null) {
                        LOGGER.warn("No se localizó base de datos Firebird para Aspel Caja en {}", rutaEmpresa);
                        return Optional.empty();
                }
                try (Connection conexion = abrirConexion(rutaBd)) {
                        ProductoCaja producto = obtenerProductoCaja(conexion, solicitud.codigoProducto());
                        if (producto == null) {
                                return Optional.empty();
                        }
                        EsquemaImpuestos esquema = obtenerEsquemaCaja(conexion, producto.esquemaImpuestos());
                        List<PrecioProductoDTO> precios = construirPreciosCaja(producto, esquema,
                                        solicitud.incluirImpuestos());
                        List<ExistenciaDetalleDTO> existencias = obtenerExistenciasCaja(conexion,
                                        solicitud.codigoProducto());
                        List<String> alternos = producto.alternativos();
                        String imagen = cargarImagenProducto(rutaEmpresa, producto.claveImagen());
                        return Optional.of(new ProductoConsultadoDTO(producto.codigo(), producto.descripcion(), alternos,
                                        producto.esquemaImpuestos(), producto.existenciaTotal(), existencias, precios, imagen,
                                        solicitud.incluirImpuestos()));
                }
        }

        private Optional<Path> localizarBaseDatos(Path carpetaEmpresa) {
                return UtilidadesRutas.buscarArchivoPorExtension(carpetaEmpresa, ".fdb");
        }

        private Connection abrirConexion(Path rutaBd) throws SQLException {
                String url = String.format("jdbc:firebirdsql://%s:%s%s", host, puerto,
                                UtilidadesRutas.normalizarParaConexion(rutaBd));
                Properties propiedades = new Properties();
                propiedades.setProperty("user", usuario);
                propiedades.setProperty("password", contrasena);
                propiedades.setProperty("encoding", "UTF8");
                return DriverManager.getConnection(url, propiedades);
        }

        private ProductoBasico obtenerProductoBasico(Connection conexion, String tablaProductos, String codigo)
                        throws SQLException {
                String sql = "SELECT cve_art, descr, exist, cve_esqimpu, cve_imagen FROM " + tablaProductos
                                + " WHERE cve_art = ?";
                try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                        ps.setString(1, codigo);
                        try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next()) {
                                        return new ProductoBasico(rs.getString("cve_art"), rs.getString("descr"),
                                                        rs.getBigDecimal("exist"), rs.getString("cve_esqimpu"),
                                                        rs.getString("cve_imagen"));
                                }
                        }
                }
                return null;
        }

        private List<String> obtenerClavesAlternas(Connection conexion, String tablaAlternos, String codigo)
                        throws SQLException {
                String sql = "SELECT cve_alter FROM " + tablaAlternos + " WHERE cve_art = ?";
                try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                        ps.setString(1, codigo);
                        try (ResultSet rs = ps.executeQuery()) {
                                List<String> claves = new ArrayList<>();
                                while (rs.next()) {
                                        String clave = rs.getString("cve_alter");
                                        if (clave != null && !clave.isBlank()) {
                                                claves.add(clave.trim());
                                        }
                                }
                                return claves;
                        }
                }
        }

        private EsquemaImpuestos obtenerEsquemaImpuestos(Connection conexion, String tablaImpuestos,
                        String claveImpuestos) throws SQLException {
                if (claveImpuestos == null || claveImpuestos.isBlank()) {
                        return EsquemaImpuestos.vacio();
                }
                String sql = "SELECT impuesto1, impuesto2, impuesto3, impuesto4, imp1aplica, imp2aplica, imp3aplica, imp4aplica FROM "
                                + tablaImpuestos + " WHERE cve_esqimpu = ?";
                try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                        ps.setString(1, claveImpuestos);
                        try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next()) {
                                        return new EsquemaImpuestos(
                                                        extraerBigDecimal(rs, "impuesto1"),
                                                        extraerBigDecimal(rs, "impuesto2"),
                                                        extraerBigDecimal(rs, "impuesto3"),
                                                        extraerBigDecimal(rs, "impuesto4"),
                                                        rs.getInt("imp1aplica"),
                                                        rs.getInt("imp2aplica"),
                                                        rs.getInt("imp3aplica"),
                                                        rs.getInt("imp4aplica"));
                                }
                        }
                }
                return EsquemaImpuestos.vacio();
        }

        private List<PrecioProductoDTO> obtenerPrecios(Connection conexion, String tablaPrecios, String codigo,
                        EsquemaImpuestos esquema, boolean incluirImpuestos) throws SQLException {
                String sql = "SELECT cve_precio, precio FROM " + tablaPrecios + " WHERE cve_art = ? ORDER BY cve_precio";
                List<PrecioProductoDTO> precios = new ArrayList<>();
                try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                        ps.setString(1, codigo);
                        try (ResultSet rs = ps.executeQuery()) {
                                while (rs.next()) {
                                        int lista = rs.getInt("cve_precio");
                                        BigDecimal precioBase = extraerBigDecimal(rs, "precio");
                                        BigDecimal impuesto = esquema.calcularImpuestos(precioBase);
                                        BigDecimal precioConImpuestos = precioBase.add(impuesto);
                                        precios.add(new PrecioProductoDTO(lista, precioBase,
                                                        incluirImpuestos ? precioConImpuestos : precioBase));
                                }
                        }
                }
                return precios;
        }

        private List<ExistenciaDetalleDTO> obtenerExistencias(Connection conexion, String tablaExistencias, String codigo)
                        throws SQLException {
                String sql = "SELECT cve_alm, exist FROM " + tablaExistencias + " WHERE cve_art = ? ORDER BY cve_alm";
                List<ExistenciaDetalleDTO> existencias = new ArrayList<>();
                try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                        ps.setString(1, codigo);
                        try (ResultSet rs = ps.executeQuery()) {
                                while (rs.next()) {
                                        String almacen = rs.getString("cve_alm");
                                        BigDecimal existencia = extraerBigDecimal(rs, "exist");
                                        existencias.add(new ExistenciaDetalleDTO(almacen, existencia));
                                }
                        }
                }
                return existencias;
        }

        private ProductoCaja obtenerProductoCaja(Connection conexion, String codigo) throws SQLException {
                String sql = "SELECT producto, descripcio, existencia, esqimp, clvalter1, clvalter2, clvalter3, preciop, precio2, precio3, precio4, imagen"
                                + " FROM catinven WHERE producto = ?";
                try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                        ps.setString(1, codigo);
                        try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next()) {
                                        List<String> alternos = new ArrayList<>();
                                        agregarSiNoVacio(alternos, rs.getString("clvalter1"));
                                        agregarSiNoVacio(alternos, rs.getString("clvalter2"));
                                        agregarSiNoVacio(alternos, rs.getString("clvalter3"));
                                        List<BigDecimal> precios = List.of(
                                                        extraerBigDecimal(rs, "preciop"),
                                                        extraerBigDecimal(rs, "precio2"),
                                                        extraerBigDecimal(rs, "precio3"),
                                                        extraerBigDecimal(rs, "precio4"));
                                        return new ProductoCaja(rs.getString("producto"), rs.getString("descripcio"),
                                                        extraerBigDecimal(rs, "existencia"), rs.getString("esqimp"),
                                                        precios, alternos, rs.getString("imagen"));
                                }
                        }
                }
                return null;
        }

        private EsquemaImpuestos obtenerEsquemaCaja(Connection conexion, String clave) throws SQLException {
                        if (clave == null || clave.isBlank()) {
                                return EsquemaImpuestos.vacio();
                        }
                        String sql = "SELECT porcen1, porcen2, porcen3, porcen4, aplica1, aplica2, aplica3, aplica4 FROM esqimp WHERE clave = ?";
                        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                                ps.setString(1, clave);
                                try (ResultSet rs = ps.executeQuery()) {
                                        if (rs.next()) {
                                                return new EsquemaImpuestos(
                                                                extraerBigDecimal(rs, "porcen1"),
                                                                extraerBigDecimal(rs, "porcen2"),
                                                                extraerBigDecimal(rs, "porcen3"),
                                                                extraerBigDecimal(rs, "porcen4"),
                                                                rs.getInt("aplica1"),
                                                                rs.getInt("aplica2"),
                                                                rs.getInt("aplica3"),
                                                                rs.getInt("aplica4"));
                                        }
                                }
                        }
                        return EsquemaImpuestos.vacio();
        }

        private List<PrecioProductoDTO> construirPreciosCaja(ProductoCaja producto, EsquemaImpuestos esquema,
                        boolean incluirImpuestos) {
                List<PrecioProductoDTO> lista = new ArrayList<>();
                List<BigDecimal> precios = producto.precios();
                for (int i = 0; i < precios.size(); i++) {
                        BigDecimal base = precios.get(i);
                        if (base == null) {
                                continue;
                        }
                        BigDecimal impuesto = esquema.calcularImpuestos(base);
                        BigDecimal conImpuesto = base.add(impuesto);
                        lista.add(new PrecioProductoDTO(i + 1, base, incluirImpuestos ? conImpuesto : base));
                }
                return lista;
        }

        private List<ExistenciaDetalleDTO> obtenerExistenciasCaja(Connection conexion, String codigo)
                        throws SQLException {
                String sql = "SELECT tienda, existienda FROM exist WHERE producto = ? ORDER BY tienda";
                List<ExistenciaDetalleDTO> lista = new ArrayList<>();
                try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                        ps.setString(1, codigo);
                        try (ResultSet rs = ps.executeQuery()) {
                                while (rs.next()) {
                                        lista.add(new ExistenciaDetalleDTO(rs.getString("tienda"),
                                                        extraerBigDecimal(rs, "existienda")));
                                }
                        }
                }
                return lista;
        }

        private void agregarSiNoVacio(List<String> lista, String valor) {
                if (valor != null && !valor.isBlank()) {
                        lista.add(valor.trim());
                }
        }

        private BigDecimal extraerBigDecimal(ResultSet rs, String columna) throws SQLException {
                BigDecimal valor = rs.getBigDecimal(columna);
                return valor != null ? valor : BigDecimal.ZERO;
        }

        private String cargarImagenProducto(Path rutaEmpresa, String claveImagen) {
                if (claveImagen == null || claveImagen.isBlank()) {
                        return null;
                }
                Path carpetaImagenes = rutaEmpresa.resolve("Imagenes");
                Path archivoPng = carpetaImagenes.resolve(claveImagen + ".png");
                Path archivoJpg = carpetaImagenes.resolve(claveImagen + ".jpg");
                Path archivo = Files.exists(archivoPng) ? archivoPng : Files.exists(archivoJpg) ? archivoJpg : null;
                if (archivo == null || !Files.exists(archivo)) {
                        return null;
                }
                try {
                        byte[] datos = Files.readAllBytes(archivo);
                        String extension = obtenerExtension(archivo.getFileName().toString());
                        String base64 = Base64.getEncoder().encodeToString(datos);
                        return "data:image/" + extension + ";base64," + base64;
                } catch (IOException ex) {
                        LOGGER.warn("No fue posible cargar la imagen {}: {}", archivo, ex.getMessage());
                        return null;
                }
        }

        private String obtenerExtension(String nombreArchivo) {
                int indice = nombreArchivo.lastIndexOf('.') + 1;
                if (indice <= 0 || indice >= nombreArchivo.length()) {
                        return "png";
                }
                return nombreArchivo.substring(indice);
        }

        private record ProductoBasico(String codigo, String descripcion, BigDecimal existenciaTotal, String claveImpuestos,
                        String claveImagen) {
        }

        private record ProductoCaja(String codigo, String descripcion, BigDecimal existenciaTotal, String esquemaImpuestos,
                        List<BigDecimal> precios, List<String> alternativos, String claveImagen) {
        }

        private static class EsquemaImpuestos {
                private final List<BigDecimal> porcentajes;
                private final List<Integer> reglas;

                EsquemaImpuestos(BigDecimal impuesto1, BigDecimal impuesto2, BigDecimal impuesto3, BigDecimal impuesto4,
                                Integer regla1, Integer regla2, Integer regla3, Integer regla4) {
                        this.porcentajes = List.of(
                                        impuesto1 != null ? impuesto1 : BigDecimal.ZERO,
                                        impuesto2 != null ? impuesto2 : BigDecimal.ZERO,
                                        impuesto3 != null ? impuesto3 : BigDecimal.ZERO,
                                        impuesto4 != null ? impuesto4 : BigDecimal.ZERO);
                        this.reglas = List.of(regla1 != null ? regla1 : 0, regla2 != null ? regla2 : 0,
                                        regla3 != null ? regla3 : 0, regla4 != null ? regla4 : 0);
                }

                static EsquemaImpuestos vacio() {
                        return new EsquemaImpuestos(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0, 0,
                                        0, 0);
                }

                BigDecimal calcularImpuestos(BigDecimal precioBase) {
                        return UtilidadesImpuestos.calcularTotalImpuestos(precioBase, porcentajes, reglas);
                }
        }
}
