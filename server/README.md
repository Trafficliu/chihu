## Setup MySQL:
### 1. Install Docker
### 2. Install MySQL server in a Docker container
```shell
docker run -p 3306:3306 --name chihu_dev_db -e MYSQL_ROOT_PASSWORD=chihu_dev_db -d mysql:8 --default-authentication-plugin=mysql_native_password -h 127.0.0.1 --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci --skip-character-set-client-handshak
```

### 3. Go to the server directory(chihu/server). Create and set up DB with queries in chihu_db_setup.sql:
```shell
docker exec -i chihu_dev_db mysql -u root -pchihu_dev_db mysql< chihu_db_setup.sql
```

### 4. Open mysql terminal then login with password:
```shell
  docker exec -it chihu_dev_db bash
  export LC_ALL="C.UTF-8" && mysql -h localhost -P 3306 -u root --protocol tcp -pchihu_dev_db -D chihu_db
```

### 5. Your terminal is now using chihu_db