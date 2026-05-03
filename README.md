# charm-back

Учебный pet-проект в формате mini-Tinder на `Java 21`.

Production deployment:

- `https://charm-app.ru`
- VPS + Docker Compose + host nginx + Let's Encrypt
- Selectel S3 backups через restic

Проект собран как multi-module Maven-репозиторий:

- `back` - основное web-приложение на `Spring Boot`, `Spring MVC`, `Spring Security`, `JSP`
- `linecount-maven-plugin` - кастомный Maven plugin для подсчета строк и выгрузки маршрутов

## Стек

- Java 21
- Maven Wrapper (`mvnw`, `mvnw.cmd`)
- Spring Boot 3
- Spring MVC
- Spring Security
- Spring JDBC
- Jakarta Servlet / JSP / JSTL
- PostgreSQL
- Redis
- Flyway
- JJWT
- HikariCP
- Jackson
- iTextPDF
- OpenAPI / Swagger UI
- Spring Boot Actuator
- Micrometer / Prometheus
- Grafana
- restic
- Selectel S3
- JUnit 5
- Mockito
- Testcontainers
- Docker / Docker Compose
- nginx
- Let's Encrypt / Certbot
- GitHub Actions
- VPS deployment

## Что умеет приложение

- регистрация, логин, логаут
- просмотр и редактирование профиля
- смена email и пароля
- загрузка фото профиля в локальное файловое хранилище
- выгрузка профиля в PDF
- список анкет и список matches
- механика рекомендаций, лайков и matches
- UI на JSP
- REST API в `/api/v1/*`
- session-based аутентификация для UI
- JWT Bearer аутентификация для REST API
- локализация через `words*.properties`

## Структура репозитория

```text
.
|-- pom.xml
|-- Dockerfile
|-- compose.yml
|-- compose.dockerhub.yml
|-- .dockerignore
|-- .gitattributes
|-- .env.example
|-- deploy/
|   |-- backup.env.example
|   |-- compose.yml
|   |-- prod.env.example
|   |-- scripts/
|   |   `-- backup.sh
|   `-- monitoring/
|       `-- prometheus/
|           `-- prometheus.yml
|-- .github/
|   `-- workflows/
|       |-- ci.yml
|       `-- docker-publish.yml
|-- back/
|   |-- pom.xml
|   |-- src/main/java/ru/andrewb/charm/back
|   |   |-- config
|   |   |-- controller
|   |   |-- dao
|   |   |-- dto
|   |   |-- mapper
|   |   |-- model
|   |   |-- normalizer
|   |   |-- security
|   |   |-- service
|   |   |-- validator
|   |   `-- web
|   |-- src/main/resources/
|   |   |-- application.yml
|   |   |-- application-docker.yml
|   |   |-- application-local.example.yml
|   |   |-- db/migration
|   |   |-- db/sql
|   |   `-- words*.properties
|   |-- src/main/webapp/
|   |   |-- WEB-INF/jsp
|   |   |-- img
|   |   `-- favicon.ico
|   `-- src/test/java/ru/andrewb/charm/back
`-- linecount-maven-plugin/
    |-- pom.xml
    `-- src/main/java/ru/andrewb/charm/plugin/linecount
```

## Конфигурация

Конфиг читается в таком порядке:

1. JVM system properties: `-Dkey=value`
2. environment variables: например `APP_DATASOURCE_URL` для `app.datasource.url`
3. `application-<profile>.yml`
4. `application.yml`

Базовый конфиг лежит в:

- `back/src/main/resources/application.yml`

Профили:

- `local` - локальный запуск вне Docker
- `docker` - запуск в Docker Compose

Шаблон локального конфига:

- `back/src/main/resources/application-local.example.yml`

Локальный файл нужно создать как:

- `back/src/main/resources/application-local.yml`

И не коммитить его в репозиторий.

Обязательные параметры приложения:

- `app.datasource.url`
- `app.datasource.username`
- `app.datasource.password`
- `app.content.base-path`
- `app.redis.host`
- `app.jwt.secret`

Полезные параметры:

- `spring.profiles.active=local`
- `spring.datasource.hikari.maximum-pool-size=10`
- `app.redis.port=6379`
- `app.jwt.access-token-ttl-min=60`

## Запуск готового образа из Docker Hub (без локальной сборки)

