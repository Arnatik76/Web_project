# MangaVerse

Минималистичный каталог и читалка манги и манхвы. Spring Boot + PostgreSQL, страницы рендерятся на сервере (Thymeleaf), без фронтенд-фреймворков.

## Быстрый старт

Нужны **Java 21+**, **Maven 3.9+** и **PostgreSQL 16** (или Docker).

```bash
cd Mangaverse
docker compose up -d db          # PostgreSQL на localhost:5432, база mangaverse
DB_PASSWORD=postgres mvn spring-boot:run
```

Откройте <http://localhost:8080>. Схема и стартовые данные создаются автоматически миграциями Flyway (`src/main/resources/db/migration`).

> Старая таблица `manga`, созданная прежним `schema.sql`, с миграциями несовместима. Используйте новую пустую базу `mangaverse`.

### Настройки

Все секреты передаются через переменные окружения, в репозитории их нет. Пример — в [`Mangaverse/.env.example`](Mangaverse/.env.example).

| Переменная | По умолчанию | Назначение |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/mangaverse` | Адрес базы |
| `DB_USERNAME` | `postgres` | Пользователь базы |
| `DB_PASSWORD` | пусто | Пароль базы |
| `MANGAVERSE_ADMIN_TOKEN` | пусто | Токен для записи через API. Пусто — запись выключена |

## Страницы

| URL | Что это |
|---|---|
| `/` | Главная: выбор недели, полки «Популярное», «Манхва», жанры |
| `/catalog?q=&genre=&type=manga\|manhwa\|manhua&sort=popular\|new\|title` | Каталог с фильтрами. Фильтры хранятся в URL, ссылкой можно поделиться |
| `/title/{id}` | Страница тайтла |

Поиск по всему сайту: `⌘K` / `Ctrl K` или `/`.

## API

Все ответы — JSON. Ошибки — в формате [RFC 7807](https://www.rfc-editor.org/rfc/rfc7807) (`application/problem+json`).

| Метод | URL | Описание |
|---|---|---|
| GET | `/api/manga/all` | Все тайтлы (по рейтингу) |
| GET | `/api/manga/popular` | То же, по убыванию рейтинга |
| GET | `/api/manga/{id}` | Один тайтл, `404` если не найден |
| GET | `/api/manga/genre?genre=Боевик` | Тайтлы жанра |
| GET | `/api/manga/genres` | Список жанров, самые частые первыми |
| GET | `/api/manga/search?q=solo` | Поиск по названию, автору и жанру без учёта регистра (`query=` тоже работает) |
| POST | `/api/manga/new` | Добавить тайтл. Требует заголовок `X-Admin-Token` |

Пример добавления:

```bash
curl -X POST localhost:8080/api/manga/new \
  -H "X-Admin-Token: $MANGAVERSE_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Berserk","author":"Kentaro Miura","genre":"Драма","type":"MANGA",
       "description":"…","rating":4.9,"imageUrl":"/images/berserk.jpg"}'
```

`type` — `MANGA`, `MANHWA` или `MANHUA`; `rating` — от 0 до 5; `imageUrl` — путь от корня сайта (`/images/…`) или `https://` URL.

## Структура

```
Mangaverse/src/main/
├── java/org/example/mangaverse/
│   ├── controllers/   PageController (HTML), MangaApiController (JSON)
│   ├── services/      бизнес-логика, сортировки каталога
│   ├── repositories/  Spring Data JDBC
│   ├── models/        Manga, MangaType
│   ├── web/           CatalogFilter — построение ссылок фильтров
│   └── config/        WebConfig, проверка админ-токена
└── resources/
    ├── db/migration/  миграции Flyway
    ├── templates/     Thymeleaf: layout-фрагменты, страницы, ошибки
    └── static/
        ├── css/       tokens → base → components → pages
        ├── js/app.js  тема, полки, поиск ⌘K, «поделиться»
        ├── img/       иконки (SVG-спрайт), favicon
        └── images/    обложки
```

## Дизайн-система

Все цвета, размеры, скругления и тайминги анимаций — токены в [`static/css/tokens.css`](Mangaverse/src/main/resources/static/css/tokens.css). В CSS нет «магических» чисел: только `var(--…)`.

- Тёмная тема по умолчанию, светлая — по системной настройке или переключателю в шапке.
- Один акцентный цвет; контраст текста не ниже WCAG AA.
- Обложки всегда 2:3, сетка 4 pt, цели касания не меньше 44 px.
- На мобильных — нижний таб-бар, полки листаются свайпом со snap.
- Анимации только `transform`/`opacity`, `prefers-reduced-motion` учитывается.

## Тесты

```bash
cd Mangaverse && mvn test
```

Тесты контроллеров и сервисов работают без базы данных.

## Дорожная карта

- [x] **Фаза 0** — безопасность и гигиена: секреты в env, XSS, валидация, миграции, чистка репозитория
- [x] **Фаза 1** — дизайн-система и новый интерфейс: главная, каталог, страница тайтла, поиск ⌘K
- [ ] **Фаза 2** — модель данных: главы, страницы, жанры M:N, slug, пагинация
- [ ] **Фаза 3** — читалка: вертикальная лента для манхвы, постраничный режим для манги, прогресс чтения
- [ ] **Фаза 4** — оптимизация изображений (WebP/AVIF, srcset), Lighthouse ≥ 95
- [ ] **Фаза 5** — аккаунты, библиотека, синхронизация прогресса

## Авторы

Jalgas Arnat, Almassov Aidyn. Лицензия MIT.
