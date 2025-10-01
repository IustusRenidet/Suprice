# Suprice

Aplicación web completa desarrollada con Spring Boot y Vaadin Hilla para consultar precios y existencias de productos en los sistemas Aspel SAE y Caja. Este documento describe en detalle la estructura del proyecto, el propósito de cada archivo relevante, las tecnologías y dependencias empleadas (con sus versiones) y los pasos de instalación y construcción con Maven y npm.

## Tecnologías principales

| Capa | Tecnología | Versión | Uso |
|------|------------|---------|-----|
| Backend | Java | 17 | Lenguaje principal del servidor. |
| Backend | Spring Boot | 3.3.4 | Framework para configurar el backend y exponer servicios REST/Endpoints Hilla. |
| Backend | Spring Security | Incluido en Spring Boot 3.3.4 | Manejo de autenticación basada en sesión. |
| Backend | Spring Data JDBC | Incluido en Spring Boot 3.3.4 | Acceso a la base de datos SQLite para usuarios. |
| Backend | Jaybird JDBC | 5.0.9.java11 | Conector JDBC para bases de datos Firebird de Aspel. |
| Backend | SQLite JDBC | 3.46.1.0 | Controlador JDBC para la base de datos local de usuarios. |
| Backend | BCrypt (jbcrypt) | 0.4 | Hash de contraseñas. |
| Backend/Frontend | Vaadin Hilla | 24.4.10 | Generación de endpoints tipados y base del frontend React. |
| Frontend | Node.js | 20.x (instalación manual) | Entorno de ejecución para herramientas de construcción. |
| Frontend | npm | 10.x (instalación local) | Gestor de paquetes para dependencias frontend. |
| Frontend | React | 18.3.1 | Biblioteca de UI. |
| Frontend | Vite | 5.4.8 | Herramienta de construcción rápida para el frontend. |

> **Nota sobre Hilla:** la versión 2.5.10 referenciada anteriormente no está disponible en los repositorios públicos y provoca errores de resolución. El proyecto utiliza **Vaadin Hilla 24.4.10**, que es la última versión estable presente en Maven Central.

## Dependencias Maven

| Grupo | Artefacto | Versión | Motivo |
|-------|-----------|---------|--------|
| org.springframework.boot | spring-boot-starter-web | 3.3.4 | Servicios REST y configuración web básica. |
| org.springframework.boot | spring-boot-starter-security | 3.3.4 | Seguridad basada en sesiones. |
| org.springframework.boot | spring-boot-starter-validation | 3.3.4 | Validación de DTOs. |
| org.springframework.boot | spring-boot-starter-data-jdbc | 3.3.4 | Acceso a datos usando JDBC para SQLite. |
| com.vaadin | hilla | 24.4.10 | Endpoints Hilla y componentes compartidos. |
| org.firebirdsql.jdbc | jaybird | 5.0.9.java11 | Conexión a bases Firebird de Aspel compatible con JDK 11+. |
| org.xerial | sqlite-jdbc | 3.46.1.0 | Conexión a la base `usuarios.db`. |
| org.mindrot | jbcrypt | 0.4 | Hash y verificación de contraseñas. |
| org.springframework.boot | spring-boot-starter-test | 3.3.4 | Dependencia de pruebas (scope `test`). |

## Dependencias npm

| Paquete | Versión | Uso |
|---------|---------|-----|
| @vaadin/react-components | ^24.4.10 | Componentes Vaadin para React. |
| @vaadin/vaadin-lumo-styles | ^24.4.10 | Tema Lumo para estilos. |
| @vaadin/icons | ^24.4.10 | Iconografía Vaadin. |
| react, react-dom | ^18.3.1 | Base de componentes frontend. |
| react-router-dom | ^6.26.1 | Enrutamiento entre vistas. |
| @vitejs/plugin-react | ^4.3.1 | Soporte React en Vite. |
| typescript | ^5.5.4 | Tipado del frontend. |
| vite | ^5.4.8 | Construcción y servidor de desarrollo. |
| @types/react, @types/react-dom, @types/react-router-dom | Tipos de TypeScript para React y router. |

## Estructura del proyecto