### Локально

1. Создать локальный `.env` на основе `.env.example`
2. Запустить окружение через Docker Compose

```powershell
git clone <repo-url>
cd charm-back
Copy-Item .env.example .env
```

В `compose.dockerhub.yml` по умолчанию лежит:

```yaml
services:
  app:
    image: andrewbcodes/charm-back:${APP_IMAGE_TAG:-latest}
```

Запуск:

```powershell
docker compose -f compose.yml -f compose.dockerhub.yml pull app
docker compose -f compose.yml -f compose.dockerhub.yml up -d --no-build
```

Остановка:

```powershell
docker compose -f compose.yml -f compose.dockerhub.yml down
```

Что поднимется:

- `postgres`
- `redis`
- `flyway`
- `app`

После старта приложение доступно на:

- `http://localhost:8080/`

### На VPS

Для production-запуска используется отдельный compose-файл:

- `deploy/compose.yml`

Он отличается от локального окружения:

- PostgreSQL и Redis не публикуют порты наружу и доступны только внутри Docker network
- приложение публикуется только на `127.0.0.1:8080`, внешний трафик принимает host nginx
- миграции Flyway применяются самим Spring Boot приложением из classpath
- дополнительно поднимаются Prometheus и Grafana для мониторинга
- Prometheus доступен только внутри Docker network и собирает метрики приложения с `/actuator/prometheus`
- Grafana публикуется только на `127.0.0.1:3000` и открывается через SSH tunnel
- для контейнеров включен `restart: unless-stopped`

Production-схема:

```text
Internet -> nginx 80/443 -> 127.0.0.1:8080 -> Docker app
```

Основной домен:

- `https://charm-app.ru`

Алиасы редиректятся на основной домен:

- `https://www.charm-app.ru`
- `https://чарм-апп.рф`
- `https://www.чарм-апп.рф`

На сервере (предварительно создать пользователя deploy):

```bash
sudo mkdir -p /opt/charm
sudo chown -R deploy:deploy /opt/charm
cd /opt/charm
```

Создать production `.env` на сервере:

```powershell
scp -i $env:USERPROFILE\.ssh\charm_vps_ed25519 deploy/prod.env.example deploy@SERVER_IP:/opt/charm/.env
```

На сервере заполнить реальные значения в `.env`:

```bash
nano /opt/charm/.env
```

Обязательные production-секреты:

- `POSTGRES_PASSWORD`
- `APP_DATASOURCE_PASSWORD`
- `APP_JWT_SECRET`

`POSTGRES_PASSWORD` и `APP_DATASOURCE_PASSWORD` должны совпадать.

В production OpenAPI endpoints отключаются через переменные окружения:

- `SPRINGDOC_API_DOCS_ENABLED=false`
- `SPRINGDOC_SWAGGER_UI_ENABLED=false`

Grafana credentials задаются через:

- `GRAFANA_ADMIN_USER`
- `GRAFANA_ADMIN_PASSWORD`

Запуск:

```bash
cd /opt/charm
docker compose pull
docker compose up -d
docker compose ps
```

После merge в `main` обновление выполняется автоматически через GitHub Actions `Docker Publish`.
Workflow копирует `deploy/*` в `/opt/charm`, не трогая серверный `.env`, затем выполняет `docker compose pull`, `docker compose up -d` и HTTPS healthcheck `https://charm-app.ru/`.

Host nginx настраивается отдельно от Docker Compose. Минимальная схема:

```nginx
server {
    listen 80;
    server_name charm-app.ru www.charm-app.ru xn----7sbb0crac8c.xn--p1ai www.xn----7sbb0crac8c.xn--p1ai;

    return 301 https://charm-app.ru$request_uri;
}

server {
    listen 443 ssl;
    server_name www.charm-app.ru xn----7sbb0crac8c.xn--p1ai www.xn----7sbb0crac8c.xn--p1ai;

    ssl_certificate /etc/letsencrypt/live/charm-app.ru/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/charm-app.ru/privkey.pem;
    include /etc/letsencrypt/options-ssl-nginx.conf;
    ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem;

    return 301 https://charm-app.ru$request_uri;
}

server {
    listen 443 ssl;
    server_name charm-app.ru;

    ssl_certificate /etc/letsencrypt/live/charm-app.ru/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/charm-app.ru/privkey.pem;
    include /etc/letsencrypt/options-ssl-nginx.conf;
    ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem;

    client_max_body_size 10m;

    location /actuator {
        return 404;
    }

    location / {
        proxy_pass http://127.0.0.1:8080;

        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
```

