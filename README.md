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
| `FLYWAY_LOCATIONS` | `classpath:db/migration,classpath:db/demo` | Папки миграций; уберите `db/demo` в продакшене |

## Страницы

| URL | Что это |
|---|---|
| `/` | Главная: выбор недели, полки «Популярное», «Манхва», жанры |
| `/catalog?q=&genre=&type=manga\|manhwa\|manhua&sort=popular\|new\|title` | Каталог с фильтрами. Фильтры хранятся в URL, ссылкой можно поделиться |
| `/title/{slug}` | Страница тайтла со списком глав (старые ссылки `/title/{id}` редиректят 301) |
| `/read/{slug}/{глава}` | Читалка, например `/read/solo-leveling/1` или `/read/naruto/12.5`; `?page=N` открывает нужную страницу |

Поиск по всему сайту: `⌘K` / `Ctrl K` или `/`.

### Читалка

- **Лента** (по умолчанию для манхвы и маньхуа): вертикальная прокрутка без промежутков, ширина 640 / 800 px или во весь экран.
- **Страницы** (по умолчанию для манги): справа налево или слева направо, масштаб по высоте или по ширине.
- Нажатие на левую или правую треть экрана листает, по центру — показывает или прячет панели. Работают свайпы и клавиши: `←` `→`, `Space`, `[` `]` (главы), `F` (полный экран).
- Панели прячутся при чтении и возвращаются при прокрутке вверх.
- Прогресс сохраняется в браузере: на главной появляется полка «Продолжить чтение», на странице тайтла — кнопка «Продолжить · гл. N» и отметки прочитанных глав.
- Следующие страницы и следующая глава подгружаются заранее; экран не гаснет во время чтения (Screen Wake Lock).
- Без JavaScript читалка работает как обычная лента.

### Демо-главы

Чтобы читалку можно было попробовать без реального контента, миграция `db/demo/R__demo_chapters.sql` добавляет каждому тайтлу 3 демо-главы с абстрактными страницами-заглушками из `static/media/demo`. В продакшене отключите её:

```bash
FLYWAY_LOCATIONS=classpath:db/migration
```

## API

Все ответы — JSON. Ошибки — в формате [RFC 7807](https://www.rfc-editor.org/rfc/rfc7807) (`application/problem+json`).

| Метод | URL | Описание |
|---|---|---|
| GET | `/api/manga/all` | Все тайтлы (по рейтингу) |
| GET | `/api/manga/popular` | То же, по убыванию рейтинга |
| GET | `/api/manga/{id}` | Один тайтл, `404` если не найден |
| GET | `/api/manga/genre?genre=Боевик` | Тайтлы жанра |
| GET | `/api/manga/{id}/chapters` | Главы тайтла по порядку |
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
│   ├── services/      каталог, читалка (главы, соседние главы)
│   ├── repositories/  Spring Data JDBC
│   ├── models/        Manga, Chapter, ChapterPage, MangaType, Slugs
│   ├── web/           CatalogFilter — построение ссылок фильтров
│   └── config/        WebConfig, проверка админ-токена
└── resources/
    ├── db/migration/  миграции Flyway
    ├── db/demo/       демо-главы (отключаются в продакшене)
    ├── templates/     Thymeleaf: layout-фрагменты, страницы, ошибки
    └── static/
        ├── css/       tokens → base → components → pages
        ├── js/app.js  тема, полки, поиск ⌘K, «продолжить чтение»
        ├── js/reader.js  читалка
        ├── img/       иконки (SVG-спрайт), favicon
        ├── images/    обложки
        └── media/demo/  страницы-заглушки для демо-глав
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
- [x] **Фаза 2** — главы, страницы с размерами, slug-адреса *(осталось: жанры M:N, пагинация каталога)*
- [x] **Фаза 3** — читалка: лента и постраничный режим, жесты, клавиши, прогресс чтения
- [ ] **Фаза 4** — оптимизация изображений (WebP/AVIF, srcset), Lighthouse ≥ 95
- [ ] **Фаза 5** — аккаунты, библиотека, синхронизация прогресса

## Авторы

Jalgas Arnat, Almassov Aidyn. Лицензия MIT.
