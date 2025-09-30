import '@vaadin/vaadin-lumo-styles/all-imports.js';
import React, { useEffect, useState } from 'react';
import ReactDOM from 'react-dom/client';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import LoginVista from './views/LoginVista';
import PrincipalVista from './views/PrincipalVista';
import AdminUsuariosVista from './views/AdminUsuariosVista';
import { SesionContexto, UsuarioSesion } from './componentes/ContextoSesion';

const consultarSesionActual = async (): Promise<UsuarioSesion | undefined> => {
  const respuesta = await fetch('/api/autenticacion/usuario-actual', {
    credentials: 'include'
  });
  if (!respuesta.ok) {
    return undefined;
  }
  const datos = await respuesta.json();
  return datos ?? undefined;
};

const App = () => {
  const [usuario, setUsuario] = useState<UsuarioSesion | undefined>(undefined);
  const [cargando, setCargando] = useState(true);

  useEffect(() => {
    consultarSesionActual()
      .then((sesion) => {
        setUsuario(sesion ?? undefined);
        setCargando(false);
      })
      .catch(() => setCargando(false));
  }, []);

  const router = createBrowserRouter([
    {
      path: '/',
      element: <LoginVista />
    },
    {
      path: '/principal',
      element: <PrincipalVista />
    },
    {
      path: '/admin',
      element: <AdminUsuariosVista />
    }
  ]);

  if (cargando) {
    return <div style={{ display: 'flex', height: '100vh', alignItems: 'center', justifyContent: 'center' }}>Cargando sesión...</div>;
  }

  return (
    <SesionContexto.Provider value={{ usuario, actualizarUsuario: setUsuario }}>
      <RouterProvider router={router} />
    </SesionContexto.Provider>
  );
};

ReactDOM.createRoot(document.getElementById('root')!).render(<App />);
