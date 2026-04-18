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
- HikariCP или `pool`-модуль
- Jackson
- jBCrypt
- iTextPDF
- JUnit 5
- Mockito
- Docker / Docker Compose

## Что умеет приложение

- регистрация, логин, логаут
- просмотр и редактирование профиля
- смена email и пароля
- загрузка фото профиля в локальное файловое хранилище
- выгрузка профиля в PDF
- список анкет и список matches
- механика рекомендаций, лайков и matches
- UI на JSP и REST API в `/api/v1/*`
- локализация через `words*.properties`

## Структура репозитория

```text
.
|-- pom.xml
|-- Dockerfile
|-- compose.yml
|-- .env.example
|-- back/
|   |-- pom.xml
|   |-- src/main/java
|   |-- src/main/resources
|   |   |-- application.yml
|   |   |-- application-docker.yml
|   |   |-- application-local.example.yml
|   |   `-- db/migration
|   |-- src/main/webapp
|   `-- src/test/java
|-- pool/
|   |-- pom.xml
|   `-- src/main/java
`-- linecount-maven-plugin/
    |-- pom.xml
    `-- src/main/java
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

Полезные параметры:

- `spring.profiles.active=local`
- `app.datasource.pool-impl=hikari`
- `app.datasource.pool-size=10`
- `app.redis.port=6379`

## Быстрый старт через Docker

1. Создать локальный `.env` на основе `.env.example`
2. Запустить окружение через Docker Compose

```powershell
cp .env.example .env
docker compose up --build
```

Что поднимется:

- `postgres`
- `redis`
- `flyway`
- `app`

После старта приложение доступно на:

- `http://localhost:8080/`

## Локальный запуск вне Docker

1. Поднять PostgreSQL и Redis локально
2. Создать `back/src/main/resources/application-local.yml` на основе `application-local.example.yml`
3. Собрать и запустить приложение

Сборка и запуск:

```powershell
./mvnw -pl back -am clean package
java -jar back/target/back-1.0-SNAPSHOT.war --spring.profiles.active=local
```

## Flyway

Миграции лежат в:

- `back/src/main/resources/db/migration`

Текущие миграции:

- `V1__create_profile_tables.sql`
- `V2__seed_dev_profiles.sql`

В Docker Compose миграции применяются автоматически отдельным `flyway` сервисом.

Для локального запуска вне Docker можно прогнать их вручную:

```powershell
./mvnw -f back/pom.xml -Dapp.datasource.url=jdbc:postgresql://localhost:5432/charm -Dapp.datasource.username=charm -Dapp.datasource.password=charmpass flyway:migrate
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
./mvnw -Pfast -pl back -am package
```

Тесты:

```powershell
./mvnw -pl back test
```

Проверка с quality profile:

```powershell
./mvnw -Pquality -pl back -am verify
```

## Основные маршруты

UI:

- `/`
- `/index`
- `/login`
- `/registration`
- `/profile`
- `/settings`
- `/charm`
- `/matches`
- `/admin/profile/*`
- `/admin/profiles`
- `/content/*`

REST:

- `/api/v1/login`
- `/api/v1/logout`
- `/api/v1/registration`
- `/api/v1/charm`
- `/api/v1/matches`
- `/api/v1/admin/profile/*`
- `/api/v1/admin/profiles`

## Особенности реализации

- приложение использует `Spring JDBC`
- основной DAO-слой построен на `JdbcTemplate`
- bulk update статусов работает через Spring transaction boundary на service-слое
- Redis используется как быстрый слой для механики `charm`, а не как основное хранилище
- файловое хранилище работает поверх локальной директории из `app.content.base-path`
- профиль `docker` предназначен для запуска через Docker Compose

## Тесты

Проект покрыт unit-тестами для:

- validator и normalizer слоев
- mapper-классов
- `ProfileService`
- `CharmService`

Запуск:

```powershell
./mvnw -pl back test
```

## Дальнейшие шаги

- дальнейший cleanup конфигурации и infrastructure beans
- возможная миграция с JSP на более современный view layer
- расширение integration- и web-тестов
- дальнейшее развитие Docker-окружения
