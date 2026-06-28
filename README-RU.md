<div align="center">
  <h3>
    <a href="README.md">EN</a> |
    <a href="README-RU.md">RU</a>
  </h3>
</div>

<div align="center">

### 🎥 Видеообзор FlectonePulse

[![FlectonePulse](https://img.youtube.com/vi/UjIlfjXzdxE/maxresdefault.jpg)](https://youtu.be/UjIlfjXzdxE "Смотреть")

</div>

<div align="center">
  <a href="https://modrinth.com/plugin/flectonepulse"><img src="https://flectone.net/pulse/bukkit.svg" alt="bukkit"></a>
  <a href="https://modrinth.com/plugin/flectonepulse"><img src="https://flectone.net/pulse/spigot.svg" alt="spigot"></a>
  <a href="https://modrinth.com/plugin/flectonepulse"><img src="https://flectone.net/pulse/paper.svg" alt="paper"></a>
  <a href="https://modrinth.com/plugin/flectonepulse"><img src="https://flectone.net/pulse/purpur.svg" alt="purpur"></a>
  <a href="https://modrinth.com/plugin/flectonepulse"><img src="https://flectone.net/pulse/folia.svg" alt="folia"></a>
  <a href="https://modrinth.com/plugin/flectonepulse"><img src="https://flectone.net/pulse/fabric.svg" height="40" alt="fabric"></a>
  <a href="https://www.curseforge.com/hytale/mods/flectonepulse"><img src="https://flectone.net/pulse/hytale.svg" alt="hytale"></a>
  <a href="https://modrinth.com/plugin/flectonepulse"><img src="https://flectone.net/pulse/bungeecord.svg" alt="bungeecord"></a>
  <a href="https://modrinth.com/plugin/flectonepulse"><img src="https://flectone.net/pulse/velocity.svg" alt="velocity"></a>

  <h1>FlectonePulse – Каждое сообщение под вашим контролем!</h1>

  <a href="https://boosty.to/thefaser"><img src="https://flectone.net/pulse/boosty.svg" alt="boosty"></a>
  <a href="https://modrinth.com/plugin/flectonepulse"><img src="https://flectone.net/pulse/modrinth.svg" alt="modrinth"></a>
  <a href="https://flectone.net/pulse/"><img src="https://flectone.net/pulse/documentation.svg" alt="документация"></a>
  <a href="https://discord.flectone.net/"><img src="https://flectone.net/pulse/discord.svg" alt="discord"></a>
</div>


## Что такое FlectonePulse? [![Спросить DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/Flectone/FlectonePulse)

FlectonePulse – это **плагин** и **мод** для **серверов Minecraft (и Hytale)**, который полностью контролирует чат, сообщения и уведомления. Ты решаешь, что видят игроки, когда они это видят и как это выглядит. Красивые настройки по умолчанию для новичков, глубокая конфигурация для всех остальных

FlectonePulse стал темой **университетской дипломной работы** и получил высшие оценки за архитектуру, техническую глубину и практическую ценность. Полный текст диплома, презентация и отчёт о проверке на плагиат находятся в открытом доступе
- 📄 [Диплом (PDF)](https://flectone.net/files/r/flectonepulse/Thesis.pdf)
- 📊 [Презентация к диплому (PPTX)](https://flectone.net/files/r/flectonepulse/Thesis_Presentation.pptx)
- ✅ [Отчёт о плагиате (PDF)](https://flectone.net/files/r/flectonepulse/Plagiarism_Report.pdf)

### Почему FlectonePulse вместо других плагинов сообщений?
| Возможность                                                                              | FlectonePulse |        Обычный плагин для сообщений         |
|------------------------------------------------------------------------------------------|:-------------:|:-------------------------------------------:|
| Полностью асинхронный – не нагружает главный поток                                       |       ✅       |                      ❌                      |
| Поддержка с 1.8.8 до самой последней версии Minecraft                                    |       ✅       |            ⚠️ (часто ограничено)            |
| Работает на Bukkit, Spigot, Paper, Purpur, Folia и Fabric                                |       ✅       |          ❌ (обычно одна платформа)          |
| Встроенная поддержка прокси (BungeeCord, Velocity)                                       |       ✅       |       ❌ (требуется отдельный плагин)        |
| Межсерверная синхронизация сообщений (через прокси или Redis)                            |       ✅       |                      ❌                      |
| [Поддержка Hytale](https://github.com/Flectone/FlectonePulse/tree/master/hytale)         |       ✅       |                      ❌                      |
| Определение языка клиента Minecraft для каждого игрока                                   |       ✅       |                      ❌                      |
| Текстуры в чате **без ресурс-пака** (`<texture:name>`)                                   |       ✅       |                      ❌                      |
| Головы игроков и встроенные спрайты прямо в сообщении (`<player_head>`, `<sprite:name>`) |       ✅       |                      ❌                      |
| Модульная архитектура – можно включить/отключить любую функцию                           |       ✅       |                 ⚠️ (редко)                  |
| 40+ встроенных команд (чат, модерация, развлечения, опросы и т.д.)                       |       ✅       |                      ❌                      |
| Кроссплатформенный чат (Discord, Telegram, Twitch)                                       |       ✅       |                      ❌                      |
| Одновременная поддержка устаревших кодов цветов и MiniMessage                            |       ✅       |           ⚠️ (обычно что-то одно)           |
| Внедрение зависимостей через Google Guice для чистой архитектуры                         |       ✅       |                      ❌                      |
| Открытый исходный код и полностью бесплатно                                              |       ✅       | ⚠️ (часто есть бесплатная и платная версия) |

### Требования

**ТРЕБУЕТСЯ** Java 17 или выше. Старые версии Minecraft, например 1.8.8, также могут работать на Java 17 (смотри [PandaSpigot](https://github.com/hpfxd/pandaspigot))

| Платформа                                    | Поддерживаемые версии      |
|----------------------------------------------|----------------------------|
| **Bukkit / Spigot / Paper / Purpur / Folia** | 1.8.8 – Последняя          |
| **Fabric**                                   | Последняя версия Minecraft |
| **BungeeCord / Waterfall**                   | Последняя                  |
| **Velocity**                                 | Последняя                  |
| **Hytale**                                   | Последняя версия Hytale    |
| **NeoForge**                                 | Запланировано              |

### Файлы конфигурации

| Файл                  | Назначение                                |
|-----------------------|-------------------------------------------|
| `config.yml`          | База данных, язык, режим прокси, кэш      |
| `command.yml`         | Включение/отключение и настройка команд   |
| `message.yml`         | Все модули сообщений, условия и поведение |
| `integration.yml`     | Сторонние плагины и внешние платформы     |
| `permission.yml`      | Права для каждого модуля                  |
| `localizations/*.yml` | Текст сообщений по языкам                 |

## 🎨 Форматирование цветов

FlectonePulse понимает любую систему цветов и преобразует их в MiniMessage. Можно смешивать старые коды и современные теги в одной строке – всё работает

| Ввод                                    | Результат                                            |
|-----------------------------------------|------------------------------------------------------|
| `&0`-`&9`, `&a`-`&f`                    | `<black>`, `<dark_blue>`, ..., `<white>`             |
| `&l` / `&m` / `&n` / `&o` / `&k` / `&r` | `<b>` / `<st>` / `<u>` / `<i>` / `<obf>` / `<reset>` |
| `&#rrggbb`, `#rrggbb`                   | `<#rrggbb>`                                          |
| MiniMessage                             | `<gradient>`, `<hover:...>`, `<click:...>`, и т.д.   |

```yaml
join:
  format: "<gradient:#FF0000:#00FF00>&lПривет</gradient> <rainbow><player></rainbow>!"
```

[![color](https://flectone.net/pulse/welcomemessage.png)](https://flectone.net/pulse/docs/message/join/)

## 🧱 Текстуры в чате без ресурс-пака

Вставляйте собственные изображения прямо в чат и MOTD сервера с помощью `<texture:name>`

[![texture motd](https://flectone.net/pulse/texturemotd.png)](https://flectone.net/pulse/docs/message/format/object/)
[![texture chat](https://flectone.net/pulse/texture.png)](https://flectone.net/pulse/docs/message/format/object/)

Аватарки игроков через `<player_head>`, встроенные спрайты Minecraft через `<sprite:name>`

[![objects in chat](https://flectone.net/pulse/object.png)](https://flectone.net/pulse/docs/message/format/object/)

## 🌍 Определение языка для каждого игрока

Включите `by_player: true`, и каждое сообщение будет доставлено на языке игрока. Если перевод отсутствует, используется язык сервера по умолчанию

[![locale](https://flectone.net/pulse/locale.gif)](https://flectone.net/pulse/docs/config/language/)

## 💬 Многоканальная система чата

Несколько именованных каналов чата с независимыми настройками. У каждого канала свой радиус, направление, задержка, звук, триггер

[![chat](https://flectone.net/pulse/chat.png)](https://flectone.net/pulse/docs/message/chat/)

Отправляйте любое отформатированное сообщение в любой destination с помощью **команды администратора `/emit`**. Доступны `CHAT` `ACTION_BAR` `BOSS_BAR` `TITLE` `SUBTITLE` `TOAST` `TAB_HEADER` `TAB_FOOTER` `BRAND`

[![commandemit](https://flectone.net/pulse/commandemit.png)](https://flectone.net/pulse/docs/command/emit/)

## 🌈 Индивидуальная настройка для игроков

Игроки открывают полноценное игровое меню `/chatsetting`, чтобы переключать сообщения, менять цвета и отключать то, что им не нужно

[![chatsetting](https://flectone.net/pulse/commandchatsetting.gif)](https://flectone.net/pulse/docs/command/chatsetting/)

## ✨ Всё – это сообщение, и всё настраивается

FlectonePulse берёт под контроль практически каждое событие, создающее текст

| Модуль                                                                                                                                                                                                           | Описание                                                                               |
|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------|
| [![MOTD](https://flectone.net/pulse/version.png)](https://flectone.net/pulse/docs/message/status/motd/)                                                                                                          | Анимированный многострочный статус сервера с собственными текстурами                   |
| [![Join](https://flectone.net/pulse/join.png)](https://flectone.net/pulse/docs/message/join/) [![Quit](https://flectone.net/pulse/quit.png)](https://flectone.net/pulse/docs/message/quit/)                      | Полное форматирование с плейсхолдерами, звуками и условиями                            |
| [![AFK](https://flectone.net/pulse/afkglobalmessage.png)](https://flectone.net/pulse/docs/message/afk/)                                                                                                          | Афк-суффикс и уведомление, когда игрок неактивен                                       |
| [![Баблы](https://flectone.net/pulse/bubble.gif)](https://flectone.net/pulse/docs/message/bubble/)                                                                                                               | Показ сообщений визуально над головами игроков                                         |
| [![Belowname](https://flectone.net/pulse/belowname.png)](https://flectone.net/pulse/docs/message/scoreboard/objective/belowname/)                                                                                | Настраиваемый текст под именем игрока                                                  |
| [![TAB](https://flectone.net/pulse/tab.png)](https://flectone.net/pulse/docs/message/tab/)                                                                                                                       | Верхняя строка, нижняя строка, формат имени игрока с пингом, рангом, миром             |
| [![Sidebar](https://flectone.net/pulse/scoreboard.png)](https://flectone.net/pulse/docs/message/sidebar/)                                                                                                        | Анимированный scoreboard в боковой панели                                              |
| [![Bossbar](https://flectone.net/pulse/bossbar_raid.png)](https://flectone.net/pulse/docs/message/bossbar/)                                                                                                      | Форматирование ванильной полосы босса                                                  |
| [![Anvil](https://flectone.net/pulse/anvil.gif)](https://flectone.net/pulse/docs/message/anvil/)                                                                                                                 | Форматированный текст (книга, табличка, наковальня)                                    |
| [![Auto](https://flectone.net/pulse/auto.png)](https://flectone.net/pulse/docs/message/auto/)                                                                                                                    | Запланированные повторяющиеся объявления                                               |
| [![Ссылки сервера](https://flectone.net/pulse/serverlink.png)](https://flectone.net/pulse/docs/message/serverlink/)                                                                                              | Настраиваемые кликабельные ссылки в меню сервера                                       |
| [![Brand](https://flectone.net/pulse/brand.png)](https://flectone.net/pulse/docs/message/brand/)                                                                                                                 | Отображение своего текста на экране отладки F3                                         |
| [![Death](https://flectone.net/pulse/deathserver.png)](https://flectone.net/pulse/docs/message/vanilla/) [![Advancement](https://flectone.net/pulse/task.png)](https://flectone.net/pulse/docs/message/vanilla/) | Замена стандартных сообщений Minecraft на свои (сообщения о смерти, достижения и т.д.) |

Полный список в [документации 🔗](https://flectone.net/pulse/docs/message/)

## 🎮 40+ встроенных команд

| Модуль                                                                                                              | Описание                                                                                                       |
|---------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------|
| [![ball](https://flectone.net/pulse/commandball.png)](https://flectone.net/pulse/docs/command/ball/)                | **/ball** – магический шар с настраиваемыми ответами                                                           |
| [![tictactoe](https://flectone.net/pulse/commandtictactoe.png)](https://flectone.net/pulse/docs/command/tictactoe/) | **/tictactoe** – игра в крестики-нолики в чате                                                                 |
| [![stream](https://flectone.net/pulse/commandstream.png)](https://flectone.net/pulse/docs/command/stream/)          | **/stream** – объявление о начале стрима                                                                       |
| [![try](https://flectone.net/pulse/commandtry.png)](https://flectone.net/pulse/docs/command/try/)                   | **/try** – случайная удача от 0% до 100%                                                                       |
| [![sprite](https://flectone.net/pulse/commandsprite.png)](https://flectone.net/pulse/docs/command/sprite/)          | **/sprite** – просмотр всех встроенных спрайтов Minecraft                                                      |
| [![poll](https://flectone.net/pulse/commandpoll.png)](https://flectone.net/pulse/docs/command/poll/)                | **/poll** – создание опросов в чате с голосованием                                                             |
| [![ban](https://flectone.net/pulse/commandban.png)](https://flectone.net/pulse/docs/command/ban/)                   | **/ban** – бан игрока с причиной и опциональной длительностью                                                  |
| [![whitelist](https://flectone.net/pulse/commandwhitelist.png)](https://flectone.net/pulse/docs/command/whitelist/) | **/whitelist** – управление белым списком с причиной и опциональной длительностью (add, remove, list, on, off) |

Уже существуют: `/afk` `/anon` `/ban` `/banlist` `/broadcast` `/chatcolor` `/chatsetting` `/clearchat` `/clearmail` `/coin` `/deletemessage` `/dice` `/do` `/emit` `/geolocate` `/helper` `/ignore` `/ignorelist` `/kick` `/mail` `/maintenance` `/me` `/mute` `/mutelist` `/nickname` `/online` `/ping` `/reply` `/spy` `/tell` `/toponline` `/translateto` `/unban` `/unmute` `/unwarn` `/warn` `/warnlist` `/whitelist`

Полный список в [документации](https://flectone.net/pulse/docs/command/) 🔗

## 🤝 Интеграция с платформами

| Платформа                                                                                                             | Что даёт                                                                                                                                           |
|-----------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------|
| [![discord](https://flectone.net/pulse/discordmessage.png)](https://flectone.net/pulse/docs/integration/discord/)     | **Discord** – двусторонняя синхронизация чата. Свои команды бота, тикер информации о канале, поддержка вебхуков с ембедами и отображением аватаров |
| [![telegram](https://flectone.net/pulse/telegrammessage2.png)](https://flectone.net/pulse/docs/integration/telegram/) | **Telegram** – двусторонняя синхронизация чата. Свои команды бота и тикер информации о канале                                                      |
| [![twitch](https://flectone.net/pulse/twitchmessage.png)](https://flectone.net/pulse/docs/integration/twitch/)        | **Twitch** – двусторонняя синхронизация чата. Свои команды бота и уведомления о начале стрима                                                      |

## 🤝 Интеграция с плагинами

| Плагин                                                                                                                                                                                                                    | Что даёт                                                      |
|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------|
| 🛡️ **[LuckPerms](https://flectone.net/pulse/docs/integration/luckperms/)**                                                                                                                                               | Префикс, суффикс, отображение группы, сортировка TAB по рангу |
| 🧩 **[PlaceholderAPI](https://flectone.net/pulse/docs/integration/placeholderapi/) / [MiniPlaceholders](https://flectone.net/pulse/docs/integration/miniplaceholders)**                                                   | Любые `%placeholder%` из других плагинов                      |
| 🎙️ **[PlasmoVoice](https://flectone.net/pulse/docs/integration/plasmovoice/) / [SimpleVoice](https://flectone.net/pulse/docs/integration/simplevoice/)**                                                                 | Синхронизация мута и игнора с голосовым чатом                 |
| 🖼️ **[SkinsRestorer](https://flectone.net/pulse/docs/integration/skinsrestorer/)**                                                                                                                                       | Настоящие скины игроков в чате и TAB                          |
| 👻 **[SuperVanish](https://flectone.net/pulse/docs/integration/supervanish/)**                                                                                                                                            | Невидимые игроки скрываются автоматически (даже через прокси) |
| 💬 **[InteractiveChat](https://flectone.net/pulse/docs/integration/interactivechat/)**                                                                                                                                    | Расширенные интерактивные элементы чата                       |
| 🔨 **[AdvancedBan](https://flectone.net/pulse/docs/integration/advancedban) / [LibertyBans](https://flectone.net/pulse/docs/integration/libertybans) / [LiteBans](https://flectone.net/pulse/docs/integration/litebans)** | Перенаправление команд бана в вашу существующую систему       |
| 🌐 **[Vault](https://flectone.net/pulse/docs/integration/vault)**                                                                                                                                                         | Совместимость прав                                            |
| 🗣️ **[Floodgate](https://flectone.net/pulse/docs/integration/floodgate) / [Geyser](https://flectone.net/pulse/docs/integration/geyser)**                                                                                 | Поддержка игроков Bedrock                                     |
| 📦 **[ItemsAdder](https://flectone.net/pulse/docs/integration/itemsadder/)**                                                                                                                                              | Собственные текстуры и предметы в сообщениях                  |

Полный список в [документации](https://flectone.net/pulse/docs/integration/) 🔗

## 🙏 Благодарности

FlectonePulse основан на этих проектах:

- 🏗️ **[Google Guice](https://github.com/google/guice)** – внедрение зависимостей
- 📚 **[JDBI](https://jdbi.org/)** вместе с **[HikariCP](https://github.com/brettwooldridge/HikariCP)** – доступ к базе данных
- 📦 **[Jackson](https://github.com/FasterXML/jackson)** – сериализация данных
- 🧙 **[PacketEvents](https://github.com/retrooper/packetevents)** – обработка событий на уровне пакетов
- 🎨 **[Adventure](https://github.com/KyoriPowered/adventure)** – форматирование текстовых компонентов
- ⌨️ **[Cloud](https://github.com/Incendo/cloud)** – команды с автодополнением
- ⏱️ **[Universal Scheduler](https://github.com/Anon8281/UniversalScheduler)** – кроссплатформенное планирование задач
- 🔣 **[Symbol Chat](https://github.com/replaceitem/symbol-chat)** – палитра Unicode-символов
- 🌐 **[MiniTranslator](https://github.com/imDaniX/MiniTranslator)** – конвертация старых кодов в MiniMessage
- 🌱 **[FlectoneChat](https://github.com/Flectone/FlectoneChat)** – предок FlectonePulse

И спасибо сообществу! Каждая звезда на GitHub и отзыв на платформах показывают, что FlectonePulse действительно полезен ⭐

## 📊 Статистика проекта

<div align="center">
  <a href="https://flectone.net/pulse/metrics" target="_blank">
    <img src="https://flectone.net/api/pulse/metrics/svg" alt="Статистика FlectonePulse">
  </a>
</div>