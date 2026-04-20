<div align="center">
  <h3>
    <a href="README.md">EN</a> | 
    <a href="README-RU.md">RU</a>
  </h3>
</div>

<div align="center">

### 🎥 Видеообзор FlectonePulse

[![FlectonePulse Hytale](https://img.youtube.com/vi/UjIlfjXzdxE/maxresdefault.jpg)](https://youtu.be/UjIlfjXzdxE "Посмотреть")

</div>

<div class="center-row" align="center">
    <h1>FlectonePulse Hytale — Каждое сообщение под вашим контролем!</h1>
    <a href="https://boosty.to/thefaser"><img src="https://flectone.net/pulse/boosty.svg" alt="boosty" class="hover-brightness"></a>
    <a href="https://flectone.net/pulse/"><img src="https://flectone.net/pulse/documentation.svg" alt="documentation" class="hover-brightness"></a>
    <a href="https://discord.flectone.net/"><img src="https://flectone.net/pulse/discord.svg" alt="discord" class="hover-brightness"></a>
</div>

## 🏆 Что делает FlectonePulse Hytale особенным?

FlectonePulse Hytale — это специализированный модуль в экосистеме FlectonePulse, который берёт под контроль чат, сообщения и уведомления, созданные специально для платформы Hytale.

- Все операции выполняются асинхронно, основной поток сервера не затрагивается
- Используется Google Guice для инъекции зависимостей, что упрощает расширение функционала
- Специально разработан для платформы Hytale с нативной поддержкой компонентов

## 🎨 Гибкое форматирование текста

Поддерживаются все форматы цветов, от устаревших (`&` или `§` для цветов) до современных тегов MiniMessage

| **Ввод кода**                                        | **Преобразование**                                             |  
|-------------------------------------------------------|-------------------------------------------------------------|  
| `&0`-`&9`, `&a`-`&f`                                  | `<black>`, `<dark_blue>`, ..., `<white>`                    |  
| `&l`/`&m`/`&n`/`&o`/`&k`/`&r`                         | `<b>` / `<st>` / `<u>` / `<i>` / `<obf>` / `<reset>`        |  
| `&#rrggbb`, `#rrggbb`, `&x&r&r&g&g&b&b`, `<##rrggbb>` | `<#rrggbb>`                                                 |  
| Теги MiniMessage                                      | `<color:#rrggbb>`, `<rainbow>`, `<click:...>`, `<font>`, и т.д. |

```yaml
# ПРИМЕР
join:
  format: "<gradient:#FF0000:#00FF00>&lПривет</gradient> <rainbow><player></rainbow>!"
```

![color](https://flectone.net/pulse/hytale/welcomemessage.png)

## 🧱 Сообщения над игроками
Визуальные сообщения над головами игроков

![Message Bubbles](https://flectone.net/pulse/hytale/bubble.gif)

## 🌈 Настройка чата с /chatsetting

![chatsetting](https://flectone.net/pulse/hytale/commandchatsetting.gif)

Команда /chatsetting открывает меню для быстрой кастомизации сообщений каждому игроку. Выберите цвета, стили и отключите ненужные сообщения

## 🌍 Умная локализация

![Locale](https://flectone.net/pulse/hytale/locale.gif)

### Как это работает
При включённом `by_player: true` FlectonePulse определяет язык клиента и показывает сообщения на нём. Если перевода нет, будет использоваться дефолтный из конфига.

## ✨ Настраиваемые элементы

| **Визуал**                                                                         | **Описание** |  
|------------------------------------------------------------------------------------|-----------------|  
| ![join](https://flectone.net/pulse/hytale/join.png)                                | **Сообщения о входе** <br> Приветствуйте игроков |  
| ![death](https://flectone.net/pulse/hytale/death.png)                              | **Сообщения о смерти** <br> Сделайте их забавными с текстом или звуками |  
| ![chat](https://flectone.net/pulse/hytale/chat.png)                                | **Форматирование чата** <br> Улучшите чат с цветами, градиентами и другими возможностями |  
| Полный список в [документации](https://flectone.net/pulse/docs/hytale/message/) 🔗 | ...                                                               |

## 🤝 Интеграции

### Внешние платформы

| **Визуал** | **Описание** |  
|---------|----------|  
| [![discord](https://flectone.net/pulse/discordmessage.png)](https://flectone.net/pulse/docs/hytale/integration/discord/) | **Discord** <br> Синхронизируйте чат сервера с каналами Discord |  
| [![telegram](https://flectone.net/pulse/telegrammessage2.png)](https://flectone.net/pulse/docs/hytale/integration/telegram/) | **Telegram** <br> Отправляйте сообщения через бота в Telegram и синхронизируйте чаты |  
| [![twitch](https://flectone.net/pulse/twitchmessage.png)](https://flectone.net/pulse/docs/hytale/integration/twitch/) | **Twitch** <br> Уведомления о стримах в чате Hytale и синхронизация чата сервера |  

### Плагины

| **Плагин**                                                                                | **Описание**                                                 |  
|-----------------------------------------------------------------------------------------------|--------------------------------------------------------------------|  
| 🛡️ **LuckPerms**                   | Управление правами и группами         |

## 🎮 Более 30 команд

| **Визуал** | **Описание** |  
|--------------|------------------------|  
| [![ball](https://flectone.net/pulse/hytale/commandball.png)](https://flectone.net/pulse/docs/hytale/command/) | **/ball** <br> Волшебный шар с множеством ответов |  
| [![stream](https://flectone.net/pulse/hytale/commandstream.png)](https://flectone.net/pulse/docs/hytale/command/) | **/stream** <br> Уведомления о стримах в чате |  
| [![try](https://flectone.net/pulse/hytale/commandtry.png)](https://flectone.net/pulse/docs/hytale/command/) | **/try** <br> Испытай удачу от 0% до 100% |  
| Полный список в [документации](https://flectone.net/pulse/docs/hytale/command/) 🔗              

## 🙏 Благодарности

FlectonePulse Hytale основан на этих проектах:

- 🏗️ **[Google Guice](https://github.com/google/guice)** — для модульного кода
- 📚 **[JDBI](https://jdbi.org/)** с **[HikariCP](https://github.com/brettwooldridge/HikariCP)** — эффективная работа с БД
- 📦 **[Jackson](https://github.com/FasterXML/jackson)** — сериализация данных
- 🧙 **[Adventure Hytale](https://github.com/ArikSquad/adventure-platform-hytale)** — форматирование текста для Hytale
- 🎨 **[Adventure](https://github.com/KyoriPowered/adventure)** — форматирование текста
- ⌨️ **[Cloud](https://github.com/Incendo/cloud)** — команды с автодополнением
- 🔣 **[Symbol Chat](https://github.com/replaceitem/symbol-chat)** — символы в чате
- 🖥️ **[HyUI](https://github.com/Elliesaur/HyUI)** — GUI-элементы
- 🌐 **[MiniTranslator](https://github.com/imDaniX/MiniTranslator)** — конвертация устаревших цветов
- 🌱 **[FlectoneChat](https://github.com/Flectone/FlectoneChat)** — предок FlectonePulse

И спасибо сообществу! Каждая звезда на GitHub и отзыв на платформах показывают, что FlectonePulse действительно нужен ⭐

## 📊 Статистика проекта
<div align="center">
  <a href="https://flectone.net/en/pulse/docs/metrics" target="_blank">
    <img src="https://flectone.net/api/pulse/metrics/svg" alt="Статистика FlectonePulse">
  </a>
</div>

## ❤️ Код открытый, а проект бесплатный

FlectonePulse Hytale полностью бесплатный. Скачивайте, модифицируйте, ставьте на сервер. А для приоритетной поддержки, раннего доступа к фичам и помощи с настройками под ваш сервер поддержите на Boosty. Это мотивирует развивать проект дальше!

<div align="center">
  <a href="https://boosty.to/thefaser"><img src="https://flectone.net/pulse/boosty.svg" alt="boosty" class="hover-brightness"></a>
  <h2><b>FlectonePulse Hytale ждёт вас! Готовы установить? 😎</b></h2>
  <br>
  <h3>P.S. Присоединяйтесь к <a href="https://discord.flectone.net/">Discord</a></h3>
</div>