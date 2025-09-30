package com.suprice.suprice.configuracion;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configura la seguridad de la aplicación utilizando sesiones y reglas específicas para las rutas.
 */
@Configuration
@EnableWebSecurity
public class ConfiguracionSeguridad {

        private static final String[] RECURSOS_PUBLICOS = {
                        "/", "/index.html", "/manifest.webmanifest", "/sw.js", "/offline.html",
                        "/VAADIN/**", "/frontend/**", "/images/**", "/icons/**", "/line-awesome/**", "/favicon.ico" };

        @Bean
        public SecurityFilterChain cadenaFiltros(HttpSecurity http) throws Exception {
                http.csrf(csrf -> csrf.disable());
                http.cors(Customizer.withDefaults());
                http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
                http.authorizeHttpRequests(autorizacion -> autorizacion
                                .requestMatchers(RECURSOS_PUBLICOS).permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/autenticacion/iniciar").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/autenticacion/cerrar").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/autenticacion/usuario-actual").permitAll()
                                .anyRequest().authenticated());
                http.httpBasic(basic -> basic.disable());
                http.formLogin(form -> form.disable());
                http.logout(cierre -> cierre.logoutUrl("/api/autenticacion/cerrar").deleteCookies("JSESSIONID"));
                return http.build();
        }

        @Bean
        public PasswordEncoder codificadorContrasenas() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public CorsConfigurationSource origenCors() {
                CorsConfiguration configuracion = new CorsConfiguration();
                configuracion.setAllowedOrigins(List.of("http://localhost:8080", "http://127.0.0.1:8080", "http://localhost:5173"));
                configuracion.setAllowedMethods(List.of("GET", "POST", "DELETE", "PUT", "OPTIONS"));
                configuracion.setAllowedHeaders(List.of("*"));
                configuracion.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuracion);
                return source;
        }
}