HTTPS-сертификат выпускается через Let's Encrypt:

```bash
sudo certbot --nginx \
  -d charm-app.ru \
  -d www.charm-app.ru \
  -d xn----7sbb0crac8c.xn--p1ai \
  -d www.xn----7sbb0crac8c.xn--p1ai
```

Prometheus и Grafana поднимаются тем же production compose:

- Prometheus собирает метрики с `app:8080/actuator/prometheus` внутри Docker network
- Grafana публикуется только на `127.0.0.1:3000`

Доступ к Grafana на VPS выполняется через SSH tunnel:

```powershell
ssh -i $env:USERPROFILE\.ssh\charm_vps_ed25519 -L 3000:127.0.0.1:3000 deploy@SERVER_IP
```

После открытия туннеля Grafana доступна локально:

```text
http://localhost:3000
```

Prometheus datasource в Grafana:

```text
http://prometheus:9090
```

## Быстрый старт через Docker (с локальной сборкой)

1. Создать локальный `.env` на основе `.env.example`
2. Убедиться, что `APP_JWT_SECRET` задан значением длиной не менее `32` символов
3. Запустить окружение через Docker Compose

```powershell
Copy-Item .env.example .env
docker compose up --build
```

## Локальный запуск вне Docker

1. Поднять PostgreSQL и Redis локально
2. Создать `back/src/main/resources/application-local.yml` на основе `application-local.example.yml`
3. Указать локальный `app.jwt.secret` (длиной не менее `32` символов)
4. Собрать и запустить приложение

```powershell
Copy-Item back/src/main/resources/application-local.example.yml back/src/main/resources/application-local.yml
./mvnw -pl back -am clean package
java -jar back/target/back-1.0-SNAPSHOT.war --spring.profiles.active=local
```

## Flyway

Миграции лежат в:

- `back/src/main/resources/db/migration`

Текущие миграции:

- `V1__create_profile_tables.sql`
- `V2__seed_dev_profiles.sql`

В локальном Docker Compose миграции применяются автоматически отдельным `flyway` сервисом.

В production compose миграции применяются самим Spring Boot приложением при старте.

Для локального запуска вне Docker можно прогнать их вручную:

```powershell
./mvnw -f back/pom.xml `
  -Dapp.datasource.url=jdbc:postgresql://localhost:5432/charm `
  -Dapp.datasource.username=charm `
  -Dapp.datasource.password=charmpass `
  flyway:migrate
```

Тестовые пользователи из `V2__seed_dev_profiles.sql`:

- `admin@charm.ru / qwerty`
- `ivanov@mail.ru / 123456`
- `sidorova@mail.ru / 456789`

Дополнительный bulk seed:

- `back/src/main/resources/db/sql/seed_bulk_1k.sql`

## Backup и restore

Production backup настроен на VPS через `restic` в S3-compatible bucket Selectel:

- endpoint: `s3.ru-7.storage.selcloud.ru`
- region: `ru-7`
- bucket: `charm-backup`
- restic repository: `s3:https://s3.ru-7.storage.selcloud.ru/charm-backup`

В репозитории лежат шаблоны production backup-конфигурации:

- `deploy/scripts/backup.sh` - backup-скрипт
- `deploy/backup.env.example` - пример env-файла для restic и S3

При деплое GitHub Actions копирует содержимое `deploy/*` в `/opt/charm`, поэтому backup-скрипт попадает на VPS как:

```bash
/opt/charm/scripts/backup.sh
```

Перед первым запуском на VPS должен быть установлен `restic`:

```bash
sudo apt update
sudo apt install -y restic
restic version
```

Файл с реальными S3-ключами создается на VPS вручную из шаблона и не хранится в git:

```bash
cp /opt/charm/backup.env.example /opt/charm/.backup.env
nano /opt/charm/.backup.env
chmod 600 /opt/charm/.backup.env
chmod +x /opt/charm/scripts/backup.sh
```

После заполнения `/opt/charm/.backup.env` restic-репозиторий нужно инициализировать один раз:

