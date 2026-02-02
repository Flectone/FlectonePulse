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
    <h1>FlectonePulse Hytale — Every message under your control!</h1>
    <a href="https://boosty.to/thefaser"><img src="https://flectone.net/pulse/boosty.svg" alt="boosty" class="hover-brightness"></a>
    <a href="https://flectone.net/pulse/"><img src="https://flectone.net/pulse/documentation.svg" alt="documentation" class="hover-brightness"></a>
    <a href="https://discord.flectone.net/"><img src="https://flectone.net/pulse/discord.svg" alt="discord" class="hover-brightness"></a>
</div>

## 🏆 What makes FlectonePulse Hytale special?

FlectonePulse Hytale is a specialized module for the FlectonePulse ecosystem that takes control of chat, messages, and notifications specifically tailored for the Hytale platform.

- All operations are performed asynchronously, the main server thread is not affected
- Uses Google Guice for dependency injection, which simplifies extending functionality
- Specifically designed for the Hytale platform with native component support

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

![color](https://flectone.net/pulse/hytale/welcomemessage.png)

## 🧱 Message bubbles
Visual message bubbles above players

![Message Bubbles](https://flectone.net/pulse/hytale/bubble.gif)

## 🌈 Chat customization with /chatsetting

![chatsetting](https://flectone.net/pulse/hytale/commandchatsetting.gif)

The /chatsetting command opens a menu for quick message customization for each player. Choose colors, styles, and disable unnecessary messages

## 🌍 Smart localization

![Locale](https://flectone.net/pulse/hytale/locale.gif)

### How it works
When `by_player: true` is enabled, FlectonePulse detects the client's language and displays messages in it. If no translation exists, the default from the config is used.

## ✨ Customizable elements

| **Visual**                                                                       | **Description** |  
|----------------------------------------------------------------------------------|-----------------|  
| ![join](https://flectone.net/pulse/hytale/join.png)                              | **Join messages** <br> Greet players |  
| ![death](https://flectone.net/pulse/hytale/death.png)                            | **Death messages** <br> Make them fun with text or sounds |  
| ![chat](https://flectone.net/pulse/hytale/chat.png)                              | **Chat formatting** <br> Enhance chat with colors, gradients and more |  
| Full list in [documentation](https://flectone.net/pulse/docs/hytale/message/) 🔗 | ...                                                               |

## 🤝 Integrations

### External platforms

| **Visual** | **Description**                                                          |  
|---------|--------------------------------------------------------------------------|  
| [![discord](https://flectone.net/pulse/discordmessage.png)](https://flectone.net/pulse/docs/hytale/integration/discord/) | **Discord** <br> Sync server chat with Discord channels                  |  
| [![telegram](https://flectone.net/pulse/telegrammessage2.png)](https://flectone.net/pulse/docs/hytale/integration/telegram/) | **Telegram** <br> Send messages via bot to Telegram and sync chats       |  
| [![twitch](https://flectone.net/pulse/twitchmessage.png)](https://flectone.net/pulse/docs/hytale/integration/twitch/) | **Twitch** <br> Stream notifications in Hytale chat and server chat sync |  

### Plugins

| **Plugin**                                                                                | **Description**                                                 |  
|-----------------------------------------------------------------------------------------------|--------------------------------------------------------------------|  
| 🛡️ **LuckPerms**                   | Permission and group management         |

## 🎮 Over 30 commands

| **Visual**                                                                                                       | **Description** |  
|------------------------------------------------------------------------------------------------------------------|------------------------|  
| [![ball](https://flectone.net/pulse/hytale/commandball.png)](https://flectone.net/pulse/docs/hytale/command/)           | **/ball** <br> Magic ball with many answers |  
| [![stream](https://flectone.net/pulse/hytale/commandstream.png)](https://flectone.net/pulse/docs/hytale/command/)       | **/stream** <br> Stream notifications in chat |  
| [![try](https://flectone.net/pulse/hytale/commandtry.png)](https://flectone.net/pulse/docs/hytale/command/)             | **/try** <br> Test your luck from 0% to 100% |  
| Full list in [documentation](https://flectone.net/pulse/docs/hytale/command/) 🔗                                        

## 🙏 Acknowledgments

FlectonePulse is built on these projects:

- 🏗️ **[Google Guice](https://github.com/google/guice)** — for modular code
- 📚 **[JDBI](https://jdbi.org/)** with **[HikariCP](https://github.com/brettwooldridge/HikariCP)** — efficient database work
- 📦 **[Jackson](https://github.com/FasterXML/jackson)** — data serialization
- 🧙 **[Adventure Hytale](https://github.com/ArikSquad/adventure-platform-hytale)** — text formatting for Hytale
- 🎨 **[Adventure](https://github.com/KyoriPowered/adventure)** — text formatting
- ⌨️ **[Cloud](https://github.com/Incendo/cloud)** — commands with autocompletion
- 🔣 **[Symbol Chat](https://github.com/replaceitem/symbol-chat)** — symbols in chat
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
  <br>
  <h3>P.S. Join <a href="https://discord.flectone.net/">Discord</a></h3>
</div>