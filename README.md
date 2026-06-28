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


## What Is FlectonePulse? [![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/Flectone/FlectonePulse)

FlectonePulse is a **plugin** and **mod** for **Minecraft (and Hytale) servers** that takes full control over chat, messages, and notifications. You decide what players see, when they see it, and how it looks. Clean defaults for beginners, deep configuration for everyone else

FlectonePulse was the subject of a **university thesis** and received top marks for its architecture, technical depth, and practical value. The full thesis, presentation, and plagiarism report are publicly available
- 📄 [Thesis (PDF)](https://flectone.net/files/r/flectonepulse/Thesis.pdf)
- 📊 [Thesis Presentation (PPTX)](https://flectone.net/files/r/flectonepulse/Thesis_Presentation.pptx)
- ✅ [Plagiarism Report (PDF)](https://flectone.net/files/r/flectonepulse/Plagiarism_Report.pdf)

### Why FlectonePulse instead of other message plugins?
| Feature                                                                        | FlectonePulse |    Typical message plugin     |
|--------------------------------------------------------------------------------|:-------------:|:-----------------------------:|
| Fully async – no main thread impact                                            |       ✅       |               ❌               |
| Supports 1.8.8 up to the latest Minecraft version                              |       ✅       |      ⚠️ (often limited)       |
| Works on Bukkit, Spigot, Paper, Purpur, Folia, and Fabric                      |       ✅       |   ❌ (usually one platform)    |
| Built‑in proxy support (BungeeCord, Velocity)                                  |       ✅       | ❌ (requires separate plugin)  |
| Cross‑server message synchronization (via proxy or Redis)                      |       ✅       |               ❌               |
| [Hytale support](https://github.com/Flectone/FlectonePulse/tree/master/hytale) |       ✅       |               ❌               |
| Per‑player Minecraft client language detection                                 |       ✅       |               ❌               |
| Textures in chat **without resource pack** (`<texture:name>`)                  |       ✅       |               ❌               |
| In‑line player heads and built‑in sprites (`<player_head>`, `<sprite:name>`)   |       ✅       |               ❌               |
| Modular design – enable/disable any feature                                    |       ✅       |          ⚠️ (rarely)          |
| 40+ built‑in commands (chat, moderation, fun, polls, etc.)                     |       ✅       |               ❌               |
| Cross‑platform chat (Discord, Telegram, Twitch)                                |       ✅       |               ❌               |
| Legacy color codes + MiniMessage support in one system                         |       ✅       | ⚠️ (usually one or the other) |
| Google Guice dependency injection for clean architecture                       |       ✅       |               ❌               |
| Open source & completely free                                                  |       ✅       |      ⚠️ (often freemium)      |

### Requirement

Java 17 or higher is **REQUIRED**. Older Minecraft versions, such as 1.8.8, can also run on Java 17 (check [PandaSpigot](https://github.com/hpfxd/pandaspigot))

| Platform                                     | Supported Versions       |
|----------------------------------------------|--------------------------|
| **Bukkit / Spigot / Paper / Purpur / Folia** | 1.8.8 – Latest           |
| **Fabric**                                   | Latest Minecraft version |
| **BungeeCord / Waterfall**                   | Latest                   |
| **Velocity**                                 | Latest                   |
| **Hytale**                                   | Latest Hytale version    |
| **NeoForge**                                 | Planned                  |

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

FlectonePulse understands every color system and converts them all to MiniMessage internally. Mix legacy codes and modern tags in the same line, it works

| Input                                   | Result                                               |
|-----------------------------------------|------------------------------------------------------|
| `&0`-`&9`, `&a`-`&f`                    | `<black>`, `<dark_blue>`, ..., `<white>`             |
| `&l` / `&m` / `&n` / `&o` / `&k` / `&r` | `<b>` / `<st>` / `<u>` / `<i>` / `<obf>` / `<reset>` |
| `&#rrggbb`, `#rrggbb`                   | `<#rrggbb>`                                          |
| MiniMessage                             | `<gradient>`, `<hover:...>`, `<click:...>`, etc.     |

```yaml
join:
  format: "<gradient:#FF0000:#00FF00>&lПривет</gradient> <rainbow><player></rainbow>!"
```

[![color](https://flectone.net/pulse/welcomemessage.png)](https://flectone.net/pulse/docs/message/join/)

## 🧱 Textures in Chat – No Resource Pack Needed

Embed custom images directly into chat messages and the server MOTD with `<texture:name>`

[![texture motd](https://flectone.net/pulse/texturemotd.png)](https://flectone.net/pulse/docs/message/format/object/)
[![texture chat](https://flectone.net/pulse/texture.png)](https://flectone.net/pulse/docs/message/format/object/)

Player skin avatars with `<player_head>`, Minecraft built-in sprites with `<sprite:name>`

[![objects in chat](https://flectone.net/pulse/object.png)](https://flectone.net/pulse/docs/message/format/object/)

## 🌍 Per-Player Language Detection

Enable `by_player: true` and every message is delivered in the player's own client language. If a translation is missing, the server default is used as fallback

[![locale](https://flectone.net/pulse/locale.gif)](https://flectone.net/pulse/docs/config/language/)

## 💬 Multi-Channel Chat System

Multiple named chat channels with independent settings. Each channel has its own range, destination, cooldown, sound, trigger

[![chat](https://flectone.net/pulse/chat.png)](https://flectone.net/pulse/docs/message/chat/)

Send any formatted message to any destination with **admin command `/emit`**. You can use destination `CHAT` `ACTION_BAR` `BOSS_BAR` `TITLE` `SUBTITLE` `TOAST` `TAB_HEADER` `TAB_FOOTER` `BRAND`

[![commandemit](https://flectone.net/pulse/commandemit.png)](https://flectone.net/pulse/docs/command/emit/)

## 🌈 Per-Player Customization

Players open a full in-game menu `/chatsetting` to toggle messages, change colors, and disable whatever they don't want

[![chatsetting](https://flectone.net/pulse/commandchatsetting.gif)](https://flectone.net/pulse/docs/command/chatsetting/)

## ✨ Everything is a Message and is Configurable

FlectonePulse takes control of virtually every event that produces text

| Module                                                                                                                                                                                                           | Description                                                                     |
|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------|
| [![MOTD](https://flectone.net/pulse/version.png)](https://flectone.net/pulse/docs/message/status/motd/)                                                                                                          | Animated, multi-line server list status with custom textures                    |
| [![Join](https://flectone.net/pulse/join.png)](https://flectone.net/pulse/docs/message/join/) [![Quit](https://flectone.net/pulse/quit.png)](https://flectone.net/pulse/docs/message/quit/)                      | Fully formatted with placeholders, sounds, and conditions                       |
| [![AFK](https://flectone.net/pulse/afkglobalmessage.png)](https://flectone.net/pulse/docs/message/afk/)                                                                                                          | Auto-mark and notify when players go idle                                       |
| [![Bubbles](https://flectone.net/pulse/bubble.gif)](https://flectone.net/pulse/docs/message/bubble/)                                                                                                             | Show messages visually above players' heads                                     |
| [![Belowname](https://flectone.net/pulse/belowname.png)](https://flectone.net/pulse/docs/message/scoreboard/objective/belowname/)                                                                                | Controlled text below player names                                              |
| [![TAB](https://flectone.net/pulse/tab.png)](https://flectone.net/pulse/docs/message/tab/)                                                                                                                       | Header, footer, player name format with ping, rank, world                       |
| [![Sidebar](https://flectone.net/pulse/scoreboard.png)](https://flectone.net/pulse/docs/message/sidebar/)                                                                                                        | Animated scoreboard sidebar with custom content                                 |
| [![Bossbar](https://flectone.net/pulse/bossbar_raid.png)](https://flectone.net/pulse/docs/message/bossbar/)                                                                                                      | Vanilla bar formatting                                                          |
| [![Anvil](https://flectone.net/pulse/anvil.gif)](https://flectone.net/pulse/docs/message/anvil/)                                                                                                                 | Custom formatted text (book, sign, anvil)                                       |
| [![Auto](https://flectone.net/pulse/auto.png)](https://flectone.net/pulse/docs/message/auto/)                                                                                                                    | Scheduled, repeating announcements                                              |
| [![Server links](https://flectone.net/pulse/serverlink.png)](https://flectone.net/pulse/docs/message/serverlink/)                                                                                                | Custom clickable links in the server menu                                       |
| [![Brand](https://flectone.net/pulse/brand.png)](https://flectone.net/pulse/docs/message/brand/)                                                                                                                 | Display custom text in the F3 debug screen                                      |
| [![Death](https://flectone.net/pulse/deathserver.png)](https://flectone.net/pulse/docs/message/vanilla/) [![Advancement](https://flectone.net/pulse/task.png)](https://flectone.net/pulse/docs/message/vanilla/) | Replace default Minecraft messages your way (death messages, advancement, etc.) |

Full list in [documentation 🔗](https://flectone.net/pulse/docs/message/)

## 🎮 40+ Built-In Commands

| Module                                                                                                              | Description                                                                                             |
|---------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------|
| [![ball](https://flectone.net/pulse/commandball.png)](https://flectone.net/pulse/docs/command/ball/)                | **/ball** – magic 8-ball with customizable answers                                                      |
| [![tictactoe](https://flectone.net/pulse/commandtictactoe.png)](https://flectone.net/pulse/docs/command/tictactoe/) | **/tictactoe** – play tic-tac-toe in chat                                                               |
| [![stream](https://flectone.net/pulse/commandstream.png)](https://flectone.net/pulse/docs/command/stream/)          | **/stream** – announce you're going live                                                                |
| [![try](https://flectone.net/pulse/commandtry.png)](https://flectone.net/pulse/docs/command/try/)                   | **/try** – random luck roll from 0% to 100%                                                             |
| [![sprite](https://flectone.net/pulse/commandsprite.png)](https://flectone.net/pulse/docs/command/sprite/)          | **/sprite** – browse and preview all Minecraft built-in sprites                                         |
| [![poll](https://flectone.net/pulse/commandpoll.png)](https://flectone.net/pulse/docs/command/poll/)                | **/poll** – create in-chat polls with voting                                                            |
| [![ban](https://flectone.net/pulse/commandban.png)](https://flectone.net/pulse/docs/command/ban/)                   | **/ban** – ban a player with a reason and optional duration                                             |
| [![whitelist](https://flectone.net/pulse/commandwhitelist.png)](https://flectone.net/pulse/docs/command/whitelist/) | **/whitelist** – manage server whitelist with reason and optional duration (add, remove, list, on, off) |

Already exist: `/afk` `/anon` `/ban` `/banlist` `/broadcast` `/chatcolor` `/chatsetting` `/clearchat` `/clearmail` `/coin` `/deletemessage` `/dice` `/do` `/emit` `/geolocate` `/helper` `/ignore` `/ignorelist` `/kick` `/mail` `/maintenance` `/me` `/mute` `/mutelist` `/nickname` `/online` `/ping` `/reply` `/spy` `/tell` `/toponline` `/translateto` `/unban` `/unmute` `/unwarn` `/warn` `/warnlist` `/whitelist`

Full list in [documentation](https://flectone.net/pulse/docs/command/) 🔗

## 🤝 Integration with Platforms

| Platform                                                                                                              | What it adds                                                                                                                    |
|-----------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------|
| [![discord](https://flectone.net/pulse/discordmessage.png)](https://flectone.net/pulse/docs/integration/discord/)     | **Discord** – bidirectional chat sync. Custom bot commands, channel info ticker, webhook support with embeds and avatar display |
| [![telegram](https://flectone.net/pulse/telegrammessage2.png)](https://flectone.net/pulse/docs/integration/telegram/) | **Telegram** – bidirectional chat sync. Custom bot commands and channel info ticker                                             |
| [![twitch](https://flectone.net/pulse/twitchmessage.png)](https://flectone.net/pulse/docs/integration/twitch/)        | **Twitch** – bidirectional chat sync. Custom bot commands and live stream notifications                                         |

## 🤝 Integration with Plugins

| Plugin                                                                                                                                                                                                                    | What it adds                                           |
|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------|
| 🛡️ **[LuckPerms](https://flectone.net/pulse/docs/integration/luckperms/)**                                                                                                                                               | Prefix, suffix, group display, TAB sorting by rank     |
| 🧩 **[PlaceholderAPI](https://flectone.net/pulse/docs/integration/placeholderapi/) / [MiniPlaceholders](https://flectone.net/pulse/docs/integration/miniplaceholders)**                                                   | Any `%placeholder%` from other plugins                 |
| 🎙️ **[PlasmoVoice](https://flectone.net/pulse/docs/integration/plasmovoice/) / [SimpleVoice](https://flectone.net/pulse/docs/integration/simplevoice/)**                                                                 | Mute and ignore sync with voice chat                   |
| 🖼️ **[SkinsRestorer](https://flectone.net/pulse/docs/integration/skinsrestorer/)**                                                                                                                                       | Real player skins in chat and TAB                      |
| 👻 **[SuperVanish](https://flectone.net/pulse/docs/integration/supervanish/)**                                                                                                                                            | Vanished players hidden automatically (even via proxy) |
| 💬 **[InteractiveChat](https://flectone.net/pulse/docs/integration/interactivechat/)**                                                                                                                                    | Enhanced interactive chat elements                     |
| 🔨 **[AdvancedBan](https://flectone.net/pulse/docs/integration/advancedban) / [LibertyBans](https://flectone.net/pulse/docs/integration/libertybans) / [LiteBans](https://flectone.net/pulse/docs/integration/litebans)** | Delegate ban commands to your existing system          |
| 🌐 **[Vault](https://flectone.net/pulse/docs/integration/vault)**                                                                                                                                                         | Permission compatibility                               |
| 🗣️ **[Floodgate](https://flectone.net/pulse/docs/integration/floodgate) / [Geyser](https://flectone.net/pulse/docs/integration/geyser)**                                                                                 | Bedrock player support                                 |
| 📦 **[ItemsAdder](https://flectone.net/pulse/docs/integration/itemsadder/)**                                                                                                                                              | Custom textures and items in messages                  |

Full list in [documentation](https://flectone.net/pulse/docs/integration/) 🔗

## 🙏 Acknowledgments

FlectonePulse is built on these projects:

- 🏗️ **[Google Guice](https://github.com/google/guice)** – dependency injection
- 📚 **[JDBI](https://jdbi.org/)** with **[HikariCP](https://github.com/brettwooldridge/HikariCP)** – database access
- 📦 **[Jackson](https://github.com/FasterXML/jackson)** – data serialization
- 🧙 **[PacketEvents](https://github.com/retrooper/packetevents)** – packet-level event handling
- 🎨 **[Adventure](https://github.com/KyoriPowered/adventure)** – text component formatting
- ⌨️ **[Cloud](https://github.com/Incendo/cloud)** – commands with tab completion
- ⏱️ **[Universal Scheduler](https://github.com/Anon8281/UniversalScheduler)** – cross-platform task scheduling
- 🔣 **[Symbol Chat](https://github.com/replaceitem/symbol-chat)** – Unicode symbol picker
- 🌐 **[MiniTranslator](https://github.com/imDaniX/MiniTranslator)** – legacy to MiniMessage conversion
- 🌱 **[FlectoneChat](https://github.com/Flectone/FlectoneChat)** – the predecessor

And thanks to the community! Every star on GitHub and review on platforms shows that FlectonePulse is genuinely useful ⭐

## 📊 Project Statistics

<div align="center">
  <a href="https://flectone.net/pulse/metrics" target="_blank">
    <img src="https://flectone.net/api/pulse/metrics/svg" alt="FlectonePulse Statistics">
  </a>
</div>