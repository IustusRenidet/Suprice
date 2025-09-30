package com.suprice.suprice.configuracion;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.data.jdbc.core.dialect.DialectResolver;
import org.springframework.data.relational.core.dialect.Dialect;
import org.sqlite.SQLiteDataSource;

/**
 * Configuración general de beans reutilizables dentro de la aplicación.
 */
@Configuration
public class ConfiguracionAplicacion {

        /**
         * Genera el origen de datos para la base SQLite que almacena los usuarios del sistema.
         *
         * @return {@link DataSource} configurado para SQLite.
         */
        @Bean
        @Primary
        public DataSource origenDatosSqlite() {
                Path ruta = Paths.get("usuarios.db").toAbsolutePath();
                SQLiteDataSource dataSource = new SQLiteDataSource();
                dataSource.setUrl("jdbc:sqlite:" + ruta);
                return dataSource;
        }

        /**
         * Bean para indicar el dialecto SQLite a Spring Data JDBC.
         * Evita el error "Cannot determine a dialect for JdbcTemplate".
         */
        @Bean
        public Dialect jdbcDialect(JdbcTemplate plantillaSqlite) {
            return DialectResolver.getDialect(plantillaSqlite);
        }

        /**
         * Proporciona una {@link JdbcTemplate} ligada al origen de datos SQLite.
         *
         * @param dataSource origen de datos primario.
         * @return plantilla JDBC lista para uso.
         */
        @Bean
        public JdbcTemplate plantillaSqlite(DataSource dataSource) {
            return new JdbcTemplate(dataSource);
        }
}
