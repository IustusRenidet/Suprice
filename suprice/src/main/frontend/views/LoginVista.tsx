import { Button } from '@vaadin/react-components/Button.js';
import { LoginForm } from '@vaadin/react-components/LoginForm.js';
import { VerticalLayout } from '@vaadin/react-components/VerticalLayout.js';
import { Notification } from '@vaadin/react-components/Notification.js';
import { useContext, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { SesionContexto } from '../componentes/ContextoSesion';
import { MENSAJE_ERROR_CONEXION, obtenerMensajeDesdeError, obtenerMensajeDesdeRespuesta } from '../utilidades/mensajesError';

const LoginVista = () => {
  const { actualizarUsuario, usuario } = useContext(SesionContexto);
  const navigate = useNavigate();

  const manejarLogin = async (evento: CustomEvent<{ username: string; password: string }>) => {
    evento.preventDefault();
    const { username, password } = evento.detail;
    try {
      const respuesta = await fetch('/api/autenticacion/iniciar', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        credentials: 'include',
        body: JSON.stringify({ nombreUsuario: username, contrasena: password })
      });
      if (respuesta.ok) {
        const datos = await respuesta.json();
        actualizarUsuario(datos);
        navigate('/principal');
        return;
      }
      const mensaje = await obtenerMensajeDesdeRespuesta(
        respuesta,
        respuesta.status >= 500 ? MENSAJE_ERROR_CONEXION : 'Credenciales incorrectas'
      );
      Notification.show(mensaje, { position: 'bottom-center', duration: 3000 });
    } catch (error) {
      Notification.show(obtenerMensajeDesdeError(error, 'No fue posible iniciar sesión'), {
        position: 'bottom-center',
        duration: 3000
      });
    }
  };

  useEffect(() => {
    if (usuario) {
      navigate('/principal');
    }
  }, [usuario, navigate]);

  return (
    <div style={{ display: 'flex', height: '100vh', alignItems: 'center', justifyContent: 'center' }}>
      <VerticalLayout theme="spacing" style={{ padding: '2rem', width: '320px', boxShadow: '0 0 16px rgba(0,0,0,0.1)' }}>
        <h2 style={{ margin: 0, textAlign: 'center' }}>Suprice</h2>
        <p style={{ marginTop: 0, textAlign: 'center', color: 'var(--lumo-secondary-text-color)' }}>
          Ingrese sus credenciales para continuar.
        </p>
        <LoginForm noForgotPassword onLogin={manejarLogin} />
        <Button theme="tertiary" onClick={() => Notification.show('Contacte al administrador para obtener acceso.')}>¿Necesita ayuda?</Button>
      </VerticalLayout>
    </div>
  );
};

export default LoginVista;
