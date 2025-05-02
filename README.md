<div align="center">
  <h3>
    <a href="README.md">EN</a> | 
    <a href="README-RU.md">РУ</a>
  </h3>
</div>

![pulse](https://flectone.net/pulse/flectonepulse.png)
<div class="center-row" align="center">
    <h1> FlectonePulse — Every Message Under Your Control! </h1>
    <a href="https://www.spigotmc.org/"><img src="https://flectone.net/pulse/bukkit.svg" alt="bukkit" class="hover-brightness"></a>
    <a href="https://www.spigotmc.org/"><img src="https://flectone.net/pulse/spigot.svg" alt="spigot" class="hover-brightness"></a>
    <a href="https://papermc.io/"><img src="https://flectone.net/pulse/paper.svg" alt="paper" class="hover-brightness"></a>
    <a href="https://purpurmc.org/"><img src="https://flectone.net/pulse/purpur.svg" alt="purpur" class="hover-brightness"></a>
    <a href="https://papermc.io/software/folia"><img src="https://flectone.net/pulse/folia.svg" alt="folia" class="hover-brightness"></a>
    <a href="https://www.spigotmc.org/wiki/bungeecord/"><img src="https://flectone.net/pulse/bungeecord.svg" alt="bungeecord" class="hover-brightness"></a>
    <a href="https://papermc.io/software/velocity"><img src="https://flectone.net/pulse/velocity.svg" alt="velocity" class="hover-brightness"></a>
</div>

---

## 🏆 Advantages That Will Blow Your Mind

- ⚡ **Maximized Asynchrony**  
  All operations **never lag** the main thread, and configs load in the background. Your server **won’t stutter** even under peak load!

- 🏭 **Flexible Architecture with Google Guice**  
  Dependency injection? Easy! **Guice** keeps code clean and logic modular. Want to add your own feature? **No problem**—the system is built for extensions!

- 🔄 **Versatility Is Our Middle Name**  
  Supports **all** popular platforms: Bukkit, Spigot, Paper, Purpur, Folia, BungeeCord, Velocity. No matter your server’s "engine"—**Pulse** adapts to any setup!

- 🎨 **Customize Every Detail**  
  Colors, animations, Discord/Telegram/Twitch integrations — **everything** under your control. Even death messages can become memes! 😈

---

<div align="center">

[![logo](https://github.com/user-attachments/assets/dc68fd41-8341-43e5-9c07-843e1ad839f1)](https://flectone.net/pulse/)  
<h3>🚀 Install FlectonePulse — Let Your Server Pulse with Rhythm! 💖</h3>
  <div>
    <a href="https://boosty.to/thefaser"><img src="https://flectone.net/pulse/boosty.svg" alt="boosty" class="hover-brightness"></a>
    <a href="https://modrinth.com/plugin/flectonepulse"><img src="https://flectone.net/pulse/modrinth.svg" alt="modrinth" class="hover-brightness"></a>
    <a href="https://flectone.net/pulse/"><img src="https://flectone.net/pulse/documentation.svg" alt="documentation" class="hover-brightness"></a>
    <a href="https://discord.flectone.net/"><img src="https://flectone.net/pulse/discord.svg" alt="discord" class="hover-brightness"></a>
  </div>
</div>

---

## 🎨 **Universal Formatting**

**FlectonePulse** has one of the most versatile text formatting systems available, supporting virtually all color formats from legacy codes to modern gradients.

| **Input Code**                                        | **Converts To**                                             |  
|-------------------------------------------------------|-------------------------------------------------------------|  
| `&0`-`&9`, `&a`-`&f`                                  | `<black>`, `<dark_blue>` ... `<white>`                      |  
| `&l`/`&m`/`&n`/`&o`/`&k`/`&r`                         | `<b>` / `<st>` / `<u>` / `<i>` / `<obf>` / `<reset>`        |  
| `&#rrggbb`, `#rrggbb`, `&x&r&r&g&g&b&b`, `<##rrggbb>` | `<#rrggbb>`                                                 |  
| `&@#abcdef-red-a@`                                    | `<gradient:#abcdef:red:green>`                              |  
| MiniMessage Tags	                                     | `<color:#rrggbb`, `<rainbow>`, `<click:...>`, `<font>` etc. |

*You can use `&` or `§` alternately for maximum flexibility*

**Example message with formatting**
```yaml
welcome-message: "<gradient:#FF0000:#00FF00>&lWelcome</gradient> <rainbow><player></rainbow>!"
```

---

## 🌈 Custom Colors — Paint Your Reality!

![color](https://cdn.modrinth.com/data/cached_images/918bc78d4897d0453625d35b3b1f4271b092651b.gif)

Create unique chat designs — players will gasp at the style 💥. Even the server console becomes more colorful!

---

## 🌍 **Smart Localization — Speak Your Players’ Language!**

### ⚙️ How It Works
[![locale](https://flectone.net/pulse/locale.gif)](https://flectone.net/pulse/docs/config#language-player)

- 🔄 **Auto-Language Detection**  
  If `language-player` is enabled in config, the plugin **automatically** detects players’ Minecraft language and displays messages accordingly!

- 🛠️ **Fallback System**  
  If localization isn’t found for a player’s language, it uses the default language from the config.

**→ Full localization guide in the** [documentation](https://flectone.net/pulse/docs/config#language-player) 🔗

---

## ✨ Personalize EVERYTHING That Moves!
**Customize default messages to match your unique vibe:**

| 🖼️ **Visual** | 💬 **Description** |  
|---------------|-----------------|  
| ![status](https://flectone.net/pulse/version.png) | **Server MOTD** <br> Turn boring text into epic billboards! Add animations or hidden Easter eggs 🥚 |  
| ![join](https://flectone.net/pulse/join.png) | **Join Messages** <br> Welcome players like rockstars: sounds, custom Title messages 🎸 |  
| ![tab](https://flectone.net/pulse/tab.png) | **TAB** <br> Show top info directly in Tab: ping, online status, donor status 🔝 |  
| ![death](https://flectone.net/pulse/deathserver.png) | **Deaths** <br> Add dark humor, epic soundtracks 💀 |  
| ![brand](https://flectone.net/pulse/brand.png) | **Branding** <br> Your logo in the F3 menu? Easy! Promote your server even here 🔍 |  
| ![advancement](https://flectone.net/pulse/task.png) | **Advancements** <br> Create unique achievement messages 🏆 |  

**...and many more settings!** Full list in [message.yml docs](https://flectone.net/pulse/docs/message/) 🚀

---

## 🤝 Integrations

### 🌍 With External Platforms

| 🖼️ **Visual** | 💬 **Description** |  
|---------|----------|  
| [![discord](https://flectone.net/pulse/discordmessage.png)](https://flectone.net/pulse/docs/integration/discord/) | **Discord** <br> Server chat in your channel + synchronized notifications 💬 |  
| [![telegram](https://flectone.net/pulse/telegrammessage2.png)](https://flectone.net/pulse/docs/integration/telegram/) | **Telegram** <br> Receive player messages and admin commands via bot 📲 |  
| [![twitch](https://flectone.net/pulse/twitchmessage.png)](https://flectone.net/pulse/docs/integration/twitch/) | **Twitch** <br> Stream alerts directly in Minecraft chat 🎥 |  

### 🔌 With Plugins

| 🖼️ **Plugin**                                                                                | 💬 **Description**                                                 |  
|-----------------------------------------------------------------------------------------------|--------------------------------------------------------------------|  
| 💬 **[InteractiveChat](https://flectone.net/pulse/docs/integration/interactivechat/)**        | Advanced chat formatting with interactive elements                 | 
| 🛡️ **[LuckPerms](https://flectone.net/pulse/docs/integration/luckperms/)**                   | Roles, permissions, and weighted groups—all under control          |  
| 🧩 **[PlaceholderAPI](https://flectone.net/pulse/docs/integration/placeholderapi/)**          | Dynamic variables like <code>%player_level%</code> in any message  |  
| 🎙️ **[PlasmoVoice & SimpleVoice](https://flectone.net/pulse/docs/integration/plasmovoice/)** | Sync ignores/mutes between voice chat and server                   |  
| 🖼️ **[SkinsRestorer](https://flectone.net/pulse/docs/integration/skinsrestorer/)**           | Custom skins in chat/TAB without hassle                            |  
| 👻 **[SuperVanish](https://flectone.net/pulse/docs/integration/supervanish/)**                | Hidden players don’t break chat/command logic                      |  
| 💰 **[Vault](https://flectone.net/pulse/docs/integration/vault/)**                            | Unified permissions via vanilla permissions                        |  

**→ Full integration list in** [documentation](https://flectone.net/pulse/docs/integration/) 🔗

---

## 🎮 30+ Commands

| 🖼️ **Visual** | 💬 **Description** |  
|--------------|------------------------|  
| [![ball](https://flectone.net/pulse/commandball.png)](https://flectone.net/pulse/docs/command/) | **/ball** <br> Magic 8-ball with 20+ response options |  
| [![tictactoe](https://flectone.net/pulse/commandtictactoe.png)](https://flectone.net/pulse/docs/command/) | **/tictactoe** <br> 3D board with animated win lines. Modes: <br> 🤖 Chinese 👥 Classic |  
| [![stream](https://flectone.net/pulse/commandstream.png)](https://flectone.net/pulse/docs/command/) | **/stream** <br> Stream alerts + auto-posting to Discord, with streamer prefix |  
| [![try](https://flectone.net/pulse/commandtry.png)](https://flectone.net/pulse/docs/command/) | **/try** <br> 0-100% roulette! |  

**→ Full command list in** [documentation](https://flectone.net/pulse/docs/command/) 🔗

---

## 🙏 Credits — Thank You for Existing!

**These projects are our foundation and inspiration:**

- 🏗️ **[Google Guice](https://github.com/google/guice)** — Boss-level dependency injection
- 📦 **[Elytrium Java Serializer](https://github.com/Elytrium/java-serializer)** — Faster data serialization than Minecraft crashes without OptiFine
- 🧙 **[PacketEvents](https://github.com/retrooper/packetevents)** — Minecraft packet magic at Dumbledore’s level
- 🎨 **[Adventure](https://github.com/KyoriPowered/adventure)** — Text styling cooler than Banksy’s graffiti
- ⌨️ **[Cloud](https://github.com/Incendo/cloud)** — Powerful command creation with advanced autocomplete
- 📊 **[Scoreboard](https://github.com/MegavexNetwork/scoreboard-library)** — Scoreboards stable on a Pentium III
- ⏱️ **[Universal Scheduler](https://github.com/Anon8281/UniversalScheduler)** — Timers more precise than a robot’s metronome
- 🔣 **[Symbol Chat](https://github.com/replaceitem/symbol-chat)** — 200+ symbols for when emojis aren’t enough 😏
- 🖥️ **[PacketUxUi](https://github.com/OceJlot/PacketUxUi)** — GUIs that shame vanilla interfaces
- 💬 **[LightChatBubbles](https://github.com/atesin/LightChatBubbles)** — Non-annoying chat bubbles even after 10 hours of play
- 🌐 **[MiniTranslator](https://github.com/imDaniX/MiniTranslator)** — A lightweight utility for converting Minecraft's legacy formatting codes
- 🌱 **[FlectoneChat](https://github.com/Flectone/FlectoneChat)** — This project’s granddaddy, still trending

**...and you, stars!** Every GitHub star is like RedBull for our code ⭐

## 📊 **Stats — We’re Trending!**
Yes, we track stats like Elon tracks Twitter 🚀
<div align="center">
  <a href="https://flectone.net/en/pulse/metrics" target="_blank">
    <img src="https://flectone.net/api/pulse/metrics/svg" alt="FlectonePulse stats">
  </a>
</div>

---

## ❤️ Project — Open Source & Free Forever
**Download, modify, use—we encourage it!** Want **priority fixes** and **personalized advice**? Support us on Boosty:  
[![boosty](https://flectone.net/pulse/boosty.svg)](https://boosty.to/thefaser)

**What you get:**
- 🚀 Technical Q&A access
- 🛠️ Custom server solutions
- 🔥 Early access to experimental features

**For others:**
- 📚 Everything’s in the [documentation](https://flectone.net/pulse/)
- 🐞 Report bugs via [Issues](https://github.com/Flectone/FlectonePulse/issues)

<div align="center">
  <h2> <b>FlectonePulse Awaits! Ready to Deploy? 😏</b> </h2>
  <a href="https://modrinth.com/plugin/flectonepulse"><img src="https://flectone.net/pulse/modrinth.svg" width="200" alt="modrinth"></a>
  <br>
  <h3>P.S. Join our <a href="https://discord.flectone.net/">Discord</a> 🎨</h3>
</div>