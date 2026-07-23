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
  <a href="https://modrinth.com/plugin/flectonepulse"><img src="https://flectone.net/assets/badges/bukkit.webp" alt="bukkit"></a>
  <a href="https://modrinth.com/plugin/flectonepulse"><img src="https://flectone.net/assets/badges/spigot.webp" alt="spigot"></a>
  <a href="https://modrinth.com/plugin/flectonepulse"><img src="https://flectone.net/assets/badges/paper.webp" alt="paper"></a>
  <a href="https://modrinth.com/plugin/flectonepulse"><img src="https://flectone.net/assets/badges/purpur.webp" alt="purpur"></a>
  <a href="https://modrinth.com/plugin/flectonepulse"><img src="https://flectone.net/assets/badges/folia.webp" alt="folia"></a>
  <a href="https://modrinth.com/plugin/flectonepulse"><img src="https://flectone.net/assets/badges/fabric.webp" height="40" alt="fabric"></a>
  <a href="https://www.curseforge.com/hytale/mods/flectonepulse"><img src="https://flectone.net/assets/badges/hytale.webp" alt="hytale"></a>
  <a href="https://modrinth.com/plugin/flectonepulse"><img src="https://flectone.net/assets/badges/bungeecord.webp" alt="bungeecord"></a>
  <a href="https://modrinth.com/plugin/flectonepulse"><img src="https://flectone.net/assets/badges/velocity.webp" alt="velocity"></a>

  <h1>FlectonePulse Hytale – Каждое сообщение под вашим контролем!</h1>

<a href="https://boosty.to/thefaser"><img src="https://flectone.net/assets/badges/boosty.webp" alt="boosty"></a>
<a href="https://modrinth.com/plugin/flectonepulse"><img src="https://flectone.net/assets/badges/modrinth.webp" alt="modrinth"></a>
<a href="https://flectone.net/pulse/"><img src="https://flectone.net/assets/badges/documentation.webp" alt="документация"></a>
<a href="https://discord.flectone.net/"><img src="https://flectone.net/assets/badges/discord.webp" alt="discord"></a>
</div>

## Что такое FlectonePulse Hytale? [![Спросить DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/Flectone/FlectonePulse)

FlectonePulse Hytale — это специальный модуль экосистемы FlectonePulse, который полностью контролирует чат, сообщения и уведомления на серверах **Hytale**. Ты решаешь, что видят игроки, когда они это видят и как это выглядит. Красивые настройки по умолчанию для новичков, глубокая конфигурация для всех остальных

