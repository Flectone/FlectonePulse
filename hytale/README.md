<div align="center">
  <h3>
    <a href="README.md">EN</a> |
    <a href="README-RU.md">RU</a>
  </h3>
</div>

<div align="center">

### 🎥 FlectonePulse Video Review

[![FlectonePulse](https://img.youtube.com/vi/UjIlfjXzdxE/maxresdefault.jpg)](https://youtu.be/UjIlfjXzdxE "Watch")

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

  <h1>FlectonePulse – Every Message Under Your Control!</h1>

<a href="https://boosty.to/thefaser"><img src="https://flectone.net/pulse/boosty.svg" alt="boosty"></a>
<a href="https://modrinth.com/plugin/flectonepulse"><img src="https://flectone.net/pulse/modrinth.svg" alt="modrinth"></a>
<a href="https://flectone.net/pulse/"><img src="https://flectone.net/pulse/documentation.svg" alt="documentation"></a>
<a href="https://discord.flectone.net/"><img src="https://flectone.net/pulse/discord.svg" alt="discord"></a>
</div>

## What Is FlectonePulse Hytale? [![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/Flectone/FlectonePulse)

FlectonePulse Hytale is a dedicated module of the FlectonePulse ecosystem that takes full control over chat, messages, and notifications on **Hytale** servers. You decide what players see, when they see it, and how it looks. Clean defaults for beginners, deep configuration for everyone else

FlectonePulse was the subject of a **university thesis** and received top marks for its architecture, technical depth, and practical value. The full thesis, presentation, and plagiarism report are publicly available:
- 📄 [Thesis (PDF)](https://flectone.net/files/r/flectonepulse/Thesis.pdf)
- 📊 [Thesis Presentation (PPTX)](https://flectone.net/files/r/flectonepulse/Thesis_Presentation.pptx)
- ✅ [Plagiarism Report (PDF)](https://flectone.net/files/r/flectonepulse/Plagiarism_Report.pdf)

### Why FlectonePulse Hytale instead of other message plugins?

| Feature                                                   | FlectonePulse Hytale | Typical message plugin |
|-----------------------------------------------------------|:--------------------:|:----------------------:|
| Fully async – no main thread impact                       |          ✅           |           ❌            |
| Native Hytale UI and component support                    |          ✅           |           ❌            |
| Modular design – enable/disable any feature               |          ✅           |      ⚠️ (rarely)       |
| Per‑player client language detection                      |          ✅           |           ❌            |
| Visual message bubbles above players                      |          ✅           |           ❌            |
| In‑game player settings menu (`/chatsetting`)             |          ✅           |           ❌            |
| Cross‑platform chat (Discord, Telegram, Twitch)           |          ✅           |           ❌            |
| Legacy color codes + MiniMessage support in one system    |          ✅           |    ⚠️ (usually one)    |
| Google Guice dependency injection for clean architecture  |          ✅           |           ❌            |
| Open source & completely free                             |          ✅           |  ⚠️ (often freemium)   |

### Requirement

Java 17 or higher is **REQUIRED**

| Platform   | Supported Versions    |
|------------|-----------------------|
| **Hytale** | Latest Hytale version |

### Configuration Files

| File                  | Purpose                                      |
|-----------------------|----------------------------------------------|
| `config.yml`          | Database, language, proxy mode, cache        |
| `command.yml`         | Enable/disable and configure commands        |
| `message.yml`         | All message modules, conditions and behavior |
| `integration.yml`     | Third-party plugins and external platforms   |
| `permission.yml`      | Permissions for every feature                |
| `localizations/*.yml` | Per-language message text                    |

## 🎨 Color Formatting

FlectonePulse Hytale understands every color system and converts them all to MiniMessage internally. Mix legacy codes and modern tags in the same line – it works

| Input                                   | Result                                               |
|-----------------------------------------|------------------------------------------------------|
| `&0`-`&9`, `&a`-`&f`                    | `<black>`, `<dark_blue>`, ..., `<white>`             |
| `&l` / `&m` / `&n` / `&o` / `&k` / `&r` | `<b>` / `<st>` / `<u>` / `<i>` / `<obf>` / `<reset>` |
| `&#rrggbb`, `#rrggbb`                   | `<#rrggbb>`                                          |
| MiniMessage                             | `<gradient>`, `<hover:...>`, `<click:...>`, etc.     |

```yaml
join:
  format: "<gradient:#FF0000:#00FF00>&lHello</gradient> <rainbow><player></rainbow>!"
```

[![color](https://flectone.net/pulse/hytale/welcomemessage.png)](https://flectone.net/pulse/docs/hytale/message/join/)

## 🧱 Message Bubbles

Show chat messages in floating bubbles above players' heads for a more immersive experience

[![Message Bubbles](https://flectone.net/pulse/hytale/bubble.gif)](https://flectone.net/pulse/docs/hytale/message/bubble/)

## 🌍 Per-Player Language Detection

Enable `by_player: true` and every message is delivered in the player's own client language. If a translation is missing, the server default is used as fallback

[![locale](https://flectone.net/pulse/hytale/locale.gif)](https://flectone.net/pulse/docs/config/language/)

## ✨ Everything is a Message and is Configurable

FlectonePulse Hytale takes control of virtually every event that produces text

| Module                                                                                                                                                | Description                                                |
|-------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------|
| [![Join](https://flectone.net/pulse/hytale/join.png)](https://flectone.net/pulse/docs/hytale/message/join/)                                           | Fully formatted join messages with placeholders and sounds |
| [![Quit](https://flectone.net/pulse/hytale/quit.png)](https://flectone.net/pulse/docs/hytale/message/quit/)                                           | Customizable quit messages                                 |
| [![Death](https://flectone.net/pulse/hytale/death.png)](https://flectone.net/pulse/docs/hytale/message/death/)                                        | Replace default death messages with your own style         |
| [![Chat](https://flectone.net/pulse/hytale/chat.png)](https://flectone.net/pulse/docs/hytale/message/chat/)                                           | Full chat formatting with gradients, hover, click and more |
| [![Bubbles](https://github.com/user-attachments/assets/2b38da15-9e76-458b-a8ac-6d6b9ae48d2c)](https://flectone.net/pulse/docs/hytale/message/bubble/) | Visual message bubbles above players                       |
| [![AFK](https://flectone.net/pulse/hytale/afkglobalmessage.png)](https://flectone.net/pulse/docs/hytale/message/afk/)                                 | Auto-mark and notify when players go idle                  |

Full list in [documentation 🔗](https://flectone.net/pulse/docs/hytale/message/)

## 🌈 Per-Player Customization

Players open a full in-game menu `/chatsetting` to toggle messages, change colors, and disable whatever they don't want

[![chatsetting](https://github.com/user-attachments/assets/7cf2f319-2eaf-4e34-812c-72bd8744170a)](https://flectone.net/pulse/docs/hytale/command/chatsetting/)

## 🎮 40+ Built-In Commands

| Module                                                                                                                     | Description                                                                                             |
|----------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------|
| [![ball](https://flectone.net/pulse/hytale/commandball.png)](https://flectone.net/pulse/docs/hytale/command/)              | **/ball** – magic 8‑ball with customizable answers                                                      |
| [![stream](https://flectone.net/pulse/hytale/commandstream.png)](https://flectone.net/pulse/docs/hytale/command/)          | **/stream** – announce you're going live                                                                |
| [![try](https://flectone.net/pulse/hytale/commandtry.png)](https://flectone.net/pulse/docs/hytale/command/)                | **/try** – random luck roll from 0% to 100%                                                             |
| [![ban](https://flectone.net/pulse/hytale/commandban.png)](https://flectone.net/pulse/docs/command/ban/)                   | **/ban** – ban a player with a reason and optional duration                                             |
| [![whitelist](https://flectone.net/pulse/hytale/commandwhitelist.png)](https://flectone.net/pulse/docs/command/whitelist/) | **/whitelist** – manage server whitelist with reason and optional duration (add, remove, list, on, off) |

Already exist: `/afk` `/anon` `/ban` `/broadcast` `/chatcolor` `/chatsetting` `/clearchat` `/coin` `/deletemessage` `/dice` `/do` `/emit` `/geolocate` `/helper` `/ignore` `/ignorelist` `/kick` `/mail` `/maintenance` `/me` `/mute` `/mutelist` `/nickname` `/online` `/ping` `/poll` `/reply` `/spy` `/tell` `/toponline` `/translateto` `/unban` `/unmute` `/unwarn` `/warn` `/warnlist` `/whitelist`

Full list in [documentation](https://flectone.net/pulse/docs/hytale/command/) 🔗

## 🤝 Integration with Platforms

| Platform                                                                                                                          | What it adds                                                                                                                    |
|-----------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------|
| [![discord](https://flectone.net/pulse/discordmessage.png)](https://flectone.net/pulse/docs/hytale/integration/discord/)          | **Discord** – bidirectional chat sync. Custom bot commands, webhook support with embeds and avatar display                      |
| [![telegram](https://flectone.net/pulse/telegrammessage2.png)](https://flectone.net/pulse/docs/hytale/integration/telegram/)      | **Telegram** – bidirectional chat sync. Custom bot commands                                                                     |
| [![twitch](https://flectone.net/pulse/twitchmessage.png)](https://flectone.net/pulse/docs/hytale/integration/twitch/)             | **Twitch** – bidirectional chat sync. Custom bot commands and live stream notifications                                         |

## 🤝 Integration with Plugins

| Plugin                                                                                      | What it adds                           |
|---------------------------------------------------------------------------------------------|----------------------------------------|
| 🛡️ **[LuckPerms](https://flectone.net/pulse/docs/hytale/integration/luckperms/)**          | Prefix, suffix, group display          |
| 🧩 **[PlaceholderAPI](https://flectone.net/pulse/docs/hytale/integration/placeholderapi/)** | Any `%placeholder%` from other plugins |

Full list in [documentation](https://flectone.net/pulse/docs/hytale/integration/) 🔗

## 🙏 Acknowledgments

FlectonePulse Hytale is built on these projects:

- 🏗️ **[Google Guice](https://github.com/google/guice)** – dependency injection
- 📚 **[JDBI](https://jdbi.org/)** with **[HikariCP](https://github.com/brettwooldridge/HikariCP)** – database access
- 📦 **[Jackson](https://github.com/FasterXML/jackson)** – data serialization
- 🧙 **[Adventure Hytale](https://github.com/ArikSquad/adventure-platform-hytale)** – text component formatting for Hytale
- 🎨 **[Adventure](https://github.com/KyoriPowered/adventure)** – text component formatting
- ⌨️ **[Cloud](https://github.com/Incendo/cloud)** – commands with tab completion
- 🔣 **[Symbol Chat](https://github.com/replaceitem/symbol-chat)** – Unicode symbol picker
- 🖥️ **[HyUI](https://github.com/Elliesaur/HyUI)** – GUI elements
- 🌐 **[MiniTranslator](https://github.com/imDaniX/MiniTranslator)** – legacy to MiniMessage conversion
- 🌱 **[FlectoneChat](https://github.com/Flectone/FlectoneChat)** – the predecessor

And thanks to the community! Every star on GitHub and review on platforms shows that FlectonePulse is genuinely useful ⭐

## 📊 Project Statistics

<div align="center">
  <a href="https://flectone.net/pulse/metrics" target="_blank">
    <img src="https://flectone.net/api/pulse/metrics/svg" alt="FlectonePulse Statistics">
  </a>
</div>