```
Suprice/
└── suprice/
    ├── pom.xml
    ├── package.json
    ├── tsconfig.json
    ├── vite.config.ts
    ├── README.md
    ├── src/
    │   ├── main/java/com/suprice/suprice/
    │   │   ├── SupriceApplication.java
    │   │   ├── configuracion/
    │   │   │   ├── ConfiguracionAplicacion.java
    │   │   │   └── ConfiguracionSeguridad.java
    │   │   ├── endpoint/
    │   │   │   ├── AutenticacionControlador.java
    │   │   │   ├── ConfiguracionControlador.java
    │   │   │   ├── ConsultaProductosControlador.java
    │   │   │   └── UsuariosControlador.java
    │   │   ├── modelo/
    │   │   │   ├── CredencialesInicioSesion.java
    │   │   │   ├── EmpresaSistemaDTO.java
    │   │   │   ├── ExistenciaDetalleDTO.java
    │   │   │   ├── PeticionUsuarioAdmin.java
    │   │   │   ├── PrecioProductoDTO.java
    │   │   │   ├── ProductoConsultadoDTO.java
    │   │   │   ├── RespuestaOperacionDTO.java
    │   │   │   ├── RolUsuario.java
    │   │   │   ├── SolicitudConsultaProducto.java
    │   │   │   ├── UsuarioDTO.java
    │   │   │   ├── UsuarioEntidad.java
    │   │   │   ├── UsuarioSesion.java
    │   │   │   └── VersionSistemaDTO.java
    │   │   ├── servicio/
    │   │   │   ├── ServicioConfiguracionAspel.java
    │   │   │   ├── ServicioConsultaProductos.java
    │   │   │   └── ServicioUsuarios.java
    │   │   └── util/
    │   │       ├── UtilidadesImpuestos.java
    │   │       └── UtilidadesRutas.java
    │   └── main/resources/
    │       └── application.properties
    └── src/main/frontend/
        ├── index.html
        ├── main.tsx
        ├── componentes/
        │   └── ContextoSesion.tsx
        └── views/
            ├── AdminUsuariosVista.tsx
            ├── LoginVista.tsx
            └── PrincipalVista.tsx
```

### Descripción de archivos y carpetas

#### Raíz del proyecto (`suprice/`)
- **pom.xml**: Configuración Maven del backend y del ciclo de construcción de Hilla.
- **package.json**: Dependencias y scripts frontend.
- **tsconfig.json**: Configuración de TypeScript para el frontend Hilla/React.
- **vite.config.ts**: Configuración del empaquetado con Vite.
- **README.md**: Este documento con instrucciones completas.