FlectonePulse стал темой **университетской дипломной работы** и получил высшие оценки за архитектуру, техническую глубину и практическую ценность. Полный текст диплома, презентация и отчёт о проверке на плагиат находятся в открытом доступе:
- 📄 [Диплом (PDF)](https://flectone.net/files/r/flectonepulse/Thesis.pdf)
- 📊 [Презентация к диплому (PPTX)](https://flectone.net/files/r/flectonepulse/Thesis_Presentation.pptx)
- ✅ [Отчёт о плагиате (PDF)](https://flectone.net/files/r/flectonepulse/Plagiarism_Report.pdf)

### Почему FlectonePulse Hytale вместо других плагинов для сообщений?

| Возможность                                                       | FlectonePulse Hytale |        Обычный плагин для сообщений         |
|-------------------------------------------------------------------|:--------------------:|:-------------------------------------------:|
| Полностью асинхронный – не нагружает главный поток                |          ✅           |                      ❌                      |
| Нативная поддержка UI и компонентов Hytale                        |          ✅           |                      ❌                      |
| Модульный дизайн – включение/отключение любой функции             |          ✅           |                 ⚠️ (редко)                  |
| Определение языка клиента для каждого игрока                      |          ✅           |                      ❌                      |
| Визуальные баблы сообщений над игроками                           |          ✅           |                      ❌                      |
| Внутриигровое меню настроек игрока (`/chatsetting`)               |          ✅           |                      ❌                      |
| Кроссплатформенный чат (Discord, Telegram, Twitch)                |          ✅           |                      ❌                      |
| Поддержка устаревших цветовых кодов + MiniMessage в одной системе |          ✅           |           ⚠️ (обычно что-то одно)           |
| Внедрение зависимостей Google Guice для чистой архитектуры        |          ✅           |                      ❌                      |
| Открытый исходный код и полностью бесплатно                       |          ✅           | ⚠️ (часто есть бесплатная и платная версия) |

### Требования

**ТРЕБУЕТСЯ** Java 17 или выше

| Платформа  | Поддерживаемые версии    |
|------------|--------------------------|
| **Hytale** | Последняя версия Hytale  |

### Файлы конфигурации

| Файл                  | Назначение                                      |
|-----------------------|-------------------------------------------------|
| `config.yml`          | База данных, язык, режим прокси, кэш            |
| `command.yml`         | Включение/отключение и настройка команд         |
| `message.yml`         | Все модули сообщений, условия и поведение       |
| `integration.yml`     | Сторонние плагины и внешние платформы           |
| `permission.yml`      | Права для каждой функции                        |
| `localizations/*.yml` | Текст сообщений по языкам                       |

## 🎨 Форматирование цветов

FlectonePulse Hytale понимает любую систему цветов и преобразует их в MiniMessage. Можно смешивать старые коды и современные теги в одной строке – всё работает

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

[![color](https://flectone.net/pulse/hytale/example_formatted_message.webp)](https://flectone.net/pulse/docs/hytale/message/join/)

## 🌍 Определение языка для каждого игрока

Включите `by_player: true`, и каждое сообщение будет доставлено на языке игрока. Если перевод отсутствует, используется язык сервера по умолчанию

[![locale](https://flectone.net/pulse/hytale/locale.webp)](https://flectone.net/pulse/docs/config/language/)

## ✨ Всё – это сообщение, и всё настраивается

FlectonePulse Hytale берёт под контроль практически каждое событие, создающее текст

| Модуль                                                                                                                                                | Описание                                                                     |
|-------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------|
| [![Join](https://flectone.net/pulse/hytale/message_join.webp)](https://flectone.net/pulse/docs/hytale/message/join/)                                 | Полностью форматированные сообщения о входе с плейсхолдерами и звуками       |
| [![Quit](https://flectone.net/pulse/hytale/message_quit.webp)](https://flectone.net/pulse/docs/hytale/message/quit/)                                 | Настраиваемые сообщения о выходе                                             |
| [![Death](https://flectone.net/pulse/hytale/message_vanilla_death.webp)](https://flectone.net/pulse/docs/hytale/message/vanilla/)                    | Замена стандартных сообщений о смерти своим стилем                           |
| [![Chat](https://flectone.net/pulse/hytale/message_chat.webp)](https://flectone.net/pulse/docs/hytale/message/chat/)                                 | Полное форматирование чата с градиентами, наведением, кликом и многим другим |
| [![Bubbles](https://flectone.net/pulse/hytale/message_bubble.webp)](https://flectone.net/pulse/docs/hytale/message/bubble/)                          | Показ сообщений визуально над головами игроков                               |
| [![AFK](https://flectone.net/pulse/hytale/message_afk.webp)](https://flectone.net/pulse/docs/hytale/message/afk/)                                   | Афк-суффикс и уведомление, когда игрок неактивен                             |

Полный список в [документации 🔗](https://flectone.net/pulse/docs/hytale/message/)

## 🌈 Индивидуальная настройка для игроков

Игроки открывают полноценное игровое меню `/chatsetting`, чтобы переключать сообщения, менять цвета и отключать то, что им не нужно

[![chatsetting](https://flectone.net/pulse/hytale/command_chatsetting.webp)](https://flectone.net/pulse/docs/hytale/command/chatsetting/)

## 🎮 40+ встроенных команд

| Модуль                                                                                                                     | Описание                                                                                                       |
|----------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------|
| [![ball](https://flectone.net/pulse/hytale/command_ball.webp)](https://flectone.net/pulse/docs/hytale/command/ball/)       | **/ball** – магический шар с настраиваемыми ответами                                                           |
| [![stream](https://flectone.net/pulse/hytale/command_stream.webp)](https://flectone.net/pulse/docs/hytale/command/stream/) | **/stream** – объявление о начале стрима                                                                       |
| [![try](https://flectone.net/pulse/hytale/command_try.webp)](https://flectone.net/pulse/docs/hytale/command/try/)          | **/try** – случайная удача от 0% до 100%                                                                       |
| [![ban](https://flectone.net/pulse/hytale/command_ban.webp)](https://flectone.net/pulse/docs/command/ban/)                 | **/ban** – бан игрока с причиной и опциональной длительностью                                                  |
| [![whitelist](https://flectone.net/pulse/hytale/command_whitelist.webp)](https://flectone.net/pulse/docs/command/whitelist/) | **/whitelist** – управление белым списком с причиной и опциональной длительностью (add, remove, list, on, off) |

Уже существуют: `/afk` `/anon` `/ban` `/broadcast` `/chatcolor` `/chatsetting` `/clearchat` `/coin` `/deletemessage` `/dice` `/do` `/emit` `/geolocate` `/helper` `/ignore` `/ignorelist` `/kick` `/mail` `/maintenance` `/me` `/mute` `/mutelist` `/nickname` `/online` `/ping` `/poll` `/reply` `/spy` `/tell` `/toponline` `/translateto` `/unban` `/unmute` `/unwarn` `/warn` `/warnlist` `/whitelist`

Полный список в [документации](https://flectone.net/pulse/docs/hytale/command/) 🔗

## 🤝 Интеграция с платформами

| Платформа                                                                                                                    | Что даёт                                                                                                                |
|------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------|
| [![discord](https://flectone.net/pulse/integration_discord_message.webp)](https://flectone.net/pulse/docs/hytale/integration/discord/)     | **Discord** – двусторонняя синхронизация чата. Свои команды бота, поддержка вебхуков с ембедами и отображением аватаров |
| [![telegram](https://flectone.net/pulse/integration_telegram_message_minecraft.webp)](https://flectone.net/pulse/docs/hytale/integration/telegram/) | **Telegram** – двусторонняя синхронизация чата. Свои команды бота                                                       |
| [![twitch](https://flectone.net/pulse/integration_twitch_message.webp)](https://flectone.net/pulse/docs/hytale/integration/twitch/)        | **Twitch** – двусторонняя синхронизация чата. Свои команды бота и уведомления о начале стрима                           |

## 🤝 Интеграция с плагинами

| Плагин                                                                                      | Что даёт                                 |
|---------------------------------------------------------------------------------------------|------------------------------------------|
| 🛡️ **[LuckPerms](https://flectone.net/pulse/docs/hytale/integration/luckperms/)**          | Префикс, суффикс, отображение группы     |
| 🧩 **[PlaceholderAPI](https://flectone.net/pulse/docs/hytale/integration/placeholderapi/)** | Любые `%placeholder%` из других плагинов |

Полный список в [документации](https://flectone.net/pulse/docs/hytale/integration/) 🔗

## 🙏 Благодарности

FlectonePulse Hytale основан на этих проектах:

- 🏗️ **[Google Guice](https://github.com/google/guice)** – внедрение зависимостей
- 📚 **[JDBI](https://jdbi.org/)** вместе с **[HikariCP](https://github.com/brettwooldridge/HikariCP)** – доступ к базе данных
- 📦 **[Jackson](https://github.com/FasterXML/jackson)** – сериализация данных
- 🧙 **[Adventure Hytale](https://github.com/ArikSquad/adventure-platform-hytale)** – форматирование текстовых компонентов для Hytale
- 🎨 **[Adventure](https://github.com/KyoriPowered/adventure)** – форматирование текстовых компонентов
- ⌨️ **[Cloud](https://github.com/Incendo/cloud)** – команды с автодополнением
- 🔣 **[Symbol Chat](https://github.com/replaceitem/symbol-chat)** – палитра Unicode-символов
- 🖥️ **[HyUI](https://github.com/Elliesaur/HyUI)** – элементы GUI
- 🌐 **[MiniTranslator](https://github.com/imDaniX/MiniTranslator)** – конвертация старых кодов в MiniMessage
- 🌱 **[FlectoneChat](https://github.com/Flectone/FlectoneChat)** – предок FlectonePulse

И спасибо сообществу! Каждая звезда на GitHub и отзыв на платформах показывают, что FlectonePulse действительно полезен ⭐

## 📊 Статистика проекта

<div align="center">
  <a href="https://flectone.net/pulse/metrics" target="_blank">
    <img src="https://flectone.net/api/pulse/metrics/svg" alt="Статистика FlectonePulse">
  </a>
</div>