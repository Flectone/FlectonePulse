package net.flectone.pulse.config.localization;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.util.constant.MessageType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

@Singleton
public class RussianLocale implements Locale {

    @Inject
    public RussianLocale() {
    }

    @Override
    public void init(Localization localization) {
        localization.cooldown = "<color:#ff7171><b>⁉</b> Слишком быстро, попробуй через <time>";

        localization.time.format = "dd'д' HH'ч' mm'м' ss.SSS'с'";
        localization.time.permanent = "НАВСЕГДА";
        localization.time.zero = "0с";

        localization.command.anon.format = "<fcolor:1>\uD83D\uDC7B <fcolor:2>Анон <fcolor:1><message>";

        localization.command.ball.format = "<color:#9370DB>❓ <display_name> спросил: <message><reset> <color:#9370DB><br>🔮 Магический шар: <u><answer></u>";
        localization.command.ball.answers = new LinkedList<>() {{
            add("Бесспорно");
            add("Никаких сомнений");
            add("Определённо да");
            add("Это база");
            add("Можешь быть уверен в этом");
            add("Вероятнее всего");
            add("Хорошие перспективы");
            add("Да");
            add("Пока не ясно, попробуй снова");
            add("Спроси позже");
            add("Лучше не рассказывать");
            add("Сейчас нельзя предсказать");
            add("Сконцентрируйся и спроси опять");
            add("Даже не думай");
            add("Нет.");
            add("Перспективы не очень хорошие");
            add("Весьма сомнительно");
        }};

        localization.command.ban.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        localization.command.ban.nullTime = "<color:#ff7171><b>⁉</b> Невозможное время";
        localization.command.ban.reasons = new Localization.ReasonMap() {{
            put("default", "Ты заблокирован на этом сервере");
        }};
        localization.command.ban.server = "<color:#ff7171>🔒 <fcolor:2><moderator><fcolor:1> заблокировал игрока <fcolor:2><player> <fcolor:1><hover:show_text:\"<fcolor:1>Айди: <id><br>Дата: <date><br>Время: <time><br>Осталось: <time_left><br>Модератор: <moderator><br>Причина: <reason>\">[ПОДРОБНЕЕ]</hover>";
        localization.command.ban.person = "<color:#ff7171>🔒 БАН 🔒 <fcolor:1><br><br>Дата: <date><br><br>Время: <time><br><br>Осталось: <time_left><br><br>Модератор: <moderator><br><br>Причина: <reason>";
        localization.command.ban.connectionAttempt = "<color:#ff7171>🔒 Заблокированный <fcolor:2><player><fcolor:1> попытался подключиться <fcolor:1><hover:show_text:\"<fcolor:1>Айди: <id><br>Дата: <date><br>Время: <time><br>Осталось: <time_left><br>Модератор: <moderator><br>Причина: <reason>\">[ПОДРОБНЕЕ]</hover>";

        localization.command.banlist.empty = "<color:#98FB98>☺ Блокировки не найдены";
        localization.command.banlist.nullPage = "<color:#ff7171><b>⁉</b> Страница не найдена";
        localization.command.banlist.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        localization.command.banlist.global.header = "<fcolor:2>▋ Блокировки: <count> <br>";
        localization.command.banlist.global.line = "<hover:show_text:\"<fcolor:1>Разблокировать <display_name>\"><click:run_command:\"<command>\"><color:#ff7171>☒ <display_name></click></hover> <fcolor:1><hover:show_text:\"<fcolor:1>Айди: <id><br>Дата: <date><br>Время: <time><br>Модератор: <moderator><br>Причина: <reason>\">[ПОДРОБНЕЕ]</hover>";
        localization.command.banlist.global.footer = "<br><fcolor:2>▋ <fcolor:2><click:run_command:\"<command> <prev_page>\">←</click> <fcolor:1>Page: <current_page>/<last_page> <fcolor:2><click:run_command:\"<command> <next_page>\">→";
        localization.command.banlist.player.header = "<fcolor:2>▋ Все блокировки: <count> <br>";
        localization.command.banlist.player.line = "<hover:show_text:\"<fcolor:1>Разблокировать <display_name>\"><click:run_command:\"<command>\"><color:#ff7171>☒ <display_name></click></hover> <fcolor:1><hover:show_text:\"<fcolor:1>Айди: <id><br>Дата: <date><br>Время: <time><br>Модератор: <moderator><br>Причина: <reason>\">[ПОДРОБНЕЕ]</hover>";
        localization.command.banlist.player.footer = "<br><fcolor:2>▋ <click:run_command:\"<command> <prev_page>\">←</click> <fcolor:1>Page: <current_page>/<last_page> <fcolor:2><click:run_command:\"<command> <next_page>\">→";

        localization.command.broadcast.format = "<br><color:#ffd500>│ \uD83D\uDD6B Сообщение для всех <br>│<br>│ Автор <display_name> <br>│<br>│ <fcolor:1><message> <br>";

        localization.command.chatcolor.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        localization.command.chatcolor.nullType = "<color:#ff7171><b>⁉</b> Тип введён неверно";
        localization.command.chatcolor.nullColor = "<color:#ff7171><b>⁉</b> Цвета введены неверно";
        localization.command.chatcolor.format = "<br><color:#98FB98>│ Твои цвета выглядят так: <br><color:#98FB98>│ <fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><color:#98FB98>│ <fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир! <br>";

        localization.command.chatsetting.noPermission = "<color:#ff7171><b>⁉</b> Нет разрешения на изменение этой настройки";
        localization.command.chatsetting.disabledSelf = "<color:#ff7171><b>⁉</b> Эта функция отключена с помощью /chatsetting";
        localization.command.chatsetting.disabledOther = "<color:#ff7171><b>⁉</b> Он отключил эту функцию с помощью /chatsetting";
        localization.command.chatsetting.inventory = "Настройки чата";
        localization.command.chatsetting.checkbox.enabledHover = "<color:#98FB98>Отображение включено";
        localization.command.chatsetting.checkbox.disabledHover = "<color:#ff7171>Отображение выключено";
        localization.command.chatsetting.checkbox.types = new LinkedHashMap<>() {{
            put(MessageType.AFK.name(), "<status_color>Афк");
            put(MessageType.ADVANCEMENT.name(), "<status_color>Достижения");
            put(MessageType.CHAT.name(), "<status_color>Сообщения чата");
            put(MessageType.COMMAND_ANON.name(), "<status_color>Команда /anon");
            put(MessageType.COMMAND_BALL.name(), "<status_color>Команда /ball");
            put(MessageType.COMMAND_BROADCAST.name(), "<status_color>Команда /broadcast");
            put(MessageType.COMMAND_COIN.name(), "<status_color>Команда /coin");
            put(MessageType.COMMAND_DICE.name(), "<status_color>Команда /dice");
            put(MessageType.COMMAND_DO.name(), "<status_color>Команда /do");
            put(MessageType.COMMAND_MAIL.name(), "<status_color>Команда /mail");
            put(MessageType.COMMAND_ME.name(), "<status_color>Команда /me");
            put(MessageType.COMMAND_POLL.name(), "<status_color>Команда /poll");
            put(MessageType.COMMAND_ROCKPAPERSCISSORS.name(), "<status_color>Команда /rockpaperscissors");
            put(MessageType.COMMAND_STREAM.name(), "<status_color>Команда /stream");
            put(MessageType.COMMAND_TELL.name(), "<status_color>Команда /tell");
            put(MessageType.COMMAND_TICTACTOE.name(), "<status_color>Команда /tictactoe");
            put(MessageType.COMMAND_TRY.name(), "<status_color>Команда /try");
            put(MessageType.DEATH.name(), "<status_color>Смерти");
            put(MessageType.FROM_DISCORD_TO_MINECRAFT.name(), "<status_color>Сообщения из Discord");
            put(MessageType.FROM_TELEGRAM_TO_MINECRAFT.name(), "<status_color>Сообщения из Telegram");
            put(MessageType.FROM_TWITCH_TO_MINECRAFT.name(), "<status_color>Сообщения из Twitch");
            put(MessageType.JOIN.name(), "<status_color>Вход на сервер");
            put(MessageType.QUIT.name(), "<status_color>Выход с сервера");
            put(MessageType.SLEEP.name(), "<status_color>Сон");
        }};
        localization.command.chatsetting.menu.chat.item = "<fcolor:2>Тип чата <br><fcolor:1>Чат для просмотра и отправки сообщений <br><br><fcolor:1>Выбранный чат <fcolor:2><chat>";
        localization.command.chatsetting.menu.chat.inventory = "Чаты";
        localization.command.chatsetting.menu.chat.types = new LinkedHashMap<>() {{
            put("default", "<fcolor:2>Стандартный чат<br><fcolor:1>Ты можешь видеть <fcolor:2>все <fcolor:1>чаты и писать в любой чат");
            put("local", "<fcolor:2>Локальный чат<br><fcolor:1>Ты можешь писать в <fcolor:2>любой <fcolor:1>чат");
            put("global", "<fcolor:2>Глобальный чат<br><fcolor:1>Ты можешь писать только в <fcolor:2>глобальный <fcolor:1>чат");
        }};
        localization.command.chatsetting.menu.see.item = "<fcolor:2>Цвета \"see\" <br><fcolor:1>Цвета для /chatcolor see <br><br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир! <br><br><u><color:#ff7171>Это видишь только ТЫ в любых сообщений";
        localization.command.chatsetting.menu.see.inventory = "Цвета";
        localization.command.chatsetting.menu.see.types = new LinkedHashMap<>() {{
            put("default", "<gradient:#70C7EF:#37B1F2>Стандартные цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
            put("white", "<gradient:#D4E4FF:#B8D2FF>Белые цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
            put("light_gray", "<gradient:#B5B9BD:#9DA2A6>Светло-серые цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
            put("gray", "<gradient:#4A5054:#3A3F42>Серые цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
            put("black", "<gradient:#17191A:#0D0E0F>Черные цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
            put("brown", "<gradient:#7A5A40:#634A34>Коричневые цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
            put("red", "<gradient:#D63E3E:#C12B2B>Красные цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
            put("orange", "<gradient:#FF8C00:#E67E00>Оранжевые цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
            put("yellow", "<gradient:#FFE83D:#FFD900>Желтые цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
            put("lime", "<gradient:#8EE53F:#7ACC29>Лаймовые цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
            put("green", "<gradient:#4BB54B:#3AA33A>Зеленые цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
            put("cyan", "<gradient:#3ECFDF:#2AB7C9>Бирюзовые цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
            put("light_blue", "<gradient:#6BB6FF:#4DA6FF>Голубые цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
            put("blue", "<gradient:#3A75FF:#1F5AFF>Синие цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
            put("purple", "<gradient:#A368C7:#8A4DBF>Фиолетовые цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
            put("magenta", "<gradient:#FF5CD9:#FF3DCF>Пурпурные цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
            put("pink", "<gradient:#FF80B7:#FF66A6>Розовые цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
        }};
        localization.command.chatsetting.menu.out.item = "<fcolor:2>Цвета \"out\" <br><fcolor:1>Цвета для /chatcolor out <br><br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир! <br><br><u><color:#ff7171>Это видят ВСЕ у твоих сообщений";
        localization.command.chatsetting.menu.out.inventory = "Цвета";
        localization.command.chatsetting.menu.out.types = new LinkedHashMap<>() {{
            put("default", "<gradient:#70C7EF:#37B1F2>Стандартные цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
            put("white", "<gradient:#D4E4FF:#B8D2FF>Белые цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
            put("light_gray", "<gradient:#B5B9BD:#9DA2A6>Светло-серые цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
            put("gray", "<gradient:#4A5054:#3A3F42>Серые цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
            put("black", "<gradient:#17191A:#0D0E0F>Черные цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
            put("brown", "<gradient:#7A5A40:#634A34>Коричневые цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
            put("red", "<gradient:#D63E3E:#C12B2B>Красные цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
            put("orange", "<gradient:#FF8C00:#E67E00>Оранжевые цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
            put("yellow", "<gradient:#FFE83D:#FFD900>Желтые цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
            put("lime", "<gradient:#8EE53F:#7ACC29>Лаймовые цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
            put("green", "<gradient:#4BB54B:#3AA33A>Зеленые цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
            put("cyan", "<gradient:#3ECFDF:#2AB7C9>Бирюзовые цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
            put("light_blue", "<gradient:#6BB6FF:#4DA6FF>Голубые цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
            put("blue", "<gradient:#3A75FF:#1F5AFF>Синие цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
            put("purple", "<gradient:#A368C7:#8A4DBF>Фиолетовые цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
            put("magenta", "<gradient:#FF5CD9:#FF3DCF>Пурпурные цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
            put("pink", "<gradient:#FF80B7:#FF66A6>Розовые цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
        }};

        localization.command.clearchat.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        localization.command.clearchat.format = "<fcolor:1>\uD83D\uDCAC Чат очищен";

        localization.command.clearmail.nullMail = "<color:#ff7171><b>⁉</b> Письма не найдено";
        localization.command.clearmail.format = "<fcolor:2>✉ [УДАЛЕНО] Письмо #<id> для <target> » <fcolor:1><message>";

        localization.command.coin.head = "орёл";
        localization.command.coin.tail = "решка";
        localization.command.coin.format = "<fcolor:1>✎ <display_name> подбросил монетку - <result>";
        localization.command.coin.formatDraw = "<fcolor:1>✎ <display_name> неудачно подбросил монетку ребром :)";

        localization.command.deletemessage.nullMessage = "<color:#ff7171><b>⁉</b> Сообщение не существует";
        localization.command.deletemessage.format = "<color:#98FB98>☒ Сообщение успешно удалено";

        localization.command.dice.format = "<fcolor:1>✎ <display_name> кинул кубики <message><reset> <fcolor:1>(<sum>)";
        localization.command.dice.symbols = new LinkedHashMap<>() {{
            put(1, "⚀");
            put(2, "⚁");
            put(3, "⚂");
            put(4, "⚃");
            put(5, "⚄");
            put(6, "⚅");
        }};

        localization.command.Do.format = "<fcolor:1>✎ <message><reset> <fcolor:1>(<i><display_name></i>)";

        localization.command.exception.execution = "<color:#ff7171><b>⁉</b> Произошла ошибка при выполнении команды <br><color:#ff7171><b>⁉</b> <exception>";
        localization.command.exception.syntax = "<hover:show_text:\"<fcolor:2>Использовать <br><fcolor:1>/<correct_syntax>\"><click:suggest_command:\"/<command> \"><fcolor:2>┌<br>│ Использование →<br>│ <fcolor:1>/<correct_syntax><br><fcolor:2>└";
        localization.command.exception.parseUnknown = "<color:#ff7171><b>⁉</b> Неизвестная ошибка аргумента в <br><input>";
        localization.command.exception.parseBoolean = "<color:#ff7171><b>⁉</b> Ожидался boolean аргумент, но ты ввёл <br><input>";
        localization.command.exception.parseNumber = "<color:#ff7171><b>⁉</b> Ожидался числовой аргумент, но ты ввёл <br><input>";
        localization.command.exception.parseString = "<color:#ff7171><b>⁉</b> Ожидался строковый аргумент, но ты ввёл <br><input>";
        localization.command.exception.permission = "<color:#ff7171><b>⁉</b> У тебя нет прав на использование этой команды";

        localization.command.flectonepulse.nullHostEditor = "<color:#ff7171><b>⁉</b> Параметр host должен быть настроен в <u>config.yml";
        localization.command.flectonepulse.formatFalse = "<color:#ff7171>★ Возникла проблема при перезагрузке <br>Ошибка: <message>";
        localization.command.flectonepulse.formatTrue = "<fcolor:2>★ <u>FlectonePulse</u> успешно перезагружен! (<i><time></i>)";
        localization.command.flectonepulse.formatWebStarting = "<fcolor:2>★ Запуск веб-сервера, подождите...";
        localization.command.flectonepulse.formatEditor = "<fcolor:2>★ Ссылка для веб-редактирования <u><fcolor:2><click:open_url:\"<url>\"><hover:show_text:\"<fcolor:2><url>\"><url>";

        localization.command.geolocate.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        localization.command.geolocate.nullOrError = "<color:#ff7171><b>⁉</b> Ошибка, попробуй чуть позже";
        localization.command.geolocate.format = "<fcolor:1>Геолокация <display_name><br>Страна: <fcolor:2><country><br><fcolor:1>Регион: <fcolor:2><region_name><br><fcolor:1>Город: <fcolor:2><city><br><fcolor:1>Часовой пояс: <fcolor:2><timezone><br><fcolor:1>Мобильный интернет? <fcolor:2><mobile><br><fcolor:1>ВПН? <fcolor:2><proxy><br><fcolor:1>Хостинг? <fcolor:2><hosting><br><fcolor:1>Айпи: <fcolor:2><query>";

        localization.command.helper.nullHelper = "<color:#ff7171><b>⁉</b> Сейчас нет людей, кто бы смог помочь";
        localization.command.helper.global = "<fcolor:2>👤 <display_name> просит помощи ⏩ <fcolor:1><message>";
        localization.command.helper.player = "<fcolor:2>👤 Запрос отправлен, ожидай ответа";

        localization.command.ignore.myself = "<color:#ff7171><b>⁉</b> Нельзя игнорировать самого себя";
        localization.command.ignore.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        localization.command.ignore.you = "<color:#ff7171><b>⁉</b> Ты его игнорируешь";
        localization.command.ignore.he = "<color:#ff7171><b>⁉</b> Он тебя игнорирует";
        localization.command.ignore.formatTrue = "<color:#ff7171>☹ Ты игнорируешь <display_name>";
        localization.command.ignore.formatFalse = "<color:#98FB98>☺ Ты перестал игнорировать <display_name>";

        localization.command.ignorelist.empty = "<color:#98FB98>☺ Игнорируемых игроков нет";
        localization.command.ignorelist.nullPage = "<color:#ff7171><b>⁉</b> Страница не найдена";
        localization.command.ignorelist.header = "<fcolor:2>▋ Игнорирования: <count> <br>";
        localization.command.ignorelist.line = "<hover:show_text:\"<fcolor:1>Перестать игнорировать <display_name>\"><click:run_command:\"<command>\"><color:#ff7171>☒ <display_name></click></hover> <fcolor:1>Дата: <date>";
        localization.command.ignorelist.footer = "<br>▋ <fcolor:2><click:run_command:\"<command> <prev_page>\">←</click> <fcolor:1>Страница: <current_page>/<last_page> <fcolor:2><click:run_command:\"<command> <next_page>\">→";

        localization.command.kick.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        localization.command.kick.reasons = new Localization.ReasonMap() {{
            put("default", "Исключён модератором");
        }};
        localization.command.kick.server = "<color:#ff7171>🔒 <fcolor:2><moderator><fcolor:1> исключил <fcolor:2><player><fcolor:1> <fcolor:1><hover:show_text:\"<fcolor:1>Айди: <id><br>Дата: <date><br>Модератор: <moderator><br>Причина: <reason>\">[ПОДРОБНЕЕ]</hover>";
        localization.command.kick.person = "<color:#ff7171>🔒 КИК 🔒 <fcolor:1><br><br>Айди: <id><br><br>Дата: <date><br><br>Модератор: <moderator><br><br>Причина: <reason>";

        localization.command.maintenance.kick = "<color:#ff7171>★ На сервере ведутся технические работы";
        localization.command.maintenance.serverDescription = "<color:#ff7171>В настоящее время проводятся технические работы";
        localization.command.maintenance.serverVersion = "Технические работы";
        localization.command.maintenance.formatTrue = "<fcolor:1>★ Ты <fcolor:2>включил <fcolor:1>технические работы на сервере";
        localization.command.maintenance.formatFalse = "<fcolor:1>★ Ты <fcolor:2>выключил <fcolor:1>технические работы на сервере";

        localization.command.mail.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        localization.command.mail.onlinePlayer = "<color:#ff7171><b>⁉</b> Игрок в сети";
        localization.command.mail.sender = "<fcolor:2>✉ Письмо #<id> для <target> » <fcolor:1><message>";
        localization.command.mail.receiver = "<fcolor:2>✉ Письмо от <display_name> » <fcolor:1><message>";

        localization.command.me.format = "<fcolor:1>✎ <display_name> <fcolor:1><message>";

        localization.command.mute.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        localization.command.mute.nullTime = "<color:#ff7171><b>⁉</b> Невозможное время";
        localization.command.mute.reasons = new Localization.ReasonMap() {{
            put("default", "Ты был замучен на сервере");
        }};
        localization.command.mute.server = "<color:#ff7171>🔒 <fcolor:2><moderator><fcolor:1> выдал мут игроку <fcolor:2><player> <fcolor:1><hover:show_text:\"<fcolor:1>Айди: <id><br>Дата: <date><br>Время: <time><br>Осталось: <time_left><br>Модератор: <moderator><br>Причина: <reason>\">[ПОДРОБНЕЕ]</hover>";
        localization.command.mute.person = "<color:#ff7171>🔒 Ты замучен, осталось <time_left>";

        localization.command.mutelist.empty = "<color:#98FB98>☺ Муты не найдены";
        localization.command.mutelist.nullPage = "<color:#ff7171><b>⁉</b> Страница не найдена";
        localization.command.mutelist.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        localization.command.mutelist.global.header = "<fcolor:2>▋ Муты: <count> <br>";
        localization.command.mutelist.global.line = "<hover:show_text:\"<fcolor:1>Размутить <display_name>\"><click:run_command:\"<command>\"><color:#ff7171>☒ <display_name></click></hover> <fcolor:1><hover:show_text:\"<fcolor:1>Айди: <id><br>Дата: <date><br>Время: <time><br>Модератор: <moderator><br>Причина: <reason>\">[ПОДРОБНЕЕ]</hover>";
        localization.command.mutelist.global.footer = "<br><fcolor:2>▋ <fcolor:2><click:run_command:\"<command> <prev_page>\">←</click> <fcolor:1>Страница: <current_page>/<last_page> <fcolor:2><click:run_command:\"<command> <next_page>\">→";
        localization.command.mutelist.player.header = "<fcolor:2>▋ Все муты: <count> <br>";
        localization.command.mutelist.player.line = "<hover:show_text:\"<fcolor:1>Размутить <display_name>\"><click:run_command:\"<command>\"><color:#ff7171>☒ <display_name></click></hover> <fcolor:1><hover:show_text:\"<fcolor:1>Айди: <id><br>Дата: <date><br>Время: <time><br>Модератор: <moderator><br>Причина: <reason>\">[ПОДРОБНЕЕ]</hover>";
        localization.command.mutelist.player.footer = "<br><fcolor:2>▋ <click:run_command:\"<command> <prev_page>\">←</click> <fcolor:1>Страница: <current_page>/<last_page> <fcolor:2><click:run_command:\"<command> <next_page>\">→";

        localization.command.online.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        localization.command.online.formatCurrent = "<fcolor:1>⌛ <display_name> сейчас на сервере";
        localization.command.online.formatFirst = "<fcolor:1>⌛ <display_name> впервые зашёл на сервер <time> назад";
        localization.command.online.formatLast = "<fcolor:1>⌛ <display_name> <fcolor:1>последний раз был на сервере <time> назад";
        localization.command.online.formatTotal = "<fcolor:1>⌛ <display_name> <fcolor:1>всего провёл на сервере <time>";

        localization.command.ping.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        localization.command.ping.format = "<fcolor:1>🖧 Пинг игрока <display_name> равен <ping>";

        localization.command.poll.nullPoll = "<color:#ff7171><b>⁉</b> Голосование не найдено";
        localization.command.poll.expired = "<color:#ff7171><b>⁉</b> Голосование завершено";
        localization.command.poll.already = "<color:#ff7171><b>⁉</b> Ты уже проголосовал в этом голосовании";
        localization.command.poll.voteTrue = "<color:#4eff52>👍 Ты выбрал <answer_id> вариант в голосовании #<id>. Всего таких голосов <count>";
        localization.command.poll.voteFalse = "<color:#ff4e4e>\uD83D\uDD93 Ты передумал об <answer_id> варианте в голосовании #<id>. Всего таких голосов <count> без тебя";
        localization.command.poll.format = "<br><color:#fce303>│ <status> <br>│ <message><reset> <color:#fce303><br>├─────────────<br><answers>";
        localization.command.poll.answerTemplate = "<color:#fce303>│ <count> → <color:#4eff52><hover:show_text:\"<color:#4eff52>Проголосовать за <bold><answer>\"><click:run_command:\"/pollvote <id> <number>\"><answer> [👍]<br>";
        localization.command.poll.status.start = "Создано новое голосование #<b><id></b>";
        localization.command.poll.status.run = "Идёт голосование #<b><id></b>";
        localization.command.poll.status.end = "Голосование #<b><id></b> завершено";
        localization.command.poll.modern.header = "Создание голосования";
        localization.command.poll.modern.inputName = "Название";
        localization.command.poll.modern.inputInitial = "";
        localization.command.poll.modern.multipleName = "Разрешить несколько ответов";
        localization.command.poll.modern.endTimeName = "Длительность (в минутах)";
        localization.command.poll.modern.repeatTimeName = "Интервал (в минутах)";
        localization.command.poll.modern.newAnswerButtonName = "Добавить ответ";
        localization.command.poll.modern.removeAnswerButtonName = "Удалить ответ";
        localization.command.poll.modern.inputAnswerName = "Ответ <number>";
        localization.command.poll.modern.inputAnswersInitial = "";
        localization.command.poll.modern.createButtonName = "Создать голосование";

        localization.command.prompt.message = "сообщение";
        localization.command.prompt.hard = "сложно?";
        localization.command.prompt.accept = "принять";
        localization.command.prompt.turn = "включить";
        localization.command.prompt.type = "тип";
        localization.command.prompt.category = "категория";
        localization.command.prompt.reason = "причина";
        localization.command.prompt.id = "айди";
        localization.command.prompt.time = "время";
        localization.command.prompt.repeatTime = "время повторения";
        localization.command.prompt.multipleVote = "мульти голосование";
        localization.command.prompt.player = "игрок";
        localization.command.prompt.number = "число";
        localization.command.prompt.color = "цвет";
        localization.command.prompt.language = "язык";
        localization.command.prompt.url = "ссылка";
        localization.command.prompt.move = "ход";
        localization.command.prompt.value = "значение";

        localization.command.reply.nullReceiver = "<color:#ff7171><b>⁉</b> Некому отвечать";

        localization.command.rockpaperscissors.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        localization.command.rockpaperscissors.nullGame = "<color:#ff7171><b>⁉</b> Этой игры не существует";
        localization.command.rockpaperscissors.wrongMove = "<color:#ff7171><b>⁉</b> Такой ход невозможен";
        localization.command.rockpaperscissors.already = "<color:#ff7171><b>⁉</b> Ты уже сходил";
        localization.command.rockpaperscissors.myself = "<color:#ff7171><b>⁉</b> Ты не можешь играть с самим собой";
        localization.command.rockpaperscissors.sender = "<fcolor:2>✂ <fcolor:1>Теперь ходит <display_name>";
        localization.command.rockpaperscissors.receiver = "<fcolor:2>✂ <display_name> <fcolor:1>предложил сыграть в камень-ножницы-бумага";
        localization.command.rockpaperscissors.formatMove = "<fcolor:2>✂ <fcolor:1>Выбери свой ход <fcolor:2><click:run_command:\"/rps <target> rock <uuid>\">[\uD83E\uDEA8 камень]</click> <click:run_command:\"/rps <target> scissors <uuid>\">[✂ ножницы]</click> <click:run_command:\"/rps <target> paper <uuid>\">[\uD83E\uDDFB бумага]</click>";
        localization.command.rockpaperscissors.formatWin = "<color:#98FB98>✂ Выиграл <display_name>! <b><sender_move></b> на <b><receiver_move></b>";
        localization.command.rockpaperscissors.formatDraw = "<color:#98FB98>✂ Ничья! Вы оба выбрали <b><move>";
        localization.command.rockpaperscissors.strategies = new LinkedHashMap<>() {{
            put("paper", "бумага");
            put("rock", "камень");
            put("scissors", "ножницы");
        }};

        localization.command.spy.formatTrue = "<fcolor:1>[👁] Ты <color:#98FB98>включил <fcolor:1>слежку";
        localization.command.spy.formatFalse = "<fcolor:1>[👁] Ты <color:#F08080>выключил <fcolor:1>слежку";
        localization.command.spy.formatLog = "<fcolor:1>[👁] <display_name> <color:#98FB98><action> <fcolor:1>→ <fcolor:2><message>";

        localization.command.stream.already = "<color:#ff7171><b>⁉</b> Ты уже включил трансляцию";
        localization.command.stream.not = "<color:#ff7171><b>⁉</b> Ты не включил трансляцию";
        localization.command.stream.prefixTrue = "<color:#ff4e4e>⏻</color:#ff4e4e> ";
        localization.command.stream.prefixFalse = "";
        localization.command.stream.urlTemplate = "<color:#ff4e4e>│ <fcolor:2><click:open_url:\"<url>\"><hover:show_text:\"<fcolor:2><url>\"><url></hover></click>";
        localization.command.stream.formatStart = "<br><color:#ff4e4e>│ 🔔 <fcolor:1>Объявление <br><color:#ff4e4e>│<br><color:#ff4e4e>│ <fcolor:1><display_name> начал трансляцию<br><color:#ff4e4e>│<br><urls><br>";
        localization.command.stream.formatEnd = "<fcolor:2>★ Спасибо за трансляцию на нашем сервере!";

        localization.command.symbol.format = "<click:suggest_command:\"<message>\"><fcolor:2>\uD83D\uDDA5 Нажми, чтобы использовать: <fcolor:1><message>";

        localization.command.tell.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        localization.command.tell.sender = "<fcolor:2>✉ <display_name> → <target> » <fcolor:1><message>";
        localization.command.tell.receiver = "<fcolor:2>✉ <display_name> → <target> » <fcolor:1><message>";
        localization.command.tell.myself = "<fcolor:2>✉ [Заметка] <fcolor:1><message>";

        localization.command.tictactoe.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        localization.command.tictactoe.myself = "<color:#ff7171><b>⁉</b> Ты не можешь играть с самим собой";
        localization.command.tictactoe.wrongGame = "<color:#ff7171><b>⁉</b> Этой игры не существует";
        localization.command.tictactoe.wrongMove = "<color:#ff7171><b>⁉</b> Такой ход невозможен";
        localization.command.tictactoe.wrongByPlayer = "<color:#ff7171><b>⁉</b> Игра закончена, потому что один из игроков не в сети";
        localization.command.tictactoe.symbol.blank = "<fcolor:1><hover:show_text:\"<fcolor:1>Ход <move>\"><click:run_command:\"/tictactoemove %d <move>\">☐</click></hover>";
        localization.command.tictactoe.symbol.first = "<fcolor:2>☑";
        localization.command.tictactoe.symbol.firstRemove = "<color:#ff7171>☑</color:#ff7171>";
        localization.command.tictactoe.symbol.firstWin = "<color:#98FB98>☑</color:#98FB98>";
        localization.command.tictactoe.symbol.second = "<fcolor:2>☒";
        localization.command.tictactoe.symbol.secondRemove = "<color:#ff7171>☒</color:#ff7171>";
        localization.command.tictactoe.symbol.secondWin = "<color:#98FB98>☒</color:#98FB98>";
        localization.command.tictactoe.field = "<fcolor:1><br>|[#][#][#]<fcolor:1>| <title> <current_move> <br><fcolor:1>|[#][#][#]<fcolor:1>| <br>|[#][#][#]<fcolor:1>| <last_move><br>";
        localization.command.tictactoe.currentMove = "<fcolor:2>☐ → <symbol>";
        localization.command.tictactoe.lastMove = "<fcolor:2>Последний ход (<move>)";
        localization.command.tictactoe.formatMove = "<fcolor:2>Ход <target> ";
        localization.command.tictactoe.formatWin = "<color:#98FB98><target> выиграл</color:#98FB98>";
        localization.command.tictactoe.formatDraw = "<color:#98FB98>Ничья \uD83D\uDC6C</color:#98FB98>";
        localization.command.tictactoe.sender = "<fcolor:1>☐ Предложение сыграть в крестики-нолики отправлено для <target>";
        localization.command.tictactoe.receiver = "<click:run_command:\"/tictactoemove %d create\"><fcolor:1>☐ Есть предложение сыграть в крестики-нолики от <display_name>, принять? [+]";

        localization.command.toponline.nullPage = "<color:#ff7171><b>⁉</b> Страница не найдена";
        localization.command.toponline.header = "<fcolor:2>▋ Игроков: <count> <br>";
        localization.command.toponline.line = "<fcolor:2><time_player> <fcolor:1>наиграл <fcolor:2><time>";
        localization.command.toponline.footer = "<br><fcolor:2>▋ <click:run_command:\"<command> <prev_page>\">←</click> <fcolor:1>Страница: <current_page>/<last_page> <fcolor:2><click:run_command:\"<command> <next_page>\">→";

        localization.command.translateto.nullOrError = "<color:#ff7171><b>⁉</b> Ошибка, возможно указан неправильный язык";
        localization.command.translateto.format = "<fcolor:1>📖 Перевод на [<language>] → <fcolor:2><message>";

        localization.command.Try.formatTrue = "<color:#98FB98>☺ <display_name> <message><reset> <color:#98FB98><percent>%";
        localization.command.Try.formatFalse = "<color:#F08080>☹ <display_name> <message><reset> <color:#F08080><percent>%";

        localization.command.unban.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        localization.command.unban.notBanned = "<color:#ff7171><b>⁉</b> Игрок не заблокирован";
        localization.command.unban.format = "<color:#98FB98>\uD83D\uDD13 <fcolor:2><moderator><color:#98FB98> разблокировал игрока <fcolor:2><player>";

        localization.command.unmute.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        localization.command.unmute.notMuted = "<color:#ff7171><b>⁉</b> Игрок не замучен";
        localization.command.unmute.format = "<color:#98FB98>\uD83D\uDD13 <fcolor:2><moderator><color:#98FB98> размутил игрока <fcolor:2><player>";

        localization.command.unwarn.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        localization.command.unwarn.notWarned = "<color:#ff7171><b>⁉</b> Игрок не имеет предупреждений";
        localization.command.unwarn.format = "<color:#98FB98>\uD83D\uDD13 <fcolor:2><moderator><color:#98FB98> снял предупреждение с игрока <fcolor:2><player>";

        localization.command.warn.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        localization.command.warn.nullTime = "<color:#ff7171><b>⁉</b> Невозможное время";
        localization.command.warn.reasons = new Localization.ReasonMap() {{
            put("default", "Ты получил предупреждение");
        }};
        localization.command.warn.server = "<color:#ff7171>🔒 <fcolor:2><moderator><fcolor:1> выдал предупреждение игроку <fcolor:2><player> <fcolor:1><hover:show_text:\"<fcolor:1>Айди: <id><br>Дата: <date><br>Время: <time><br>Осталось: <time_left><br>Модератор: <moderator><br>Причина: <reason>\">[ПОДРОБНЕЕ]</hover>";
        localization.command.warn.person = "<color:#ff7171>🔒 Ты получил предупреждение на <time>";

        localization.command.warnlist.empty = "<color:#98FB98>☺ Предупреждения не найдены";
        localization.command.warnlist.nullPage = "<color:#ff7171><b>⁉</b> Страница не найдена";
        localization.command.warnlist.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        localization.command.warnlist.global.header = "<fcolor:2>▋ Предупреждения: <count> <br>";
        localization.command.warnlist.global.line = "<hover:show_text:\"<fcolor:1>Снять предупреждение <display_name>\"><click:run_command:\"<command>\"><color:#ff7171>☒ <display_name></click></hover> <fcolor:1><hover:show_text:\"<fcolor:1>Айди: <id><br>Дата: <date><br>Время: <time><br>Модератор: <moderator><br>Причина: <reason>\">[ПОДРОБНЕЕ]</hover>";
        localization.command.warnlist.global.footer = "<br><fcolor:2>▋ <click:run_command:\"<command> <prev_page>\">←</click> <fcolor:1>Страница: <current_page>/<last_page> <fcolor:2><click:run_command:\"<command> <next_page>\">→";
        localization.command.warnlist.player.header = "<fcolor:2>▋ Все предупреждения: <count> <br>";
        localization.command.warnlist.player.line = "<hover:show_text:\"<fcolor:1>Снять предупреждение <display_name>\"><click:run_command:\"<command>\"><color:#ff7171>☒ <display_name></click></hover> <fcolor:1><hover:show_text:\"<fcolor:1>Айди: <id><br>Дата: <date><br>Время: <time><br>Модератор: <moderator><br>Причина: <reason>\">[ПОДРОБНЕЕ]</hover>";
        localization.command.warnlist.player.footer = "<br><fcolor:2>▋ <click:run_command:\"<command> <prev_page>\">←</click> <fcolor:1>Страница: <current_page>/<last_page> <fcolor:2><click:run_command:\"<command> <next_page>\">→";

        localization.integration.discord.forMinecraft = "<fcolor:2><name> <fcolor:1>» <fcolor:4><message>";
        localization.integration.discord.infoChannel = new LinkedHashMap<>() {{
            put("айди", "ТПС <tps>");
        }};
        localization.integration.discord.messageChannel = new LinkedHashMap<>() {{
            put("CHAT_GLOBAL", new Localization.Integration.Discord.ChannelEmbed());
        }};
        localization.integration.discord.messageChannel.get("CHAT_GLOBAL").content = "<final_message>";
        localization.integration.discord.messageChannel.get("CHAT_GLOBAL").webhook.enable = false;
        localization.integration.discord.messageChannel.get("CHAT_GLOBAL").webhook.avatar = "https://mc-heads.net/avatar/<skin>/32.png";
        localization.integration.discord.messageChannel.get("CHAT_GLOBAL").webhook.content = "";
        localization.integration.discord.messageChannel.get("CHAT_GLOBAL").embed.enable = false;
        localization.integration.discord.messageChannel.get("CHAT_GLOBAL").embed.color = "";
        localization.integration.discord.messageChannel.get("CHAT_GLOBAL").embed.title = "";
        localization.integration.discord.messageChannel.get("CHAT_GLOBAL").embed.url = "";
        localization.integration.discord.messageChannel.get("CHAT_GLOBAL").embed.author.name = "";
        localization.integration.discord.messageChannel.get("CHAT_GLOBAL").embed.author.url = "";
        localization.integration.discord.messageChannel.get("CHAT_GLOBAL").embed.author.iconUrl = "https://mc-heads.net/avatar/<skin>/16.png";
        localization.integration.discord.messageChannel.get("CHAT_GLOBAL").embed.description = "";
        localization.integration.discord.messageChannel.get("CHAT_GLOBAL").embed.thumbnail = "";
        localization.integration.discord.messageChannel.get("CHAT_GLOBAL").embed.fields = new ArrayList<>(List.of(new Localization.Integration.Discord.Embed.Field("", "", false)));
        localization.integration.discord.messageChannel.get("CHAT_GLOBAL").embed.image = "";
        localization.integration.discord.messageChannel.get("CHAT_GLOBAL").embed.timestamp = true;
        localization.integration.discord.messageChannel.get("CHAT_GLOBAL").embed.footer.text = "";
        localization.integration.discord.messageChannel.get("CHAT_GLOBAL").embed.footer.iconUrl = "https://mc-heads.net/avatar/<skin>/16.png";

        localization.integration.telegram.forMinecraft = "<fcolor:2><name> <fcolor:1>» <fcolor:4><message>";
        localization.integration.telegram.messageChannel = new LinkedHashMap<>() {{
            put("CHAT_GLOBAL", "<final_message>");
        }};

        localization.integration.twitch.forMinecraft = "<fcolor:2><name> <fcolor:1>» <fcolor:4><message>";
        localization.integration.twitch.messageChannel = new LinkedHashMap<>() {{
            put("CHAT_GLOBAL", "<final_message>");
        }};

        localization.message.advancement.formatTask = "<fcolor:1>🌠 <display_name> получил достижение «<advancement>»";
        localization.message.advancement.formatGoal = "<fcolor:1>🌠 <display_name> достиг цели «<advancement>»";
        localization.message.advancement.formatChallenge = "<fcolor:1>🌠 <display_name> завершил испытание «<advancement>»";
        localization.message.advancement.formatTaken = "<fcolor:1>🌠 <display_name> потерял достижение «<advancement>»";
        localization.message.advancement.tag.task = "<color:#4eff52>[<hover:show_text:\"<color:#4eff52><advancement>\"><advancement></hover>]";
        localization.message.advancement.tag.challenge = "<color:#834eff>[<hover:show_text:\"<color:#834eff><advancement>\"><advancement></hover>]";
        localization.message.advancement.revoke.manyToOne = "<fcolor:1>🌠 Отозвано <fcolor:2><advancements> <fcolor:1>достижений у игрока <target>";
        localization.message.advancement.revoke.oneToMany = "<fcolor:1>🌠 Достижение «<advancement>» отозвано у <fcolor:2><players> <fcolor:1>игрокам";
        localization.message.advancement.revoke.manyToMany = "<fcolor:1>🌠 Отозвано <fcolor:2><advancements> <fcolor:1>достижений у <fcolor:2><players> <fcolor:1>игроков";
        localization.message.advancement.revoke.oneToOne = "<fcolor:1>🌠 Отозвано достижение «<advancement>» у игрока <target>";
        localization.message.advancement.revoke.criterionToMany = "<fcolor:1>🌠 Отозвано условие «<fcolor:2><criterion><fcolor:1>» достижения <advancement> у <fcolor:2><players> <fcolor:1>игроков";
        localization.message.advancement.revoke.criterionToOne = "<fcolor:1>🌠 Отозвано условие «<fcolor:2><criterion><fcolor:1>» достижения <advancement> у игрока <target>";
        localization.message.advancement.grant.manyToOne = "<fcolor:1>🌠 Выдано <fcolor:2><advancements> <fcolor:1>достижений игроку <target>";
        localization.message.advancement.grant.oneToMany = "<fcolor:1>🌠 Достижение «<advancement>» выдано <fcolor:2><players> <fcolor:1>игрокам";
        localization.message.advancement.grant.manyToMany = "<fcolor:1>🌠 Выдано <fcolor:2><advancements> <fcolor:1>достижений <fcolor:2><players> <fcolor:1>игрокам";
        localization.message.advancement.grant.oneToOne = "<fcolor:1>🌠 Достижение «<advancement>» выдано игроку <target>";
        localization.message.advancement.grant.criterionToMany = "<fcolor:1>🌠 Условие «<fcolor:2><criterion><fcolor:1>» достижения <advancement> зачтено <fcolor:2><players> <fcolor:1>игрокам";
        localization.message.advancement.grant.criterionToOne = "<fcolor:1>🌠 Условие «<fcolor:2><criterion><fcolor:1>» достижения <advancement> зачтено игроку <target>";

        localization.message.afk.suffix = " <color:#FFFF00>⌚</color>";
        localization.message.afk.formatTrue.global = "<gradient:#ffd500:#FFFF00>⌚ <player> отошёл";
        localization.message.afk.formatTrue.local = "<gradient:#ffd500:#FFFF00>⌚ Ты отошёл от игры";
        localization.message.afk.formatFalse.global = "<gradient:#ffd500:#FFFF00>⌚ <player> вернулся";
        localization.message.afk.formatFalse.local = "<gradient:#ffd500:#FFFF00>⌚ Ты вернулся в игру";

        localization.message.attribute.baseValue.get = "<fcolor:1>❤ Базовое значение атрибута «<fcolor:2><lang:'<attribute>'><fcolor:1>» у сущности <target> равно <fcolor:2><value>";
        localization.message.attribute.baseValue.reset = "<fcolor:1>❤ Базовое значение атрибута «<fcolor:2><lang:'<attribute>'><fcolor:1>» у сущности <target> возвращено к <fcolor:2><value>";
        localization.message.attribute.baseValue.set = "<fcolor:1>❤ Базовое значение атрибута «<fcolor:2><lang:'<attribute>'><fcolor:1>» у сущности <target> изменено на <fcolor:2><value>";
        localization.message.attribute.modifier.add = "<fcolor:1>❤ Добавлен модификатор <fcolor:2><modifier> <fcolor:1>к атрибуту «<fcolor:2><lang:'<attribute>'><fcolor:1>» у сущности <target>";
        localization.message.attribute.modifier.remove = "<fcolor:1>❤ Удалён модификатор <fcolor:2><modifier> <fcolor:1>с атрибута «<fcolor:2><lang:'<attribute>'><fcolor:1>» у сущности <target>";
        localization.message.attribute.modifier.valueGet = "<fcolor:1>❤ Значение модификатора <fcolor:2><modifier> <fcolor:1>атрибута «<fcolor:2><lang:'<attribute>'><fcolor:1>» у сущности <target> равно <fcolor:2><value>";
        localization.message.attribute.valueGet = "<fcolor:1>❤ Значение атрибута «<fcolor:2><lang:'<attribute>'><fcolor:1>» у сущности <target> равно <fcolor:2><value>";

        localization.message.auto.types = new LinkedHashMap<>() {{
            put("announcement", new LinkedList<>() {{
                add("<br><fcolor:1>◇ Сервер использует <click:open_url:\"https://flectone.net/pulse/\"><hover:show_text:\"<fcolor:2>https://flectone.net/pulse/\"><fcolor:2>FlectonePulse</hover></click> :)<br>");
                add("<br><fcolor:1>      ❝ Заходи в дискорд ❠<br><fcolor:2>    <u><click:open_url:\"https://discord.flectone.net\"><hover:show_text:\"<fcolor:2>https://discord.flectone.net\">https://discord.flectone.net</hover></click></u><br>");
                add("<br><fcolor:1>⚡ Поддержи <fcolor:2>FlectonePulse <fcolor:1>на Boosty <br><fcolor:1>⚡ <u><click:open_url:\"https://boosty.to/thefaser/\"><hover:show_text:\"<fcolor:2>https://boosty.to/thefaser/\">https://boosty.to/thefaser/</hover></click></u><br>");
                add("<br><fcolor:1>   ✉ Заходи в телеграм ✉ <br><fcolor:2>    <u><click:open_url:\"https://t.me/flectone\"><hover:show_text:\"<fcolor:2>https://t.me/flectone\">https://t.me/flectone</hover></click></u><br>");
            }});
        }};

        localization.message.bed.noSleep = "<fcolor:1>\uD83D\uDECC Вы можете спать только ночью или во время грозы";
        localization.message.bed.notSafe = "<fcolor:1>\uD83D\uDECC Вы не можете уснуть, пока рядом есть монстры";
        localization.message.bed.obstructed = "<fcolor:1>\uD83D\uDECC Эта кровать заблокирована";
        localization.message.bed.occupied = "<fcolor:1>\uD83D\uDECC Эта кровать занята";
        localization.message.bed.tooFarAway = "<fcolor:1>\uD83D\uDECC Вы не можете уснуть, кровать слишком далеко";

        localization.message.brand.values = new LinkedList<>() {{
            add("<white>Майнкрафт");
            add("<aqua>Майнкрафт");
        }};

        localization.message.bubble.format = "<fcolor:3><message>";

        localization.message.chat.nullChat = "<color:#ff7171><b>⁉</b> На сервер выключен чат";
        localization.message.chat.nullReceiver = "<color:#ff7171><b>⁉</b> Тебя никто не услышал";
        localization.message.chat.types = new LinkedHashMap<>() {{
            put("global", "<delete><display_name> <world_prefix>»<fcolor:4> <message><reset><translate>");
            put("local", "<delete><display_name><fcolor:3>: <message><reset><translate>");
        }};

        localization.message.clear.single = "<fcolor:1>\uD83C\uDF0A Удалено <fcolor:2><items> <fcolor:1>предметов у игрока <target>";
        localization.message.clear.multiple = "<fcolor:1>\uD83C\uDF0A Удалено <fcolor:2><items> <fcolor:1>предметов у <fcolor:2><players> <fcolor:1>игроков";

        localization.message.clone.format = "<fcolor:1>⏹ Успешно скопировано <fcolor:2><blocks> <fcolor:1>блоков";

        localization.message.commandblock.notEnabled = "<fcolor:1>\uD83E\uDD16 На этом сервере командные блоки отключены";
        localization.message.commandblock.format = "<fcolor:1>\uD83E\uDD16 Команда задана: <fcolor:2><command>";

        localization.message.damage.format = "<fcolor:1>\uD83D\uDDE1 Нанесено <fcolor:2><amount> <fcolor:1>урона <target>";

        localization.message.death.types = new LinkedHashMap<>() {{
            put("death.attack.anvil", "<fcolor:1>☠ <target> раздавлен упавшей наковальней");
            put("death.attack.anvil.player", "<fcolor:1>☠ <target> был раздавлен упавшей наковальней, пока боролся с <killer>");
            put("death.attack.arrow", "<fcolor:1>☠ <target> застрелен <killer>");
            put("death.attack.arrow.item", "<fcolor:1>☠ <target> застрелен <killer> с помощью <fcolor:2>[<killer_item>]<fcolor:1>");
            put("death.attack.badRespawnPoint.message", "<fcolor:1>☠ <target> стал жертвой <fcolor:2>[<click:open_url:\"https://www.youtube.com/watch?v=dQw4w9WgXcQ\"><hover:show_text:\"<fcolor:2>MCPE-28723\">жестоких правил игры</hover></click>]");
            put("death.attack.cactus", "<fcolor:1>☠ <target> исколот до смерти");
            put("death.attack.cactus.player", "<fcolor:1>☠ <target> наткнулся на кактус, спасаясь от <killer>");
            put("death.attack.cramming", "<fcolor:1>☠ <target> расплющен в лепёшку");
            put("death.attack.cramming.player", "<fcolor:1>☠ <target> расплющен <killer>");
            put("death.attack.dragonBreath", "<fcolor:1>☠ <target> испепелён дыханием дракона");
            put("death.attack.dragonBreath.player", "<fcolor:1>☠ <target> сварился заживо в драконьем дыхании из-за <killer>");
            put("death.attack.drown", "<fcolor:1>☠ <target> утонул");
            put("death.attack.drown.player", "<fcolor:1>☠ <target> утонул, спасаясь от <killer>");
            put("death.attack.dryout", "<fcolor:1>☠ <target> умер от обезвоживания");
            put("death.attack.dryout.player", "<fcolor:1>☠ <target> умер от обезвоживания, спасаясь от <killer>");
            put("death.attack.even_more_magic", "<fcolor:1>☠ <target> был убит неизведанной магией");
            put("death.attack.explosion", "<fcolor:1>☠ <target> взорвался");
            put("death.attack.explosion.player", "<fcolor:1>☠ <target> был взорван <killer>");
            put("death.attack.explosion.item", "<fcolor:1>☠ <target> был взорван <killer> с помощью <fcolor:2>[<killer_item>]<fcolor:1>");
            put("death.attack.explosion.player.item", "<fcolor:1>☠ <target> был взорван <killer> с помощью <fcolor:2>[<killer_item>]<fcolor:1>");
            put("death.attack.fall", "<fcolor:1>☠ <target> разбился вдребезги");
            put("death.attack.fall.player", "<fcolor:1>☠ <target> разбился вдребезги, спасаясь от <killer>");
            put("death.attack.fallingBlock", "<fcolor:1>☠ <target> раздавлен упавшим блоком");
            put("death.attack.fallingBlock.player", "<fcolor:1>☠ <target> был раздавлен упавшим блоком, пока боролся с <killer>");
            put("death.attack.fallingStalactite", "<fcolor:1>☠ <target> был пронзён обрушившимся сталактитом");
            put("death.attack.fallingStalactite.player", "<fcolor:1>☠ <target> был пронзён обрушившимся сталактитом, пока боролся с <killer>");
            put("death.attack.fireball", "<fcolor:1>☠ <target> убит файерболом <killer>");
            put("death.attack.fireball.item", "<fcolor:1>☠ <target> убит файерболом <killer> с помощью <fcolor:2>[<killer_item>]<fcolor:1>");
            put("death.attack.fireworks", "<fcolor:1>☠ <target> с треском разлетелся");
            put("death.attack.fireworks.item", "<fcolor:1>☠ <target> с треском разлетелся из-за фейерверка <killer>, выпущенного из <fcolor:2>[<killer_item>]<fcolor:1>");
            put("death.attack.fireworks.player", "<fcolor:1>☠ <target> с треском разлетелся, пока боролся с <killer>");
            put("death.attack.flyIntoWall", "<fcolor:1>☠ <target> преобразовал кинетическую энергию во внутреннюю");
            put("death.attack.flyIntoWall.player", "<fcolor:1>☠ <target> преобразовал кинетическую энергию во внутреннюю, спасаясь от <killer>");
            put("death.attack.freeze", "<fcolor:1>☠ <target> замёрз насмерть");
            put("death.attack.freeze.player", "<fcolor:1>☠ <target> замёрз насмерть благодаря <killer>");
            put("death.attack.generic", "<fcolor:1>☠ <target> умер");
            put("death.attack.generic.player", "<fcolor:1>☠ <target> умер из-за <killer>");
            put("death.attack.genericKill", "<fcolor:1>☠ <target> убит");
            put("death.attack.genericKill.player", "<fcolor:1>☠ <target> был убит, сражаясь с <killer>");
            put("death.attack.hotFloor", "<fcolor:1>☠ <target> обнаружил, что пол — это лава");
            put("death.attack.hotFloor.player", "<fcolor:1>☠ <target> зашёл в опасную зону из-за <killer>");
            put("death.attack.inFire", "<fcolor:1>☠ <target> умер в огне");
            put("death.attack.inFire.player", "<fcolor:1>☠ <target> сгорел в огне, пока боролся с <killer>");
            put("death.attack.inWall", "<fcolor:1>☠ <target> погребён заживо");
            put("death.attack.inWall.player", "<fcolor:1>☠ <target> был погребён заживо, пока боролся с <killer>");
            put("death.attack.indirectMagic", "<fcolor:1>☠ <target> был убит <killer> с помощью магии");
            put("death.attack.indirectMagic.item", "<fcolor:1>☠ <target> был убит <killer> с помощью <fcolor:2>[<killer_item>]<fcolor:1>");
            put("death.attack.lava", "<fcolor:1>☠ <target> решил поплавать в лаве");
            put("death.attack.lava.player", "<fcolor:1>☠ <target> упал в лаву, убегая от <killer>");
            put("death.attack.lightningBolt", "<fcolor:1>☠ <target> был поражён молнией");
            put("death.attack.lightningBolt.player", "<fcolor:1>☠ <target> был поражён молнией, пока боролся с <killer>");
            put("death.attack.mace_smash", "<fcolor:1>☠ <target> был сокрушён <killer>");
            put("death.attack.mace_smash.item", "<fcolor:1>☠ <target> был сокрушён <killer> с помощью <fcolor:2>[<killer_item>]<fcolor:1>");
            put("death.attack.magic", "<fcolor:1>☠ <target> был убит магией");
            put("death.attack.magic.player", "<fcolor:1>☠ <target> был убит магией, убегая от <killer>");
            put("death.attack.mob", "<fcolor:1>☠ <target> был убит <killer>");
            put("death.attack.mob.item", "<fcolor:1>☠ <target> был убит <killer> с помощью <fcolor:2>[<killer_item>]<fcolor:1>");
            put("death.attack.onFire", "<fcolor:1>☠ <target> сгорел заживо");
            put("death.attack.onFire.item", "<fcolor:1>☠ <target> был сожжён дотла, пока боролся с <killer>, держащим <fcolor:2>[<killer_item>]<fcolor:1>");
            put("death.attack.onFire.player", "<fcolor:1>☠ <target> был сожжён дотла, пока боролся с <killer>");
            put("death.attack.outOfWorld", "<fcolor:1>☠ <target> выпал из мира");
            put("death.attack.outOfWorld.player", "<fcolor:1>☠ <target> не захотел жить в том же мире, что и <killer>");
            put("death.attack.outsideBorder", "<fcolor:1>☠ <target> покинул пределы этого мира");
            put("death.attack.outsideBorder.player", "<fcolor:1>☠ <target> покинул пределы этого мира, пока боролся с <killer>");
            put("death.attack.player", "<fcolor:1>☠ <target> был убит <killer>");
            put("death.attack.player.item", "<fcolor:1>☠ <target> был убит <killer> с помощью <fcolor:2>[<killer_item>]<fcolor:1>");
            put("death.attack.sonic_boom", "<fcolor:1>☠ <target> был уничтожен звуковым зарядом");
            put("death.attack.sonic_boom.item", "<fcolor:1>☠ <target> был уничтожен звуковым зарядом, спасаясь от <killer>, держащего <fcolor:2>[<killer_item>]<fcolor:1>");
            put("death.attack.sonic_boom.player", "<fcolor:1>☠ <target> был уничтожен звуковым зарядом, спасаясь от <killer>");
            put("death.attack.stalagmite", "<fcolor:1>☠ <target> пронзён сталагмитом");
            put("death.attack.stalagmite.player", "<fcolor:1>☠ <target> был пронзён сталагмитом, пока боролся с <killer>");
            put("death.attack.starve", "<fcolor:1>☠ <target> умер от голода");
            put("death.attack.starve.player", "<fcolor:1>☠ <target> умер от голода, пока боролся с <killer>");
            put("death.attack.sting", "<fcolor:1>☠ <target> изжален до смерти");
            put("death.attack.sting.item", "<fcolor:1>☠ <target> был изжален до смерти <killer> с помощью <fcolor:2>[<killer_item>]<fcolor:1>");
            put("death.attack.sting.player", "<fcolor:1>☠ <target> изжален до смерти <killer>");
            put("death.attack.sweetBerryBush", "<fcolor:1>☠ <target> искололся до смерти в кустах сладких ягод");
            put("death.attack.sweetBerryBush.player", "<fcolor:1>☠ <target> искололся до смерти в кустах сладких ягод, спасаясь от <killer>");
            put("death.attack.thorns", "<fcolor:1>☠ <target> был убит, пытаясь навредить <killer>");
            put("death.attack.thorns.item", "<fcolor:1>☠ <target> был убит <fcolor:2>[<killer_item>]<fcolor:1>, пытаясь навредить <killer>");
            put("death.attack.thrown", "<fcolor:1>☠ <target> был избит <killer>");
            put("death.attack.thrown.item", "<fcolor:1>☠ <target> был избит <killer> с помощью <fcolor:2>[<killer_item>]<fcolor:1>");
            put("death.attack.trident", "<fcolor:1>☠ <target> был пронзён <killer>");
            put("death.attack.trident.item", "<fcolor:1>☠ <target> пронзён <killer> с помощью <fcolor:2>[<killer_item>]<fcolor:1>");
            put("death.attack.wither", "<fcolor:1>☠ <target> иссушён");
            put("death.attack.wither.player", "<fcolor:1>☠ <target> был иссушён, пока боролся с <killer>");
            put("death.attack.witherSkull", "<fcolor:1>☠ <target> был поражён черепом из <killer>");
            put("death.attack.witherSkull.item", "<fcolor:1>☠ <target> был поражён черепом из <killer> с помощью <fcolor:2>[<killer_item>]<fcolor:1>");
            put("death.fell.accident.generic", "<fcolor:1>☠ <target> разбился насмерть");
            put("death.fell.accident.ladder", "<fcolor:1>☠ <target> свалился с лестницы");
            put("death.fell.accident.other_climbable", "<fcolor:1>☠ <target> сорвался");
            put("death.fell.accident.scaffolding", "<fcolor:1>☠ <target> сорвался с подмосток");
            put("death.fell.accident.twisting_vines", "<fcolor:1>☠ <target> сорвался с вьющейся лозы");
            put("death.fell.accident.vines", "<fcolor:1>☠ <target> сорвался с лианы");
            put("death.fell.accident.weeping_vines", "<fcolor:1>☠ <target> сорвался с плакучей лозы");
            put("death.fell.assist", "<fcolor:1>☠ <target> свалился благодаря <killer>");
            put("death.fell.assist.item", "<fcolor:1>☠ <target> был обречён на падение <killer> с помощью <fcolor:2>[<killer_item>]<fcolor:1>");
            put("death.fell.finish", "<fcolor:1>☠ <target> упал с высоты и был добит <killer>");
            put("death.fell.finish.item", "<fcolor:1>☠ <target> упал с высоты и был добит <killer> с помощью <fcolor:2>[<killer_item>]<fcolor:1>");
            put("death.fell.killer", "<fcolor:1>☠ <target> был обречён на падение");
        }};

        localization.message.debugstick.empty = "<fcolor:1>\uD83D\uDD27 Свойства объекта <fcolor:2><property> <fcolor:1>не заданы";
        localization.message.debugstick.select = "<fcolor:1>\uD83D\uDD27 выбрано «<fcolor:2><property><fcolor:1>» (<fcolor:2><value><fcolor:1>)";
        localization.message.debugstick.update = "<fcolor:1>\uD83D\uDD27 «<fcolor:2><property><fcolor:1>»: <fcolor:2><value>";

        localization.message.deop.format = "<fcolor:1>\uD83E\uDD16 <target> больше не является оператором сервера";

        localization.message.dialog.clear.single = "<fcolor:1>\uD83D\uDDD4 Убран диалог у игрока «<target>»";
        localization.message.dialog.clear.multiple = "<fcolor:1>\uD83D\uDDD4 Убран диалог у <fcolor:2><players> <fcolor:1>игроков";
        localization.message.dialog.show.single = "<fcolor:1>\uD83D\uDDD4 Отображён диалог игроку «<target>»";
        localization.message.dialog.show.multiple = "<fcolor:1>\uD83D\uDDD4 Отображён диалог <fcolor:2><players> <fcolor:1>игрокам";

        localization.message.difficulty.query = "<fcolor:1>⚔ Сложность игры: <fcolor:2><lang:'<difficulty>'>";
        localization.message.difficulty.success = "<fcolor:1>⚔ Установлена сложность игры: <fcolor:2><lang:'<difficulty>'>";

        localization.message.effect.clear.everything.single = "<fcolor:1>⚗ Убраны все эффекты с <target>";
        localization.message.effect.clear.everything.multiple = "<fcolor:1>⚗ Убраны все эффекты с <fcolor:2><players> <fcolor:1>целей";
        localization.message.effect.clear.specific.single = "<fcolor:1>⚗ Убран эффект «<fcolor:2><lang:'<effect>'><fcolor:1>» с <target>";
        localization.message.effect.clear.specific.multiple = "<fcolor:1>⚗ Убран эффект «<fcolor:2><lang:'<effect>'><fcolor:1>» с <fcolor:2><players> <fcolor:1>целей";
        localization.message.effect.give.single = "<fcolor:1>⚗ Применён эффект «<fcolor:2><lang:'<effect>'><fcolor:1>» к <target>";
        localization.message.effect.give.multiple = "<fcolor:1>⚗ Применён эффект <fcolor:2><lang:'<effect>'><fcolor:1>» к <fcolor:2><players> <fcolor:1>целям";

        localization.message.enchant.single = "<fcolor:1>\uD83D\uDCD6 Наложены чары «<fcolor:2><enchantment><fcolor:1>» на предмет <target>";
        localization.message.enchant.multiple = "<fcolor:1>\uD83D\uDCD6 Наложены чары «<fcolor:2><enchantment><fcolor:1>» на предмет <fcolor:2><players><fcolor:1> сущностей";

        localization.message.execute.pass = "<fcolor:1>⚡ Условие выполнено";
        localization.message.execute.passCount = "<fcolor:1>⚡ Условие выполнено; счётчик: <fcolor:2><count>";

        localization.message.experience.add.levels.single = "<fcolor:1>⏺ Выдано <fcolor:2><amount> <fcolor:1>уровней игроку <target>";
        localization.message.experience.add.levels.multiple = "<fcolor:1>⏺ Выдано <fcolor:2><amount> <fcolor:1>уровней <fcolor:2><players> <fcolor:1>игрокам";
        localization.message.experience.add.points.single = "<fcolor:1>⏺ Выдано <fcolor:2><amount> <fcolor:1>единиц опыта игроку <target>";
        localization.message.experience.add.points.multiple = "<fcolor:1>⏺ Выдано <fcolor:2><amount> <fcolor:1>единиц опыта <fcolor:2><players> <fcolor:1>игрокам";
        localization.message.experience.query.levels = "<fcolor:1>⏺ <target> имеет <fcolor:2><amount> <fcolor:1>уровней";
        localization.message.experience.query.points = "<fcolor:1>⏺ <target> имеет <fcolor:2><amount> <fcolor:1>ед. опыта";
        localization.message.experience.set.levels.single = "<fcolor:1>⏺ Установлено <fcolor:2><amount> <fcolor:1>уровней игроку <target>";
        localization.message.experience.set.levels.multiple = "<fcolor:1>⏺ Установлено <fcolor:2><amount> <fcolor:1>уровней <fcolor:2><players> <fcolor:1>игрокам";
        localization.message.experience.set.points.single = "<fcolor:1>⏺ Установлено <fcolor:2><amount> <fcolor:1>единиц опыта игроку <target>";
        localization.message.experience.set.points.multiple = "<fcolor:1>⏺ Установлено <fcolor:2><amount> <fcolor:1>единиц опыта <fcolor:2><players> <fcolor:1>игрокам";

        localization.message.fill.format = "<fcolor:1>⏹ Успешно заполнено <fcolor:2><blocks> <fcolor:1>блоков";

        localization.message.fillbiome.format = "<fcolor:1>⏹ Заменены биомы между точками <fcolor:2><x1><fcolor:1>, <fcolor:2><y1><fcolor:1>, <fcolor:2><z1> <fcolor:1>и <fcolor:2><x2><fcolor:1>, <fcolor:2><y2><fcolor:1>, <fcolor:2><z2>";
        localization.message.fillbiome.formatCount = "<fcolor:1>⏹ Биом был заменён у <fcolor:2><blocks> <fcolor:1>блоков между точками <fcolor:2><x1><fcolor:1>, <fcolor:2><y1><fcolor:1>, <fcolor:2><z1> <fcolor:1>и <fcolor:2><x2><fcolor:1>, <fcolor:2><y2><fcolor:1>, <fcolor:2><z2>";

        localization.message.format.replacement.spoilerSymbol = "█";
        localization.message.format.replacement.values = new LinkedHashMap<>() {{
            put("smile", "<click:suggest_command:\":)\"><hover:show_text:\":)\">☺</hover></click>");
            put("big_smile", "<click:suggest_command:\":D\"><hover:show_text:\":D\">☻</hover></click>");
            put("sad", "<click:suggest_command:\":(\"><hover:show_text:\":(\">☹</hover></click>");
            put("ok_hand", "<click:suggest_command:\":ok:\"><hover:show_text:\":ok:\">\uD83D\uDD92</hover></click>");
            put("thumbs_up", "<click:suggest_command:\":+1:\"><hover:show_text:\":+1:\">\uD83D\uDD92</hover></click>");
            put("thumbs_down", "<click:suggest_command:\":-1:\"><hover:show_text:\":-1:\">\uD83D\uDD93</hover></click>");
            put("cool_smile", "<click:suggest_command:\":cool:\"><hover:show_text:\":cool:\">\uD83D\uDE0E</hover></click>");
            put("cool_glasses", "<click:suggest_command:\"B)\"><hover:show_text:\"B)\">\uD83D\uDE0E</hover></click>");
            put("clown", "<click:suggest_command:\":clown:\"><hover:show_text:\":clown:\">\uD83E\uDD21</hover></click>");
            put("heart", "<click:suggest_command:\"<3\"><hover:show_text:\"<3\">❤</hover></click>");
            put("laughing", "<click:suggest_command:\"XD\"><hover:show_text:\"XD\">\uD83D\uDE06</hover></click>");
            put("confused", "<click:suggest_command:\"%)\"><hover:show_text:\"%)\">\uD83D\uDE35</hover></click>");
            put("happy", "<click:suggest_command:\"=D\"><hover:show_text:\"=D\">\uD83D\uDE03</hover></click>");
            put("angry", "<click:suggest_command:\">:(\"><hover:show_text:\">:(\">\uD83D\uDE21</hover></click>");
            put("ascii_idk", "<click:suggest_command:\":idk:\"><hover:show_text:\":idk:\">¯\\_(ツ)_/¯</hover></click>");
            put("ascii_angry", "<click:suggest_command:\":angry:\"><hover:show_text:\":angry:\">(╯°□°)╯︵ ┻━┻</hover></click>");
            put("ascii_happy", "<click:suggest_command:\":happy:\"><hover:show_text:\":happy:\">＼(＾O＾)／</hover></click>");
            put("ping", "<fcolor:2><ping>");
            put("tps", "<fcolor:2><tps>");
            put("online", "<fcolor:2><online>");
            put("coords", "<fcolor:2><x> <y> <z>");
            put("stats", "<color:#ff7171><hp>♥</color> <color:#3de0d8><armor>🛡 <color:#e33059><attack>🗡 <color:#4eff52><exp>⏺ <color:#f0a01f><food>🍖");
            put("skin", "<click:open_url:\"<message_1>\"><hover:show_text:\"<fcolor:2><pixels>\"><fcolor:2><u>👨 Skin</u></hover></click>");
            put("item", "<fcolor:2>[<message_1>]");
            put("url", "<click:open_url:\"<message_1>\"><hover:show_text:\"<fcolor:2>Open url <br><u><message_1>\"><fcolor:2><u>🗗 Url</u></hover></click>");
            put("image", "<click:open_url:\"<message_1>\"><hover:show_text:\"<fcolor:2><pixels>\"><fcolor:2><u>🖃 Image</u></hover></click>");
            put("spoiler", "<hover:show_text:\"<fcolor:2><message_1>\"><fcolor:2><symbols></hover>");
            put("bold", "<b><message_1></b>");
            put("italic", "<i><message_1></i>");
            put("underline", "<u><message_1></u>");
            put("obfuscated", "<obf><message_1></obf>");
            put("strikethrough", "<st><message_1></st>");
        }};

        localization.message.format.mention.person = "<fcolor:2>Тебя упомянули!";
        localization.message.format.mention.format = "<fcolor:2>@<target>";

        localization.message.format.moderation.delete.placeholder = "<color:#ff7171><hover:show_text:\"<color:#ff7171>Нажми, чтобы удалить\"><click:run_command:\"/deletemessage <uuid>\">[x] ";
        localization.message.format.moderation.delete.format = "<fcolor:3><i>Сообщение удалено</i>";

        localization.message.format.moderation.newbie.reason = "Ты ещё слишком новичок";

        localization.message.format.moderation.swear.symbol = "❤";

        localization.message.format.name_.constant = "";
        localization.message.format.name_.display = "<click:suggest_command:\"/msg <player> \"><hover:show_text:\"<fcolor:2>Написать <player>\"><vault_prefix><stream_prefix><fcolor:2><player><afk_suffix><vault_suffix></hover></click>";
        localization.message.format.name_.entity = "<fcolor:2><hover:show_text:\"<fcolor:2><name> <br><fcolor:1>Тип <fcolor:2><lang:'<type>'> <br><fcolor:1>Айди <fcolor:2><uuid>\"><name></hover>";
        localization.message.format.name_.unknown = "<fcolor:2><name>";
        localization.message.format.name_.invisible = "<fcolor:2>\uD83D\uDC7B Невидимка";

        localization.message.format.questionAnswer.questions = new LinkedHashMap<>() {{
            put("server", "<fcolor:2>[Вопрос-Ответ] @<player><fcolor:1>, это ванильный сервер в Майнкрафте!");
            put("flectone", "<fcolor:2>[Вопрос-Ответ] @<player><fcolor:1>, это бренд и проекты созданные TheFaser'ом");
        }};

        localization.message.format.translate.action = " <click:run_command:\"/translateto <language> <language> <message>\"><hover:show_text:\"<fcolor:2>Перевести сообщение\"><fcolor:1>⇄";

        localization.message.gamemode.setDefault = "<fcolor:1>\uD83D\uDDD8 Новый режим игры по умолчанию: <fcolor:2><lang:'<gamemode>'>";
        localization.message.gamemode.self = "<fcolor:1>\uD83D\uDDD8 Твой режим игры изменён на <fcolor:2><lang:'<gamemode>'>";
        localization.message.gamemode.other = "<fcolor:1>\uD83D\uDDD8 Режим игры игрока <target> изменён на <fcolor:2><lang:'<gamemode>'>";

        localization.message.gamerule.formatQuery = "<fcolor:1>\uD83D\uDDD0 Значение игрового правила <fcolor:2><gamerule><fcolor:1>: <fcolor:2><value>";
        localization.message.gamerule.formatSet = "<fcolor:1>\uD83D\uDDD0 Установлено значение игрового правила <fcolor:2><gamerule><fcolor:1>: <fcolor:2><value>";

        localization.message.give.single = "<fcolor:1>⛏ Выдано <fcolor:2><items> <fcolor:1>[<fcolor:2><hover:show_text:\"<fcolor:2><give_item>\"><give_item></hover><fcolor:1>] игроку <target>";
        localization.message.give.multiple = "<fcolor:1>⛏ Выдано <fcolor:2><items> <fcolor:1>[<fcolor:2><hover:show_text:\"<fcolor:2><give_item>\"><give_item></hover><fcolor:1>] <fcolor:2><players> <fcolor:1>игрокам";

        localization.message.greeting.format = "<br>[#][#][#][#][#][#][#][#]<br>[#][#][#][#][#][#][#][#]<br>[#][#][#][#][#][#][#][#]<br>[#][#][#][#][#][#][#][#]<br>[#][#][#][#][#][#][#][#]  <fcolor:1>Привет,<br>[#][#][#][#][#][#][#][#]  <player><br>[#][#][#][#][#][#][#][#]<br>[#][#][#][#][#][#][#][#]<br>";

        localization.message.join.format = "<color:#4eff52>→ <display_name>";
        localization.message.join.formatFirstTime = "<color:#4eff52>→ <display_name> <fcolor:1>впервые тут!";

        localization.message.kill.single = "<fcolor:1>☠ <fcolor:1><target> был убит";
        localization.message.kill.multiple = "<fcolor:1>☠ <fcolor:1>Уничтожено <fcolor:2><entities> <fcolor:1>сущностей";

        localization.message.locate.biome = "<fcolor:1>\uD83D\uDDFA Ближайший биом с типом <fcolor:2><value> <fcolor:1>находится по координатам <fcolor:2><hover:show_text:\"<fcolor:2>Нажми для телепортации\"><click:suggest_command:\"/tp @s <x> <y> <z>\">[<x>, <y>, <z>]</click></hover> <fcolor:1>(в <fcolor:2><blocks> <fcolor:1>блоках отсюда)";
        localization.message.locate.poi = "<fcolor:1>\uD83D\uDDFA Ближайшая точка интереса с типом <fcolor:2><value> <fcolor:1>находится по координатам <fcolor:2><fcolor:2><hover:show_text:\"<fcolor:2>Нажми для телепортации\"><click:suggest_command:\"/tp @s <x> <y> <z>\">[<x>, <y>, <z>]</click></hover> <fcolor:1>(в <fcolor:2><blocks> <fcolor:1>блоках отсюда)";
        localization.message.locate.structure = "<fcolor:1>\uD83D\uDDFA Ближайшее строение типа <fcolor:2><value> <fcolor:1>находится по координатам <fcolor:2><fcolor:2><hover:show_text:\"<fcolor:2>Нажми для телепортации\"><click:suggest_command:\"/tp @s <x> <y> <z>\">[<x>, <y>, <z>]</click></hover> <fcolor:1>(в <fcolor:2><blocks> <fcolor:1>блоках отсюда)";

        localization.message.objective.belowname.format = "<fcolor:1>мс";

        localization.message.op.format = "<fcolor:1>\uD83E\uDD16 <target> назначен оператором сервера";

        localization.message.particle.format = "<fcolor:1>❄ Отображена частица «<fcolor:2><particle><fcolor:1>»";

        localization.message.quit.format = "<color:#ff4e4e>← <display_name>";

        localization.message.recipe.give.single = "<fcolor:1>\uD83D\uDCA1 Выдано <fcolor:2><recipes> <fcolor:1>рецептов игроку <target>";
        localization.message.recipe.give.multiple = "<fcolor:1>\uD83D\uDCA1 Выдан(о) <fcolor:2><recipes> <fcolor:1>рецепт(ов) <fcolor:2><players> <fcolor:1>игрокам";
        localization.message.recipe.take.single = "<fcolor:1>\uD83D\uDCA1 Изъято <fcolor:2><recipes> <fcolor:1>рецептов у игрока <target>";
        localization.message.recipe.take.multiple = "<fcolor:1>\uD83D\uDCA1 Изъято <fcolor:2><recipes> <fcolor:1>рецептов у <fcolor:2><players> <fcolor:1>игроков";

        localization.message.reload.format = "<fcolor:1>\uD83D\uDEC8 Перезагрузка!";

        localization.message.ride.dismount = "<fcolor:1>\uD83C\uDFC7 <target> больше не сидит на <second_target>";
        localization.message.ride.mount = "<fcolor:1>\uD83C\uDFC7 <target> теперь сидит на <second_target>";

        localization.message.rightclick.format = "<fcolor:1>◁ <display_name> ▷";

        localization.message.rotate.format = "<fcolor:1>\uD83E\uDD38 <target> повёрнут";

        localization.message.save.disabled = "<fcolor:1>\uD83D\uDEC8 Автосохранение отключено";
        localization.message.save.enabled = "<fcolor:1>\uD83D\uDEC8 Автосохранение включено";
        localization.message.save.saving = "<fcolor:1>\uD83D\uDEC8 Сохранение мира (может занять некоторое время!)";
        localization.message.save.success = "<fcolor:1>\uD83D\uDEC8 Игра сохранена";

        localization.message.seed.format = "<fcolor:1>\uD83D\uDD11 Ключ генератора: [<fcolor:2><hover:show_text:'<fcolor:2>Нажми, чтобы скопировать в буфер обмена'><click:copy_to_clipboard:<seed>><seed></click></hover><fcolor:1>]";

        localization.message.setblock.format = "<fcolor:1>⏹ Изменён блок в точке <fcolor:2><x><fcolor:1>, <fcolor:2><y><fcolor:1>, <fcolor:2><z><fcolor:1>";

        localization.message.sidebar.values = new LinkedList<>() {{
            add(new LinkedList<>() {{
                add(" ");
                add("<fcolor:1>Пинг <ping>");
                add(" ");
                add("<fcolor:1>FlectonePulse");
            }});
            add(new LinkedList<>() {{
                add(" ");
                add("<fcolor:2>ТПС <tps>");
                add(" ");
                add("<fcolor:2>FlectonePulse");
            }});
        }};

        localization.message.sleep.notPossible = "<fcolor:1>\uD83D\uDECC Никакой отдых не поможет пропустить эту ночь";
        localization.message.sleep.playersSleeping = "<fcolor:1>\uD83D\uDECC <fcolor:2><players_sleeping> <fcolor:1>из <fcolor:2><players> <fcolor:1>игроков спят";
        localization.message.sleep.skippingNight = "<fcolor:1>\uD83D\uDECC Вы проспите всю ночь";

        localization.message.sound.play.multiple =  "<fcolor:1>\uD83D\uDD0A Воспроизведён звук «<fcolor:2><sound><fcolor:1>» <fcolor:2><players> <fcolor:1>игрокам";
        localization.message.sound.play.single = "<fcolor:1>\uD83D\uDD0A Воспроизведён звук «<fcolor:2><sound><fcolor:1>» игроку <target>";
        localization.message.sound.stop.sourceAny = "<fcolor:1>\uD83D\uDD07 Отключены все звуки от источника «<fcolor:2><source><fcolor:1>»";
        localization.message.sound.stop.sourceSound = "<fcolor:1>\uD83D\uDD07 Отключён звук «<fcolor:2><sound><fcolor:1>» от источника «<fcolor:2><source><fcolor:1>»";
        localization.message.sound.stop.sourcelessAny = "<fcolor:1>\uD83D\uDD07 Отключены все звуки";
        localization.message.sound.stop.sourcelessSound = "<fcolor:1>\uD83D\uDD07 Отключён звук «<fcolor:2><sound><fcolor:1>»";

        localization.message.spawn.notValid = "<fcolor:1>\uD83D\uDECC У вас нет кровати или заряженного якоря возрождения, либо доступ к ним затруднён";
        localization.message.spawn.set = "<fcolor:1>\uD83D\uDECC Точка возрождения установлена";
        localization.message.spawn.setWorld = "<fcolor:1>\uD83D\uDECC Установлена точка возрождения мира <fcolor:2><x><fcolor:1>, <fcolor:2><y><fcolor:1>, <fcolor:2><z><fcolor:1> [<fcolor:2><angle><fcolor:1>]";
        localization.message.spawn.single = "<fcolor:1>\uD83D\uDECC Установлена точка возрождения <fcolor:2><x><fcolor:1>, <fcolor:2><y><fcolor:1>, <fcolor:2><z><fcolor:1> [<fcolor:2><angle><fcolor:1>] в <fcolor:2><world> <fcolor:1>для <target>";
        localization.message.spawn.multiple = "<fcolor:1>\uD83D\uDECC Установлена точка возрождения <fcolor:2><x><fcolor:1>, <fcolor:2><y><fcolor:1>, <fcolor:2><z><fcolor:1> [<fcolor:2><angle><fcolor:1>] в <fcolor:2><world> <fcolor:1>для <fcolor:2><players><fcolor:1> игроков";

        localization.message.status.motd.values = new LinkedList<>() {{
            add("<fcolor:1>Добро пожаловать на сервер!");
            add("<fcolor:1>Присоединяйся и наслаждайся уникальным опытом игры!");
            add("<fcolor:1>У нас дружелюбное сообщество - будь вежлив и уважай других!");
            add("<fcolor:1>Приятной игры! Если есть вопросы, обращайся к администрации");
        }};
        localization.message.status.players.full = "<color:#ff7171>Сервер полон";
        localization.message.status.players.samples = new LinkedList<>(List.of(new Localization.Message.Status.Players.Sample()));
        localization.message.status.version.name = "Майнкрафт сервер";

        localization.message.stop.format = "<fcolor:1>\uD83D\uDEC8 Выключение сервера";

        localization.message.summon.format = "<fcolor:1>\uD83D\uDC3A Сущность «<target>» создана";

        localization.message.tab.footer.lists = new LinkedList<>() {{
            add(new LinkedList<>() {{
                add(" ");
                add("<fcolor:1>Привет <fcolor:2><player><fcolor:1>!");
                add(" ");
            }});
            add(new LinkedList<>() {{
                add(" ");
                add("<fcolor:1>ТПС <tps>, Онлайн <online>");
                add(" ");
            }});
        }};
        localization.message.tab.header.lists = new LinkedList<>() {{
            add(new LinkedList<>() {{
                add(" ");
                add("<fcolor:1>❤");
                add(" ");
            }});
            add(new LinkedList<>() {{
                add(" ");
                add("<fcolor:1>\uD83D\uDC7E");
                add(" ");
            }});
        }};
        localization.message.tab.playerlistname.format = "<world_prefix>▋ <reset><vault_prefix><stream_prefix><fcolor:2><player><afk_suffix><vault_suffix>";

        localization.message.teleport.entity.single = "<fcolor:1>\uD83C\uDF00 <target> телепортирован к <second_target>";
        localization.message.teleport.entity.multiple = "<fcolor:1>\uD83C\uDF00 <fcolor:2><entities> <fcolor:1>сущностей телепортированы к <second_target>";
        localization.message.teleport.location.single = "<fcolor:1>\uD83C\uDF00 <target> телепортирован в точку <fcolor:2><x><fcolor:1>, <fcolor:2><y><fcolor:1>, <fcolor:2><z><fcolor:1>";
        localization.message.teleport.location.multiple = "<fcolor:1>\uD83C\uDF00 <fcolor:2><entities> <fcolor:1>сущностей телепортированы в точку <fcolor:2><x><fcolor:1>, <fcolor:2><y><fcolor:1>, <fcolor:2><z><fcolor:1>";

        localization.message.time.query = "<fcolor:1>☽ Время: <fcolor:2><time>";
        localization.message.time.set = "<fcolor:1>☽ Установлено время: <fcolor:2><time>";

        localization.message.update.formatPlayer = "<fcolor:1><fcolor:2>(FlectonePulse) <fcolor:1>Твоя версия <fcolor:2><current_version> <fcolor:1>устарела! Обновись до <fcolor:2><latest_version> <fcolor:1>с помощью <url:https://modrinth.com/plugin/flectonepulse>, чтобы получить новые возможности!";
        localization.message.update.formatConsole = "<fcolor:1>Твоя версия <fcolor:2><current_version> <fcolor:1>устарела! Обновись до <fcolor:2><latest_version> <fcolor:1>с помощью <click:open_url:https://modrinth.com/plugin/flectonepulse>https://modrinth.com/plugin/flectonepulse";

        localization.message.weather.formatClear = "<fcolor:1>☀ Установлена <fcolor:2>ясная <fcolor:1>погода";
        localization.message.weather.formatRain = "<fcolor:1>\uD83C\uDF27 Установлена <fcolor:2>дождливая <fcolor:1>погода";
        localization.message.weather.formatThunder = "<fcolor:1>⛈ Установлена <fcolor:2>грозовая <fcolor:1>погода";

        localization.message.worldborder.center =  "<fcolor:1>\uD83D\uDEAB Установлен центр границ мира: <fcolor:2><value><fcolor:1>, <fcolor:2><second_value>";
        localization.message.worldborder.damage.amount = "<fcolor:1>\uD83D\uDEAB Установлено значение урона, ежесекундно наносимого за границами мира: <fcolor:2><value> <fcolor:1>за блок";
        localization.message.worldborder.damage.buffer = "<fcolor:1>\uD83D\uDEAB Установлен предел нанесения урона за границами мира: <fcolor:2><value> <fcolor:1>блоков";
        localization.message.worldborder.get = "<fcolor:1>\uD83D\uDEAB Ширина границы мира: <fcolor:2><value> <fcolor:1>блоков";
        localization.message.worldborder.set.grow = "<fcolor:1>\uD83D\uDEAB Через <fcolor:2><second_value> <fcolor:1>секунд ширина границы мира увеличится до <fcolor:2><value> <fcolor:1>блоков";
        localization.message.worldborder.set.immediate = "<fcolor:1>\uD83D\uDEAB Установлена граница мира шириной <fcolor:2><value> <fcolor:1>блоков";
        localization.message.worldborder.set.shrink = "<fcolor:1>\uD83D\uDEAB Через <fcolor:2><second_value> <fcolor:1>секунд ширина границы мира уменьшится до <fcolor:2><value> <fcolor:1>блоков";
        localization.message.worldborder.warning.distance = "<fcolor:1>\uD83D\uDEAB Установлено расстояние предупреждения о пересечении границы мира: <fcolor:2><value> <fcolor:1>блоков";
        localization.message.worldborder.warning.time = "<fcolor:1>\uD83D\uDEAB Установлено время предупреждения о столкновении с границей мира: <fcolor:2><value> <fcolor:1>секунд";
    }

}
