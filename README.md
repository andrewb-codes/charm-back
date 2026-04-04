# charm-back

Учебный pet-проект в формате mini-Tinder на `Java 21`.

Приложение собрано как multi-module Maven-репозиторий:

- `back` - основное web-приложение на `Jakarta Servlet/JSP`, собирается в `war`
- `pool` - собственный JDBC connection pool
- `linecount-maven-plugin` - кастомный Maven plugin для подсчета строк и выгрузки маршрутов

## Быстрый старт

1. Поднять PostgreSQL и Redis
2. Создать `back/src/main/resources/application-local.properties`
3. Применить миграции Flyway
4. Запустить приложение через Cargo

```powershell
docker run -d --name charm-postgres -e POSTGRES_DB=charm -e POSTGRES_USER=charm -e POSTGRES_PASSWORD=charmpass -p 5432:5432 postgres:17
docker run -d --name charm-redis -p 6379:6379 redis:7 redis-server --appendonly yes
.\mvnw.cmd -f back\pom.xml "-Dapp.datasource.url=jdbc:postgresql://localhost:5432/charm" "-Dapp.datasource.username=charm" "-Dapp.datasource.password=charmpass" flyway:migrate
.\mvnw.cmd -pl back -Dapp.profile.active=local cargo:run
```

## Стек

- Java 21
- Maven Wrapper (`mvnw`, `mvnw.cmd`)
- Jakarta Servlet / JSP / JSTL
- PostgreSQL
- Redis
- Flyway
- HikariCP или `pool`-модуль
- SLF4J Simple
- Jackson
- jBCrypt
- iTextPDF
- JUnit 5
- Mockito

## Что умеет приложение

- регистрация, логин, логаут
- просмотр и редактирование профиля
- смена email и пароля
- загрузка фото профиля в локальное файловое хранилище
- выгрузка профиля в PDF
- список анкет и список матчей
- механика рекомендаций, лайков и матчей
- web-интерфейс на JSP и REST API в `/api/v1/*`
- локализация через `words*.properties`
- unit-тесты бизнес-логики и миграции БД через Flyway

## Структура репозитория

```text
.
|-- pom.xml
|-- back/
|   |-- pom.xml
|   |-- src/main/java
|   |-- src/main/resources
|   |   `-- db/migration
|   |-- src/main/webapp
|   |-- src/test/java
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
2. переменные окружения: `APP_DATASOURCE_URL` для `app.datasource.url`
3. `application-<profile>.properties`
4. `application.properties`

Базовые настройки лежат в `back/src/main/resources/application.properties`.
Шаблон для локального профиля лежит в `back/src/main/resources/application-example.properties`.

Обязательные параметры:

- `app.datasource.url`
- `app.datasource.username`
- `app.datasource.password`
- `app.content.base-path`
- `app.redis.host`

Полезные опции:

- `app.profile.active=local`
- `app.datasource.pool.impl=hikari` или другое значение для собственного `pool`
- `app.datasource.pool.size=10`
- `app.redis.port=6379`

Настройки Redis, которые влияют на механику `charm`:

- `app.redis.charm-queue-ttl-sec` - TTL очереди кандидатов для пользователя
- `app.redis.charm-empty-ttl-sec` - TTL "пустого маркера", чтобы временно не ходить в БД, если кандидатов нет
- `app.redis.charm-lock-ttl-sec` - TTL Redis-лока на refill очереди кандидатов

Пример локального файла:

```properties
app.datasource.url=jdbc:postgresql://localhost:5432/charm
app.datasource.username=charm
app.datasource.password=charmpass
app.datasource.driver-class-name=org.postgresql.Driver

app.content.base-path=C:/tmp/charm-content

app.redis.host=localhost
app.redis.port=6379

app.profile.active=local
```

Локальный файл лучше создать как:

- `back/src/main/resources/application-local.properties`

и не коммитить его в репозиторий.

## Подготовка инфраструктуры

Нужны:

- PostgreSQL с пустой БД `charm`
- Redis
- директория для файлового контента, указанная в `app.content.base-path`

### Docker-инфраструктура

Создать volume и сеть:

```powershell
docker volume create charm-postgres-data
docker volume create charm-redis-data
docker network create charm-net
```

Поднять PostgreSQL:

```powershell
docker run -d `
  --name charm-postgres `
  --network charm-net `
  -e POSTGRES_DB=charm `
  -e POSTGRES_USER=charm `
  -e POSTGRES_PASSWORD=charmpass `
  -p 5432:5432 `
  -v charm-postgres-data:/var/lib/postgresql/data `
  postgres:17
```

Поднять Redis:

```powershell
docker run -d `
  --name charm-redis `
  --network charm-net `
  -p 6379:6379 `
  -v charm-redis-data:/data `
  redis:7 redis-server --appendonly yes
