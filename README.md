<div align="center">
  <h3>
    <a href="README.md">EN</a> | 
    <a href="README-RU.md">RU</a>
  </h3>
</div>

![pulse](https://flectone.net/pulse/flectonepulse.png)
<div class="center-row" align="center">
    <h1>FlectonePulse — Every message under your control!</h1>
    <a href="https://www.spigotmc.org/"><img src="https://flectone.net/pulse/bukkit.svg" alt="bukkit" class="hover-brightness"></a>
    <a href="https://www.spigotmc.org/"><img src="https://flectone.net/pulse/spigot.svg" alt="spigot" class="hover-brightness"></a>
    <a href="https://papermc.io/"><img src="https://flectone.net/pulse/paper.svg" alt="paper" class="hover-brightness"></a>
    <a href="https://purpurmc.org/"><img src="https://flectone.net/pulse/purpur.svg" alt="purpur" class="hover-brightness"></a>
    <a href="https://papermc.io/software/folia"><img src="https://flectone.net/pulse/folia.svg" alt="folia" class="hover-brightness"></a>
    <a href="https://www.spigotmc.org/wiki/bungeecord/"><img src="https://flectone.net/pulse/bungeecord.svg" alt="bungeecord" class="hover-brightness"></a>
    <a href="https://papermc.io/software/velocity"><img src="https://flectone.net/pulse/velocity.svg" alt="velocity" class="hover-brightness"></a>
</div>

<div align="center">

### 🎥 FlectonePulse Video Review

[![FlectonePulse](https://img.youtube.com/vi/UjIlfjXzdxE/maxresdefault.jpg)](https://youtu.be/UjIlfjXzdxE "View")

</div>

---

## 🏆 Key Features

- ⚡ **Optimized Performance**  
  All operations run asynchronously, ensuring the main thread remains unaffected. Configuration files load in the background, keeping your server smooth even during high traffic

- 🏭 **Modular Architecture with Google Guice**  
  Built with dependency injection for clean, maintainable code. Easily extend functionality to suit your server’s needs

- 🔄 **Broad Compatibility**  
  Seamlessly supports Bukkit, Spigot, Paper, Purpur, Folia, BungeeCord, and Velocity. FlectonePulse adapts to any server environment

- 🎨 **Extensive Customization**  
  Tailor colors, animations, and integrations with Discord, Telegram, or Twitch. Transform even death messages into unique experiences!

---

<div align="center">

[![logo](https://github.com/user-attachments/assets/dc68fd41-8341-43e5-9c07-843e1ad839f1)](https://flectone.net/pulse/)  
<h3>🚀 Install FlectonePulse — Bring Your Server to Life! 💖</h3>
  <div>
    <a href="https://boosty.to/thefaser"><img src="https://flectone.net/pulse/boosty.svg" alt="boosty" class="hover-brightness"></a>
    <a href="https://modrinth.com/plugin/flectonepulse"><img src="https://flectone.net/pulse/modrinth.svg" alt="modrinth" class="hover-brightness"></a>
    <a href="https://flectone.net/pulse/"><img src="https://flectone.net/pulse/documentation.svg" alt="documentation" class="hover-brightness"></a>
    <a href="https://discord.flectone.net/"><img src="https://flectone.net/pulse/discord.svg" alt="discord" class="hover-brightness"></a>
  </div>
</div>

---

## 🎨 **Flexible Text Formatting**

FlectonePulse offers a highly versatile text formatting system, supporting legacy color codes, modern gradients, and MiniMessage tags for maximum flexibility

| **Input Code**                                        | **Converts To**                                             |  
|-------------------------------------------------------|-------------------------------------------------------------|  
| `&0`-`&9`, `&a`-`&f`                                  | `<black>`, `<dark_blue>`, ..., `<white>`                    |  
| `&l`/`&m`/`&n`/`&o`/`&k`/`&r`                         | `<b>` / `<st>` / `<u>` / `<i>` / `<obf>` / `<reset>`        |  
| `&#rrggbb`, `#rrggbb`, `&x&r&r&g&g&b&b`, `<##rrggbb>` | `<#rrggbb>`                                                 |  
| MiniMessage Tags                                      | `<color:#rrggbb>`, `<rainbow>`, `<click:...>`, `<font>`, etc. |

*Use `&` or `§` interchangeably for convenience*

```yaml
welcome-message: "<gradient:#FF0000:#00FF00>&lПривет</gradient> <rainbow><player></rainbow>!"
```

![color](https://flectone.net/pulse/welcomemessage.png)

---

## 🌈 **Chat Customization with /chatsetting**

![chatsetting](https://flectone.net/pulse/commandchatsetting.gif)

Use the `/chatsetting` command to create stunning chat designs. Customize messages with vibrant colors and styles

---

## 🌍 **Intelligent Localization**

### ⚙️ How It Works
[![locale](https://flectone.net/pulse/locale.gif)](https://flectone.net/pulse/docs/config#language-player)

- 🔄 **Automatic Language Detection**  
  With `language-player` enabled, FlectonePulse detects players’ Minecraft language settings and displays messages in their preferred language

- 🛠️ **Fallback Mechanism**  
  If a player’s language isn’t available, the plugin defaults to the configured language

**→ Learn more in the** [documentation](https://flectone.net/pulse/docs/config#language-player) 🔗

---

## ✨ **Customizable Features**

Tailor every aspect of your server’s messaging to match your vision:

| 🖼️ **Visual** | 💬 **Description** |  
|---------------|-----------------|  
| ![status](https://flectone.net/pulse/version.png) | **Server MOTD** <br> Transform the MOTD with animations or custom messages |  
| ![join](https://flectone.net/pulse/join.png) | **Join Messages** <br> Greet players with custom sounds or Title messages |  
| ![tab](https://flectone.net/pulse/tab.png) | **TAB** <br> Display key info like ping, online status, or ranks in the Tab menu |  
| ![death](https://flectone.net/pulse/deathserver.png) | **Death Messages** <br> Add flair with custom text or sounds |  
| ![brand](https://flectone.net/pulse/brand.png) | **Branding** <br> Showcase your server logo in the F3 menu |  
| ![advancement](https://flectone.net/pulse/task.png) | **Advancements** <br> Create unique achievement announcements |  
| ...                                                | ...                                                               |

**...and more!** Full details in the [message.yml documentation](https://flectone.net/pulse/docs/message/) 🚀

---

## 🤝 **Integrations**

### 🌍 External Platforms

| 🖼️ **Visual** | 💬 **Description** |  
|---------|----------|  
| [![discord](https://flectone.net/pulse/discordmessage.png)](https://flectone.net/pulse/docs/integration/discord/) | **Discord** <br> Sync server chat with Discord channels and notifications |  
| [![telegram](https://flectone.net/pulse/telegrammessage2.png)](https://flectone.net/pulse/docs/integration/telegram/) | **Telegram** <br> Relay player messages and admin commands via a bot |  
| [![twitch](https://flectone.net/pulse/twitchmessage.png)](https://flectone.net/pulse/docs/integration/twitch/) | **Twitch** <br> Display stream alerts in Minecraft chat |  

### 🔌 Plugins

| 🖼️ **Plugin**                                                                                | 💬 **Description**                                                 |  
|-----------------------------------------------------------------------------------------------|--------------------------------------------------------------------|  
| 💬 **[InteractiveChat](https://flectone.net/pulse/docs/integration/interactivechat/)**        | Enhanced chat formatting with interactive components              | 
| 🛡️ **[LuckPerms](https://flectone.net/pulse/docs/integration/luckperms/)**                   | Manage roles, permissions, and weighted groups seamlessly         |  
| 🧩 **[PlaceholderAPI](https://flectone.net/pulse/docs/integration/placeholderapi/)**          | Use dynamic variables like `%player_level%` in messages           |  
| 🎙️ **[PlasmoVoice & SimpleVoice](https://flectone.net/pulse/docs/integration/plasmovoice/)** | Synchronize ignores/mutes between voice and server chat           |  
| 🖼️ **[SkinsRestorer](https://flectone.net/pulse/docs/integration/skinsrestorer/)**           | Display custom skins in chat and TAB effortlessly                 |  
| 👻 **[SuperVanish](https://flectone.net/pulse/docs/integration/supervanish/)**                | Ensure hidden players don’t disrupt chat or commands              |  
| 💰 **[Vault](https://flectone.net/pulse/docs/integration/vault/)**                            | Support unified permissions via standard APIs                     |  
| ...                                                                                           | ...                                                               |

**→ Full integration details in the** [documentation](https://flectone.net/pulse/docs/integration/) 🔗

---

## 🎮 **30+ Commands**

| 🖼️ **Visual** | 💬 **Description** |  
|--------------|------------------------|  
| [![ball](https://flectone.net/pulse/commandball.png)](https://flectone.net/pulse/docs/command/) | **/ball** <br> Magic 8-ball with over 20 response options |  
| [![tictactoe](https://flectone.net/pulse/commandtictactoe.png)](https://flectone.net/pulse/docs/command/) | **/tictactoe** <br> Play on a 3D board |  
| [![stream](https://flectone.net/pulse/commandstream.png)](https://flectone.net/pulse/docs/command/) | **/stream** <br> Stream alerts directly in Minecraft chat |  
| [![try](https://flectone.net/pulse/commandtry.png)](https://flectone.net/pulse/docs/command/) | **/try** <br> Test your luck with a 0-100% chance roulette |  
| ...                                                                                           | ...                                                              |

**→ Full command list in the** [documentation](https://flectone.net/pulse/docs/command/) 🔗

---

## ❓ **FAQ**

Have questions? Check our [FAQ page](https://flectone.net/pulse/docs/) in the documentation for answers to common queries about setup, configuration, and troubleshooting

---

## 🙏 **Credits**

FlectonePulse is built on the shoulders of these excellent projects:

- 🏗️ **[Google Guice](https://github.com/google/guice)** — Dependency injection framework for modular code
- 📚 **[JDBI](https://jdbi.org/)** with **[HikariCP](https://github.com/brettwooldridge/HikariCP)** — Efficient database access with connection pooling for optimal performance
- 📦 **[Elytrium Java Serializer](https://github.com/Elytrium/java-serializer)** — Efficient data serialization
- 🧙 **[PacketEvents](https://github.com/retrooper/packetevents)** — Advanced packet handling for Minecraft
- 🎨 **[Adventure](https://github.com/KyoriPowered/adventure)** — Modern text formatting and styling
- ⌨️ **[Cloud](https://github.com/Incendo/cloud)** — Command framework with robust autocomplete
- 📊 **[Scoreboard](https://github.com/MegavexNetwork/scoreboard-library)** — Reliable scoreboard management
- ⏱️ **[Universal Scheduler](https://github.com/Anon8281/UniversalScheduler)** — Precise task scheduling
- 🔣 **[Symbol Chat](https://github.com/replaceitem/symbol-chat)** — Extended symbol support for chat
- 🖥️ **[PacketUxUi](https://github.com/OceJlot/PacketUxUi)** — Enhanced GUI components
- 💬 **[LightChatBubbles](https://github.com/atesin/LightChatBubbles)** — Lightweight chat bubble system
- 🌐 **[MiniTranslator](https://github.com/imDaniX/MiniTranslator)** — Converts legacy Minecraft formatting codes
- 🌱 **[FlectoneChat](https://github.com/Flectone/FlectoneChat)** — This project’s granddaddy of FlectonePulse

**Thank you to our community!** Every GitHub star fuels our motivation ⭐

## 📊 **Project Stats**
<div align="center">
  <a href="https://flectone.net/en/pulse/metrics" target="_blank">
    <img src="https://flectone.net/api/pulse/metrics/svg" alt="FlectonePulse stats">
  </a>
</div>

---

## ❤️ **Open Source & Free**

FlectonePulse is free to download, modify, and use. For **priority support** and **custom solutions**, consider supporting us on Boosty:  
[![boosty](https://flectone.net/pulse/boosty.svg)](https://boosty.to/thefaser)

**Benefits of supporting:**
- 🚀 Technical Q&A access
- 🛠️ Tailored server solutions
- 🔥 Early access to new features

**For everyone:**
- 📚 Explore the [documentation](https://flectone.net/pulse/)
- 🐞 Report issues via [GitHub Issues](https://github.com/Flectone/FlectonePulse/issues)

<div align="center">
  <h2><b>FlectonePulse Awaits! Ready to Deploy? 😎</b></h2>
  <a href="https://modrinth.com/plugin/flectonepulse"><img src="https://flectone.net/pulse/modrinth.svg" width="200" alt="modrinth"></a>
  <br>
  <h3>P.S. Join our <a href="https://discord.flectone.net/">Discord</a> 🎉</h3>
</div>