```bash
source /opt/charm/.backup.env
restic init
restic snapshots
```

Если репозиторий уже был создан раньше, `restic init` повторно выполнять не нужно.

Backup состоит из двух частей:

- PostgreSQL dump в формате `pg_dump -Fc`
- архив пользовательского файлового хранилища `charm-content`

Секреты для backup хранятся на VPS в файле:

```bash
/opt/charm/.backup.env
```

Файл не должен попадать в git. В нем задаются переменные:

```bash
export AWS_ACCESS_KEY_ID="..."
export AWS_SECRET_ACCESS_KEY="..."
export AWS_DEFAULT_REGION="ru-7"

export RESTIC_REPOSITORY="s3:https://s3.ru-7.storage.selcloud.ru/charm-backup"
export RESTIC_PASSWORD="..."
```

`AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY` и `AWS_DEFAULT_REGION` используются restic для подключения к S3.
`RESTIC_REPOSITORY` указывает, где лежит backup-репозиторий.
`RESTIC_PASSWORD` используется restic для шифрования backup; без него восстановление невозможно.

Скрипт:

1. создает dump базы из контейнера `charm-postgres`
2. создает архив файлов из контейнера `charm-back`
3. отправляет оба файла в Selectel S3 через `restic backup`
4. применяет retention policy через `restic forget --prune`
5. удаляет локальные backup-файлы старше 14 дней

Ручной запуск:

```bash
/opt/charm/scripts/backup.sh
```

Проверка snapshots:

```bash
source /opt/charm/.backup.env
restic snapshots
```

Backup запускается ежедневно через cron:

```cron
20 3 * * * /opt/charm/scripts/backup.sh >> /opt/charm/backups/backup.log 2>&1
```

Локальные backup-файлы лежат в:

```bash
/opt/charm/backups/db
/opt/charm/backups/content
```

### Restore check

Восстановить последний snapshot из Selectel S3 в тестовую директорию:

```bash
source /opt/charm/.backup.env
mkdir -p /opt/charm/restore-test
restic restore latest --target /opt/charm/restore-test
```

После restore файлы обычно лежат по исходным абсолютным путям внутри target-директории:

```bash
/opt/charm/restore-test/opt/charm/backups/db/*.dump
/opt/charm/restore-test/opt/charm/backups/content/*.tar.gz
```

Проверить восстановление базы в отдельном временном PostgreSQL-контейнере:

```bash
docker run --rm --name charm-restore-test \
  -e POSTGRES_DB=charm \
  -e POSTGRES_USER=charm \
  -e POSTGRES_PASSWORD=charmpass \
  -d postgres:17
```

Восстановить dump:

```bash
cat /opt/charm/restore-test/opt/charm/backups/db/*.dump | docker exec -i charm-restore-test \
  pg_restore -U charm -d charm --clean --if-exists
```

Проверить таблицы и количество профилей:

```bash
docker exec -it charm-restore-test psql -U charm -d charm -c '\dt'
docker exec -it charm-restore-test psql -U charm -d charm -c 'select count(*) from profile;'
```

Удалить тестовый контейнер:

```bash
docker rm -f charm-restore-test
```

Проверить архив пользовательских файлов:

```bash
mkdir -p /opt/charm/content-restore-test
tar -tzf /opt/charm/restore-test/opt/charm/backups/content/*.tar.gz | head
tar -xzf /opt/charm/restore-test/opt/charm/backups/content/*.tar.gz -C /opt/charm/content-restore-test
find /opt/charm/content-restore-test -type f | head
```

Backup считается рабочим только после успешной проверки восстановления базы и пользовательских файлов.

## Maven команды

Сборка:

```powershell
./mvnw -pl back -am clean package
```

Быстрая сборка без тестов:

```powershell
./mvnw -pl back -am package -DskipTests
```

Unit-тесты:

```powershell
./mvnw -pl back -am test
```

Полная проверка включая integration-тесты:

```powershell
./mvnw -pl back -am verify
```

## Тесты

Проект покрыт:

- unit-тестами для `validator`, `normalizer`, `mapper`, `security`, `service`, exception handlers
- integration-тестами для auth flow, profile flow, admin access, DAO-слоя и `charm` flow

Схема запуска тестов:

