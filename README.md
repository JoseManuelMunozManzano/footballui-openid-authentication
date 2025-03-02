# SPRING BOOT 3 COOKBOOK

Configurando una aplicación MVC con autenticación OpenId.

Se usa `Spring Boot`, `Thymeleaf`, `Spring Web`, `Spring Session`, `Spring Data Redis(Access+Driver)`, `OAuth2 Client` y `Spring Security`.

## Creación de proyecto

Uso Spring Initializr: `https://start.spring.io/`

![alt Spring Initialzr](./images/01-Spring-Initializr.png)

## Ejecución del proyecto

- Clonar/descargar este proyecto
- Ejecutar el proyecto del Authorization Server
    - `https://github.com/JoseManuelMunozManzano/Spring-Boot-football-auth`
    - Ver su README.md, la parte con título `Configurando una aplicación MVC con autenticación OpenId`.
- Ejecutar el proyecto de Resource Server
  - `https://github.com/JoseManuelMunozManzano/Spring-Boot-football-resource`
  - Ver su README.md, la parte con título `Configurando una aplicación MVC con autenticación OpenId`.
- Ejecutar este proyecto con el comando: `./mvnw spring-boot:run`
  - O ejecutar directamente desde IntelliJ Idea
- Acceder, en el navegador, a la siguiente ruta: `http://localshot:9080`
  - Pulsar en el enlace `who you are`. Esto nos lleva a la pantalla de login
  - Indicar usuario (user) y contraseña (password). Esto nos lleva a la pantalla de consentimientos requeridos (a mi no me sale)
  - Después nos aparece lo que podemos ver en la data OpenId
  - Si pulsamos el enlace Teams aparecen los teams que hay en nuestro Resource Server.

## Configuración

Hay una incompatibilidad conocida entre `Thymeleaf` y `Spring Security`.

Para integrar ambos componentes es necesario añadir esta dependencia, aunque me la añadió automáticamente al crearse el proyecto:

```xml
<dependency>
    <groupId>org.thymeleaf.extras</groupId>
    <artifactId>thymeleaf-extras-springsecurity6</artifactId>
</dependency>
```

Como estamos usando el enfoque Model View Controller (MVC) crearemos un controller que rellenará un modelo y se presentará en la vista. La vista se renderiza usando el motor de plantillas Thymeleaf.

Creamos el paquete `controllers` y dentro el controlador `FootballController.java`, cuyos métodos devuelven el nombre de la vista a renderizar.

Para la vista, creamos una plantilla Thymeleaf en la carpeta `resources/templates`, llamada `home.html`.

Tiene una enlace que queremos proteger.

Configuramos la aplicación para que pueda autenticarse usando nuestro Authorization Server.

Para ello configuramos el cliente Oauth2 en `application.yml`. Necesitaremos los parámetros que usamos en el Authorization Server. 

Configuramos la aplicación para proteger todas las páginas salvo `home` y usamos las credenciales de login OAuth2.

Para ello creamos el paquete `config` y el archivo de configuración `SecurityCon`figuration.java`.

Creamos otra vista en la carpeta `resources/templates`, llamada `myself.html`.

Ahora, en el proyecto `https://github.com/JoseManuelMunozManzano/Spring-Boot-football-resource` tenemos que permitir que la audiencia acceda a la API RESTful.

Por último, necesitamos configurar Redis. Para ello solo necesitamos `hostname` y `port`. Esto lo añadimos a `application.yml`.

Y, por último, para evitar problemas de puertos, ya que para ejecutar esta app hace falta ejecutar otros dos proyectos, haremos, en `application.yml` que el puerto de esta app sea la `9080`.

Tener en cuenta que este puerto en el mismo indicado en el proyecto de `Authorization Server` y que si lo cambiamos aquí, tenemos que poner el mismo puerto en ese otro proyecto.

## Como funciona

Hemos protegido la aplicación usando OIDC (OpenID Connect). Es un protocolo de autenticación que se basa en OAuth2.

Proporciona una forma estandarizada para los usuarios de hacer login a aplicaciones web o apps móviles usando sus cuentas existentes con IdPs (Identity Provider).

Un IdP o Proveedor de Identidad es el servicio que autentica a los usuarios y emite los tokens de identidad que permiten a las aplicaciones (clientes) verificar quién es el usuario autenticado.

Ejemplo: imagina que estás construyendo una aplicación web y quieres permitir que los usuarios inicien sesión con Google. En este caso:

- Google es el IdP: Es quien autentica a los usuarios y proporciona información sobre su identidad
- Tu aplicación es el Cliente OIDC: Recibe el ID Token de Google para verificar quién es el usuario

En este ejercicio, usamos nuestro Authorization Server como un IdP.

El servidor OIDC proporciona un Discovery Endpoint (.well-known/openid-configuration), el cual expone toda la información necesaria para que las aplicaciones cliente se configuren automáticamente.

En nuestra app, usamos `authorization code grant flow` que implica los siguientes pasos.

**Flujo de autorización (Authorization Code Grant Flow)**

1. El cliente redirige al usuario al servidor de autorización, requiriendo los scopes
2. El servidor de autorización autentica al usuario y pide consentimiento si es necesario
3. El servidor de autorización redirige al usuario de vuelta al cliente con un código de autorización de corta vida. La aplicación de cliente rescata el código de autorización en el token endpoint (proporcionado por el discovery endpoint)
   - El servidor de autorización devuelve los tokens con los scopes solicitados. Si el IdP lo requiere, el usuario debe otorgar consentimiento antes de que los tokens sean emitidos
   - Los siguientes tokens son devueltos por el servidor de autorización:
     - Id token que contiene la información de la sesión. Solo se usa para propósitos de autenticación
     - Un token de acceso, que contiene información de autorización y los scopes permitidos. Se usa para acceder a recursos protegidos en APIs
     - Un token de refresco, que se usa para solicitar un nuevo Access Token antes de que expire. No renueva el ID Token, ya que este solo es válido en la sesión de autenticación inicial.

**Gestión del estado y sesiones**
Debido a las redirecciones en OAuth2/OIDC, la aplicación cliente necesita mantener el estado del usuario.

Para ello, usamos Redis como gestor de sesiones.

**Orden de arranque**
El cliente necesita acceder al Discovery Endpoint en el arranque.

Por ello, debemos iniciar primero el Authorization Server y luego el cliente.

**Protección de rutas**
En nuestra aplicación, la única página accesible sin autenticación es /.

Para acceder a cualquier otra página (/myself, /teams), el usuario debe estar autenticado.

Si intenta acceder sin sesión, se inicia automáticamente el proceso de autorización.