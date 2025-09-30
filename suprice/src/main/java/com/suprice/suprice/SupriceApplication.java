package com.suprice.suprice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Aplicación principal de Suprice encargada de inicializar el contexto de Spring Boot.
 */
@SpringBootApplication
@EnableAsync
public class SupriceApplication {

        public static void main(String[] args) {
                SpringApplication.run(SupriceApplication.class, args);
        }
}
