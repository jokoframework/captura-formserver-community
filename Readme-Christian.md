# Instrucciones para Levantar Captura-web

## Requisitos Previos

- Descargar e instalar IntelliJ Ultimate.
- Instalar PostgreSQL y MongoDB.
- Descargar PgAdmin para PostgreSQL.

## Pasos para Levantar el Proyecto

1. **Clonar los Repositorios:**

    - Crear una carpeta para el proyecto y clonar los siguientes repositorios:
        ```
        git clone https://github.com/jokoframework/captura-formserver-community.git
        git clone https://github.com/jokoframework/captura-community-docker.git
        git clone https://github.com/jokoframework/captura-exchange-community.git
        git clone https://github.com/jokoframework/captura-form_definitions-community.git
        ```

2. **Levantar el Entorno Docker:**

    - Ingresar a la carpeta `captura-community-docker` y ejecutar el siguiente comando para levantar los contenedores Docker:
        ```
        docker-compose up
        ```

3. **Registro de la Base de Datos:**

    - Acceder a `localhost:5050` desde un navegador.
    - Configurar la base de datos en PgAdmin:
        - Registrar servidor con los siguientes detalles:
            - Nombre del servidor: captura_db
            - Host/Address: postgres_container
            - Puerto: 5432
            - Base de datos de mantenimiento: postgres
            - Usuario: postgres
            - Contraseña: captura
    
4. **Configuración del Directorio Clientes:**

    - Crear el directorio `/opt/clientes`.
    - Descargar los archivos `joko-captura.tar.gz` y `captura.tar.gz` desde https://drive.google.com/drive/u/1/folders/12i2hLCtjAfemQDIX712v2s1428GgFpUA y extraerlos en el directorio recién creado.

5. **Configuración de Variable de Entorno `MOBILEFORM_HOME`:**

    - Ir al directorio donde se encuentra IntelliJ Idea (p.ej., `/idea-version/bin`) y ejecutar el siguiente comando para configurar la variable de entorno `MOBILEFORM_HOME`:
        ```
        export MOBILEFORM_HOME=/opt/clientes/captura/profile
        ```
        
6. **Configuración de Propiedades:**

    - Editar el archivo `/opt/clientes/captura/profile/jdbc.properties` y configurar:
        ```
        jdbc.driverClassName=org.postgresql.Driver
        jdbc.url=jdbc:postgresql://localhost:25432/mobileforms_database
        jdbc.username=postgres
        jdbc.password=captura
        ```
    - Editar el archivo `/opt/clientes/captura/profile/mongo.properties` y configurar:
        ```
        mongo.host = localhost
        mongo.port = 27017
        mongo.database = mobileforms
        mongo.dataCollection = data
        mongo.user = captura
        mongo.pwd = captura123
        mongo.useAuthentication = true
        ```

7. **Configuración en IntelliJ:**

    - Abrir el proyecto desde IntelliJ Ultimate.
    - Ir a `File` > `Project Structure`:
        - SDK: 1.8
        - Nivel de Lenguaje: 8 - Lambdas, anotaciones de tipo, etc.
    - Configurar Tomcat:
        - Hacer clic en `Current file` > `Edit Configurations` > `Add New` > `Tomcat Server` > `Local`.
        - Nombre: Tomcat
        - Tomcat home: `/opt/clientes/captura-apache-tomcat`
        - URL: http://localhost:8080/mf/login/login.mob
        - `fix` > `fs-web:war exploded`
        - Aplicación context: `/mf`
        - Variables de entorno:
            - `MOBILEFORMS_HOME`: `/opt/clientes/captura/profile`
        - Aplicar y guardar la configuración.

8. **Ejecución del Proyecto:**
    - Ir al directorio opt/clientes/captura/apachetomcat-version/bin y dar permisos
       ```bash
        chmod u+x *.sh
        ```
    - Abrir la terminal e ingresar a la carpeta `captura-formserver-community`.
    - Cambiar a la rama `sync-2023-12`:
        ```
        git checkout sync-2023-12
        ```
    - Ejecutar el proyecto para que se cree la carpeta `.m2` en el directorio personal.

9. **Configuración de Dependencias de Maven:**

    - Descargar los archivos `maven-captura-libs.tar.gz` y `maven-captura-libs-2.zip` desde https://drive.google.com/drive/u/1/folders/12i2hLCtjAfemQDIX712v2s1428GgFpUA.
    - Extraer el contenido en la carpeta `.m2` en tu directorio personal.
    - Desde IntelliJ, hacer clic en el icono "m" y seleccionar "Reload".
    - Ejecutar el proyecto.

10. **Iniciar Sesión en la Aplicación:**

    Utiliza las siguientes credenciales para iniciar sesión:
    
    - Usuario: root@mobileforms.sodep.com.py
    - Contraseña: 123456

