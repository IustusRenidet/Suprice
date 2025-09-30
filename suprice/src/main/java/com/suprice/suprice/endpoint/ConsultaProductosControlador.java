package com.suprice.suprice.endpoint;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.suprice.suprice.modelo.RespuestaOperacionDTO;
import com.suprice.suprice.modelo.SolicitudConsultaProducto;
import com.suprice.suprice.modelo.UsuarioSesion;
import com.suprice.suprice.servicio.ServicioConsultaProductos;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

/**
 * Controlador encargado de procesar las consultas de productos.
 */
@RestController
@RequestMapping("/api/productos")
@Validated
public class ConsultaProductosControlador {

        private static final Logger LOGGER = LoggerFactory.getLogger(ConsultaProductosControlador.class);

        private final ServicioConsultaProductos servicioConsultaProductos;
        private ThreadPoolTaskExecutor ejecutor;

        public ConsultaProductosControlador(ServicioConsultaProductos servicioConsultaProductos) {
                this.servicioConsultaProductos = servicioConsultaProductos;
        }

        @PostConstruct
        public void configurarEjecutor() {
                ejecutor = new ThreadPoolTaskExecutor();
                ejecutor.setCorePoolSize(4);
                ejecutor.setMaxPoolSize(8);
                ejecutor.setQueueCapacity(20);
                ejecutor.setThreadNamePrefix("consulta-producto-");
                ejecutor.initialize();
        }

        @PostMapping("/consultar")
        public CompletableFuture<ResponseEntity<?>> consultarProducto(@Valid @RequestBody SolicitudConsultaProducto solicitud,
                        HttpSession session) {
                Optional<UsuarioSesion> sesion = AutenticacionControlador.obtenerSesion(session);
                if (sesion.isEmpty()) {
                        return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
                }
                return CompletableFuture.supplyAsync(() -> servicioConsultaProductos.consultarProducto(solicitud), ejecutor)
                                .thenApply(resultado -> resultado
                                                .<ResponseEntity<?>>map(ResponseEntity::ok)
                                                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                                                                .body(new RespuestaOperacionDTO(false,
                                                                                "Producto no localizado en la base de datos"))))
                                .exceptionally(ex -> {
                                        LOGGER.error("Error consultando producto {}: {}", solicitud.codigoProducto(),
                                                        ex.getMessage());
                                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                                                        new RespuestaOperacionDTO(false,
                                                                        "Error interno al consultar el producto"));
                                });
        }

        public Executor obtenerEjecutor() {
                return ejecutor;
        }
}
