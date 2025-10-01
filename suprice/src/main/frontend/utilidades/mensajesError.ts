export const MENSAJE_ERROR_CONEXION =
  'No fue posible conectar con el servidor. Verifique que el servicio de backend esté en ejecución.';

export const obtenerMensajeDesdeError = (error: unknown, mensajePorDefecto: string): string => {
  if (error instanceof TypeError) {
    return MENSAJE_ERROR_CONEXION;
  }
  if (error instanceof Error && error.message) {
    return error.message;
  }
  return mensajePorDefecto;
};

export const obtenerMensajeDesdeRespuesta = async (
  respuesta: Response,
  mensajePorDefecto: string
): Promise<string> => {
  if (respuesta.status >= 500) {
    return MENSAJE_ERROR_CONEXION;
  }
  try {
    const datos = await respuesta.clone().json();
    if (datos && typeof datos === 'object' && 'mensaje' in datos) {
      const mensaje = (datos as { mensaje?: unknown }).mensaje;
      if (typeof mensaje === 'string' && mensaje.trim().length > 0) {
        return mensaje;
      }
    }
  } catch (error) {
    console.warn('No fue posible interpretar la respuesta del servidor', error);
  }
  return mensajePorDefecto;
};