#### Backend (`src/main/java/com/suprice/suprice/`)
- **SupriceApplication.java**: Clase principal de Spring Boot que inicia la aplicación.
- **configuracion/ConfiguracionAplicacion.java**: Define beans de base de datos SQLite, inicialización del usuario administrador y configuración de utilidades generales.
- **configuracion/ConfiguracionSeguridad.java**: Configura Spring Security con sesiones, reglas de autorización y filtros de autenticación.
- **endpoint/AutenticacionControlador.java**: Endpoints REST/Hilla para iniciar sesión y obtener información de la sesión.
- **endpoint/ConfiguracionControlador.java**: Proporciona la exploración de sistemas Aspel, versiones y empresas disponibles mediante escaneo de directorios.
- **endpoint/ConsultaProductosControlador.java**: Expone la consulta asíncrona de productos (precios, existencias e imágenes).
- **endpoint/UsuariosControlador.java**: API para administrar usuarios (solo accesible al administrador).
- **modelo/**: DTOs, enums y entidades usadas para transportar datos entre frontend y backend.
  - **CredencialesInicioSesion.java**: Datos para el formulario de login.
  - **EmpresaSistemaDTO.java / VersionSistemaDTO.java**: Información para llenar los ComboBox de empresas y versiones.
  - **ExistenciaDetalleDTO.java**: Existencia por almacén/tienda.
  - **PeticionUsuarioAdmin.java**: Peticiones para crear/eliminar usuarios.
  - **PrecioProductoDTO.java**: Listas de precios calculadas con impuestos.
  - **ProductoConsultadoDTO.java**: Respuesta completa de la consulta de producto.
  - **RespuestaOperacionDTO.java**: Resultado estándar para operaciones administrativas.
  - **RolUsuario.java**: Enumeración de roles permitidos (ADMIN, USUARIO).
  - **SolicitudConsultaProducto.java**: Datos necesarios para lanzar la consulta de productos.
  - **UsuarioDTO.java / UsuarioEntidad.java / UsuarioSesion.java**: Representaciones del usuario en distintos contextos.
- **servicio/**: Lógica de negocio.
  - **ServicioConfiguracionAspel.java**: Escaneo de rutas Aspel, cacheo y validación de conexiones Firebird.
  - **ServicioConsultaProductos.java**: Construye y ejecuta consultas SQL a Firebird, calcula precios con o sin impuestos y arma la respuesta.
  - **ServicioUsuarios.java**: Gestión de usuarios en SQLite, hash BCrypt y manejo del usuario admin.
- **util/**: Utilidades auxiliares.
  - **UtilidadesImpuestos.java**: Funciones para aplicar reglas de impuestos Aspel.
  - **UtilidadesRutas.java**: Construcción y validación de rutas a las bases de datos e imágenes.

#### Recursos (`src/main/resources/`)
- **application.properties**: Configuración de Spring (ruta de SQLite, logs, propiedades personalizadas).

#### Frontend (`src/main/frontend/`)
- **index.html**: Plantilla HTML inicial para Vite.
- **main.tsx**: Punto de entrada React/Vite que monta la aplicación y define las rutas.
- **componentes/ContextoSesion.tsx**: Contexto React que mantiene el estado de la sesión y facilita su consumo en las vistas.
- **views/LoginVista.tsx**: Formulario de inicio de sesión con validaciones.
- **views/PrincipalVista.tsx**: Vista principal con selección de sistema/versión/empresa, búsqueda de productos y temporizador de limpieza.
- **views/AdminUsuariosVista.tsx**: Panel de administración para crear y eliminar usuarios.

## Requisitos previos

1. **JDK 17** con la variable `JAVA_HOME` configurada.
2. **Maven 3.9.x** disponible en la ruta del sistema.
3. **Node.js 20.x** instalado manualmente (descargado desde [nodejs.org](https://nodejs.org)).
4. Extensiones recomendadas en Visual Studio Code: Extension Pack for Java, Spring Boot Extension Pack, Hilla Extension, TypeScript Language Features y Maven for Java.

## Instalación y construcción

### Paso 1: Clonar el repositorio
```bash
git clone https://github.com/tu-usuario/Suprice.git
cd Suprice/suprice
```

### Paso 2: Verificar dependencias Maven
Asegúrate de que la versión de Hilla sea **24.4.10** en el `pom.xml`. Esta es la versión publicada en Maven Central. Si necesitas limpiar el caché previo de versiones inexistentes, ejecuta:
```bash
mvn -U dependency:purge-local-repository
```

### Paso 3: Instalar dependencias frontend
```bash
npm install
```
> Este comando instala los paquetes oficiales de Vaadin 24.4.x. Al eliminar `@hilla/react-components` se evita el error `ETARGET` ocasionado por solicitar versiones inexistentes como `@hilla/react-components@^2.5.10`.

### Paso 4: Construir recursos del frontend
```bash
npm run build
```
Genera la versión optimizada del frontend bajo `target/frontend`. Ejecuta este paso cada vez que modifiques el código TypeScript antes de empaquetar con Maven.

### Paso 5: Construir el proyecto completo con Maven
```bash
mvn clean install
```
El build empaqueta el backend con Spring Boot e incluye los recursos generados en el paso anterior.

### Empaquetado final

Para generar el artefacto distribuible ejecuta el ciclo de empaquetado de Maven:

```bash
mvn clean package
```

Este comando produce `target/suprice-0.0.1-SNAPSHOT.jar`. Gracias a `spring-boot-maven-plugin`, el JAR incluye:

- El backend Spring Boot con el servidor embebido (Tomcat) listo para ejecutarse con `java -jar`.
- Los recursos frontend optimizados que se copiaron desde `target/frontend` al directorio `BOOT-INF/classes/META-INF/resources/` durante el empaquetado.

Con el archivo generado puedes desplegar la aplicación en cualquier entorno con JDK 17 disponible:

```bash
java -jar target/suprice-0.0.1-SNAPSHOT.jar
```


Si deseas omitir pruebas automáticas (no definidas por defecto), puedes usar:
```bash
mvn clean install -DskipTests
```

### Paso 6: Ejecutar la aplicación
```bash
mvn spring-boot:run
```
Esto levanta el backend en `http://localhost:8080` y el frontend integrado de Hilla. Para desarrollo frontend autónomo puedes ejecutar `npm run dev` en otra terminal.

> 💡 **Importante:** el frontend en modo desarrollo utiliza un proxy hacia `http://localhost:8080`. Si el backend no está en ejecución, las peticiones a `/api/...` devolverán errores 500 o `ECONNREFUSED`. Asegúrate de mantener `mvn spring-boot:run` activo mientras pruebas la interfaz con `npm run dev`.

## Uso de la aplicación
1. Accede a `http://localhost:8080` y autentícate con un usuario válido (el administrador inicial es `admin` / `]mYMI&Rep711`, se recomienda cambiarlo).
2. Selecciona el sistema (SAE o Caja), luego la versión detectada y la empresa.
3. Ingresa el código del producto; la consulta mostrará descripción, imagen, precios (hasta cuatro listas con o sin impuestos) y existencias totales y por almacén/tienda.
4. La vista se limpia automáticamente después de 10 segundos.
5. Si inicias sesión como administrador, tendrás acceso al panel de usuarios para altas/bajas en tiempo real.

## Resolución de problemas
- **Error `No matching version found for @hilla/react-components@^2.5.10`**: ya no es necesario instalar ese paquete. Elimínalo de `package.json`, borra `node_modules` y vuelve a ejecutar `npm install`.
- **Error Maven relacionado con `hilla-maven-plugin`**: el proyecto ya no ejecuta automáticamente el plugin de Hilla. Si deseas regenerar clientes TypeScript con la herramienta oficial, instala `@vaadin/hilla-generator` globalmente y ejecútalo manualmente.
- **Problemas con rutas Aspel**: revisa `application.properties` para definir rutas personalizadas o usa los selectores dinámicos de la interfaz que escanean los directorios por defecto.

## Licencia
(╬≖_≖) Aquí pondría mi licencia, si tan solo tuviera unaaaa (ง •̀_•́)ง