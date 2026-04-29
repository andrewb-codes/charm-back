# charm-back

Учебный pet-проект в формате mini-Tinder на `Java 21`.

Проект собран как multi-module Maven-репозиторий:

- `back` - основное web-приложение на `Spring Boot`, `Spring MVC`, `Spring Security`, `JSP`
- `pool` - собственный JDBC connection pool
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
- HikariCP или `pool`-модуль
- Jackson
- iTextPDF
- OpenAPI / Swagger UI
- JUnit 5
- Mockito
- Testcontainers
- Docker / Docker Compose
- GitHub Actions

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
|-- compose.prod.yml
|-- .dockerignore
|-- .env.example
|-- .env.prod.example
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
|-- pool/
|   |-- pom.xml
|   `-- src/main/java/ru/andrewb/charm/pool
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
- `app.datasource.pool-impl=hikari`
- `app.datasource.pool-size=10`
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

- `compose.prod.yml`

Он отличается от локального окружения:

- PostgreSQL и Redis не публикуют порты наружу и доступны только внутри Docker network
- миграции Flyway применяются самим Spring Boot приложением из classpath
- для контейнеров включен `restart: unless-stopped`

На сервере (предварительно создать пользователя deploy):

```bash
sudo mkdir -p /opt/charm
sudo chown -R deploy:deploy /opt/charm
cd /opt/charm
```

Скопировать файлы:

```powershell
scp -i $env:USERPROFILE\.ssh\charm_vps_ed25519 compose.prod.yml deploy@SERVER_IP:/opt/charm/compose.yml
scp -i $env:USERPROFILE\.ssh\charm_vps_ed25519 .env.prod.example deploy@SERVER_IP:/opt/charm/.env
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

Запуск:

```bash
cd /opt/charm
docker compose pull
docker compose up -d
docker compose ps
```

Обновление после публикации нового Docker image:

```bash
cd /opt/charm
docker compose pull
docker compose up -d
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

`Docker Publish` собирает Docker image и публикует его в Docker Hub:

- при push в `main` публикуются теги `latest` и `dev`
- при push git tag вида `v0.4.0` публикуется Docker tag `0.4.0`

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

Чтобы выпустить новую версию Docker image:

```powershell
git tag -a v0.4.0 -m "Release v0.4.0"
git push origin v0.4.0
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

После запуска приложения доступны:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- OpenAPI YAML: `http://localhost:8080/v3/api-docs.yaml`
- Группа REST API: `http://localhost:8080/v3/api-docs/rest-api`

В Swagger UI можно получить JWT через `POST /api/v1/auth/login`, нажать `Authorize` и вставить токен в формате `Bearer <token>`.

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

- настройка домена и HTTPS для VPS-deploy
- добавление CD workflow для автоматического обновления VPS после публикации Docker image
- отключение dev-only возможностей в production
- настройка backup PostgreSQL volume и пользовательского content volume
- дальнейший cleanup конфигурации и infrastructure beans
- возможная миграция с JSP на более современный view layer
