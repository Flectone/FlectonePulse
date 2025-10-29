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

<div class="center-row" align="center">
    <a href="https://www.spigotmc.org/"><img src="https://flectone.net/pulse/bukkit.svg" alt="bukkit" class="hover-brightness"></a>
    <a href="https://www.spigotmc.org/"><img src="https://flectone.net/pulse/spigot.svg" alt="spigot" class="hover-brightness"></a>
    <a href="https://papermc.io/"><img src="https://flectone.net/pulse/paper.svg" alt="paper" class="hover-brightness"></a>
    <a href="https://purpurmc.org/"><img src="https://flectone.net/pulse/purpur.svg" alt="purpur" class="hover-brightness"></a>
    <a href="https://papermc.io/software/folia"><img src="https://flectone.net/pulse/folia.svg" alt="folia" class="hover-brightness"></a>
    <a href="https://www.spigotmc.org/wiki/bungeecord/"><img src="https://flectone.net/pulse/bungeecord.svg" alt="bungeecord" class="hover-brightness"></a>
    <a href="https://papermc.io/software/velocity"><img src="https://flectone.net/pulse/velocity.svg" alt="velocity" class="hover-brightness"></a>
    <h1>FlectonePulse — Every message under your control!</h1>
    <a href="https://boosty.to/thefaser"><img src="https://flectone.net/pulse/boosty.svg" alt="boosty" class="hover-brightness"></a>
    <a href="https://modrinth.com/plugin/flectonepulse"><img src="https://flectone.net/pulse/modrinth.svg" alt="modrinth" class="hover-brightness"></a>
    <a href="https://flectone.net/pulse/"><img src="https://flectone.net/pulse/documentation.svg" alt="documentation" class="hover-brightness"></a>
    <a href="https://discord.flectone.net/"><img src="https://flectone.net/pulse/discord.svg" alt="discord" class="hover-brightness"></a>
</div>

## 🏆 What makes FlectonePulse special?

FlectonePulse is a plugin and mod for Minecraft servers that takes control of chat, messages, and notifications. Beginners will especially love it because the setup is simple, and the result is a beautiful chat, integrations, and useful commands without any hassle.

- All operations are performed asynchronously, the main server thread is not affected
- Uses Google Guice for dependency injection, which simplifies extending functionality
- Supports all popular platforms Bukkit, Spigot, Paper, Purpur, Folia, Fabric, BungeeCord, Waterfall, and Velocity on versions 1.8.8 to the latest

## 🎨 Flexible text formatting

Supports all color formats, from legacy (`&` or `§` for colors) to modern MiniMessage tags

| **Input code**                                        | **Transformation**                                             |  
|-------------------------------------------------------|-------------------------------------------------------------|  
| `&0`-`&9`, `&a`-`&f`                                  | `<black>`, `<dark_blue>`, ..., `<white>`                    |  
| `&l`/`&m`/`&n`/`&o`/`&k`/`&r`                         | `<b>` / `<st>` / `<u>` / `<i>` / `<obf>` / `<reset>`        |  
| `&#rrggbb`, `#rrggbb`, `&x&r&r&g&g&b&b`, `<##rrggbb>` | `<#rrggbb>`                                                 |  
| MiniMessage tags                                      | `<color:#rrggbb>`, `<rainbow>`, `<click:...>`, `<font>`, etc. |

```yaml
# EXAMPLE
join:
  format: "<gradient:#FF0000:#00FF00>&lHello</gradient> <rainbow><player></rainbow>!"
```