- `./mvnw test` - только unit-тесты через `surefire`
- `./mvnw verify` - unit + integration-тесты через `failsafe`

Integration-тесты используют Testcontainers:

- `PostgreSQLContainer`
- `GenericContainer` для Redis

## CI/CD

В проекте настроены GitHub Actions workflows:

- `CI` - запускается на pull request и push в `main`
- `Docker Publish` - запускается на push в `main` и на git tags вида `v*.*.*`

`CI` проверяет проект автоматически:

```powershell
./mvnw -pl back -am test
./mvnw -pl back -am verify
```

`test` запускает unit-тесты через Surefire. `verify` запускает unit- и integration-тесты через Surefire/Failsafe.

`Docker Publish` собирает Docker image, публикует его в Docker Hub и деплоит `latest` на VPS:

- при push в `main` публикуются теги `latest` и `dev`
- после успешной публикации из `main` workflow копирует `deploy/*` на сервер
- сервер выполняет `docker compose pull`, `docker compose up -d` и HTTPS healthcheck
- при push git tag вида `v0.4.0` публикуется Docker tag `0.4.0`
- при push git tag автодеплой на VPS не выполняется

Docker image:

```text
andrewbcodes/charm-back
```

Для публикации используются GitHub Actions secrets:

```text
DOCKERHUB_TOKEN
```

Имя пользователя Docker Hub берется из GitHub Actions variable:

```text
DOCKERHUB_USERNAME
```

Для автодеплоя на VPS используются GitHub Actions variables:

```text
VPS_HOST
VPS_USER
VPS_APP_DIR
```

И GitHub Actions secret:

```text
VPS_SSH_KEY
```

## Основные маршруты

UI:

- `/`
- `/index`
- `/login`
- `/logout`
- `/registration`
- `/profile`
- `/settings`
- `/charm`
- `/matches`
- `/admin/profile/*`
- `/admin/profiles`
- `/content/*`

REST:

- `POST /api/v1/auth/login`
- `POST /api/v1/registration`
- `GET /api/v1/profile`
- `PUT /api/v1/profile`
- `PUT /api/v1/profile/email`
- `PUT /api/v1/profile/password`
- `GET /api/v1/charm`
- `POST /api/v1/charm`
- `GET /api/v1/matches`
- `GET /api/v1/admin/profiles`
- `GET /api/v1/admin/profiles/{id}`
- `PUT /api/v1/admin/profiles/{id}`
- `DELETE /api/v1/admin/profiles/{id}`

## JWT для REST API

REST API работает stateless и ожидает Bearer token в заголовке `Authorization`.

Получение токена:

```powershell
curl -X POST http://localhost:8080/api/v1/auth/login `
  -H "Content-Type: application/json" `
  -d "{\"email\":\"admin@charm.ru\",\"password\":\"qwerty\"}"
```

Пример использования токена:

```powershell
curl http://localhost:8080/api/v1/profile `
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

Без токена защищенные REST endpoints должны возвращать `401`.

## Swagger / OpenAPI

REST API документируется через `springdoc-openapi`.

Локально и в dev-окружении доступны:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- OpenAPI YAML: `http://localhost:8080/v3/api-docs.yaml`
- Группа REST API: `http://localhost:8080/v3/api-docs/rest-api`

В Swagger UI можно получить JWT через `POST /api/v1/auth/login`, нажать `Authorize` и вставить токен в формате `Bearer <token>`.

В production Swagger UI и OpenAPI endpoints отключены через:

- `SPRINGDOC_API_DOCS_ENABLED=false`
- `SPRINGDOC_SWAGGER_UI_ENABLED=false`

## Особенности реализации

- приложение использует `Spring JDBC`
- основной DAO-слой построен на `JdbcTemplate`
- bulk update статусов работает через Spring transaction boundary на service-слое
- UI остается на form login и HTTP session
- `/api/v1/**` использует отдельную stateless security chain с JWT filter
- Redis используется как быстрый слой для механики `charm`, а не как основное хранилище
- файловое хранилище работает поверх локальной директории из `app.content.base-path`
- профиль `docker` предназначен для запуска через Docker Compose

## Дальнейшие шаги

- добавление k6 smoke/load tests
- дальнейший cleanup конфигурации и infrastructure beans
- возможная миграция с JSP на более современный view layer
