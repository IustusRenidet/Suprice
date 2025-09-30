package com.suprice.suprice.servicio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.suprice.suprice.modelo.EmpresaSistemaDTO;
import com.suprice.suprice.modelo.TipoSistemaAspel;
import com.suprice.suprice.modelo.VersionSistemaDTO;
import com.suprice.suprice.util.UtilidadesRutas;

/**
 * Localiza versiones y empresas disponibles de los sistemas Aspel instalados en el equipo.
 */
@Service
public class ServicioConfiguracionAspel {

        private static final Logger LOGGER = LoggerFactory.getLogger(ServicioConfiguracionAspel.class);

        private static final Pattern SUFIJO_EMPRESA = Pattern.compile(".*?(\\d{2})$");

        private final Path rutaBase;

        private final Map<TipoSistemaAspel, List<VersionSistemaDTO>> cacheVersiones = new ConcurrentHashMap<>();
        private final Map<String, List<EmpresaSistemaDTO>> cacheEmpresas = new ConcurrentHashMap<>();

        public ServicioConfiguracionAspel() {
                String rutaAmbiente = System.getenv().getOrDefault("ASPBASE",
                                "C:\\Program Files (x86)\\Common Files\\Aspel\\Sistemas Aspel");
                this.rutaBase = Paths.get(rutaAmbiente).toAbsolutePath();
        }

        public List<VersionSistemaDTO> listarVersiones(TipoSistemaAspel sistema) {
                return cacheVersiones.computeIfAbsent(sistema, this::cargarVersiones);
        }

        public List<EmpresaSistemaDTO> listarEmpresas(TipoSistemaAspel sistema, String rutaVersion) {
                String clave = sistema.name() + "|" + rutaVersion;
                return cacheEmpresas.computeIfAbsent(clave, key -> cargarEmpresas(rutaVersion));
        }

        private List<VersionSistemaDTO> cargarVersiones(TipoSistemaAspel sistema) {
                if (!Files.isDirectory(rutaBase)) {
                        LOGGER.warn("La ruta base {} no existe o no es accesible", rutaBase);
                        return List.of();
                }
                try (Stream<Path> stream = Files.list(rutaBase)) {
                        List<VersionSistemaDTO> versiones = stream.filter(Files::isDirectory)
                                        .filter(path -> coincideSistema(path.getFileName().toString(), sistema))
                                        .sorted(Comparator.comparing(Path::getFileName).reversed())
                                        .map(path -> new VersionSistemaDTO(path.getFileName().toString(), path.toAbsolutePath().toString()))
                                        .collect(Collectors.toCollection(ArrayList::new));
                        return versiones;
                } catch (IOException ex) {
                        LOGGER.error("Error al listar versiones del sistema {}: {}", sistema, ex.getMessage());
                        return List.of();
                }
        }

        private boolean coincideSistema(String nombreCarpeta, TipoSistemaAspel sistema) {
                String mayusculas = nombreCarpeta.toUpperCase();
                return mayusculas.startsWith(sistema.name());
        }

        private List<EmpresaSistemaDTO> cargarEmpresas(String rutaVersion) {
                Path carpetaVersion = UtilidadesRutas.aPath(rutaVersion);
                if (carpetaVersion == null || !Files.isDirectory(carpetaVersion)) {
                        LOGGER.warn("La ruta de versión {} no es válida", rutaVersion);
                        return List.of();
                }
                Path carpetaEmpresas = localizarCarpetaEmpresas(carpetaVersion).orElse(carpetaVersion);
                try (Stream<Path> stream = Files.list(carpetaEmpresas)) {
                        return stream.filter(Files::isDirectory)
                                        .map(path -> new EmpresaSistemaDTO(path.getFileName().toString(),
                                                        path.toAbsolutePath().toString(), obtenerSufijo(path)))
                                        .sorted(Comparator.comparing(EmpresaSistemaDTO::nombre))
                                        .collect(Collectors.toList());
                } catch (IOException ex) {
                        LOGGER.error("No fue posible listar empresas en {}: {}", carpetaEmpresas, ex.getMessage());
                        return List.of();
                }
        }

        private Optional<Path> localizarCarpetaEmpresas(Path carpetaVersion) {
                Path empresas = carpetaVersion.resolve("Empresas");
                if (Files.isDirectory(empresas)) {
                        return Optional.of(empresas);
                }
                try (Stream<Path> stream = Files.walk(carpetaVersion, 2)) {
                        return stream.filter(Files::isDirectory)
                                        .filter(path -> path.getFileName().toString().equalsIgnoreCase("Empresas"))
                                        .findFirst();
                } catch (IOException ex) {
                        LOGGER.warn("No se localizaron carpetas de empresas en {}: {}", carpetaVersion, ex.getMessage());
                        return Optional.empty();
                }
        }

        private String obtenerSufijo(Path carpetaEmpresa) {
                String nombre = carpetaEmpresa.getFileName().toString();
                Matcher matcher = SUFIJO_EMPRESA.matcher(nombre);
                if (matcher.matches()) {
                        return matcher.group(1);
                }
                return "01";
        }

        public void limpiarCacheVersiones() {
                cacheVersiones.clear();
        }

        public void limpiarCacheEmpresas() {
                cacheEmpresas.clear();
        }
}
