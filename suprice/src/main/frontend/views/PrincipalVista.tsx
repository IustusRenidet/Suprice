import { useContext, useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { SesionContexto } from '../componentes/ContextoSesion';
import { ComboBox } from '@vaadin/react-components/ComboBox.js';
import { TextField } from '@vaadin/react-components/TextField.js';
import { Checkbox } from '@vaadin/react-components/Checkbox.js';
import { Button } from '@vaadin/react-components/Button.js';
import { Notification } from '@vaadin/react-components/Notification.js';
import { HorizontalLayout } from '@vaadin/react-components/HorizontalLayout.js';
import { VerticalLayout } from '@vaadin/react-components/VerticalLayout.js';
import { Icon } from '@vaadin/react-components/Icon.js';

import '@vaadin/icons/vaadin-icons.js';

interface VersionSistemaDTO {
  nombre: string;
  ruta: string;
}

interface EmpresaSistemaDTO {
  nombre: string;
  ruta: string;
  sufijoTablas: string;
}

interface PrecioProductoDTO {
  lista: number;
  precioSinImpuestos: number;
  precioConImpuestos: number;
}

interface ExistenciaDetalleDTO {
  almacen: string;
  existencia: number;
}

interface ProductoConsultadoDTO {
  codigo: string;
  descripcion: string;
  clavesAlternas: string[];
  esquemaImpuestos: string;
  existenciaTotal: number;
  existencias: ExistenciaDetalleDTO[];
  precios: PrecioProductoDTO[];
  imagenBase64?: string | null;
  impuestosIncluidos: boolean;
}

type TipoSistemaAspel = 'SAE' | 'CAJA';

const PrincipalVista = () => {
  const { usuario, actualizarUsuario } = useContext(SesionContexto);
  const navigate = useNavigate();
  const [sistemas, setSistemas] = useState<TipoSistemaAspel[]>([]);
  const [sistemaSeleccionado, setSistemaSeleccionado] = useState<TipoSistemaAspel | undefined>(undefined);
  const [versiones, setVersiones] = useState<VersionSistemaDTO[]>([]);
  const [versionSeleccionada, setVersionSeleccionada] = useState<VersionSistemaDTO | undefined>(undefined);
  const [empresas, setEmpresas] = useState<EmpresaSistemaDTO[]>([]);
  const [empresaSeleccionada, setEmpresaSeleccionada] = useState<EmpresaSistemaDTO | undefined>(undefined);
  const [codigoProducto, setCodigoProducto] = useState('');
  const [incluirImpuestos, setIncluirImpuestos] = useState(true);
  const [producto, setProducto] = useState<ProductoConsultadoDTO | undefined>(undefined);
  const temporizadorRef = useRef<number | undefined>(undefined);

  useEffect(() => {
    return () => {
      if (temporizadorRef.current) {
        window.clearTimeout(temporizadorRef.current);
      }
    };
  }, []);

  useEffect(() => {
    if (!usuario) {
      navigate('/');
    }
  }, [usuario, navigate]);

  useEffect(() => {
    fetch('/api/configuracion/sistemas', { credentials: 'include' })
      .then((respuesta) => {
        if (!respuesta.ok) {
          throw new Error('No fue posible obtener los sistemas disponibles.');
        }
        return respuesta.json();
      })
      .then((datos: TipoSistemaAspel[]) => {
        setSistemas(datos);
        if (datos.length > 0) {
          setSistemaSeleccionado(datos[0]);
        }
      })
      .catch((error) => Notification.show(error.message, { position: 'bottom-center', duration: 3000 }));
  }, []);

  useEffect(() => {
    if (!sistemaSeleccionado) {
      setVersiones([]);
      setVersionSeleccionada(undefined);
      return;
    }
    fetch(`/api/configuracion/versiones?sistema=${sistemaSeleccionado}`, { credentials: 'include' })
      .then((respuesta) => {
        if (!respuesta.ok) {
          throw new Error('No fue posible obtener las versiones.');
        }
        return respuesta.json();
      })
      .then((datos: VersionSistemaDTO[]) => {
        setVersiones(datos);
        if (datos.length > 0) {
          setVersionSeleccionada(datos[0]);
        }
      })
      .catch((error) => Notification.show(error.message, { position: 'bottom-center', duration: 3000 }));
  }, [sistemaSeleccionado]);

  useEffect(() => {
    if (!versionSeleccionada || !sistemaSeleccionado) {
      setEmpresas([]);
      setEmpresaSeleccionada(undefined);
      return;
    }
    fetch(`/api/configuracion/empresas?sistema=${sistemaSeleccionado}&rutaVersion=${encodeURIComponent(versionSeleccionada.ruta)}`, {
      credentials: 'include'
    })
      .then((respuesta) => {
        if (!respuesta.ok) {
          throw new Error('No fue posible cargar las empresas.');
        }
        return respuesta.json();
      })
      .then((datos: EmpresaSistemaDTO[]) => {
        setEmpresas(datos);
        if (datos.length > 0) {
          setEmpresaSeleccionada(datos[0]);
        }
      })
      .catch((error) => Notification.show(error.message, { position: 'bottom-center', duration: 3000 }));
  }, [versionSeleccionada, sistemaSeleccionado]);

  const limpiarProducto = () => {
    setProducto(undefined);
  };

  const programarLimpieza = () => {
    if (temporizadorRef.current) {
      window.clearTimeout(temporizadorRef.current);
    }
    temporizadorRef.current = window.setTimeout(() => {
      limpiarProducto();
    }, 10000);
  };

  const consultarProducto = async () => {
    if (!empresaSeleccionada || !versionSeleccionada || !sistemaSeleccionado) {
      Notification.show('Seleccione sistema, versión y empresa.', { duration: 3000, position: 'bottom-center' });
      return;
    }
    if (!codigoProducto.trim()) {
      Notification.show('Ingrese un código de producto.', { duration: 2000, position: 'bottom-center' });
      return;
    }
    const respuesta = await fetch('/api/productos/consultar', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      credentials: 'include',
      body: JSON.stringify({
        sistema: sistemaSeleccionado,
        rutaVersion: versionSeleccionada.ruta,
        rutaEmpresa: empresaSeleccionada.ruta,
        sufijoTablas: empresaSeleccionada.sufijoTablas,
        codigoProducto: codigoProducto.trim(),
        incluirImpuestos
      })
    });
    if (respuesta.ok) {
      const datos = (await respuesta.json()) as ProductoConsultadoDTO;
      setProducto(datos);
      programarLimpieza();
    } else {
      const error = await respuesta.json().catch(() => ({ mensaje: 'Producto no encontrado' }));
      Notification.show(error.mensaje ?? 'No se localizaron resultados', { duration: 3000, position: 'bottom-center' });
      limpiarProducto();
    }
  };

  const cerrarSesion = async () => {
    await fetch('/api/autenticacion/cerrar', { method: 'POST', credentials: 'include' });
    actualizarUsuario(undefined);
    navigate('/');
  };

  const esAdmin = usuario?.rol === 'ADMINISTRADOR';

  const tablaExistencias = useMemo(() => {
    if (!producto) {
      return null;
    }
    return (
      <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: '1rem' }}>
        <thead>
          <tr>
            <th style={{ textAlign: 'left', borderBottom: '1px solid var(--lumo-contrast-20pct)' }}>Almacén/Tienda</th>
            <th style={{ textAlign: 'right', borderBottom: '1px solid var(--lumo-contrast-20pct)' }}>Existencia</th>
          </tr>
        </thead>
        <tbody>
          {producto.existencias.map((existencia) => (
            <tr key={existencia.almacen}>
              <td style={{ padding: '0.25rem 0' }}>{existencia.almacen}</td>
              <td style={{ textAlign: 'right' }}>{existencia.existencia.toFixed(2)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    );
  }, [producto]);

  const tablaPrecios = useMemo(() => {
    if (!producto) {
      return null;
    }
    return (
      <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: '1rem' }}>
        <thead>
          <tr>
            <th style={{ textAlign: 'left', borderBottom: '1px solid var(--lumo-contrast-20pct)' }}>Lista</th>
            <th style={{ textAlign: 'right', borderBottom: '1px solid var(--lumo-contrast-20pct)' }}>Precio base</th>
            <th style={{ textAlign: 'right', borderBottom: '1px solid var(--lumo-contrast-20pct)' }}>Precio con impuestos</th>
          </tr>
        </thead>
        <tbody>
          {producto.precios.map((precio) => (
            <tr key={precio.lista}>
              <td style={{ padding: '0.25rem 0' }}>Lista {precio.lista}</td>
              <td style={{ textAlign: 'right' }}>{precio.precioSinImpuestos.toFixed(2)}</td>
              <td style={{ textAlign: 'right' }}>{precio.precioConImpuestos.toFixed(2)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    );
  }, [producto]);

  return (
    <div style={{ padding: '1rem 2rem' }}>
      <HorizontalLayout style={{ width: '100%', alignItems: 'center', justifyContent: 'space-between' }}>
        <h2 style={{ margin: 0 }}>Suprice</h2>
        <HorizontalLayout theme="spacing" style={{ alignItems: 'center' }}>
          {esAdmin && (
            <Button theme="primary" onClick={() => navigate('/admin')}>
              <Icon icon="vaadin:cog" slot="prefix" /> Panel de usuarios
            </Button>
          )}
          <Button theme="error tertiary" onClick={cerrarSesion}>
            <Icon icon="vaadin:sign-out" slot="prefix" /> Salir
          </Button>
        </HorizontalLayout>
      </HorizontalLayout>

      <VerticalLayout theme="spacing" style={{ marginTop: '1rem', maxWidth: '960px' }}>
        <HorizontalLayout theme="spacing" style={{ flexWrap: 'wrap' }}>
          <ComboBox
            label="Sistema"
            items={sistemas.map((sistema) => ({ label: sistema, value: sistema }))}
            value={sistemaSeleccionado}
            itemLabelPath="label"
            itemValuePath="value"
            onValueChanged={(evento) => setSistemaSeleccionado(evento.detail.value as TipoSistemaAspel)}
            allowCustomValue={false}
            placeholder={sistemas.length === 0 ? 'Cargando sistemas' : undefined}
          />
          <ComboBox
            label="Versión"
            items={versiones.map((v) => ({ label: v.nombre, value: v.ruta }))}
            value={versionSeleccionada?.ruta}
            itemLabelPath="label"
            itemValuePath="value"
            onValueChanged={(evento) => {
              const seleccionado = versiones.find((v) => v.ruta === evento.detail.value);
              setVersionSeleccionada(seleccionado);
            }}
            allowCustomValue={false}
            placeholder={versiones.length === 0 ? 'Seleccione sistema' : undefined}
          />
          <ComboBox
            label="Empresa"
            items={empresas.map((e) => ({ label: e.nombre, value: e.ruta }))}
            value={empresaSeleccionada?.ruta}
            itemLabelPath="label"
            itemValuePath="value"
            onValueChanged={(evento) => {
              const seleccion = empresas.find((e) => e.ruta === evento.detail.value);
              setEmpresaSeleccionada(seleccion);
            }}
            allowCustomValue={false}
            placeholder={empresas.length === 0 ? 'Seleccione versión' : undefined}
          />
        </HorizontalLayout>

        <HorizontalLayout theme="spacing" style={{ flexWrap: 'wrap', alignItems: 'flex-end' }}>
          <TextField
            label="Código de producto"
            value={codigoProducto}
            onValueChanged={(evento) => setCodigoProducto(evento.detail.value)}
            onKeyDown={(evento) => {
              if (evento.key === 'Enter') {
                consultarProducto();
              }
            }}
            style={{ minWidth: '240px' }}
          />
          <Checkbox
            label="Incluir impuestos"
            checked={incluirImpuestos}
            onCheckedChanged={(evento) => setIncluirImpuestos(evento.detail.value)}
          />
          <Button theme="primary" onClick={consultarProducto}>
            <Icon icon="vaadin:search" slot="prefix" /> Consultar
          </Button>
          <Button theme="tertiary" onClick={limpiarProducto}>
            Limpiar
          </Button>
        </HorizontalLayout>
      </VerticalLayout>

      {producto && (
        <div
          style={{
            marginTop: '2rem',
            padding: '1.5rem',
            borderRadius: 'var(--lumo-border-radius-l)',
            boxShadow: '0 0 20px rgba(0,0,0,0.1)',
            maxWidth: '960px'
          }}
        >
          <HorizontalLayout theme="spacing" style={{ alignItems: 'flex-start', width: '100%' }}>
            <div style={{ flex: '1 1 auto' }}>
              <h3 style={{ marginTop: 0 }}>{producto.descripcion}</h3>
              <p style={{ marginTop: 0 }}>Código: {producto.codigo}</p>
              {producto.clavesAlternas.length > 0 && (
                <p>Claves alternas: {producto.clavesAlternas.join(', ')}</p>
              )}
              <p>Esquema de impuestos: {producto.esquemaImpuestos || 'No definido'}</p>
              <p>Existencia total: {producto.existenciaTotal.toFixed(2)}</p>
              {tablaExistencias}
            </div>
            {producto.imagenBase64 && (
              <img
                src={producto.imagenBase64}
                alt={producto.descripcion}
                style={{ maxWidth: '200px', borderRadius: 'var(--lumo-border-radius-m)' }}
              />
            )}
          </HorizontalLayout>
          {tablaPrecios}
          <p style={{ marginTop: '1rem', color: 'var(--lumo-secondary-text-color)' }}>
            La información se limpia automáticamente en 10 segundos para proteger los datos.
          </p>
        </div>
      )}
    </div>
  );
};

export default PrincipalVista;
