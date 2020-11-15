## Configuración de Mongo en Captura

### 1. Crear usuario administrador

Todo lo que está entre `[]` abajo debe ser modificado

```sh
$ mongo --port 27017
> use admin
> db.createUser( { user: "capAdmin", pwd: "[YOUR_ADMIN_PASS]”, roles: [ { role: "userAdminAnyDatabase", db: "admin" } ] } )

```

Anotar este usuario y password en algún lado.

### 2. Re-start del servicio Mongo con autentiación habilitada

Al levantar el servicio de mongo, si se usa un archivo `mongo.conf`, agregar la línea:

```bash
security:
  authorization: enabled
```
Y luego hacer el re-start del servicio.

O bien hacer el re-start del servicio con el comando:

```sh
$ mongod --auth --port 27017 --dbpath /data/db1
```

### 3. Crear usuario de testing

**Observación:** Esto es solo para servidores donde se ejecuten los tests unitarios

Todo lo que esté entre `[]` abajo debe ser modificado.

1) Nos conectamos con el user admin

```
$ mongo --port 27017 -u "capAdmin" -p [YOUR_ADMIN_PASS] --authenticationDatabase "admin"
```

2) Creamos el usuario sobre la base de testing `mobileforms_test``

```sh
> use mobileforms_test
> var myRoles = [{role: "readWrite", db:"mobileforms_test"}, {role:"dbAdmin", db:"mobileforms_test"}]
> db.createUser({ user: "capTest", pwd:"[CAP_TEST_PASS]", roles: myRoles })
```

Este usuario debe tener también permisos de hacer drop

### 4. Crear usuario de Captura

1) Nos conectamos con el user admin

```sh
$ mongo --port 27017 -u "capAdmin" -p [YOUR_ADMIN_PASS] --authenticationDatabase "admin"
```

2) Creamos el usuario sobre la base de producción `mongo_mf_slot0_demo`

```
> use mongo_mf_slot0_demo
> var myRoles = [ {role: "readWrite", db: "mongo_mf_slot0_demo"} ]
> db.createUser({ user: "capApp", pwd:"[CAP_APP_PASS]", roles: myRoles })
```

### 4. Configurar las credenciales en Captura

Para el usuario de producción

```sh
$ vi /srv/workspace/configuration/slot0/profile/mongo.properties 
```

```bash
mongo.host = localhost
mongo.port = 27017
mongo.database = mongo_mf_slot0_demo
mongo.dataCollection = data
mongo.user = capApp
mongo.pwd = [CAPP_APP_PASS]
```

Para servidores o entornos donde se ejecutan los test unitarios

```sh
$ vi $MOBILEFORMS_HOME/mongo-test.properties
```

```bash
mongo.host = localhost
mongo.port = 27017
mongo.database = mobileforms_test
mongo.user = capTest
mongo.pwd = [CAPP_TEST_PASS]
```

