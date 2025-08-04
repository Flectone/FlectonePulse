<div align="center">
  <h3>
    <a href="README.md">EN</a> | 
    <a href="README-RU.md">RU</a>
  </h3>
</div>

![pulse](https://flectone.net/pulse/flectonepulse.png)
<div class="center-row" align="center">
    <h1>FlectonePulse — Каждое сообщение под вашим контролем!</h1>
    <a href="https://www.spigotmc.org/"><img src="https://flectone.net/pulse/bukkit.svg" alt="bukkit" class="hover-brightness"></a>
    <a href="https://www.spigotmc.org/"><img src="https://flectone.net/pulse/spigot.svg" alt="spigot" class="hover-brightness"></a>
    <a href="https://papermc.io/"><img src="https://flectone.net/pulse/paper.svg" alt="paper" class="hover-brightness"></a>
    <a href="https://purpurmc.org/"><img src="https://flectone.net/pulse/purpur.svg" alt="purpur" class="hover-brightness"></a>
    <a href="https://papermc.io/software/folia"><img src="https://flectone.net/pulse/folia.svg" alt="folia" class="hover-brightness"></a>
    <a href="https://www.spigotmc.org/wiki/bungeecord/"><img src="https://flectone.net/pulse/bungeecord.svg" alt="bungeecord" class="hover-brightness"></a>
    <a href="https://papermc.io/software/velocity"><img src="https://flectone.net/pulse/velocity.svg" alt="velocity" class="hover-brightness"></a>
</div>

<div align="center">

### 🎥 FlectonePulse Видеообзор

[![FlectonePulse](https://img.youtube.com/vi/UjIlfjXzdxE/maxresdefault.jpg)](https://youtu.be/UjIlfjXzdxE "Смотреть")

</div>

---

## 🏆 Ключевые особенности

- ⚡ **Оптимизированная производительность**  
  Все операции выполняются асинхронно, что не влияет на основной поток. Конфигурационные файлы загружаются в фоновом режиме, обеспечивая плавную работу сервера даже при высокой нагрузке

- 🏭 **Модульная архитектура с Google Guice**  
  Построена с использованием внедрения зависимостей для чистого и поддерживаемого кода. Легко расширяемая функциональность

- 🔄 **Широкая совместимость**  
  Полная поддержка Bukkit, Spigot, Paper, Purpur, Folia, BungeeCord и Velocity. FlectonePulse адаптируется к любой серверной среде

- 🎨 **Обширные возможности настройки**  
  Настраивайте цвета, анимации и интеграции с Discord, Telegram или Twitch. Преобразуйте даже сообщения о смерти в уникальный опыт!

---

<div align="center">

[![logo](https://github.com/user-attachments/assets/dc68fd41-8341-43e5-9c07-843e1ad839f1)](https://flectone.net/pulse/)  
<h3>🚀 Установите FlectonePulse — Оживите ваш сервер! 💖</h3>
  <div>
    <a href="https://boosty.to/thefaser"><img src="https://flectone.net/pulse/boosty.svg" alt="boosty" class="hover-brightness"></a>
    <a href="https://modrinth.com/plugin/flectonepulse"><img src="https://flectone.net/pulse/modrinth.svg" alt="modrinth" class="hover-brightness"></a>
    <a href="https://flectone.net/pulse/"><img src="https://flectone.net/pulse/documentation.svg" alt="documentation" class="hover-brightness"></a>
    <a href="https://discord.flectone.net/"><img src="https://flectone.net/pulse/discord.svg" alt="discord" class="hover-brightness"></a>
  </div>
</div>

---

## 🎨 **Гибкое форматирование текста**

FlectonePulse предлагает универсальную систему форматирования текста, поддерживающую устаревшие цветовые коды, современные градиенты и теги MiniMessage для максимальной гибкости

| **Входной код**                                       | **Преобразуется в**                                         |  
|-------------------------------------------------------|-------------------------------------------------------------|  
| `&0`-`&9`, `&a`-`&f`                                  | `<black>`, `<dark_blue>`, ..., `<white>`                    |  
| `&l`/`&m`/`&n`/`&o`/`&k`/`&r`                         | `<b>` / `<st>` / `<u>` / `<i>` / `<obf>` / `<reset>`        |  
| `&#rrggbb`, `#rrggbb`, `&x&r&r&g&g&b&b`, `<##rrggbb>` | `<#rrggbb>`                                                 |  
| Теги MiniMessage                                      | `<color:#rrggbb>`, `<rainbow>`, `<click:...>`, `<font>`, и т.д. |

*Используйте `&` или `§` взаимозаменяемо для удобства*

```yaml
welcome-message: "<gradient:#FF0000:#00FF00>&lПривет</gradient> <rainbow><player></rainbow>!"
```

![color](https://flectone.net/pulse/welcomemessage.png)

---

## 🌈 **Настройка чата с /chatsetting**

![chatsetting](https://flectone.net/pulse/commandchatsetting.gif)

Используйте команду `/chatsetting` для создания потрясающих дизайнов чата. Настраивайте сообщения с яркими цветами и стилями

---

## 🌍 **Автоматическая локализация**

### ⚙️ Как это работает
[![locale](https://flectone.net/pulse/locale.gif)](https://flectone.net/pulse/docs/config#language-player)

- 🔄 **Автоматическое определение языка**  
  При включенной опции `language-player` FlectonePulse определяет языковые настройки Minecraft игроков и отображает сообщения на их предпочитаемом языке

- 🛠️ **Механизм отката**  
  Если язык игрока недоступен, плагин по умолчанию использует настроенный язык

**→ Подробнее в** [документации](https://flectone.net/pulse/docs/config#language-player) 🔗

---

## ✨ **Настраиваемые функции**

Настройте каждый аспект сообщений вашего сервера под ваше видение:

| 🖼️ **Визуал** | 💬 **Описание** |venida  
|---------------|-----------------|  
| ![status](https://flectone.net/pulse/version.png) | **MOTD сервера** <br> Преобразуйте MOTD с анимациями или кастомными сообщениями |  
| ![join](https://flectone.net/pulse/join.png) | **Сообщения о входе** <br> Приветствуйте игроков с кастомными звуками или Title сообщениями |  
| ![tab](https://flectone.net/pulse/tab.png) | **TAB** <br> Отображайте ключевую информацию, такую как пинг, онлайн-статус или ранги в меню TAB |  
| ![death](https://flectone.net/pulse/deathserver.png) | **Сообщения о смерти** <br> Добавьте изюминку с кастомным текстом или звуками |  
| ![brand](https://flectone.net/pulse/brand.png) | **Брендинг** <br> Покажите логотип вашего сервера в меню F3 |  
| ![advancement](https://flectone.net/pulse/task.png) | **Достижения** <br> Создавайте уникальные объявления о достижениях |  
| ...                                                | ...                                                               |

**...и многое другое!** Полные детали в [документации message.yml](https://flectone.net/pulse/docs/message/) 🚀

---

## 🤝 **Интеграции**

### 🌍 Внешние платформы

| 🖼️ **Визуал** | 💬 **Описание** |  
|---------|----------|  
| [![discord](https://flectone.net/pulse/discordmessage.png)](https://flectone.net/pulse/docs/integration/discord/) | **Discord** <br> Синхронизируйте чат сервера с каналами и уведомлениями Discord |  
| [![telegram](https://flectone.net/pulse/telegrammessage2.png)](https://flectone.net/pulse/docs/integration/telegram/) | **Telegram** <br> Передавайте сообщения игроков и команды администраторов через бота |  
| [![twitch](https://flectone.net/pulse/twitchmessage.png)](https://flectone.net/pulse/docs/integration/twitch/) | **Twitch** <br> Отображайте оповещения о стримах в чате Minecraft |  

### 🔌 Плагины

| 🖼️ **Плагин**                                                                                | 💬 **Описание**                                                 |  
|-----------------------------------------------------------------------------------------------|--------------------------------------------------------------------|
| 🛡️ **[LuckPerms](https://flectone.net/pulse/docs/integration/luckperms/)**                   | Управляйте ролями, разрешениями и весовыми группами без проблем    |  
| 🧩 **[PlaceholderAPI](https://flectone.net/pulse/docs/integration/placeholderapi/)**          | Используйте динамические переменные, такие как `%player_level%`, в сообщениях |  
| 🎙️ **[PlasmoVoice & SimpleVoice](https://flectone.net/pulse/docs/integration/plasmovoice/)** | Синхронизируйте игнорирования/муты между голосовым и серверным чатом |  
| 🖼️ **[SkinsRestorer](https://flectone.net/pulse/docs/integration/skinsrestorer/)**           | Отображайте кастомные скины в чате и TAB без усилий                |  
| 👻 **[SuperVanish](https://flectone.net/pulse/docs/integration/supervanish/)**                | Убедитесь, что скрытые игроки невидимые          |  
| 💰 **[Vault](https://flectone.net/pulse/docs/integration/vault/)**                            | Поддержка унифицированных разрешений через стандартные API         |  
| ...                                                                                           | ...                                                               |

**→ Полные детали интеграции в** [документации](https://flectone.net/pulse/docs/integration/) 🔗

---

## 🎮 **30+ команд**

| 🖼️ **Визуал** | 💬 **Описание** |  
|--------------|------------------------|  
| [![ball](https://flectone.net/pulse/commandball.png)](https://flectone.net/pulse/docs/command/) | **/ball** <br> Магический шар с более чем 20 вариантами ответов |  
| [![tictactoe](https://flectone.net/pulse/commandtictactoe.png)](https://flectone.net/pulse/docs/command/) | **/tictactoe** <br> Играйте на 3D-доске |  
| [![stream](https://flectone.net/pulse/commandstream.png)](https://flectone.net/pulse/docs/command/) | **/stream** <br> Оповещения о стримах прямо в чате Minecraft |  
| [![try](https://flectone.net/pulse/commandtry.png)](https://flectone.net/pulse/docs/command/) | **/try** <br> Испытайте удачу с рулеткой от 0 до 100% |  
| ...                                                                                           | ...                                                               |

**→ Полный список команд в** [документации](https://flectone.net/pulse/docs/command/) 🔗

---

## ❓ **FAQ**

Есть вопросы? Проверьте нашу [страницу FAQ](https://flectone.net/pulse/docs/) в документации для ответов на распространенные вопросы о настройке, конфигурации и устранении неполадок

---

## 🙏 **Благодарности**

FlectonePulse построен на плечах этих отличных проектов:

- 🏗️ **[Google Guice](https://github.com/google/guice)** — Фреймворк внедрения зависимостей для модульного кода
- 📚 **[JDBI](https://jdbi.org/)** с **[HikariCP](https://github.com/brettwooldridge/HikariCP)** — Эффективный доступ к базе данных с пулом соединений для оптимальной производительности
- 📦 **[Elytrium Java Serializer](https://github.com/Elytrium/java-serializer)** — Эффективная сериализация данных
- 🧙 **[PacketEvents](https://github.com/retrooper/packetevents)** — Продвинутая обработка пакетов для Minecraft
- 🎨 **[Adventure](https://github.com/KyoriPowered/adventure)** — Современное форматирование и стилизация текста
- ⌨️ **[Cloud](https://github.com/Incendo/cloud)** — Фреймворк команд с надежным автодополнением
- ⏱️ **[Universal Scheduler](https://github.com/Anon8281/UniversalScheduler)** — Точное планирование задач
- 🔣 **[Symbol Chat](https://github.com/replaceitem/symbol-chat)** — Расширенная поддержка символов для чата
- 🖥️ **[PacketUxUi](https://github.com/OceJlot/PacketUxUi)** — Улучшенные компоненты GUI
- 💬 **[LightChatBubbles](https://github.com/atesin/LightChatBubbles)** — Легкая система сообщений над головой
- 🌐 **[MiniTranslator](https://github.com/imDaniX/MiniTranslator)** — Конвертирует устаревшие коды форматирования Minecraft
- 🌱 **[FlectoneChat](https://github.com/Flectone/FlectoneChat)** — Дедушка этого проекта FlectonePulse

**Спасибо нашему сообществу!** Каждая звезда на GitHub подпитывает нашу мотивацию ⭐

## 📊 **Статистика проекта**
<div align="center">
  <a href="https://flectone.net/en/pulse/metrics" target="_blank">
    <img src="https://flectone.net/api/pulse/metrics/svg" alt="Статистика FlectonePulse">
  </a>
</div>

---

## ❤️ **Open Source & Бесплатно**

FlectonePulse бесплатен для скачивания, изменения и использования. Для **приоритетной поддержки** и **кастомных решений** подумайте о поддержке нас на Boosty:  
[![boosty](https://flectone.net/pulse/boosty.svg)](https://boosty.to/thefaser)

**Преимущества поддержки:**
- 🚀 Доступ к техническим вопросам и ответам
- 🛠️ Индивидуальные решения для вашего сервера
- 🔥 Ранний доступ к новым функциям

**Для всех:**
- 📚 Изучите [документацию](https://flectone.net/pulse/)
- 🐞 Сообщайте о проблемах через [GitHub Issues](https://github.com/Flectone/FlectonePulse/issues)

<div align="center">
  <h2><b>FlectonePulse ждет! Давай заливать на сервер? 😎</b></h2>
  <a href="https://modrinth.com/plugin/flectonepulse"><img src="https://flectone.net/pulse/modrinth.svg" width="200" alt="modrinth"></a>
  <br>
  <h3>P.S. Присоединяйтесь к нашему <a href="https://discord.flectone.net/">Discord</a> 🎉</h3>
</div>