![color](https://flectone.net/pulse/welcomemessage.png)

## 🧱 Player heads in chat

Add avatars to messages using the placeholder `<player_head>` or specify a nickname `<player_head:name>`. No resource pack needed, all players will see it

![object](https://flectone.net/pulse/object.png)

## 🌈 Chat customization with /chatsetting

![chatsetting](https://flectone.net/pulse/commandchatsetting.gif)

The /chatsetting command opens a menu for quick message customization for each player. Choose colors, styles, and disable unnecessary messages

## 🌍 Smart localization

### How it works
[![locale](https://flectone.net/pulse/locale.gif)](https://flectone.net/pulse/docs/config/language/)

When `by_player: true` is enabled, FlectonePulse detects the client's language and displays messages in it. If no translation exists, the default from the config is used. More in the [documentation](https://flectone.net/pulse/docs/config#language-player) 🔗

## ✨ Customizable elements

| **Visual** | **Description** |  
|---------------|-----------------|  
| ![status](https://flectone.net/pulse/version.png) | **Server MOTD** <br> Add custom texts for greetings in the server list |  
| ![join](https://flectone.net/pulse/join.png) | **Join messages** <br> Greet players |  
| ![tab](https://flectone.net/pulse/tab.png) | **TAB menu** <br> Show ping, online, ranks — all in one place |  
| ![death](https://flectone.net/pulse/deathserver.png) | **Death messages** <br> Make them fun with text or sounds |  
| ![brand](https://flectone.net/pulse/brand.png) | **Branding** <br> Add server name to the F3 menu |  
| ![advancement](https://flectone.net/pulse/task.png) | **Advancements** <br> Custom advancement messages |  
| Full list in [documentation](https://flectone.net/pulse/docs/message/) 🔗                                                | ...                                                               |

## 🤝 Integrations

### External platforms

| **Visual** | **Description** |  
|---------|----------|  
| [![discord](https://flectone.net/pulse/discordmessage.png)](https://flectone.net/pulse/docs/integration/discord/) | **Discord** <br> Sync server chat with Discord channels |  
| [![telegram](https://flectone.net/pulse/telegrammessage2.png)](https://flectone.net/pulse/docs/integration/telegram/) | **Telegram** <br> Send messages via bot to Telegram and sync chats |  
| [![twitch](https://flectone.net/pulse/twitchmessage.png)](https://flectone.net/pulse/docs/integration/twitch/) | **Twitch** <br> Stream notifications in Minecraft chat and server chat sync |  

### Plugins

| **Plugin**                                                                                | **Description**                                                 |  
|-----------------------------------------------------------------------------------------------|--------------------------------------------------------------------|  
| 💬 **[InteractiveChat](https://flectone.net/pulse/docs/integration/interactivechat/)**        | Interactive elements in chat              | 
| 🛡️ **[LuckPerms](https://flectone.net/pulse/docs/integration/luckperms/)**                   | Permission and group management         |  
| 🧩 **[PlaceholderAPI](https://flectone.net/pulse/docs/integration/placeholderapi/)**          | Placeholders from other plugins, e.g. %player_level%           |  
| 🎙️ **[PlasmoVoice & SimpleVoice](https://flectone.net/pulse/docs/integration/plasmovoice/)** | Ignore and mute sync in voice chat           |  
| 🖼️ **[SkinsRestorer](https://flectone.net/pulse/docs/integration/skinsrestorer/)**           | Skin display in chat and TAB                 |  
| 👻 **[SuperVanish](https://flectone.net/pulse/docs/integration/supervanish/)**                | Hide vanished players              |  
| Full list in [documentation](https://flectone.net/pulse/docs/integration/) 🔗                                                                                          | ...                                                               |

## 🎮 Over 30 commands

| **Visual** | **Description** |  
|--------------|------------------------|  
| [![ball](https://flectone.net/pulse/commandball.png)](https://flectone.net/pulse/docs/command/) | **/ball** <br> Magic ball with many answers |  
| [![tictactoe](https://flectone.net/pulse/commandtictactoe.png)](https://flectone.net/pulse/docs/command/) | **/tictactoe** <br> Tic-tac-toe |  
| [![stream](https://flectone.net/pulse/commandstream.png)](https://flectone.net/pulse/docs/command/) | **/stream** <br> Stream notifications in chat |  
| [![try](https://flectone.net/pulse/commandtry.png)](https://flectone.net/pulse/docs/command/) | **/try** <br> Test your luck from 0% to 100% |  
| Full list in [documentation](https://flectone.net/pulse/docs/command/) 🔗                                                                                           | ...                                                              |

## ❓ FAQ

Questions about installation or setup? Check the [FAQ](https://flectone.net/pulse/docs/) in the documentation

## 🙏 Acknowledgments

FlectonePulse is built on these projects:

- 🏗️ **[Google Guice](https://github.com/google/guice)** — for modular code
- 📚 **[JDBI](https://jdbi.org/)** with **[HikariCP](https://github.com/brettwooldridge/HikariCP)** — efficient database work
- 📦 **[Jackson](https://github.com/FasterXML/jackson)** — data serialization
- 🧙 **[PacketEvents](https://github.com/retrooper/packetevents)** — packet handling
- 🎨 **[Adventure](https://github.com/KyoriPowered/adventure)** — text formatting
- ⌨️ **[Cloud](https://github.com/Incendo/cloud)** — commands with autocompletion
- ⏱️ **[Universal Scheduler](https://github.com/Anon8281/UniversalScheduler)** — task scheduling
- 🔣 **[Symbol Chat](https://github.com/replaceitem/symbol-chat)** — symbols in chat
- 🖥️ **[PacketUxUi](https://github.com/OceJlot/PacketUxUi)** — GUI elements
- 💬 **[LightChatBubbles](https://github.com/atesin/LightChatBubbles)** — messages above head
- 🌐 **[MiniTranslator](https://github.com/imDaniX/MiniTranslator)** — legacy color conversion
- 🌱 **[FlectoneChat](https://github.com/Flectone/FlectoneChat)** — predecessor of FlectonePulse

And thanks to the community! Every star on GitHub and review on platforms shows that FlectonePulse is truly needed ⭐

## 📊 Project statistics
<div align="center">
  <a href="https://flectone.net/en/pulse/metrics" target="_blank">
    <img src="https://flectone.net/api/pulse/metrics/svg" alt="FlectonePulse Statistics">
  </a>
</div>

## ❤️ Open source and free

FlectonePulse is completely free. Download, modify, put on your server. For priority support, early access to features, and help with server-specific setup, support on Boosty. It motivates further development!

<div align="center">
  <a href="https://boosty.to/thefaser"><img src="https://flectone.net/pulse/boosty.svg" alt="boosty" class="hover-brightness"></a>
  <h2><b>FlectonePulse is waiting for you! Ready to install? 😎</b></h2>
  <a href="https://modrinth.com/plugin/flectonepulse"><img src="https://flectone.net/pulse/modrinth.svg" width="200" alt="modrinth"></a>
  <br>
  <h3>P.S. Join <a href="https://discord.flectone.net/">Discord</a></h3>
</div>