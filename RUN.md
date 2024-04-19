# Setup development environment

## Captura Form Server

- Use JDK 8
- Install MongoDB 4.4.x
- Tested with Postgres 9.x o 10.x (should work with newers too)

## Clone the Repositories

```
git clone https://github.com/jokoframework/captura-formserver-community.git
git clone https://github.com/jokoframework/captura-community-docker.git
git clone https://github.com/jokoframework/captura-exchange-community.git
git clone https://github.com/jokoframework/captura-form_definitions-community.git
```
## Setup Maven Dependencies

- Download the necessary files from [this GitHub repository](https://github.com/orgs/jokoframework/teams/captura-community/repositories).
- Ensure you have the downloaded files on your system.
- Reload the dependencies from your IDE.
  
##
- Copy sample setup directory `conf/captura_template/profile/` to `$HOME/captura/profile` 
```
cp -va conf/captura_template/profile /opt/captura/profile
```
- Set an environment variable called `MOBILEFORMS_HOME` pointing to it. (.properties files)
For example
```
export MOBILEFORMS_HOME=/opt/captura/profile
```

- Create webfiles as profile sibling

```
/opt/captura/webfiles
```

## Setup storage 
### MongoDB
- In `$MOBILEFORMS_HOME` you'll find the following files, adapt to your workstation credentials

   - Mongodb configurarion `mongo.properties` 
    
   Configure mongdb for accept direct connections from `localhost`. 
   
   ####For production environments, user and password are required. 
    
- [Configure mongodb](https://www.digitalocean.com/community/tutorials/how-to-install-and-secure-mongodb-on-ubuntu-16-04#part-two-securing-mongodb)
    
```
mongo.host = localhost
mongo.port = 27017
mongo.database = mobileforms
mongo.dataCollection = data
mongo.user = captura
mongo.pwd = captura123
mongo.useAuthentication = true
```

   - Required for mongodb 4.x: Create mobileforms database, with some data in it.
```
$ mongo 
> use mobileforms
switched to db mobileforms
> db.user.insert({name: "captura", "desc": "Form server"})
WriteResult({ "nInserted" : 1 })
> show dbs
admin        0.000GB
config       0.000GB
local        0.000GB
mobileforms  0.000GB
```   

### PostgreSQL setup
- Create a database in postgres 9.1 with the same name as in `jdbc.properties`
    - Set up credentials  
    - `jdbc.properties` and `jdbc-test.properties`

    Change the url, username and password. 

    - `jdbc-test.properties` is used for the integration tests. They should be different databases

```
jdbc.driverClassName=org.postgresql.Driver
jdbc.url=jdbc:postgresql://localhost:25432/mobileforms_database
jdbc.username=postgres
jdbc.password=captura
```

   - Hibernate configuration `hibernate.properties`
    
```
hibernate.hbm2ddl.auto=update
hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
hibernate.show_sql=false
```

- Test that you can compile the project with helper scripts under [scripts](https://github.com/jokoframework/captura-formserver-community/tree/main/scripts) directory

1. Customize your sources dir in both scripts

```
clone.sh
CAPTURA_SOURCES_DIR="/tmp/captura-sources"
```

```
compile.sh
BASE_DIR="/home/intruder/git/captura-community"
```

* `clone.sh` will clone all the dependencies source code
* `compile.sh` from source code directory created by `clone.sh` will compile and install dependencies and build form server WAR package.

- Part of the compilation process is to run liquibase, so next step is only needed if you make changes to the database model
   - Run liquibase in the *fs_web* project with maven

```
cd fs_web
mvn -DskipTests org.codehaus.mojo:properties-maven-plugin:read-project-properties liquibase:update
```


## Server paths configurations
    
- server.properties

Change the path to the home for webfiles configured before, make sure `images`, `csv` and `upload` folders, under `server.home` are writable for Tomcat process.

```
server.home = /opt/captura/webfiles
server.images.folder = /images
server.csv.folder = /csv
server.upload.folder = /uploads
```

## Mail configuration

This configuration are not mandatory for local development, you can skip it unless you're testing/developing e-mail related issues. 

- `mail.properties`

Please use your own user. If you don't want to or won't need to send mails you can set up the delay to a very high value

```
mail.host = smtp.gmail.com
mail.port = 587
mail.username = sodep.mf@gmail.com
mail.password = xxxxx
mail.transport.protocol = smtp
mail.smtp.auth = true
mail.smtp.starttls.enable = true
mail.debug = true
mail.queue.delay = 600000
mail.queue.sendMax = 1
```

- Deploy in tomcat 7

-  Add this parameters to the VM Arguments for tomcat process

```
 -Xms128m -Xmx1024m -XX:MaxPermSize=256m -Dlog4j.configuration="file:///opt/captura/profile/log4j.xml"
```

`fs_web` project has a log4j.xml, you can use one of your choice for your environment.

- If you are going to use an Android emulator, we recommend to setup a host pointing to your LAN/WIFI IP Address, to ease testing from Android app. Android 9+ has a whitelist for plain text connection to `*.sodep.com.py` 
  Add this to your `/etc/hosts`
```
# captura dev
192.168.1.100	dev.sodep.com.py
```

### YOU WILL NOT BE ABLE TO TEST FROM AN ANDROID DEVICES 9 OR ABOVE UNLESS YOU HAVE CONTROL OVER YOUR DNS. 
Android 8 and below; can connect to plain text servers.


## IDE Setup
You should be able to import to any IDE with maven project capabilities

-  Add a server Tomcat in Eclipse STS (or in your favorite IDE)
#### Remember to setup hades-settings.xml for maven configuration 
- Most IDEs will allow you to deploy war to your tomcat

**Example Configuration in IntelliJ:**

- Open the project using IntelliJ IDEA.
- Go to `File` > `Project Structure`:
    - SDK: 1.8
    - Language level: 8 - Lambdas, type annotations, etc.
- Configure Tomcat:
    - Click on `Current file` > `Edit Configurations` > `Add New` > `Tomcat Server` > `Local`.
    - Name: Tomcat
    - Tomcat home: `tomcat location`
    - URL: http://localhost:8080/mf/login/login.mob
    - `fix` > `fs-web:war exploded`
    - Application context: `/mf`
    - Environment variables:
        - `MOBILEFORMS_HOME`: `/opt/captura/profile`
    - Apply and save the configuration.


## Manual deploy

- Build your war. Make sure your environemnts are properly set

```
cda@dua:~/captura/apache-tomcat-7.0.103/bin$ echo $MOBILEFORMS_HOME 
/home/cda/captura/profile
cda@dua:~/captura/apache-tomcat-7.0.103/bin$ echo $JAVA_OPTS 
-Xms128m -Xmx1024m -XX:MaxPermSize=256m -Dlog4j.configuration="file:///opt/captura/profile/log4j.xml"

mvn  clean package

cda@dua:~/captura/apache-tomcat-7.0.103/bin$ ./catalina.sh start
```
Copy to your deploy directory
```
cp fs_web/target/fs-web-1.4.1-SNAPSHOT.war ~/captura/apache-tomcat-7.0.103/webapps/fs-web.war
```

## Login and last steps to start designing forms

- Go to http://dev.sodep.com.py:8080/fs-web or http://localhost:8080/fs-web
- Developer environment has 2 default users

	Root: root@mobileforms.sodep.com.py
	Admin user for designing forms: admin@testappmf.sodep.com.py 

  Both passwords are: 123456

- As Root user, in web app you'll have to adapt to your environment this two System Parameters, they are sent to the mobile client. 

```
2020-03-27 17:54:11 [py.com.sodep.mobileforms.impl.services.config.SystemParametersBundle] DEBUG - Adding parameter #1002 - http://dev.sodep.com.py/fs-web/
2020-03-27 17:54:11 [py.com.sodep.mobileforms.impl.services.config.SystemParametersBundle] DEBUG - Adding parameter #1021 - http://dev.sodep.com.py:8080/fs-web/api/document/upload/file?handle={handle}
```

## Troubleshooting
(needs translation)

- Al correr liquibase se pueden tener problemas con una incorrecta configuración de la variable de entorno `MOBILEFORMS_HOME`. En ese caso se recomienda volver a revisar dicha configuración. La alternativa es pasar directamente la variable como argumento al comando liquibase ejecutando con maven:
     mvn -DMOBILEFORMS_HOME=/opt/captura/profile -s ~/.m2/captura-settings.xml org.codehaus.mojo:properties-maven-plugin:read-project-properties liquibase:update

- Lo mismo se aplica si se está corriendo liquibase desde el Eclipse, se debe configurar la variable como argumento de la JVM en la pestaña JRE de la sección Run configurations.

- Un caso similar al anterior puede ocurrir al deployar la aplicación y levantar el tomcat. Se debe asegurar que esté bien configurada la variable `MOBILEFORMS_HOME` o pasar como argumento a la JVM del Tomcar:
  ``` 
     -Xms128m -Xmx1024m -XX:MaxPermSize=256m
      -Dlog4j.configuration="file:///opt/captura/profile/log4j.xml"
      -DMOBILEFORMS_HOME=/opt/captura/profile
  ```

- Para un correcto deploy el Tomcat, el proyecto web debe tener un context distinto a "/", verificar que esta configuración sea correcta, en caso de que no tenga, colocar "`/fs_web`".

-  En Eclipse esto se configura accediendo a las propiedades (click derecho, Properties) del proyecto `fs_web`, luego se escribe "web" y se debe buscar el Web Context-root.
