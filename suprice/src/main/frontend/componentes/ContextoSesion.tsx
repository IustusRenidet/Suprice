import { createContext } from 'react';

export type RolUsuario = 'ADMINISTRADOR' | 'CONSULTA';

export interface UsuarioSesion {
  nombreUsuario: string;
  rol: RolUsuario;
}

export interface EstadoSesion {
  usuario?: UsuarioSesion;
  actualizarUsuario: (usuario?: UsuarioSesion) => void;
}

export const SesionContexto = createContext<EstadoSesion>({
  actualizarUsuario: () => undefined
});
