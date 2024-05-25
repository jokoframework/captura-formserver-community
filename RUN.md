# Setup development environment

## Captura Form Server Requirements

- JDK 8
- Tomcat

## Clone the Repositories

```
git clone https://github.com/jokoframework/simplecaptcha.git 
git clone https://github.com/jokoframework/sodep-export-utilities.git 
git clone https://github.com/jokoframework/swagger-springmvc-community.git 
git clone https://github.com/jokoframework/gandalf-community.git 
git clone https://github.com/jokoframework/license-community.git 
git clone https://github.com/jokoframework/captura-formserver-community.git
git clone https://github.com/jokoframework/captura-community-docker.git
git clone https://github.com/jokoframework/captura-exchange-community.git
git clone https://github.com/jokoframework/captura-form_definitions-community.git
```
  
## Docker Environment Setup

Navigate to the the path where you cloned the repositories and go to the `captura-community-docker` folder and run the following command to set up Docker containers:
```
docker compose up
```
## $HOME/captura/profile Setup

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

## Storage Setup
In `$MOBILEFORMS_HOME` you'll find the following files, adapt to your workstation credentials
### MongoDB

   - Update `mongo.properties` 
    
   Configure mongdb for accept direct connections from `localhost` and the variables according to the docker-compose.yml file. 
   
```
mongo.host = localhost
mongo.port = 27017
mongo.database = mobileforms
mongo.dataCollection = data
mongo.user = captura
mongo.pwd = captura123
mongo.useAuthentication = true
```

### PostgreSQL 
- Update `jdbc.properties`

Change the url, username and password according to the docker-compose.yml file. 

```
jdbc.driverClassName=org.postgresql.Driver
jdbc.url=jdbc:postgresql://localhost:25432/mobileforms_database
jdbc.username=postgres
jdbc.password=captura
```
- `jdbc-test.properties` is used for the integration tests. They should be different databases

- Hibernate configuration `hibernate.properties`
    
```
hibernate.hbm2ddl.auto=update
hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
hibernate.show_sql=false
```

## Server paths configurations
    
- `server.properties`

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

## IDE Setup
You should be able to import to any IDE with maven project capabilities

-  Add a Tomcat server in your favorite IDE
#### Setup hades-settings.xml for maven configuration if neccesary
- Most IDEs will allow you to deploy war to your tomcat
  
## Grant permissions to Tomcat 
Navigate to the directory where Tomcat is located, then to the bin directory and grant permissions.
```
chmod u+x *.sh
```

## (Example) Configuration in IntelliJ Ultimate IDE:

- Open the project using IntelliJ IDEA.
- Go to `File` > `Project Structure`:
    - SDK: 1.8
    - Language level: 8 - Lambdas, type annotations, etc.
- Tomcat Configuration:
    - Click on `Current file` > `Edit Configurations` > `Add New` > `Tomcat Server` > `Local`.
    - Name: Tomcat
    - Tomcat home: `tomcat location`
    - URL: http://localhost:8080/mf/login/login.mob
    - `fix` > `fs-web:war exploded`
    - Application context: `/mf`
    - Environment variables:
        - `MOBILEFORMS_HOME`: `/opt/captura/profile`
    - Apply and save the configuration.


## Login and last steps to start designing forms

- Go to http://dev.sodep.com.py:8080/mf/login/login.mob or http://localhost:8080/mf/login/login.mob
- Developer environment has 2 default users

	- Root: root@mobileforms.sodep.com.py
	- Admin user for designing forms: admin@testappmf.sodep.com.py 

  Both passwords are: 123456


## Troubleshooting

- For a proper deployment of Tomcat, the web project must have a context different from "/", verify that this configuration is correct, if it is not, set it to "/fs_web".

- In Eclipse, this is configured by accessing the properties (right-click, Properties) of the fs_web project, then type "web" and search for the Web Context-root.