```

Загрузить схему и тестовые данные в PostgreSQL через Flyway:

```powershell
.\mvnw.cmd -f back\pom.xml "-Dapp.datasource.url=jdbc:postgresql://localhost:5432/charm" "-Dapp.datasource.username=charm" "-Dapp.datasource.password=charmpass" flyway:migrate
```

Файлы миграций находятся в:
- `back/src/main/resources/db/migration`

Текущие миграции:
- `V1__create_profile_tables.sql`
- `V2__seed_dev_profiles.sql`

При первом запуске Flyway создаст схему и применит dev-данные, при повторных запусках будут применяться только новые миграции.

Дополнительный bulk seed не входит в обязательные миграции и может использоваться отдельно для локального наполнения тестовыми данными:
- `back/src/main/resources/sql/seed_bulk_1k.sql`

Тестовые данные:

- `V2__seed_dev_profiles.sql` создает несколько тестовых пользователей:
  - `admin@charm.ru / qwerty`
  - `ivanov@mail.ru / 123456`
  - `sidorova@mail.ru / 456789`
- `back/src/main/resources/sql/seed_bulk_1k.sql` создает тысячу тестовых пользователей

## Запуск

Перед запуском приложения нужно применить миграции Flyway:

```powershell
.\mvnw.cmd -f back\pom.xml "-Dapp.datasource.url=jdbc:postgresql://localhost:5432/charm" "-Dapp.datasource.username=charm" "-Dapp.datasource.password=charmpass" flyway:migrate
```

Запуск через embedded Tomcat 11 и Cargo:

```powershell
.\mvnw.cmd -pl back -am package
```
```powershell
.\mvnw.cmd -pl back -Dapp.profile.active=local cargo:run
```

По умолчанию приложение поднимается на:

- `http://localhost:8080/`

Если конфиг удобнее передавать через переменные окружения:

```powershell
$env:APP_PROFILE_ACTIVE="local"
$env:APP_DATASOURCE_PASSWORD="charmpass"
$env:APP_CONTENT_BASE_PATH="C:\tmp\charm-content"
.\mvnw.cmd -pl back -am cargo:run
```

## Maven-профили

- `fast` - пропускает тесты
- `quality` - запускает кастомный plugin с целями `routes` и `count`
- `release` - тесты включены

Примеры:

```powershell
.\mvnw.cmd -Pfast -pl back -am package
.\mvnw.cmd -Pquality -pl back -am verify
```

## Тесты

Основные команды:

```powershell
.\mvnw.cmd -pl back test
.\mvnw.cmd -Pquality -pl back -am verify
```

Проект покрыт unit-тестами для:

- validator и normalizer слоев
- security helper-классов
- mapper-классов
- `ProfileService` и `CharmService`
- `AuthUtils` и `RequestParamUtils`

## Основные маршруты

UI:

- `/index`
- `/login`
- `/registration`
- `/profile`
- `/profiles`
- `/settings`
- `/charm`
- `/matches`
- `/content/*`

REST:

- `/api/v1/login`
- `/api/v1/logout`
- `/api/v1/registration`
- `/api/v1/profile/*`
- `/api/v1/profiles`
- `/api/v1/charm`
- `/api/v1/matches`

## Особенности реализации

- при старте `SetupCheck` валидирует обязательный конфиг, создает каталог контента и делает `ping` в PostgreSQL и Redis
- профиль можно экспортировать в PDF через `GET /profile/pdf`, а для админа целевой профиль выбирается параметром `?id=...`
- файловое хранилище работает поверх локальной директории из `app.content.base-path`
- освобождение ресурсов пулов и Redis выполняется в `ApplicationListener`

## Как используется Redis

Redis в проекте используется не как основное хранилище, а как быстрый слой для механики `charm`.

- для каждого пользователя кэшируется очередь следующих кандидатов в ключах вида `charm:queue:<userId>`
- если подходящих анкет сейчас нет, ставится временный маркер `charm:empty:<userId>`, чтобы не дергать БД на каждый запрос
- при refill очереди используется Redis-lock `charm:lock:<userId>`, чтобы несколько параллельных запросов одного пользователя не пересобирали очередь одновременно

Лок настроен так:

- захват делается через `SET key value NX EX <ttl>`
- значение лока - случайный token
- освобождение делается Lua-скриптом: ключ удаляется только если token совпал

Практический смысл TTL:

- `charm-queue-ttl-sec` ограничивает жизнь кэша очереди кандидатов
- `charm-empty-ttl-sec` задает cooldown, когда приложение не пытается заново искать кандидатов в БД
- `charm-lock-ttl-sec` ограничивает время жизни лока, чтобы он не завис навсегда при падении обработчика

## Дальнейшие шаги

- миграция приложения на Spring Boot
- перевод конфигурации и инфраструктурных компонентов на Spring beans
- использование Flyway как единственного источника правды для схемы БД
- контейнеризация приложения через Docker и запуск полного окружения через `docker compose`
- расширение тестов integration- и web-уровня
