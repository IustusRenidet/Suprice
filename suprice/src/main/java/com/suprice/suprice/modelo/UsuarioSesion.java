package com.suprice.suprice.modelo;

import java.io.Serializable;

/**
 * Representa los datos básicos del usuario autenticado y almacenados en sesión.
 */
public class UsuarioSesion implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String nombreUsuario;
        private final RolUsuario rol;

        public UsuarioSesion(String nombreUsuario, RolUsuario rol) {
                this.nombreUsuario = nombreUsuario;
                this.rol = rol;
        }

        public String getNombreUsuario() {
                return nombreUsuario;
        }

        public RolUsuario getRol() {
                return rol;
        }
}
