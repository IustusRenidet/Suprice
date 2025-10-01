import { useContext, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { SesionContexto } from '../componentes/ContextoSesion';
import { Grid } from '@vaadin/react-components/Grid.js';
import { GridColumn } from '@vaadin/react-components/GridColumn.js';
import { Button } from '@vaadin/react-components/Button.js';
import { Dialog } from '@vaadin/react-components/Dialog.js';
import { TextField } from '@vaadin/react-components/TextField.js';
import { PasswordField } from '@vaadin/react-components/PasswordField.js';
import { Select } from '@vaadin/react-components/Select.js';
import { Notification } from '@vaadin/react-components/Notification.js';
import { HorizontalLayout } from '@vaadin/react-components/HorizontalLayout.js';
import { MENSAJE_ERROR_CONEXION, obtenerMensajeDesdeError, obtenerMensajeDesdeRespuesta } from '../utilidades/mensajesError';

interface UsuarioDTO {
  nombreUsuario: string;
  rol: 'ADMINISTRADOR' | 'CONSULTA';
}

const AdminUsuariosVista = () => {
  const { usuario } = useContext(SesionContexto);
  const navigate = useNavigate();
  const [usuarios, setUsuarios] = useState<UsuarioDTO[]>([]);
  const [dialogoAbierto, setDialogoAbierto] = useState(false);
  const [nuevoUsuario, setNuevoUsuario] = useState({ nombre: '', contrasena: '', rol: 'CONSULTA' as 'CONSULTA' | 'ADMINISTRADOR' });
  const [cargando, setCargando] = useState(false);

  useEffect(() => {
    if (!usuario) {
      navigate('/');
      return;
    }
    if (usuario.rol !== 'ADMINISTRADOR') {
      navigate('/principal');
      return;
    }
    cargarUsuarios();
  }, [usuario, navigate]);

  const mostrarNotificacion = (mensaje: string) =>
    Notification.show(mensaje, { duration: 3000, position: 'bottom-center' });

  const cargarUsuarios = async () => {
    try {
      const respuesta = await fetch('/api/usuarios', { credentials: 'include' });
      if (!respuesta.ok) {
        const mensaje = await obtenerMensajeDesdeRespuesta(respuesta, 'No fue posible obtener los usuarios');
        mostrarNotificacion(mensaje);
        return;
      }
      const datos = (await respuesta.json()) as UsuarioDTO[];
      setUsuarios(datos);
    } catch (error) {
      mostrarNotificacion(obtenerMensajeDesdeError(error, MENSAJE_ERROR_CONEXION));
    }
  };

  const eliminarUsuario = async (nombre: string) => {
    if (!window.confirm(`¿Eliminar al usuario ${nombre}?`)) {
      return;
    }
    try {
      const respuesta = await fetch(`/api/usuarios/${encodeURIComponent(nombre)}`, {
        method: 'DELETE',
        credentials: 'include'
      });
      if (respuesta.ok) {
        mostrarNotificacion('Usuario eliminado correctamente');
        cargarUsuarios();
        return;
      }
      const mensaje = await obtenerMensajeDesdeRespuesta(respuesta, 'No fue posible eliminar al usuario');
      mostrarNotificacion(mensaje);
    } catch (error) {
      mostrarNotificacion(obtenerMensajeDesdeError(error, MENSAJE_ERROR_CONEXION));
    }
  };

  const crearUsuario = async () => {
    if (!nuevoUsuario.nombre.trim() || !nuevoUsuario.contrasena.trim()) {
      Notification.show('Ingrese un nombre y contraseña válidos.', { duration: 2000, position: 'bottom-center' });
      return;
    }
    setCargando(true);
    try {
      const respuesta = await fetch('/api/usuarios', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        credentials: 'include',
        body: JSON.stringify({
          nombreUsuario: nuevoUsuario.nombre.trim(),
          contrasena: nuevoUsuario.contrasena.trim(),
          rol: nuevoUsuario.rol
        })
      });
      if (respuesta.ok || respuesta.status === 201) {
        mostrarNotificacion('Usuario creado correctamente');
        setDialogoAbierto(false);
        setNuevoUsuario({ nombre: '', contrasena: '', rol: 'CONSULTA' });
        cargarUsuarios();
        return;
      }
      const mensaje = await obtenerMensajeDesdeRespuesta(respuesta, 'No fue posible crear el usuario');
      mostrarNotificacion(mensaje);
    } catch (error) {
      mostrarNotificacion(obtenerMensajeDesdeError(error, MENSAJE_ERROR_CONEXION));
    } finally {
      setCargando(false);
    }
  };

  return (
    <div style={{ padding: '1rem 2rem' }}>
      <HorizontalLayout style={{ justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h2 style={{ marginBottom: 0 }}>Administración de usuarios</h2>
          <p style={{ marginTop: 0, color: 'var(--lumo-secondary-text-color)' }}>
            Agregue o elimine usuarios que podrán acceder a Suprice.
          </p>
        </div>
        <Button theme="tertiary" onClick={() => navigate('/principal')}>
          Volver a consultas
        </Button>
      </HorizontalLayout>

      <Button theme="primary" style={{ marginTop: '1rem' }} onClick={() => setDialogoAbierto(true)}>
        Nuevo usuario
      </Button>

      <Grid items={usuarios} style={{ marginTop: '1rem', maxWidth: '720px' }}>
        <GridColumn path="nombreUsuario" header="Usuario" autoWidth></GridColumn>
        <GridColumn path="rol" header="Rol" autoWidth></GridColumn>
        <GridColumn
          header="Acciones"
          autoWidth
          renderer={({ item }) => (
            <Button theme="error tertiary" onClick={() => eliminarUsuario(item.nombreUsuario)}>
              Eliminar
            </Button>
          )}
        ></GridColumn>
      </Grid>

      <Dialog opened={dialogoAbierto} headerTitle="Nuevo usuario" onOpenedChanged={(evento) => setDialogoAbierto(evento.detail.value)}>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', minWidth: '320px' }}>
          <TextField
            label="Nombre de usuario"
            value={nuevoUsuario.nombre}
            onValueChanged={(evento) => setNuevoUsuario((actual) => ({ ...actual, nombre: evento.detail.value }))}
          />
          <PasswordField
            label="Contraseña"
            value={nuevoUsuario.contrasena}
            onValueChanged={(evento) => setNuevoUsuario((actual) => ({ ...actual, contrasena: evento.detail.value }))}
          />
          <Select
            label="Rol"
            items={[
              { label: 'Consulta', value: 'CONSULTA' },
              { label: 'Administrador', value: 'ADMINISTRADOR' }
            ]}
            value={nuevoUsuario.rol}
            onValueChanged={(evento) => setNuevoUsuario((actual) => ({ ...actual, rol: evento.detail.value as 'CONSULTA' | 'ADMINISTRADOR' }))}
          />
          <HorizontalLayout theme="spacing" style={{ justifyContent: 'flex-end' }}>
            <Button theme="tertiary" onClick={() => setDialogoAbierto(false)}>
              Cancelar
            </Button>
            <Button theme="primary" onClick={crearUsuario} disabled={cargando}>
              Guardar
            </Button>
          </HorizontalLayout>
        </div>
      </Dialog>
    </div>
  );
};

export default AdminUsuariosVista;
