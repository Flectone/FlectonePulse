package net.flectone.pulse.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;
import net.elytrium.serializer.annotations.Transient;
import net.flectone.pulse.util.constant.MessageType;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@SuppressWarnings({"FieldMayBeFinal", "unused", "WriteOnlyObject"})
@Comment(
        value = {
                @CommentValue("  ___       ___  __  ___  __        ___ "),
                @CommentValue(" |__  |    |__  /  `  |  /  \\ |\\ | |__"),
                @CommentValue(" |    |___ |___ \\__,  |  \\__/ | \\| |___"),
                @CommentValue("  __             __   ___ "),
                @CommentValue(" |__) |  | |    /__` |__  "),
                @CommentValue(" |    \\__/ |___ .__/ |___   /\\"),
                @CommentValue("                           /  \\"),
                @CommentValue(" __/\\___  ____/\\_____  ___/    \\______"),
                @CommentValue("        \\/           \\/  "),
                @CommentValue(" "),
        },
        at = Comment.At.PREPEND
)
@Getter
public final class Localization extends FileSerializable implements ModuleConfig {

    public static final String FOLDER_NAME = "localizations";

    @Transient
    private final String language;

    public Localization(Path projectPath, String language) {
        super(Paths.get(projectPath.toString(), FOLDER_NAME, language + ".yml"));

        this.language = language;

        if (language.equalsIgnoreCase("ru_ru")) {
            initRU_RU();
        }
    }

    private void initRU_RU() {
        cooldown = "<color:#ff7171><b>⁉</b> Слишком быстро, попробуй через <time>";

        time.format = "dd'д' HH'ч' mm'м' ss.SSS'с'";
        time.permanent = "НАВСЕГДА";
        time.zero = "0с";

        command.anon.format = "<fcolor:1>\uD83D\uDC7B <fcolor:2>Анон <fcolor:1><message>";

        command.dice.format = "<fcolor:1>✎ <display_name> кинул кубики <message><reset> <fcolor:1>(<sum>)";

        command.ball.format = "<color:#9370DB>❓ <display_name> спросил: <message><reset> <color:#9370DB><br>🔮 Магический шар: <u><answer></u>";
        command.ball.answers.clear();
        command.ball.answers.addAll(List.of(
                "Бесспорно",
                "Никаких сомнений",
                "Определённо да",
                "Это база",
                "Можешь быть уверен в этом",
                "Вероятнее всего",
                "Хорошие перспективы",
                "Да",
                "Пока не ясно, попробуй снова",
                "Спроси позже",
                "Лучше не рассказывать",
                "Сейчас нельзя предсказать",
                "Сконцентрируйся и спроси опять",
                "Даже не думай",
                "Нет.",
                "Перспективы не очень хорошие",
                "Весьма сомнительно")
        );

        command.online.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        command.online.formatCurrent = "<fcolor:1>⌛ <display_name> сейчас на сервере";
        command.online.formatFirst = "<fcolor:1>⌛ <display_name> впервые зашёл на сервер <time> назад";
        command.online.formatLast = "<fcolor:1>⌛ <display_name> <fcolor:1>последний раз был на сервере <time> назад";
        command.online.formatTotal = "<fcolor:1>⌛ <display_name> <fcolor:1>всего провёл на сервере <time>";

        command.ping.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        command.ping.format = "<fcolor:1>🖧 Пинг игрока <display_name> равен <ping>";

        command.coin.head = "орёл";
        command.coin.tail = "решка";
        command.coin.format = "<fcolor:1>✎ <display_name> подбросил монетку - <result>";
        command.coin.formatDraw = "<fcolor:1>✎ <display_name> неудачно подбросил монетку ребром :)";

        command.deletemessage.nullMessage = "<color:#ff7171><b>⁉</b> Сообщение не существует";
        command.deletemessage.format = "<color:#98FB98>☒ Сообщение успешно удалено";

        command.translateto.nullOrError = "<color:#ff7171><b>⁉</b> Ошибка, возможно указан неправильный язык";
        command.translateto.format = "<fcolor:1>📖 Перевод на [<language>] → <fcolor:2><message>";

        command.clearchat.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        command.clearchat.format = "<fcolor:1>\uD83D\uDCAC Чат очищен";

        command.geolocate.nullOrError = "<color:#ff7171><b>⁉</b> Ошибка, попробуй чуть позже";
        command.geolocate.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        command.geolocate.format = "<fcolor:1>Геолокация <display_name><br>Страна: <fcolor:2><country><br><fcolor:1>Регион: <fcolor:2><region_name><br><fcolor:1>Город: <fcolor:2><city><br><fcolor:1>Часовой пояс: <fcolor:2><timezone><br><fcolor:1>Мобильный интернет? <fcolor:2><mobile><br><fcolor:1>ВПН? <fcolor:2><proxy><br><fcolor:1>Хостинг? <fcolor:2><hosting><br><fcolor:1>Айпи: <fcolor:2><query>";

        command.broadcast.format = "<br><color:#ffd500>│ \uD83D\uDD6B Сообщение для всех <br>│<br>│ Автор <display_name> <br>│<br>│ <fcolor:1><message> <br>";

        command.spy.formatLog = "<fcolor:1>[👁] <display_name> <color:#98FB98><action> <fcolor:1>→ <fcolor:2><message>";
        command.spy.formatTrue = "<fcolor:1>[👁] Ты <color:#98FB98>включил <fcolor:1>слежку";
        command.spy.formatFalse = "<fcolor:1>[👁] Ты <color:#F08080>выключил <fcolor:1>слежку";

        command.stream.not = "<color:#ff7171><b>⁉</b> Ты не включил трансляцию";
        command.stream.formatEnd = "<fcolor:2>★ Спасибо за трансляцию на нашем сервере!";
        command.stream.already = "<color:#ff7171><b>⁉</b> Ты уже включил трансляцию";
        command.stream.formatStart = "<br><color:#ff4e4e>│ 🔔 <fcolor:1>Объявление <br><color:#ff4e4e>│<br><color:#ff4e4e>│ <fcolor:1><display_name> начал трансляцию<br><color:#ff4e4e>│<br><urls><br>";

        command.kick.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        command.kick.reasons.clear();
        command.kick.reasons.put("default", "Исключён модератором");
        command.kick.server = "<color:#ff7171>🔒 <fcolor:2><moderator><fcolor:1> исключил <fcolor:2><player><fcolor:1> <fcolor:1><hover:show_text:\"<fcolor:1>Айди: <id><br>Дата: <date><br>Модератор: <moderator><br>Причина: <reason>\">[ПОДРОБНЕЕ]</hover>";
        command.kick.person = "<color:#ff7171>🔒 КИК 🔒 <fcolor:1><br><br>Айди: <id><br><br>Дата: <date><br><br>Модератор: <moderator><br><br>Причина: <reason>";

        command.helper.nullHelper = "<color:#ff7171><b>⁉</b> Сейчас нет людей, кто бы смог помочь";
        command.helper.global = "<fcolor:2>👤 <display_name> просит помощи ⏩ <fcolor:1><message>";
        command.helper.player = "<fcolor:2>👤 Запрос отправлен, ожидай ответа";

        command.tell.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        command.tell.sender = "<fcolor:2>✉ <display_name> → <target> » <fcolor:1><message>";
        command.tell.receiver = "<fcolor:2>✉ <display_name> → <target> » <fcolor:1><message>";
        command.tell.myself = "<fcolor:2>✉ [Заметка] <fcolor:1><message>";

        command.reply.nullReceiver = "<color:#ff7171><b>⁉</b> Некому отвечать";

        command.poll.expired = "<color:#ff7171><b>⁉</b> Голосование завершено";
        command.poll.already = "<color:#ff7171><b>⁉</b> Ты уже проголосовал в этом голосовании";
        command.poll.nullPoll = "<color:#ff7171><b>⁉</b> Голосование не найдено";
        command.poll.voteTrue = "<color:#4eff52>👍 Ты выбрал <answer_id> вариант в голосовании #<id>. Всего таких голосов <count>";
        command.poll.voteFalse = "<color:#ff4e4e>\uD83D\uDD93 Ты передумал об <answer_id> варианте в голосовании #<id>. Всего таких голосов <count> без тебя";
        command.poll.format = "<br><color:#fce303>│ <status> <br>│ <message><reset> <color:#fce303><br>├─────────────<br><answers>";
        command.poll.status.start = "Создано новое голосование #<b><id></b>";
        command.poll.status.run = "Идёт голосование #<b><id></b>";
        command.poll.status.end = "Голосование #<b><id></b> завершено";
        command.poll.modern.header = "Создание голосования";
        command.poll.modern.inputName = "Название";
        command.poll.modern.inputInitial = "";
        command.poll.modern.multipleName = "Разрешить несколько ответов";
        command.poll.modern.endTimeName = "Длительность (в минутах)";
        command.poll.modern.repeatTimeName = "Интервал (в минутах)";
        command.poll.modern.newAnswerButtonName = "Добавить ответ";
        command.poll.modern.removeAnswerButtonName = "Удалить ответ";
        command.poll.modern.inputAnswerName = "Ответ <number>";
        command.poll.modern.inputAnswersInitial = "";
        command.poll.modern.createButtonName = "Создать голосование";
        command.poll.answerTemplate = "<color:#fce303>│ <count> → <color:#4eff52><hover:show_text:\"<color:#4eff52>Проголосовать за <bold><answer>\"><click:run_command:\"/pollvote <id> <number>\"><answer> [👍]<br>";

        command.ignore.myself = "<color:#ff7171><b>⁉</b> Нельзя игнорировать самого себя";
        command.ignore.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        command.ignore.you = "<color:#ff7171><b>⁉</b> Ты его игнорируешь";
        command.ignore.he = "<color:#ff7171><b>⁉</b> Он тебя игнорирует";
        command.ignore.formatTrue = "<color:#ff7171>☹ Ты игнорируешь <display_name>";
        command.ignore.formatFalse = "<color:#98FB98>☺ Ты перестал игнорировать <display_name>";

        command.ignorelist.empty = "<color:#98FB98>☺ Игнорируемых игроков нет";
        command.ignorelist.nullPage = "<color:#ff7171><b>⁉</b> Страница не найдена";
        command.ignorelist.header = "<fcolor:2>▋ Игнорирования: <count> <br>";
        command.ignorelist.line = "<hover:show_text:\"<fcolor:1>Перестать игнорировать <display_name>\"><click:run_command:\"<command>\"><color:#ff7171>☒ <display_name></click></hover> <fcolor:1>Дата: <date>";
        command.ignorelist.footer = "<br>▋ <fcolor:2><click:run_command:\"<command> <prev_page>\">←</click> <fcolor:1>Страница: <current_page>/<last_page> <fcolor:2><click:run_command:\"<command> <next_page>\">→";

        command.ban.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        command.ban.nullTime = "<color:#ff7171><b>⁉</b> Невозможное время";
        command.ban.reasons.clear();
        command.ban.reasons.put("default", "Ты заблокирован на этом сервере");
        command.ban.server = "<color:#ff7171>🔒 <fcolor:2><moderator><fcolor:1> заблокировал игрока <fcolor:2><player> <fcolor:1><hover:show_text:\"<fcolor:1>Айди: <id><br>Дата: <date><br>Время: <time><br>Осталось: <time_left><br>Модератор: <moderator><br>Причина: <reason>\">[ПОДРОБНЕЕ]</hover>";
        command.ban.person = "<color:#ff7171>🔒 БАН 🔒 <fcolor:1><br><br>Дата: <date><br><br>Время: <time><br><br>Осталось: <time_left><br><br>Модератор: <moderator><br><br>Причина: <reason>";
        command.ban.connectionAttempt = "<color:#ff7171>🔒 Заблокированный <fcolor:2><player><fcolor:1> попытался подключиться <fcolor:1><hover:show_text:\"<fcolor:1>Айди: <id><br>Дата: <date><br>Время: <time><br>Осталось: <time_left><br>Модератор: <moderator><br>Причина: <reason>\">[ПОДРОБНЕЕ]</hover>";

        command.unban.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        command.unban.notBanned = "<color:#ff7171><b>⁉</b> Игрок не заблокирован";
        command.unban.format = "<color:#98FB98>\uD83D\uDD13 <fcolor:2><moderator><color:#98FB98> разблокировал игрока <fcolor:2><player>";

        command.banlist.empty = "<color:#98FB98>☺ Блокировки не найдены";
        command.banlist.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        command.banlist.nullPage = "<color:#ff7171><b>⁉</b> Страница не найдена";
        command.banlist.global.header = "<fcolor:2>▋ Блокировки: <count> <br>";
        command.banlist.global.line = "<hover:show_text:\"<fcolor:1>Разблокировать <display_name>\"><click:run_command:\"<command>\"><color:#ff7171>☒ <display_name></click></hover> <fcolor:1><hover:show_text:\"<fcolor:1>Айди: <id><br>Дата: <date><br>Время: <time><br>Модератор: <moderator><br>Причина: <reason>\">[ПОДРОБНЕЕ]</hover>";
        command.banlist.global.footer = "<br><fcolor:2>▋ <fcolor:2><click:run_command:\"<command> <prev_page>\">←</click> <fcolor:1>Page: <current_page>/<last_page> <fcolor:2><click:run_command:\"<command> <next_page>\">→";
        command.banlist.player.header = "<fcolor:2>▋ Все блокировки: <count> <br>";
        command.banlist.player.line = "<hover:show_text:\"<fcolor:1>Разблокировать <display_name>\"><click:run_command:\"<command>\"><color:#ff7171>☒ <display_name></click></hover> <fcolor:1><hover:show_text:\"<fcolor:1>Айди: <id><br>Дата: <date><br>Время: <time><br>Модератор: <moderator><br>Причина: <reason>\">[ПОДРОБНЕЕ]</hover>";
        command.banlist.player.footer = "<br><fcolor:2>▋ <click:run_command:\"<command> <prev_page>\">←</click> <fcolor:1>Page: <current_page>/<last_page> <fcolor:2><click:run_command:\"<command> <next_page>\">→";

        command.mute.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        command.mute.nullTime = "<color:#ff7171><b>⁉</b> Невозможное время";
        command.mute.reasons.clear();
        command.mute.reasons.put("default", "Ты был замучен на сервере");
        command.mute.server = "<color:#ff7171>🔒 <fcolor:2><moderator><fcolor:1> выдал мут игроку <fcolor:2><player> <fcolor:1><hover:show_text:\"<fcolor:1>Айди: <id><br>Дата: <date><br>Время: <time><br>Осталось: <time_left><br>Модератор: <moderator><br>Причина: <reason>\">[ПОДРОБНЕЕ]</hover>";
        command.mute.person = "<color:#ff7171>🔒 Ты замучен, осталось <time_left>";

        command.unmute.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        command.unmute.notMuted = "<color:#ff7171><b>⁉</b> Игрок не замучен";
        command.unmute.format = "<color:#98FB98>\uD83D\uDD13 <fcolor:2><moderator><color:#98FB98> размутил игрока <fcolor:2><player>";

        command.mutelist.empty = "<color:#98FB98>☺ Муты не найдены";
        command.mutelist.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        command.mutelist.nullPage = "<color:#ff7171><b>⁉</b> Страница не найдена";
        command.mutelist.global.header = "<fcolor:2>▋ Муты: <count> <br>";
        command.mutelist.global.line = "<hover:show_text:\"<fcolor:1>Размутить <display_name>\"><click:run_command:\"<command>\"><color:#ff7171>☒ <display_name></click></hover> <fcolor:1><hover:show_text:\"<fcolor:1>Айди: <id><br>Дата: <date><br>Время: <time><br>Модератор: <moderator><br>Причина: <reason>\">[ПОДРОБНЕЕ]</hover>";
        command.mutelist.global.footer = "<br><fcolor:2>▋ <fcolor:2><click:run_command:\"<command> <prev_page>\">←</click> <fcolor:1>Страница: <current_page>/<last_page> <fcolor:2><click:run_command:\"<command> <next_page>\">→";
        command.mutelist.player.header = "<fcolor:2>▋ Все муты: <count> <br>";
        command.mutelist.player.line = "<hover:show_text:\"<fcolor:1>Размутить <display_name>\"><click:run_command:\"<command>\"><color:#ff7171>☒ <display_name></click></hover> <fcolor:1><hover:show_text:\"<fcolor:1>Айди: <id><br>Дата: <date><br>Время: <time><br>Модератор: <moderator><br>Причина: <reason>\">[ПОДРОБНЕЕ]</hover>";
        command.mutelist.player.footer = "<br><fcolor:2>▋ <click:run_command:\"<command> <prev_page>\">←</click> <fcolor:1>Страница: <current_page>/<last_page> <fcolor:2><click:run_command:\"<command> <next_page>\">→";

        command.warn.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        command.warn.nullTime = "<color:#ff7171><b>⁉</b> Невозможное время";
        command.warn.reasons.clear();
        command.warn.reasons.put("default", "Ты получил предупреждение");
        command.warn.server = "<color:#ff7171>🔒 <fcolor:2><moderator><fcolor:1> выдал предупреждение игроку <fcolor:2><player> <fcolor:1><hover:show_text:\"<fcolor:1>Айди: <id><br>Дата: <date><br>Время: <time><br>Осталось: <time_left><br>Модератор: <moderator><br>Причина: <reason>\">[ПОДРОБНЕЕ]</hover>";
        command.warn.person = "<color:#ff7171>🔒 Ты получил предупреждение на <time>";

        command.unwarn.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        command.unwarn.notWarned = "<color:#ff7171><b>⁉</b> Игрок не имеет предупреждений";
        command.unwarn.format = "<color:#98FB98>\uD83D\uDD13 <fcolor:2><moderator><color:#98FB98> снял предупреждение с игрока <fcolor:2><player>";

        command.warnlist.empty = "<color:#98FB98>☺ Предупреждения не найдены";
        command.warnlist.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        command.warnlist.nullPage = "<color:#ff7171><b>⁉</b> Страница не найдена";
        command.warnlist.global.header = "<fcolor:2>▋ Предупреждения: <count> <br>";
        command.warnlist.global.line = "<hover:show_text:\"<fcolor:1>Снять предупреждение <display_name>\"><click:run_command:\"<command>\"><color:#ff7171>☒ <display_name></click></hover> <fcolor:1><hover:show_text:\"<fcolor:1>Айди: <id><br>Дата: <date><br>Время: <time><br>Модератор: <moderator><br>Причина: <reason>\">[ПОДРОБНЕЕ]</hover>";
        command.warnlist.global.footer = "<br><fcolor:2>▋ <click:run_command:\"<command> <prev_page>\">←</click> <fcolor:1>Страница: <current_page>/<last_page> <fcolor:2><click:run_command:\"<command> <next_page>\">→";
        command.warnlist.player.header = "<fcolor:2>▋ Все предупреждения: <count> <br>";
        command.warnlist.player.line = "<hover:show_text:\"<fcolor:1>Снять предупреждение <display_name>\"><click:run_command:\"<command>\"><color:#ff7171>☒ <display_name></click></hover> <fcolor:1><hover:show_text:\"<fcolor:1>Айди: <id><br>Дата: <date><br>Время: <time><br>Модератор: <moderator><br>Причина: <reason>\">[ПОДРОБНЕЕ]</hover>";
        command.warnlist.player.footer = "<br><fcolor:2>▋ <click:run_command:\"<command> <prev_page>\">←</click> <fcolor:1>Страница: <current_page>/<last_page> <fcolor:2><click:run_command:\"<command> <next_page>\">→";

        command.flectonepulse.nullHostEditor = "<color:#ff7171><b>⁉</b> Параметр host должен быть настроен в <u>config.yml";
        command.flectonepulse.formatFalse = "<color:#ff7171>★ Возникла проблема при перезагрузке <br>Ошибка: <message>";
        command.flectonepulse.formatTrue = "<fcolor:2>★ <u>FlectonePulse</u> успешно перезагружен! (<i><time></i>)";
        command.flectonepulse.formatWebStarting =  "<fcolor:2>★ Запуск веб-сервера, подождите...";
        command.flectonepulse.formatEditor = "<fcolor:2>★ Ссылка для веб-редактирования <u><fcolor:2><click:open_url:\"<url>\"><hover:show_text:\"<fcolor:2><url>\"><url>";

        command.chatcolor.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        command.chatcolor.format = "<br><color:#98FB98>│ Твои цвета выглядят так: <br><color:#98FB98>│ <fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><color:#98FB98>│ <fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир! <br>";
        command.chatcolor.nullType = "<color:#ff7171><b>⁉</b> Тип введён неверно";
        command.chatcolor.nullColor = "<color:#ff7171><b>⁉</b> Цвета введены неверно";

        command.chatsetting.noPermission = "<color:#ff7171><b>⁉</b> Нет разрешения на изменение этой настройки";
        command.chatsetting.disabledSelf = "<color:#ff7171><b>⁉</b> Эта функция отключена с помощью /chatsetting";
        command.chatsetting.disabledOther = "<color:#ff7171><b>⁉</b> Он отключил эту функцию с помощью /chatsetting";
        command.chatsetting.inventory = "Настройки чата";
        command.chatsetting.checkbox.enabledHover = "<color:#98FB98>Отображение включено";
        command.chatsetting.checkbox.disabledHover = "<color:#ff7171>Отображение выключено";
        command.chatsetting.checkbox.types.clear();
        command.chatsetting.checkbox.types.put(MessageType.AFK.name(), "<status_color>Афк");
        command.chatsetting.checkbox.types.put(MessageType.ADVANCEMENT.name(), "<status_color>Достижения");
        command.chatsetting.checkbox.types.put(MessageType.CHAT.name(), "<status_color>Сообщения чата");
        command.chatsetting.checkbox.types.put(MessageType.COMMAND_ANON.name(), "<status_color>Команда /anon");
        command.chatsetting.checkbox.types.put(MessageType.COMMAND_BALL.name(), "<status_color>Команда /ball");
        command.chatsetting.checkbox.types.put(MessageType.COMMAND_BROADCAST.name(), "<status_color>Команда /broadcast");
        command.chatsetting.checkbox.types.put(MessageType.COMMAND_COIN.name(), "<status_color>Команда /coin");
        command.chatsetting.checkbox.types.put(MessageType.COMMAND_DICE.name(), "<status_color>Команда /dice");
        command.chatsetting.checkbox.types.put(MessageType.COMMAND_DO.name(), "<status_color>Команда /do");
        command.chatsetting.checkbox.types.put(MessageType.COMMAND_MAIL.name(), "<status_color>Команда /mail");
        command.chatsetting.checkbox.types.put(MessageType.COMMAND_ME.name(), "<status_color>Команда /me");
        command.chatsetting.checkbox.types.put(MessageType.COMMAND_POLL.name(), "<status_color>Команда /poll");
        command.chatsetting.checkbox.types.put(MessageType.COMMAND_ROCKPAPERSCISSORS.name(), "<status_color>Команда /rockpaperscissors");
        command.chatsetting.checkbox.types.put(MessageType.COMMAND_STREAM.name(), "<status_color>Команда /stream");
        command.chatsetting.checkbox.types.put(MessageType.COMMAND_TELL.name(), "<status_color>Команда /tell");
        command.chatsetting.checkbox.types.put(MessageType.COMMAND_TICTACTOE.name(), "<status_color>Команда /tictactoe");
        command.chatsetting.checkbox.types.put(MessageType.COMMAND_TRY.name(), "<status_color>Команда /try");
        command.chatsetting.checkbox.types.put(MessageType.DEATH.name(), "<status_color>Смерти");
        command.chatsetting.checkbox.types.put(MessageType.FROM_DISCORD_TO_MINECRAFT.name(), "<status_color>Сообщения из Discord");
        command.chatsetting.checkbox.types.put(MessageType.FROM_TELEGRAM_TO_MINECRAFT.name(), "<status_color>Сообщения из Telegram");
        command.chatsetting.checkbox.types.put(MessageType.FROM_TWITCH_TO_MINECRAFT.name(), "<status_color>Сообщения из Twitch");
        command.chatsetting.checkbox.types.put(MessageType.JOIN.name(), "<status_color>Вход на сервер");
        command.chatsetting.checkbox.types.put(MessageType.QUIT.name(), "<status_color>Выход с сервера");
        command.chatsetting.checkbox.types.put(MessageType.SLEEP.name(), "<status_color>Сон");
        command.chatsetting.menu.chat.item = "<fcolor:2>Тип чата <br><fcolor:1>Чат для просмотра и отправки сообщений <br><br><fcolor:1>Выбранный чат <fcolor:2><chat>";
        command.chatsetting.menu.chat.inventory = "Чаты";
        command.chatsetting.menu.chat.types.clear();
        command.chatsetting.menu.chat.types.put("default", "<fcolor:2>Стандартный чат<br><fcolor:1>Ты можешь видеть <fcolor:2>все <fcolor:1>чаты и писать в любой чат");
        command.chatsetting.menu.chat.types.put("local", "<fcolor:2>Локальный чат<br><fcolor:1>Ты можешь писать в <fcolor:2>любой <fcolor:1>чат");
        command.chatsetting.menu.chat.types.put("global", "<fcolor:2>Глобальный чат<br><fcolor:1>Ты можешь писать только в <fcolor:2>глобальный <fcolor:1>чат");
        command.chatsetting.menu.see.item = "<fcolor:2>Цвета \"see\" <br><fcolor:1>Цвета для /chatcolor see <br><br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир! <br><br><u><color:#ff7171>Это видишь только ТЫ в любых сообщений";
        command.chatsetting.menu.see.inventory = "Цвета";
        command.chatsetting.menu.see.types.clear();
        command.chatsetting.menu.see.types.put("default", "<gradient:#70C7EF:#37B1F2>Стандартные цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
        command.chatsetting.menu.see.types.put("white", "<gradient:#D4E4FF:#B8D2FF>Белые цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
        command.chatsetting.menu.see.types.put("light_gray", "<gradient:#B5B9BD:#9DA2A6>Светло-серые цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
        command.chatsetting.menu.see.types.put("gray", "<gradient:#4A5054:#3A3F42>Серые цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
        command.chatsetting.menu.see.types.put("black", "<gradient:#17191A:#0D0E0F>Черные цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
        command.chatsetting.menu.see.types.put("brown", "<gradient:#7A5A40:#634A34>Коричневые цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
        command.chatsetting.menu.see.types.put("red", "<gradient:#D63E3E:#C12B2B>Красные цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
        command.chatsetting.menu.see.types.put("orange", "<gradient:#FF8C00:#E67E00>Оранжевые цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
        command.chatsetting.menu.see.types.put("yellow", "<gradient:#FFE83D:#FFD900>Желтые цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
        command.chatsetting.menu.see.types.put("lime", "<gradient:#8EE53F:#7ACC29>Лаймовые цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
        command.chatsetting.menu.see.types.put("green", "<gradient:#4BB54B:#3AA33A>Зеленые цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
        command.chatsetting.menu.see.types.put("cyan", "<gradient:#3ECFDF:#2AB7C9>Бирюзовые цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
        command.chatsetting.menu.see.types.put("light_blue", "<gradient:#6BB6FF:#4DA6FF>Голубые цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
        command.chatsetting.menu.see.types.put("blue", "<gradient:#3A75FF:#1F5AFF>Синие цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
        command.chatsetting.menu.see.types.put("purple", "<gradient:#A368C7:#8A4DBF>Фиолетовые цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
        command.chatsetting.menu.see.types.put("magenta", "<gradient:#FF5CD9:#FF3DCF>Пурпурные цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
        command.chatsetting.menu.see.types.put("pink", "<gradient:#FF80B7:#FF66A6>Розовые цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
        command.chatsetting.menu.out.item = "<fcolor:2>Цвета \"out\" <br><fcolor:1>Цвета для /chatcolor out <br><br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир! <br><br><u><color:#ff7171>Это видят ВСЕ у твоих сообщений";
        command.chatsetting.menu.out.inventory = "Цвета";
        command.chatsetting.menu.out.types.clear();
        command.chatsetting.menu.out.types.put("default", "<gradient:#70C7EF:#37B1F2>Стандартные цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
        command.chatsetting.menu.out.types.put("white", "<gradient:#D4E4FF:#B8D2FF>Белые цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
        command.chatsetting.menu.out.types.put("light_gray", "<gradient:#B5B9BD:#9DA2A6>Светло-серые цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
        command.chatsetting.menu.out.types.put("gray", "<gradient:#4A5054:#3A3F42>Серые цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
        command.chatsetting.menu.out.types.put("black", "<gradient:#17191A:#0D0E0F>Черные цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
        command.chatsetting.menu.out.types.put("brown", "<gradient:#7A5A40:#634A34>Коричневые цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
        command.chatsetting.menu.out.types.put("red", "<gradient:#D63E3E:#C12B2B>Красные цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
        command.chatsetting.menu.out.types.put("orange", "<gradient:#FF8C00:#E67E00>Оранжевые цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
        command.chatsetting.menu.out.types.put("yellow", "<gradient:#FFE83D:#FFD900>Желтые цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
        command.chatsetting.menu.out.types.put("lime", "<gradient:#8EE53F:#7ACC29>Лаймовые цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
        command.chatsetting.menu.out.types.put("green", "<gradient:#4BB54B:#3AA33A>Зеленые цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
        command.chatsetting.menu.out.types.put("cyan", "<gradient:#3ECFDF:#2AB7C9>Бирюзовые цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
        command.chatsetting.menu.out.types.put("light_blue", "<gradient:#6BB6FF:#4DA6FF>Голубые цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
        command.chatsetting.menu.out.types.put("blue", "<gradient:#3A75FF:#1F5AFF>Синие цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
        command.chatsetting.menu.out.types.put("purple", "<gradient:#A368C7:#8A4DBF>Фиолетовые цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
        command.chatsetting.menu.out.types.put("magenta", "<gradient:#FF5CD9:#FF3DCF>Пурпурные цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");
        command.chatsetting.menu.out.types.put("pink", "<gradient:#FF80B7:#FF66A6>Розовые цвета<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: привет мир! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: привет мир!");

        command.symbol.format = "<click:suggest_command:\"<message>\"><fcolor:2>\uD83D\uDDA5 Нажми, чтобы использовать: <fcolor:1><message>";

        command.mail.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        command.mail.onlinePlayer = "<color:#ff7171><b>⁉</b> Игрок в сети";
        command.mail.sender = "<fcolor:2>✉ Письмо #<id> для <target> » <fcolor:1><message>";
        command.mail.receiver = "<fcolor:2>✉ Письмо от <display_name> » <fcolor:1><message>";

        command.clearmail.nullMail = "<color:#ff7171><b>⁉</b> Письма не найдено";
        command.clearmail.format = "<fcolor:2>✉ [УДАЛЕНО] Письмо #<id> для <target> » <fcolor:1><message>";

        command.tictactoe.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        command.tictactoe.myself = "<color:#ff7171><b>⁉</b> Ты не можешь играть с самим собой";
        command.tictactoe.sender = "<fcolor:1>☐ Предложение сыграть в крестики-нолики отправлено для <target>";
        command.tictactoe.receiver = "<click:run_command:\"/tictactoemove %d create\"><fcolor:1>☐ Есть предложение сыграть в крестики-нолики от <display_name>, принять? [+]";
        command.tictactoe.wrongGame = "<color:#ff7171><b>⁉</b> Этой игры не существует";
        command.tictactoe.wrongByPlayer = "<color:#ff7171><b>⁉</b> Игра закончена, потому что один из игроков не в сети";
        command.tictactoe.wrongMove = "<color:#ff7171><b>⁉</b> Такой ход невозможен";
        command.tictactoe.formatMove = "<fcolor:2>Ход <target> ";
        command.tictactoe.lastMove = "<fcolor:2>Последний ход (<move>)";
        command.tictactoe.formatWin = "<color:#98FB98><target> выиграл</color:#98FB98>";
        command.tictactoe.formatDraw = "<color:#98FB98>Ничья \uD83D\uDC6C</color:#98FB98>";
        command.tictactoe.symbol.blank = "<fcolor:1><hover:show_text:\"<fcolor:1>Ход <move>\"><click:run_command:\"/tictactoemove %d <move>\">☐</click></hover>";

        command.toponline.nullPage = "<color:#ff7171><b>⁉</b> Страница не найдена";
        command.toponline.header = "<fcolor:2>▋ Игроков: <count> <br>";
        command.toponline.line = "<fcolor:2><time_player> <fcolor:1>наиграл <fcolor:2><time>";
        command.toponline.footer = "<br><fcolor:2>▋ <click:run_command:\"<command> <prev_page>\">←</click> <fcolor:1>Страница: <current_page>/<last_page> <fcolor:2><click:run_command:\"<command> <next_page>\">→";

        command.maintenance.kick = "<color:#ff7171>★ На сервере ведутся технические работы";
        command.maintenance.serverDescription = "<color:#ff7171>В настоящее время проводятся технические работы";
        command.maintenance.serverVersion = "Технические работы";
        command.maintenance.formatTrue = "<fcolor:1>★ Ты <fcolor:2>включил <fcolor:1>технические работы на сервере";
        command.maintenance.formatFalse = "<fcolor:1>★ Ты <fcolor:2>выключил <fcolor:1>технические работы на сервере";

        command.rockpaperscissors.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        command.rockpaperscissors.wrongMove = "<color:#ff7171><b>⁉</b> Такой ход невозможен";
        command.rockpaperscissors.already = "<color:#ff7171><b>⁉</b> Ты уже сходил";
        command.rockpaperscissors.nullGame = "<color:#ff7171><b>⁉</b> Этой игры не существует";
        command.rockpaperscissors.myself = "<color:#ff7171><b>⁉</b> Ты не можешь играть с самим собой";
        command.rockpaperscissors.formatMove = "<fcolor:2>✂ <fcolor:1>Выбери свой ход <fcolor:2><click:run_command:\"/rps <target> rock <uuid>\">[\uD83E\uDEA8 камень]</click> <click:run_command:\"/rps <target> scissors <uuid>\">[✂ ножницы]</click> <click:run_command:\"/rps <target> paper <uuid>\">[\uD83E\uDDFB бумага]</click>";
        command.rockpaperscissors.sender = "<fcolor:2>✂ <fcolor:1>Теперь ходит <display_name>";
        command.rockpaperscissors.receiver = "<fcolor:2>✂ <display_name> <fcolor:1>предложил сыграть в камень-ножницы-бумага";
        command.rockpaperscissors.formatDraw = "<color:#98FB98>✂ Ничья! Вы оба выбрали <b><move>";
        command.rockpaperscissors.formatWin = "<color:#98FB98>✂ Выиграл <display_name>! <b><sender_move></b> на <b><receiver_move></b>";
        command.rockpaperscissors.strategies.clear();
        command.rockpaperscissors.strategies.putAll(Map.of(
                "paper", "бумага",
                "rock", "камень",
                "scissors", "ножницы"
        ));

        command.exception.execution = "<color:#ff7171><b>⁉</b> Произошла ошибка при выполнении команды <br><color:#ff7171><b>⁉</b> <exception>";
        command.exception.syntax = "<hover:show_text:\"<fcolor:2>Использовать <br><fcolor:1>/<correct_syntax>\"><click:suggest_command:\"/<command> \"><fcolor:2>┌<br>│ Использование →<br>│ <fcolor:1>/<correct_syntax><br><fcolor:2>└";
        command.exception.parseUnknown = "<color:#ff7171><b>⁉</b> Неизвестная ошибка аргумента в <br><input>";
        command.exception.parseBoolean = "<color:#ff7171><b>⁉</b> Ожидался boolean аргумент, но ты ввёл <br><input>";
        command.exception.parseNumber = "<color:#ff7171><b>⁉</b> Ожидался числовой аргумент, но ты ввёл <br><input>";
        command.exception.parseString = "<color:#ff7171><b>⁉</b> Ожидался строковый аргумент, но ты ввёл <br><input>";
        command.exception.permission = "<color:#ff7171><b>⁉</b> У тебя нет прав на использование этой команды";

        command.prompt.message = "сообщение";
        command.prompt.hard = "сложно?";
        command.prompt.accept = "принять";
        command.prompt.turn = "включить";
        command.prompt.type = "тип";
        command.prompt.category = "категория";
        command.prompt.reason = "причина";
        command.prompt.id = "айди";
        command.prompt.time = "время";
        command.prompt.repeatTime = "время повторения";
        command.prompt.multipleVote = "мульти голосование";
        command.prompt.player = "игрок";
        command.prompt.number = "число";
        command.prompt.color = "цвет";
        command.prompt.language = "язык";
        command.prompt.url = "ссылка";
        command.prompt.move = "ход";
        command.prompt.value = "значение";

        integration.discord.infoChannel.clear();
        integration.discord.infoChannel.put("айди", "ТПС <tps>");

        message.advancement.task = "<fcolor:1>🌠 <display_name> получил достижение «<advancement>»";
        message.advancement.goal = "<fcolor:1>🌠 <display_name> достиг цели «<advancement>»";
        message.advancement.challenge = "<fcolor:1>🌠 <display_name> завершил испытание «<advancement>»";
        message.advancement.taken = "<fcolor:1>🌠 <display_name> потерял достижение «<advancement>»";
        message.advancement.tag.task = "<color:#4eff52>[<hover:show_text:\"<color:#4eff52><advancement>\"><advancement></hover>]";
        message.advancement.tag.challenge = "<color:#834eff>[<hover:show_text:\"<color:#834eff><advancement>\"><advancement></hover>]";
        message.advancement.revoke.manyToOne = "<fcolor:1>🌠 Отозвано <fcolor:2><advancements> <fcolor:1>достижений у игрока <target>";
        message.advancement.revoke.oneToMany = "<fcolor:1>🌠 Достижение «<advancement>» отозвано у <fcolor:2><players> <fcolor:1>игрокам";
        message.advancement.revoke.manyToMany = "<fcolor:1>🌠 Отозвано <fcolor:2><advancements> <fcolor:1>достижений у <fcolor:2><players> <fcolor:1>игроков";
        message.advancement.revoke.oneToOne = "<fcolor:1>🌠 Отозвано достижение «<advancement>» у игрока <target>";
        message.advancement.revoke.criterionToMany = "<fcolor:1>🌠 Отозвано условие «<fcolor:2><criterion><fcolor:1>» достижения <advancement> у <fcolor:2><players> <fcolor:1>игроков";
        message.advancement.revoke.criterionToOne = "<fcolor:1>🌠 Отозвано условие «<fcolor:2><criterion><fcolor:1>» достижения <advancement> у игрока <target>";
        message.advancement.grant.manyToOne = "<fcolor:1>🌠 Выдано <fcolor:2><advancements> <fcolor:1>достижений игроку <target>";
        message.advancement.grant.oneToMany = "<fcolor:1>🌠 Достижение «<advancement>» выдано <fcolor:2><players> <fcolor:1>игрокам";
        message.advancement.grant.manyToMany = "<fcolor:1>🌠 Выдано <fcolor:2><advancements> <fcolor:1>достижений <fcolor:2><players> <fcolor:1>игрокам";
        message.advancement.grant.oneToOne = "<fcolor:1>🌠 Достижение «<advancement>» выдано игроку <target>";
        message.advancement.grant.criterionToMany = "<fcolor:1>🌠 Условие «<fcolor:2><criterion><fcolor:1>» достижения <advancement> зачтено <fcolor:2><players> <fcolor:1>игрокам";
        message.advancement.grant.criterionToOne = "<fcolor:1>🌠 Условие «<fcolor:2><criterion><fcolor:1>» достижения <advancement> зачтено игроку <target>";

        message.auto.types.clear();
        message.auto.types.put("announcement", new LinkedList<>(){
            {
                push("<br><fcolor:1>◇ Сервер использует <click:open_url:\"https://flectone.net/pulse/\"><hover:show_text:\"<fcolor:2>https://flectone.net/pulse/\"><fcolor:2>FlectonePulse</hover></click> :)<br>");
                push("<br><fcolor:1>      ❝ Заходи в дискорд ❠<br><fcolor:2>    <u><click:open_url:\"https://discord.flectone.net\"><hover:show_text:\"<fcolor:2>https://discord.flectone.net\">https://discord.flectone.net</hover></click></u><br>");
                push("<br><fcolor:1>⚡ Поддержи <fcolor:2>FlectonePulse <fcolor:1>на Boosty <br><fcolor:1>⚡ <u><click:open_url:\"https://boosty.to/thefaser/\"><hover:show_text:\"<fcolor:2>https://boosty.to/thefaser/\">https://boosty.to/thefaser/</hover></click></u><br>");
                push("<br><fcolor:1>   ✉ Заходи в телеграм ✉ <br><fcolor:2>    <u><click:open_url:\"https://t.me/flectone\"><hover:show_text:\"<fcolor:2>https://t.me/flectone\">https://t.me/flectone</hover></click></u><br>");
            }
        });

        message.bed.noSleep = "<fcolor:1>\uD83D\uDECC Вы можете спать только ночью или во время грозы";
        message.bed.notSafe = "<fcolor:1>\uD83D\uDECC Вы не можете уснуть, пока рядом есть монстры";
        message.bed.obstructed = "<fcolor:1>\uD83D\uDECC Эта кровать заблокирована";
        message.bed.occupied = "<fcolor:1>\uD83D\uDECC Эта кровать занята";
        message.bed.tooFarAway = "<fcolor:1>\uD83D\uDECC Вы не можете уснуть, кровать слишком далеко";

        message.brand.values.clear();
        message.brand.values.addAll(Arrays.asList("<white>Майнкрафт", "<aqua>Майнкрафт"));

        message.chat.types.clear();
        message.chat.types.putAll(Map.of(
                "local", "<delete><display_name><fcolor:3>: <message><reset><translate>",
                "global", "<delete><display_name> <world_prefix>»<fcolor:4> <message><reset><translate>"
        ));
        message.chat.nullChat = "<color:#ff7171><b>⁉</b> На сервер выключен чат";
        message.chat.nullReceiver = "<color:#ff7171><b>⁉</b> Тебя никто не услышал";

        message.clear.single = "<fcolor:1>\uD83C\uDF0A Удалено <fcolor:2><items> <fcolor:1>предметов у игрока <target>";
        message.clear.multiple = "<fcolor:1>\uD83C\uDF0A Удалено <fcolor:2><items> <fcolor:1>предметов у <fcolor:2><players> <fcolor:1>игроков";

        message.clone.format = "<fcolor:1>⏹ Успешно скопировано <fcolor:2><blocks> <fcolor:1>блоков";

        message.commandblock.notEnabled = "<fcolor:1>\uD83E\uDD16 На этом сервере командные блоки отключены";
        message.commandblock.format = "<fcolor:1>\uD83E\uDD16 Команда задана: <fcolor:2><command>";

        message.damage.format = "<fcolor:1>\uD83D\uDDE1 Нанесено <fcolor:2><amount> <fcolor:1>урона <target>";

        message.afk.formatTrue.global = "<gradient:#ffd500:#FFFF00>⌚ <player> отошёл";
        message.afk.formatTrue.local = "<gradient:#ffd500:#FFFF00>⌚ Ты отошёл от игры";
        message.afk.formatFalse.global = "<gradient:#ffd500:#FFFF00>⌚ <player> вернулся";
        message.afk.formatFalse.local = "<gradient:#ffd500:#FFFF00>⌚ Ты вернулся в игру";

        message.attribute.baseValue.get = "<fcolor:1>❤ Базовое значение атрибута «<fcolor:2><lang:'<attribute>'><fcolor:1>» у сущности <target> равно <fcolor:2><value>";
        message.attribute.baseValue.reset = "<fcolor:1>❤ Базовое значение атрибута «<fcolor:2><lang:'<attribute>'><fcolor:1>» у сущности <target> возвращено к <fcolor:2><value>";
        message.attribute.baseValue.set = "<fcolor:1>❤ Базовое значение атрибута «<fcolor:2><lang:'<attribute>'><fcolor:1>» у сущности <target> изменено на <fcolor:2><value>";
        message.attribute.modifier.add = "<fcolor:1>❤ Добавлен модификатор <fcolor:2><modifier> <fcolor:1>к атрибуту «<fcolor:2><lang:'<attribute>'><fcolor:1>» у сущности <target>";
        message.attribute.modifier.remove = "<fcolor:1>❤ Удалён модификатор <fcolor:2><modifier> <fcolor:1>с атрибута «<fcolor:2><lang:'<attribute>'><fcolor:1>» у сущности <target>";
        message.attribute.modifier.valueGet = "<fcolor:1>❤ Значение модификатора <fcolor:2><modifier> <fcolor:1>атрибута «<fcolor:2><lang:'<attribute>'><fcolor:1>» у сущности <target> равно <fcolor:2><value>";
        message.attribute.valueGet = "<fcolor:1>❤ Значение атрибута «<fcolor:2><lang:'<attribute>'><fcolor:1>» у сущности <target> равно <fcolor:2><value>";

        message.death.types.put("death.attack.anvil", "<fcolor:1>☠ <target> раздавлен упавшей наковальней");
        message.death.types.put("death.attack.anvil.player", "<fcolor:1>☠ <target> был раздавлен упавшей наковальней, пока боролся с <killer>");
        message.death.types.put("death.attack.arrow", "<fcolor:1>☠ <target> застрелен <killer>");
        message.death.types.put("death.attack.arrow.item", "<fcolor:1>☠ <target> застрелен <killer> с помощью <fcolor:2>[<killer_item>]<fcolor:1>");
        message.death.types.put("death.attack.badRespawnPoint.message", "<fcolor:1>☠ <target> стал жертвой <fcolor:2>[<click:open_url:\"https://www.youtube.com/watch?v=dQw4w9WgXcQ\"><hover:show_text:\"<fcolor:2>MCPE-28723\">жестоких правил игры</hover></click>]");
        message.death.types.put("death.attack.cactus", "<fcolor:1>☠ <target> исколот до смерти");
        message.death.types.put("death.attack.cactus.player", "<fcolor:1>☠ <target> наткнулся на кактус, спасаясь от <killer>");
        message.death.types.put("death.attack.cramming", "<fcolor:1>☠ <target> расплющен в лепёшку");
        message.death.types.put("death.attack.cramming.player", "<fcolor:1>☠ <target> расплющен <killer>");
        message.death.types.put("death.attack.dragonBreath", "<fcolor:1>☠ <target> испепелён дыханием дракона");
        message.death.types.put("death.attack.dragonBreath.player", "<fcolor:1>☠ <target> сварился заживо в драконьем дыхании из-за <killer>");
        message.death.types.put("death.attack.drown", "<fcolor:1>☠ <target> утонул");
        message.death.types.put("death.attack.drown.player", "<fcolor:1>☠ <target> утонул, спасаясь от <killer>");
        message.death.types.put("death.attack.dryout", "<fcolor:1>☠ <target> умер от обезвоживания");
        message.death.types.put("death.attack.dryout.player", "<fcolor:1>☠ <target> умер от обезвоживания, спасаясь от <killer>");
        message.death.types.put("death.attack.even_more_magic", "<fcolor:1>☠ <target> был убит неизведанной магией");
        message.death.types.put("death.attack.explosion", "<fcolor:1>☠ <target> взорвался");
        message.death.types.put("death.attack.explosion.player", "<fcolor:1>☠ <target> был взорван <killer>");
        message.death.types.put("death.attack.explosion.item", "<fcolor:1>☠ <target> был взорван <killer> с помощью <fcolor:2>[<killer_item>]<fcolor:1>");
        message.death.types.put("death.attack.explosion.player.item", "<fcolor:1>☠ <target> был взорван <killer> с помощью <fcolor:2>[<killer_item>]<fcolor:1>");
        message.death.types.put("death.attack.fall", "<fcolor:1>☠ <target> разбился вдребезги");
        message.death.types.put("death.attack.fall.player", "<fcolor:1>☠ <target> разбился вдребезги, спасаясь от <killer>");
        message.death.types.put("death.attack.fallingBlock", "<fcolor:1>☠ <target> раздавлен упавшим блоком");
        message.death.types.put("death.attack.fallingBlock.player", "<fcolor:1>☠ <target> был раздавлен упавшим блоком, пока боролся с <killer>");
        message.death.types.put("death.attack.fallingStalactite", "<fcolor:1>☠ <target> был пронзён обрушившимся сталактитом");
        message.death.types.put("death.attack.fallingStalactite.player", "<fcolor:1>☠ <target> был пронзён обрушившимся сталактитом, пока боролся с <killer>");
        message.death.types.put("death.attack.fireball", "<fcolor:1>☠ <target> убит файерболом <killer>");
        message.death.types.put("death.attack.fireball.item", "<fcolor:1>☠ <target> убит файерболом <killer> с помощью <fcolor:2>[<killer_item>]<fcolor:1>");
        message.death.types.put("death.attack.fireworks", "<fcolor:1>☠ <target> с треском разлетелся");
        message.death.types.put("death.attack.fireworks.item", "<fcolor:1>☠ <target> с треском разлетелся из-за фейерверка <killer>, выпущенного из <fcolor:2>[<killer_item>]<fcolor:1>");
        message.death.types.put("death.attack.fireworks.player", "<fcolor:1>☠ <target> с треском разлетелся, пока боролся с <killer>");
        message.death.types.put("death.attack.flyIntoWall", "<fcolor:1>☠ <target> преобразовал кинетическую энергию во внутреннюю");
        message.death.types.put("death.attack.flyIntoWall.player", "<fcolor:1>☠ <target> преобразовал кинетическую энергию во внутреннюю, спасаясь от <killer>");
        message.death.types.put("death.attack.freeze", "<fcolor:1>☠ <target> замёрз насмерть");
        message.death.types.put("death.attack.freeze.player", "<fcolor:1>☠ <target> замёрз насмерть благодаря <killer>");
        message.death.types.put("death.attack.generic", "<fcolor:1>☠ <target> умер");
        message.death.types.put("death.attack.generic.player", "<fcolor:1>☠ <target> умер из-за <killer>");
        message.death.types.put("death.attack.genericKill", "<fcolor:1>☠ <target> убит");
        message.death.types.put("death.attack.genericKill.player", "<fcolor:1>☠ <target> был убит, сражаясь с <killer>");
        message.death.types.put("death.attack.hotFloor", "<fcolor:1>☠ <target> обнаружил, что пол — это лава");
        message.death.types.put("death.attack.hotFloor.player", "<fcolor:1>☠ <target> зашёл в опасную зону из-за <killer>");
        message.death.types.put("death.attack.inFire", "<fcolor:1>☠ <target> умер в огне");
        message.death.types.put("death.attack.inFire.player", "<fcolor:1>☠ <target> сгорел в огне, пока боролся с <killer>");
        message.death.types.put("death.attack.inWall", "<fcolor:1>☠ <target> погребён заживо");
        message.death.types.put("death.attack.inWall.player", "<fcolor:1>☠ <target> был погребён заживо, пока боролся с <killer>");
        message.death.types.put("death.attack.indirectMagic", "<fcolor:1>☠ <target> был убит <killer> с помощью магии");
        message.death.types.put("death.attack.indirectMagic.item", "<fcolor:1>☠ <target> был убит <killer> с помощью <fcolor:2>[<killer_item>]<fcolor:1>");
        message.death.types.put("death.attack.lava", "<fcolor:1>☠ <target> решил поплавать в лаве");
        message.death.types.put("death.attack.lava.player", "<fcolor:1>☠ <target> упал в лаву, убегая от <killer>");
        message.death.types.put("death.attack.lightningBolt", "<fcolor:1>☠ <target> был поражён молнией");
        message.death.types.put("death.attack.lightningBolt.player", "<fcolor:1>☠ <target> был поражён молнией, пока боролся с <killer>");
        message.death.types.put("death.attack.mace_smash", "<fcolor:1>☠ <target> был сокрушён <killer>");
        message.death.types.put("death.attack.mace_smash.item", "<fcolor:1>☠ <target> был сокрушён <killer> с помощью <fcolor:2>[<killer_item>]<fcolor:1>");
        message.death.types.put("death.attack.magic", "<fcolor:1>☠ <target> был убит магией");
        message.death.types.put("death.attack.magic.player", "<fcolor:1>☠ <target> был убит магией, убегая от <killer>");
        message.death.types.put("death.attack.mob", "<fcolor:1>☠ <target> был убит <killer>");
        message.death.types.put("death.attack.mob.item", "<fcolor:1>☠ <target> был убит <killer> с помощью <fcolor:2>[<killer_item>]<fcolor:1>");
        message.death.types.put("death.attack.onFire", "<fcolor:1>☠ <target> сгорел заживо");
        message.death.types.put("death.attack.onFire.item", "<fcolor:1>☠ <target> был сожжён дотла, пока боролся с <killer>, держащим <fcolor:2>[<killer_item>]<fcolor:1>");
        message.death.types.put("death.attack.onFire.player", "<fcolor:1>☠ <target> был сожжён дотла, пока боролся с <killer>");
        message.death.types.put("death.attack.outOfWorld", "<fcolor:1>☠ <target> выпал из мира");
        message.death.types.put("death.attack.outOfWorld.player", "<fcolor:1>☠ <target> не захотел жить в том же мире, что и <killer>");
        message.death.types.put("death.attack.outsideBorder", "<fcolor:1>☠ <target> покинул пределы этого мира");
        message.death.types.put("death.attack.outsideBorder.player", "<fcolor:1>☠ <target> покинул пределы этого мира, пока боролся с <killer>");
        message.death.types.put("death.attack.player", "<fcolor:1>☠ <target> был убит <killer>");
        message.death.types.put("death.attack.player.item", "<fcolor:1>☠ <target> был убит <killer> с помощью <fcolor:2>[<killer_item>]<fcolor:1>");
        message.death.types.put("death.attack.sonic_boom", "<fcolor:1>☠ <target> был уничтожен звуковым зарядом");
        message.death.types.put("death.attack.sonic_boom.item", "<fcolor:1>☠ <target> был уничтожен звуковым зарядом, спасаясь от <killer>, держащего <fcolor:2>[<killer_item>]<fcolor:1>");
        message.death.types.put("death.attack.sonic_boom.player", "<fcolor:1>☠ <target> был уничтожен звуковым зарядом, спасаясь от <killer>");
        message.death.types.put("death.attack.stalagmite", "<fcolor:1>☠ <target> пронзён сталагмитом");
        message.death.types.put("death.attack.stalagmite.player", "<fcolor:1>☠ <target> был пронзён сталагмитом, пока боролся с <killer>");
        message.death.types.put("death.attack.starve", "<fcolor:1>☠ <target> умер от голода");
        message.death.types.put("death.attack.starve.player", "<fcolor:1>☠ <target> умер от голода, пока боролся с <killer>");
        message.death.types.put("death.attack.sting", "<fcolor:1>☠ <target> изжален до смерти");
        message.death.types.put("death.attack.sting.item", "<fcolor:1>☠ <target> был изжален до смерти <killer> с помощью <fcolor:2>[<killer_item>]<fcolor:1>");
        message.death.types.put("death.attack.sting.player", "<fcolor:1>☠ <target> изжален до смерти <killer>");
        message.death.types.put("death.attack.sweetBerryBush", "<fcolor:1>☠ <target> искололся до смерти в кустах сладких ягод");
        message.death.types.put("death.attack.sweetBerryBush.player", "<fcolor:1>☠ <target> искололся до смерти в кустах сладких ягод, спасаясь от <killer>");
        message.death.types.put("death.attack.thorns", "<fcolor:1>☠ <target> был убит, пытаясь навредить <killer>");
        message.death.types.put("death.attack.thorns.item", "<fcolor:1>☠ <target> был убит <fcolor:2>[<killer_item>]<fcolor:1>, пытаясь навредить <killer>");
        message.death.types.put("death.attack.thrown", "<fcolor:1>☠ <target> был избит <killer>");
        message.death.types.put("death.attack.thrown.item", "<fcolor:1>☠ <target> был избит <killer> с помощью <fcolor:2>[<killer_item>]<fcolor:1>");
        message.death.types.put("death.attack.trident", "<fcolor:1>☠ <target> был пронзён <killer>");
        message.death.types.put("death.attack.trident.item", "<fcolor:1>☠ <target> пронзён <killer> с помощью <fcolor:2>[<killer_item>]<fcolor:1>");
        message.death.types.put("death.attack.wither", "<fcolor:1>☠ <target> иссушён");
        message.death.types.put("death.attack.wither.player", "<fcolor:1>☠ <target> был иссушён, пока боролся с <killer>");
        message.death.types.put("death.attack.witherSkull", "<fcolor:1>☠ <target> был поражён черепом из <killer>");
        message.death.types.put("death.attack.witherSkull.item", "<fcolor:1>☠ <target> был поражён черепом из <killer> с помощью <fcolor:2>[<killer_item>]<fcolor:1>");
        message.death.types.put("death.fell.accident.generic", "<fcolor:1>☠ <target> разбился насмерть");
        message.death.types.put("death.fell.accident.ladder", "<fcolor:1>☠ <target> свалился с лестницы");
        message.death.types.put("death.fell.accident.other_climbable", "<fcolor:1>☠ <target> сорвался");
        message.death.types.put("death.fell.accident.scaffolding", "<fcolor:1>☠ <target> сорвался с подмосток");
        message.death.types.put("death.fell.accident.twisting_vines", "<fcolor:1>☠ <target> сорвался с вьющейся лозы");
        message.death.types.put("death.fell.accident.vines", "<fcolor:1>☠ <target> сорвался с лианы");
        message.death.types.put("death.fell.accident.weeping_vines", "<fcolor:1>☠ <target> сорвался с плакучей лозы");
        message.death.types.put("death.fell.assist", "<fcolor:1>☠ <target> свалился благодаря <killer>");
        message.death.types.put("death.fell.assist.item", "<fcolor:1>☠ <target> был обречён на падение <killer> с помощью <fcolor:2>[<killer_item>]<fcolor:1>");
        message.death.types.put("death.fell.finish", "<fcolor:1>☠ <target> упал с высоты и был добит <killer>");
        message.death.types.put("death.fell.finish.item", "<fcolor:1>☠ <target> упал с высоты и был добит <killer> с помощью <fcolor:2>[<killer_item>]<fcolor:1>");
        message.death.types.put("death.fell.killer", "<fcolor:1>☠ <target> был обречён на падение");

        message.debugstick.empty = "<fcolor:1>\uD83D\uDD27 Свойства объекта <fcolor:2><property> <fcolor:1>не заданы";
        message.debugstick.select = "<fcolor:1>\uD83D\uDD27 выбрано «<fcolor:2><property><fcolor:1>» (<fcolor:2><value><fcolor:1>)";
        message.debugstick.update = "<fcolor:1>\uD83D\uDD27 «<fcolor:2><property><fcolor:1>»: <fcolor:2><value>";

        message.deop.format = "<fcolor:1>\uD83E\uDD16 <target> больше не является оператором сервера";

        message.dialog.clear.single = "<fcolor:1>\uD83D\uDDD4 Убран диалог у игрока «<target>»";
        message.dialog.clear.multiple = "<fcolor:1>\uD83D\uDDD4 Убран диалог у <fcolor:2><players> <fcolor:1>игроков";
        message.dialog.show.single = "<fcolor:1>\uD83D\uDDD4 Отображён диалог игроку «<target>»";
        message.dialog.show.multiple = "<fcolor:1>\uD83D\uDDD4 Отображён диалог <fcolor:2><players> <fcolor:1>игрокам";

        message.difficulty.query = "<fcolor:1>⚔ Сложность игры: <fcolor:2><lang:'<difficulty>'>";
        message.difficulty.success = "<fcolor:1>⚔ Установлена сложность игры: <fcolor:2><lang:'<difficulty>'>";

        message.effect.clear.everything.single = "<fcolor:1>⚗ Убраны все эффекты с <target>";
        message.effect.clear.everything.multiple = "<fcolor:1>⚗ Убраны все эффекты с <fcolor:2><players> <fcolor:1>целей";
        message.effect.clear.specific.single = "<fcolor:1>⚗ Убран эффект «<fcolor:2><lang:'<effect>'><fcolor:1>» с <target>";
        message.effect.clear.specific.multiple = "<fcolor:1>⚗ Убран эффект «<fcolor:2><lang:'<effect>'><fcolor:1>» с <fcolor:2><players> <fcolor:1>целей";
        message.effect.give.single = "<fcolor:1>⚗ Применён эффект «<fcolor:2><lang:'<effect>'><fcolor:1>» к <target>";
        message.effect.give.multiple = "<fcolor:1>⚗ Применён эффект <fcolor:2><lang:'<effect>'><fcolor:1>» к <fcolor:2><players> <fcolor:1>целям";

        message.enchant.single = "<fcolor:1>\uD83D\uDCD6 Наложены чары «<fcolor:2><enchantment><fcolor:1>» на предмет <target>";
        message.enchant.multiple = "<fcolor:1>\uD83D\uDCD6 Наложены чары «<fcolor:2><enchantment><fcolor:1>» на предмет <fcolor:2><players><fcolor:1> сущностей";

        message.execute.pass = "<fcolor:1>⚡ Условие выполнено";
        message.execute.passCount = "<fcolor:1>⚡ Условие выполнено; счётчик: <fcolor:2><count>";

        message.experience.add.levels.single = "<fcolor:1>⏺ Выдано <fcolor:2><amount> <fcolor:1>уровней игроку <target>";
        message.experience.add.levels.multiple = "<fcolor:1>⏺ Выдано <fcolor:2><amount> <fcolor:1>уровней <fcolor:2><players> <fcolor:1>игрокам";
        message.experience.add.points.single = "<fcolor:1>⏺ Выдано <fcolor:2><amount> <fcolor:1>единиц опыта игроку <target>";
        message.experience.add.points.multiple = "<fcolor:1>⏺ Выдано <fcolor:2><amount> <fcolor:1>единиц опыта <fcolor:2><players> <fcolor:1>игрокам";
        message.experience.query.levels = "<fcolor:1>⏺ <target> имеет <fcolor:2><amount> <fcolor:1>уровней";
        message.experience.query.points = "<fcolor:1>⏺ <target> имеет <fcolor:2><amount> <fcolor:1>ед. опыта";
        message.experience.set.levels.single = "<fcolor:1>⏺ Установлено <fcolor:2><amount> <fcolor:1>уровней игроку <target>";
        message.experience.set.levels.multiple = "<fcolor:1>⏺ Установлено <fcolor:2><amount> <fcolor:1>уровней <fcolor:2><players> <fcolor:1>игрокам";
        message.experience.set.points.single = "<fcolor:1>⏺ Установлено <fcolor:2><amount> <fcolor:1>единиц опыта игроку <target>";
        message.experience.set.points.multiple = "<fcolor:1>⏺ Установлено <fcolor:2><amount> <fcolor:1>единиц опыта <fcolor:2><players> <fcolor:1>игрокам";

        message.fill.format = "<fcolor:1>⏹ Успешно заполнено <fcolor:2><blocks> <fcolor:1>блоков";

        message.fillbiome.format = "<fcolor:1>⏹ Заменены биомы между точками <fcolor:2><x1><fcolor:1>, <fcolor:2><y1><fcolor:1>, <fcolor:2><z1> <fcolor:1>и <fcolor:2><x2><fcolor:1>, <fcolor:2><y2><fcolor:1>, <fcolor:2><z2>";
        message.fillbiome.formatCount = "<fcolor:1>⏹ Биом был заменён у <fcolor:2><blocks> <fcolor:1>блоков между точками <fcolor:2><x1><fcolor:1>, <fcolor:2><y1><fcolor:1>, <fcolor:2><z1> <fcolor:1>и <fcolor:2><x2><fcolor:1>, <fcolor:2><y2><fcolor:1>, <fcolor:2><z2>";

        message.format.replacement.values.put("skin", "<click:open_url:\"<message_1>\"><hover:show_text:\"<fcolor:2><pixels>\"><fcolor:2><u>👨 Скин</u></hover></click>");
        message.format.replacement.values.put("url", "<click:open_url:\"<message_1>\"><hover:show_text:\"<fcolor:2>Открыть ссылку <br><u><message_1>\"><fcolor:2><u>🗗 Ссылка</u></hover></click>");
        message.format.replacement.values.put("image", "<click:open_url:\"<message_1>\"><hover:show_text:\"<fcolor:2><pixels>\"><fcolor:2><u>🖃 Картинка</u></hover></click>");

        message.format.mention.person = "<fcolor:2>Тебя упомянули!";

        message.format.moderation.delete.placeholder = "<color:#ff7171><hover:show_text:\"<color:#ff7171>Нажми, чтобы удалить\"><click:run_command:\"/deletemessage <uuid>\">[x] ";
        message.format.moderation.delete.format = "<fcolor:3><i>Сообщение удалено</i>";

        message.format.moderation.newbie.reason = "Ты ещё слишком новичок";

        message.format.name_.display = "<click:suggest_command:\"/msg <player> \"><hover:show_text:\"<fcolor:2>Написать <player>\"><vault_prefix><stream_prefix><fcolor:2><player><afk_suffix><vault_suffix></hover></click>";
        message.format.name_.entity = "<fcolor:2><hover:show_text:\"<fcolor:2><name> <br><fcolor:1>Тип <fcolor:2><lang:'<type>'> <br><fcolor:1>Айди <fcolor:2><uuid>\"><name></hover>";
        message.format.name_.unknown = "<fcolor:2><name>";
        message.format.name_.invisible = "<fcolor:2>\uD83D\uDC7B Невидимка";

        message.format.translate.action = " <click:run_command:\"/translateto <language> <language> <message>\"><hover:show_text:\"<fcolor:2>Перевести сообщение\"><fcolor:1>⇄";

        message.format.questionAnswer.questions.clear();
        message.format.questionAnswer.questions.put("server", "<fcolor:2>[Вопрос-Ответ] @<player><fcolor:1>, это ванильный сервер в Майнкрафте!");
        message.format.questionAnswer.questions.put("flectone", "<fcolor:2>[Вопрос-Ответ] @<player><fcolor:1>, это бренд и проекты созданные TheFaser'ом");

        message.gamemode.setDefault = "<fcolor:1>\uD83D\uDDD8 Новый режим игры по умолчанию: <fcolor:2><lang:'<gamemode>'>";
        message.gamemode.self = "<fcolor:1>\uD83D\uDDD8 Твой режим игры изменён на <fcolor:2><lang:'<gamemode>'>";
        message.gamemode.other = "<fcolor:1>\uD83D\uDDD8 Режим игры игрока <target> изменён на <fcolor:2><lang:'<gamemode>'>";

        message.gamerule.formatQuery = "<fcolor:1>\uD83D\uDDD0 Значение игрового правила <fcolor:2><gamerule><fcolor:1>: <fcolor:2><value>";
        message.gamerule.formatSet = "<fcolor:1>\uD83D\uDDD0 Установлено значение игрового правила <fcolor:2><gamerule><fcolor:1>: <fcolor:2><value>";

        message.give.single = "<fcolor:1>⛏ Выдано <fcolor:2><items> <fcolor:1>[<fcolor:2><hover:show_text:\"<fcolor:2><give_item>\"><give_item></hover><fcolor:1>] игроку <target>";
        message.give.multiple = "<fcolor:1>⛏ Выдано <fcolor:2><items> <fcolor:1>[<fcolor:2><hover:show_text:\"<fcolor:2><give_item>\"><give_item></hover><fcolor:1>] <fcolor:2><players> <fcolor:1>игрокам";

        message.greeting.format = "<br>[#][#][#][#][#][#][#][#]<br>[#][#][#][#][#][#][#][#]<br>[#][#][#][#][#][#][#][#]<br>[#][#][#][#][#][#][#][#]<br>[#][#][#][#][#][#][#][#]  <fcolor:1>Привет,<br>[#][#][#][#][#][#][#][#]  <player><br>[#][#][#][#][#][#][#][#]<br>[#][#][#][#][#][#][#][#]<br>";

        message.join.formatFirstTime = "<color:#4eff52>→ <display_name> <fcolor:1>впервые тут!";

        message.kill.single = "<fcolor:1>☠ <fcolor:1><target> был убит";
        message.kill.multiple = "<fcolor:1>☠ <fcolor:1>Уничтожено <fcolor:2><entities> <fcolor:1>сущностей";

        message.locate.biome = "<fcolor:1>\uD83D\uDDFA Ближайший биом с типом <fcolor:2><value> <fcolor:1>находится по координатам <fcolor:2><hover:show_text:\"<fcolor:2>Нажми для телепортации\"><click:suggest_command:\"/tp @s <x> <y> <z>\">[<x>, <y>, <z>]</click></hover> <fcolor:1>(в <fcolor:2><blocks> <fcolor:1>блоках отсюда)";
        message.locate.poi = "<fcolor:1>\uD83D\uDDFA Ближайшая точка интереса с типом <fcolor:2><value> <fcolor:1>находится по координатам <fcolor:2><fcolor:2><hover:show_text:\"<fcolor:2>Нажми для телепортации\"><click:suggest_command:\"/tp @s <x> <y> <z>\">[<x>, <y>, <z>]</click></hover> <fcolor:1>(в <fcolor:2><blocks> <fcolor:1>блоках отсюда)";
        message.locate.structure = "<fcolor:1>\uD83D\uDDFA Ближайшее строение типа <fcolor:2><value> <fcolor:1>находится по координатам <fcolor:2><fcolor:2><hover:show_text:\"<fcolor:2>Нажми для телепортации\"><click:suggest_command:\"/tp @s <x> <y> <z>\">[<x>, <y>, <z>]</click></hover> <fcolor:1>(в <fcolor:2><blocks> <fcolor:1>блоках отсюда)";

        message.objective.belowname.format = "<fcolor:1>мс";

        message.op.format = "<fcolor:1>\uD83E\uDD16 <target> назначен оператором сервера";

        message.particle.format = "<fcolor:1>❄ Отображена частица «<fcolor:2><particle><fcolor:1>»";

        message.reload.format = "<fcolor:1>\uD83D\uDEC8 Перезагрузка!";

        message.ride.dismount = "<fcolor:1>\uD83C\uDFC7 <target> больше не сидит на <second_target>";
        message.ride.mount = "<fcolor:1>\uD83C\uDFC7 <target> теперь сидит на <second_target>";

        message.sidebar.values.clear();
        message.sidebar.values.addAll(new LinkedList<>(){
            {
                push(new LinkedList<>(){
                    {
                        push(" ");
                        push("<fcolor:1>Пинг <ping>");
                        push(" ");
                        push("<fcolor:1>FlectonePulse");
                    }
                });
                push(new LinkedList<>(){
                    {
                        push(" ");
                        push("<fcolor:2>ТПС <tps>");
                        push(" ");
                        push("<fcolor:2>FlectonePulse");
                    }
                });
            }
        });

        message.rotate.format = "<fcolor:1>\uD83E\uDD38 <target> повёрнут";

        message.save.disabled = "<fcolor:1>\uD83D\uDEC8 Автосохранение отключено";
        message.save.enabled = "<fcolor:1>\uD83D\uDEC8 Автосохранение включено";
        message.save.saving = "<fcolor:1>\uD83D\uDEC8 Сохранение мира (может занять некоторое время!)";
        message.save.success = "<fcolor:1>\uD83D\uDEC8 Игра сохранена";

        message.seed.format = "<fcolor:1>\uD83D\uDD11 Ключ генератора: [<fcolor:2><hover:show_text:'<fcolor:2>Нажми, чтобы скопировать в буфер обмена'><click:copy_to_clipboard:<seed>><seed></click></hover><fcolor:1>]";

        message.setblock.format = "<fcolor:1>⏹ Изменён блок в точке <fcolor:2><x><fcolor:1>, <fcolor:2><y><fcolor:1>, <fcolor:2><z><fcolor:1>";

        message.sleep.notPossible = "<fcolor:1>\uD83D\uDECC Никакой отдых не поможет пропустить эту ночь";
        message.sleep.playersSleeping = "<fcolor:1>\uD83D\uDECC <fcolor:2><players_sleeping> <fcolor:1>из <fcolor:2><players> <fcolor:1>игроков спят";
        message.sleep.skippingNight = "<fcolor:1>\uD83D\uDECC Вы проспите всю ночь";

        message.spawn.notValid = "<fcolor:1>\uD83D\uDECC У вас нет кровати или заряженного якоря возрождения, либо доступ к ним затруднён";
        message.spawn.set = "<fcolor:1>\uD83D\uDECC Точка возрождения установлена";
        message.spawn.setWorld = "<fcolor:1>\uD83D\uDECC Установлена точка возрождения мира <fcolor:2><x><fcolor:1>, <fcolor:2><y><fcolor:1>, <fcolor:2><z><fcolor:1> [<fcolor:2><angle><fcolor:1>]";
        message.spawn.single = "<fcolor:1>\uD83D\uDECC Установлена точка возрождения <fcolor:2><x><fcolor:1>, <fcolor:2><y><fcolor:1>, <fcolor:2><z><fcolor:1> [<fcolor:2><angle><fcolor:1>] в <fcolor:2><world> <fcolor:1>для <target>";
        message.spawn.multiple = "<fcolor:1>\uD83D\uDECC Установлена точка возрождения <fcolor:2><x><fcolor:1>, <fcolor:2><y><fcolor:1>, <fcolor:2><z><fcolor:1> [<fcolor:2><angle><fcolor:1>] в <fcolor:2><world> <fcolor:1>для <fcolor:2><players><fcolor:1> игроков";

        message.status.motd.values.clear();
        message.status.motd.values.addAll(List.of(
                "<fcolor:1>Добро пожаловать на сервер!",
                "<fcolor:1>Присоединяйся и наслаждайся уникальным опытом игры!",
                "<fcolor:1>У нас дружелюбное сообщество - будь вежлив и уважай других!",
                "<fcolor:1>Приятной игры! Если есть вопросы, обращайся к администрации"
        ));

        message.status.version.name = "Майнкрафт сервер";

        message.status.players.full = "<color:#ff7171>Сервер полон";

        message.stop.format = "<fcolor:1>\uD83D\uDEC8 Выключение сервера";

        message.summon.format = "<fcolor:1>\uD83D\uDC3A Сущность «<target>» создана";

        message.tab.footer.lists.clear();
        message.tab.footer.lists.addAll(new LinkedList<>(){
            {
                push(new LinkedList<>(){
                    {
                        push(" ");
                        push("<fcolor:1>Привет <fcolor:2><player><fcolor:1>!");
                        push(" ");
                    }
                });
                push(new LinkedList<>(){
                    {
                        push(" ");
                        push("<fcolor:1>ТПС <tps>, Онлайн <online>");
                        push(" ");
                    }
                });
            }
        });

        message.teleport.entity.single = "<fcolor:1>\uD83C\uDF00 <target> телепортирован к <second_target>";
        message.teleport.entity.multiple = "<fcolor:1>\uD83C\uDF00 <fcolor:2><entities> <fcolor:1>сущностей телепортированы к <second_target>";
        message.teleport.location.single = "<fcolor:1>\uD83C\uDF00 <target> телепортирован в точку <fcolor:2><x><fcolor:1>, <fcolor:2><y><fcolor:1>, <fcolor:2><z><fcolor:1>";
        message.teleport.location.multiple = "<fcolor:1>\uD83C\uDF00 <fcolor:2><entities> <fcolor:1>сущностей телепортированы в точку <fcolor:2><x><fcolor:1>, <fcolor:2><y><fcolor:1>, <fcolor:2><z><fcolor:1>";

        message.time.query = "<fcolor:1>☽ Время: <fcolor:2><time>";
        message.time.set = "<fcolor:1>☽ Установлено время: <fcolor:2><time>";

        message.update.formatPlayer = "<fcolor:1><fcolor:2>(FlectonePulse) <fcolor:1>Твоя версия <fcolor:2><current_version> <fcolor:1>устарела! Обновись до <fcolor:2><latest_version> <fcolor:1>с помощью <url:https://modrinth.com/plugin/flectonepulse>, чтобы получить новые возможности!";
        message.update.formatConsole = "<fcolor:1>Твоя версия <fcolor:2><current_version> <fcolor:1>устарела! Обновись до <fcolor:2><latest_version> <fcolor:1>с помощью <click:open_url:https://modrinth.com/plugin/flectonepulse>https://modrinth.com/plugin/flectonepulse";

        message.weather.formatClear = "<fcolor:1>☀ Установлена <fcolor:2>ясная <fcolor:1>погода";
        message.weather.formatRain = "<fcolor:1>\uD83C\uDF27 Установлена <fcolor:2>дождливая <fcolor:1>погода";
        message.weather.formatThunder = "<fcolor:1>⛈ Установлена <fcolor:2>грозовая <fcolor:1>погода";

        message.worldborder.center =  "<fcolor:1>\uD83D\uDEAB Установлен центр границ мира: <fcolor:2><value><fcolor:1>, <fcolor:2><second_value>";
        message.worldborder.damage.amount = "<fcolor:1>\uD83D\uDEAB Установлено значение урона, ежесекундно наносимого за границами мира: <fcolor:2><value> <fcolor:1>за блок";
        message.worldborder.damage.buffer = "<fcolor:1>\uD83D\uDEAB Установлен предел нанесения урона за границами мира: <fcolor:2><value> <fcolor:1>блоков";
        message.worldborder.get = "<fcolor:1>\uD83D\uDEAB Ширина границы мира: <fcolor:2><value> <fcolor:1>блоков";
        message.worldborder.set.grow = "<fcolor:1>\uD83D\uDEAB Через <fcolor:2><second_value> <fcolor:1>секунд ширина границы мира увеличится до <fcolor:2><value> <fcolor:1>блоков";
        message.worldborder.set.immediate = "<fcolor:1>\uD83D\uDEAB Установлена граница мира шириной <fcolor:2><value> <fcolor:1>блоков";
        message.worldborder.set.shrink = "<fcolor:1>\uD83D\uDEAB Через <fcolor:2><second_value> <fcolor:1>секунд ширина границы мира уменьшится до <fcolor:2><value> <fcolor:1>блоков";
        message.worldborder.warning.distance = "<fcolor:1>\uD83D\uDEAB Установлено расстояние предупреждения о пересечении границы мира: <fcolor:2><value> <fcolor:1>блоков";
        message.worldborder.warning.time = "<fcolor:1>\uD83D\uDEAB Установлено время предупреждения о столкновении с границей мира: <fcolor:2><value> <fcolor:1>секунд";
    }


    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/")})

    private String cooldown = "<color:#ff7171><b>⁉</b> Too fast, you'll be able to use it in <time>";

    private Time time = new Time();

    @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/")})
    private Command command = new Command();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/integration/")})
    private Integration integration = new Integration();
    @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/")})
    private Message message = new Message();

    @Getter
    public static final class Time {
        private String format = "dd'd' HH'h' mm'm' ss.SSS's'";
        private String permanent = "PERMANENT";
        private String zero = "0s";
    }

    @Getter
    @Setter
    public static final class Command implements CommandConfig, Localizable {

        private Exception exception = new Exception();
        private Prompt prompt = new Prompt();

        @Override
        public SubCommandConfig getAfk() {
            return null;
        }

        @Getter
        @NoArgsConstructor
        public static final class Exception {
            private String execution = "<color:#ff7171><b>⁉</b> An error occurred while executing the command <br><color:#ff7171><b>⁉</b> <exception>";
            private String parseUnknown = "<color:#ff7171><b>⁉</b> Unknown argument error while parsing <br><input>";
            private String parseBoolean = "<color:#ff7171><b>⁉</b> A boolean argument was expected, but you entered <br><input>";
            private String parseNumber = "<color:#ff7171><b>⁉</b> A number argument was expected, but you entered <br><input>";
            private String parseString = "<color:#ff7171><b>⁉</b> A string argument was expected, but you entered <br><input>";
            private String permission = "<color:#ff7171><b>⁉</b> You don't have permission to use this command";
            private String syntax = "<hover:show_text:\"<fcolor:2>Use <br><fcolor:1>/<correct_syntax>\"><click:suggest_command:\"/<command> \"><fcolor:2>┌<br>│ Usage →<br>│ <fcolor:1>/<correct_syntax><br><fcolor:2>└";
        }

        @Getter
        @NoArgsConstructor
        public static final class Prompt {
            private String message = "message";
            private String hard = "hard";
            private String accept = "accept";
            private String turn = "turn on";
            private String type = "type";
            private String reason = "reason";
            private String category = "category";
            private String id = "id";
            private String time = "time";
            private String repeatTime = "repeat time";
            private String multipleVote = "multiple vote";
            private String player = "player";
            private String number = "number";
            private String color = "color";
            private String language = "language";
            private String url = "url";
            private String move = "move";
            private String value = "value";
        }

        @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/anon/")})
        private Anon anon = new Anon();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/ball/")})
        private Ball ball = new Ball();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/ban/")})
        private Ban ban = new Ban();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/banlist/")})
        private Banlist banlist = new Banlist();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/broadcast/")})
        private Broadcast broadcast = new Broadcast();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/chatcolor/")})
        private Chatcolor chatcolor = new Chatcolor();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/chatsetting/")})
        private Chatsetting chatsetting = new Chatsetting();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/clearchat/")})
        private Clearchat clearchat = new Clearchat();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/clearmail/")})
        private Clearmail clearmail = new Clearmail();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/coin/")})
        private Coin coin = new Coin();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/deletemessage/")})
        private Deletemessage deletemessage = new Deletemessage();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/dice/")})
        private Dice dice = new Dice();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/do/")})
        private Do Do = new Do();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/flectonepulse/")})
        private Flectonepulse flectonepulse = new Flectonepulse();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/geolocate/")})
        private Geolocate geolocate = new Geolocate();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/helper/")})
        private Helper helper = new Helper();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/ignore/")})
        private Ignore ignore = new Ignore();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/ignorelist/")})
        private Ignorelist ignorelist = new Ignorelist();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/kick/")})
        private Kick kick = new Kick();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/mail/")})
        private Mail mail = new Mail();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/maintenance/")})
        private Maintenance maintenance = new Maintenance();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/me/")})
        private Me me = new Me();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/mute/")})
        private Mute mute = new Mute();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/mutelist/")})
        private Mutelist mutelist = new Mutelist();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/online/")})
        private Online online = new Online();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/ping/")})
        private Ping ping = new Ping();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/poll/")})
        private Poll poll = new Poll();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/reply/")})
        private Reply reply = new Reply();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/rockpaperscissors/")})
        private Rockpaperscissors rockpaperscissors = new Rockpaperscissors();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/spy/")})
        private Spy spy = new Spy();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/stream/")})
        private Stream stream = new Stream();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/symbol/")})
        private Symbol symbol = new Symbol();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/tell/")})
        private Tell tell = new Tell();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/tictactoe/")})
        private Tictactoe tictactoe = new Tictactoe();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/toponline/")})
        private Toponline toponline = new Toponline();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/translateto/")})
        private Translateto translateto = new Translateto();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/try/")})
        private Try Try = new Try();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/unban/")})
        private Unban unban = new Unban();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/unmute/")})
        private Unmute unmute = new Unmute();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/unwarn/")})
        private Unwarn unwarn = new Unwarn();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/warn/")})
        private Warn warn = new Warn();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/warnlist/")})
        private Warnlist warnlist = new Warnlist();

        @Getter
        public static final class Anon implements SubCommandConfig, Localizable {
            private String format = "<fcolor:1>\uD83D\uDC7B <fcolor:2>Anon <fcolor:1><message>";
        }

        @Getter
        public static final class Ball implements SubCommandConfig, Localizable {
            private String format = "<color:#9370DB>❓ <display_name> asked: <message><reset> <color:#9370DB><br>\uD83D\uDD2E Ball answered: <u><answer></u>";
            private List<String> answers = new LinkedList<>(){
                {
                    push("Undeniably");
                    push("No doubt about it");
                    push("Definitely yes");
                    push("That's the base");
                    push("You can be sure of it");
                    push("Most likely");
                    push("Good prospects");
                    push("Yes");
                    push("It's not clear yet, try again");
                    push("Ask later");
                    push("It's better not to tell");
                    push("Can't predict now");
                    push("Concentrate and ask again");
                    push("Don't even think about it");
                    push("No.");
                    push("The prospects are not good");
                    push("Very doubtful");
                }
            };
        }

        @Getter
        public static final class Ban implements SubCommandConfig, Localizable {
            private String nullPlayer = "<color:#ff7171><b>⁉</b> This player does not exist";
            private String nullTime = "<color:#ff7171><b>⁉</b> Incorrect time";
            private ReasonMap reasons = new ReasonMap(){
                {
                    put("default", "You have been banned from this server");
                }
            };
            private String server = "<color:#ff7171>🔒 <fcolor:2><moderator> <fcolor:1>has banned <fcolor:2><player> <fcolor:1><hover:show_text:\"<fcolor:1>ID: <id><br>Date: <date><br>Time: <time><br>Remaining time: <time_left><br>Moderator: <moderator><br>Reason: <reason>\">[INFO]</hover>";
            private String person = "<color:#ff7171>🔒 BAN 🔒<fcolor:1><br><br>Date: <date><br><br>Time: <time><br><br>Remaining time: <time_left><br><br>Moderator: <moderator><br><br>Reason: <reason>";
            private String connectionAttempt = "<color:#ff7171>🔒 Banned <fcolor:2><player> <fcolor:1>tried to log in <hover:show_text:\"<fcolor:1>ID: <id><br>Date: <date><br>Time: <time><br>Remaining time: <time_left><br>Moderator: <moderator><br>Reason: <reason>\">[INFO]</hover>";

            @Getter
            @AllArgsConstructor
            public static final class Type {
                private String connectionAttempt;
                private ReasonMap reasons;
            }
        }

        @Getter
        public static final class Banlist implements SubCommandConfig, Localizable {
            private String empty = "<color:#98FB98>☺ No bans found";
            private String nullPage = "<color:#ff7171><b>⁉</b> This page doesn't exist";
            private String nullPlayer = "<color:#ff7171><b>⁉</b> This player does not exist";
            private ListTypeMessage global = new ListTypeMessage(
                    "<fcolor:2>▋ Bans: <count> <br>",
                    "<hover:show_text:\"<fcolor:1>Click to unban <display_name>\"><click:run_command:\"<command>\"><color:#ff7171>☒ <display_name></click></hover> <fcolor:1><hover:show_text:\"<fcolor:1>ID: <id><br>Date: <date><br>Time: <time><br>Moderator: <moderator><br>Reason: <reason>\">[INFO]</hover>",
                    "<br><fcolor:2>▋ <click:run_command:\"<command> <prev_page>\">←</click> <fcolor:1>Page: <current_page>/<last_page> <fcolor:2><click:run_command:\"<command> <next_page>\">→"
            );

            private ListTypeMessage player = new ListTypeMessage(
                    "<fcolor:2>▋ All bans: <count> <br>",
                    "<hover:show_text:\"<fcolor:1>Click to unban <display_name>\"><click:run_command:\"<command>\"><color:#ff7171>☒ <display_name></click></hover> <fcolor:1><hover:show_text:\"<fcolor:1>ID: <id><br>Date: <date><br>Time: <time><br>Moderator: <moderator><br>Reason: <reason>\">[INFO]</hover>",
                    "<br><fcolor:2>▋ <click:run_command:\"<command> <prev_page>\">←</click> <fcolor:1>Page: <current_page>/<last_page> <fcolor:2><click:run_command:\"<command> <next_page>\">→"
            );
        }

        @Getter
        public static final class Broadcast implements SubCommandConfig, Localizable {
            private String format = "<br><color:#ffd500>│ \uD83D\uDD6B Message for everyone <br>│<br>│ Author <display_name> <br>│<br>│ <fcolor:1><message> <br>";
        }

        @Getter
        public static final class Chatcolor implements SubCommandConfig, Localizable {
            private String nullPlayer = "<color:#ff7171><b>⁉</b> This player does not exist";
            private String nullType = "<color:#ff7171><b>⁉</b> Incorrect type entered";
            private String nullColor = "<color:#ff7171><b>⁉</b> Incorrect colors entered";
            private String format = "<br><color:#98FB98>│ Your colors: <br><color:#98FB98>│ <fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><color:#98FB98>│ <fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world! <br>";
        }

        @Getter
        public static final class Chatsetting implements SubCommandConfig, Localizable {
            private String noPermission = "<color:#ff7171><b>⁉</b> No permission to change this setting";
            private String disabledSelf = "<color:#ff7171><b>⁉</b> This feature is disabled via /chatsetting";
            private String disabledOther = "<color:#ff7171><b>⁉</b> He disabled this feature via /chatsetting";

            private String inventory = "Chat Settings";
            private Checkbox checkbox = new Checkbox();
            private Menu menu = new Menu();

            @Getter
            @NoArgsConstructor
            public static final class Checkbox {
                private String enabledColor = "<color:#98FB98>";
                private String enabledHover = "<status_color>Display enabled";
                private String disabledColor = "<color:#ff7171>";
                private String disabledHover = "<status_color>Display disabled";

                private Map<String, String> types = new LinkedHashMap<>() {
                    {
                        put(MessageType.AFK.name(), "<status_color>Afk");
                        put(MessageType.ADVANCEMENT.name(), "<status_color>Advancement");
                        put(MessageType.CHAT.name(), "<status_color>Chat messages");
                        put(MessageType.COMMAND_ANON.name(), "<status_color>Command /anon");
                        put(MessageType.COMMAND_BALL.name(), "<status_color>Command /ball");
                        put(MessageType.COMMAND_BROADCAST.name(), "<status_color>Command /broadcast");
                        put(MessageType.COMMAND_COIN.name(), "<status_color>Command /coin");
                        put(MessageType.COMMAND_DICE.name(), "<status_color>Command /dice");
                        put(MessageType.COMMAND_DO.name(), "<status_color>Command /do");
                        put(MessageType.COMMAND_MAIL.name(), "<status_color>Command /mail");
                        put(MessageType.COMMAND_ME.name(), "<status_color>Command /me");
                        put(MessageType.COMMAND_POLL.name(), "<status_color>Command /poll");
                        put(MessageType.COMMAND_ROCKPAPERSCISSORS.name(), "<status_color>Command /rockpaperscissors");
                        put(MessageType.COMMAND_STREAM.name(), "<status_color>Command /stream");
                        put(MessageType.COMMAND_TELL.name(), "<status_color>Command /tell");
                        put(MessageType.COMMAND_TICTACTOE.name(), "<status_color>Command /tictactoe");
                        put(MessageType.COMMAND_TRY.name(), "<status_color>Command /try");
                        put(MessageType.DEATH.name(), "<status_color>Death");
                        put(MessageType.FROM_DISCORD_TO_MINECRAFT.name(), "<status_color>Messages from Discord");
                        put(MessageType.FROM_TELEGRAM_TO_MINECRAFT.name(), "<status_color>Messages from Telegram");
                        put(MessageType.FROM_TWITCH_TO_MINECRAFT.name(), "<status_color>Messages from Twitch");
                        put(MessageType.JOIN.name(), "<status_color>Join");
                        put(MessageType.QUIT.name(), "<status_color>Quit");
                        put(MessageType.SLEEP.name(), "<status_color>Sleep");
                    }
                };
            }

            @Getter
            @NoArgsConstructor
            public static final class Menu {

                private SubMenu chat = new SubMenu("<fcolor:2>Chat type <br><fcolor:1>Chat for viewing and sending messages <br><br><fcolor:1>Your chat is <fcolor:2><chat>", "Chats", new LinkedHashMap<>() {
                    {
                        put("default", "<fcolor:2>Default chat<br><fcolor:1>You can see <fcolor:2>all <fcolor:1>chats and write to any chat");
                        put("local", "<fcolor:2>Local chat<br><fcolor:1>You can write to <fcolor:2>any <fcolor:1>chats");
                        put("global", "<fcolor:2>Global chat<br><fcolor:1>You can only write to <fcolor:2>global <fcolor:1>chat");
                    }
                });
                private SubMenu see = new SubMenu("<fcolor:2>Colors \"see\" <br><fcolor:1>Colors for /chatcolor see <br><br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world! <br><br><u><color:#ff7171>Only you see it in all messages", "Colors", new LinkedHashMap<>() {
                    {
                        put("default", "<gradient:#70C7EF:#37B1F2>Default colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
                        put("white", "<gradient:#D4E4FF:#B8D2FF>White colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
                        put("light_gray", "<gradient:#B5B9BD:#9DA2A6>Light gray colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
                        put("gray", "<gradient:#4A5054:#3A3F42>Gray colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
                        put("black", "<gradient:#17191A:#0D0E0F>Black colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
                        put("brown", "<gradient:#7A5A40:#634A34>Brown colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
                        put("red", "<gradient:#D63E3E:#C12B2B>Red colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
                        put("orange", "<gradient:#FF8C00:#E67E00>Orange colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
                        put("yellow", "<gradient:#FFE83D:#FFD900>Yellow colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
                        put("lime", "<gradient:#8EE53F:#7ACC29>Lime colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
                        put("green", "<gradient:#4BB54B:#3AA33A>Green colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
                        put("cyan", "<gradient:#3ECFDF:#2AB7C9>Cyan colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
                        put("light_blue", "<gradient:#6BB6FF:#4DA6FF>Light blue colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
                        put("blue", "<gradient:#3A75FF:#1F5AFF>Blue colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
                        put("purple", "<gradient:#A368C7:#8A4DBF>Purple colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
                        put("magenta", "<gradient:#FF5CD9:#FF3DCF>Magenta colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
                        put("pink", "<gradient:#FF80B7:#FF66A6>Pink colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
                    }
                });
                private SubMenu out = new SubMenu("<fcolor:2>Colors \"out\" <br><fcolor:1>Colors for /chatcolor out <br><br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world! <br><br><u><color:#ff7171>Everyone sees it in your messages", "Colors", new LinkedHashMap<>() {
                    {
                        put("default", "<gradient:#70C7EF:#37B1F2>Default colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
                        put("white", "<gradient:#D4E4FF:#B8D2FF>White colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
                        put("light_gray", "<gradient:#B5B9BD:#9DA2A6>Light gray colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
                        put("gray", "<gradient:#4A5054:#3A3F42>Gray colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
                        put("black", "<gradient:#17191A:#0D0E0F>Black colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
                        put("brown", "<gradient:#7A5A40:#634A34>Brown colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
                        put("red", "<gradient:#D63E3E:#C12B2B>Red colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
                        put("orange", "<gradient:#FF8C00:#E67E00>Orange colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
                        put("yellow", "<gradient:#FFE83D:#FFD900>Yellow colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
                        put("lime", "<gradient:#8EE53F:#7ACC29>Lime colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
                        put("green", "<gradient:#4BB54B:#3AA33A>Green colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
                        put("cyan", "<gradient:#3ECFDF:#2AB7C9>Cyan colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
                        put("light_blue", "<gradient:#6BB6FF:#4DA6FF>Light blue colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
                        put("blue", "<gradient:#3A75FF:#1F5AFF>Blue colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
                        put("purple", "<gradient:#A368C7:#8A4DBF>Purple colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
                        put("magenta", "<gradient:#FF5CD9:#FF3DCF>Magenta colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
                        put("pink", "<gradient:#FF80B7:#FF66A6>Pink colors<br><fcolor:1>(local) <fcolor:2><player><fcolor:3>: hello world! <br><fcolor:1>(global) <fcolor:2><player><fcolor:4>: hello world!");
                    }
                });

                @Getter
                @NoArgsConstructor
                @AllArgsConstructor
                public static class SubMenu {
                    private String item;
                    private String inventory;
                    private Map<String, String> types;
                }

            }
        }

        @Getter
        public static final class Clearchat implements SubCommandConfig, Localizable {
            private String nullPlayer = "<color:#ff7171><b>⁉</b> This player does not exist";
            private String format = "<fcolor:1>💬 Chat is cleared";
        }

        @Getter
        public static final class Clearmail implements SubCommandConfig, Localizable {
            private String nullMail = "<color:#ff7171><b>⁉</b> This mail does not exist";
            private String format = "<fcolor:2>✉ [REMOVED] Mail #<id> for <target> » <fcolor:1><message>";
        }

        @Getter
        public static final class Coin implements SubCommandConfig, Localizable {
            private String head = "heads";
            private String tail = "tails";
            private String format = "<fcolor:1>✎ <display_name> player got <result>";
            private String formatDraw = "<fcolor:1>✎ <display_name> player got edge :)";
        }

        @Getter
        public static final class Deletemessage implements SubCommandConfig, Localizable {
            private String nullMessage = "<color:#ff7171><b>⁉</b> This message does not exist";
            private String format = "<color:#98FB98>☒ Successfully deleted message";
        }

        @Getter
        public static final class Dice implements SubCommandConfig, Localizable {
            private Map<Integer, String> symbols = new LinkedHashMap<>(){
                {
                    put(1, "⚀");
                    put(2, "⚁");
                    put(3, "⚂");
                    put(4, "⚃");
                    put(5, "⚄");
                    put(6, "⚅");
                }
            };
            private String format = "<fcolor:1>✎ <display_name> roll <message><reset> <fcolor:1>(<sum>)";
        }

        @Getter
        public static final class Do implements SubCommandConfig, Localizable {
            private String format = "<fcolor:1>✎ <message><reset> <fcolor:1>(<i><display_name></i>)";
        }

        @Getter
        public static final class Flectonepulse implements SubCommandConfig, Localizable {
            private String nullHostEditor = "<color:#ff7171><b>⁉</b> The host parameter cannot be empty and must be configured in <u>config.yml";
            private String formatFalse = "<color:#ff7171>★ An has error occurred while reloading <br>Error: <message>";
            private String formatTrue = "<fcolor:2>★ <u>FlectonePulse</u> successfully reloaded! (<i><time></i>)";
            private String formatWebStarting = "<fcolor:2>★ Web server starting, please wait...";
            private String formatEditor = "<fcolor:2>★ Link for web editing <u><fcolor:2><click:open_url:\"<url>\"><hover:show_text:\"<fcolor:2><url>\"><url>";
        }

        @Getter
        public static final class Geolocate implements SubCommandConfig, Localizable {
            private String nullPlayer = "<color:#ff7171><b>⁉</b> This player does not exist";
            private String nullOrError = "<color:#ff7171><b>⁉</b> Problem receiving information, try again";
            private String format = "<fcolor:1>Geolocation for <display_name><br>Country: <fcolor:2><country><br><fcolor:1>Region: <fcolor:2><region_name><br><fcolor:1>City: <fcolor:2><city><br><fcolor:1>Timezone: <fcolor:2><timezone><br><fcolor:1>Mobile connection: <fcolor:2><mobile><br><fcolor:1>VPN: <fcolor:2><proxy><br><fcolor:1>Hosting: <fcolor:2><hosting><br><fcolor:1>IP: <fcolor:2><query>";
        }

        @Getter
        public static final class Helper implements SubCommandConfig, Localizable {
            private String nullHelper = "<color:#ff7171><b>⁉</b> There are no people who can help you";
            private String global = "<fcolor:2>👤 <display_name> needs help ⏩ <fcolor:1><message>";
            private String player = "<fcolor:2>👤 Request sent, awaiting reply";
        }

        @Getter
        public static final class Ignore implements SubCommandConfig, Localizable {
            private String nullPlayer = "<color:#ff7171><b>⁉</b> This player does not exist";
            private String myself = "<color:#ff7171><b>⁉</b> You can't ignore yourself";
            private String he = "<color:#ff7171><b>⁉</b> You can't write to him because he ignore you";
            private String you = "<color:#ff7171><b>⁉</b> You can't write to him because you ignore him";
            private String formatTrue = "<color:#ff7171>☹ You ignore <display_name>";
            private String formatFalse = "<color:#98FB98>☺ You unignore <display_name>";
        }

        @Getter
        public static final class Ignorelist implements SubCommandConfig, Localizable {
            private String empty = "<color:#98FB98>☺ You don't ignore anyone";
            private String nullPage = "<color:#ff7171><b>⁉</b> This page doesn't exist";
            private String header = "<fcolor:2>▋ Ignores: <count><br>";
            private String line = "<hover:show_text:\"<fcolor:1>Click to unignore <display_name>\"><click:run_command:\"<command>\"><color:#ff7171>☒ <display_name></click></hover> <fcolor:1>Date: <date>";
            private String footer = "<br>▋ <fcolor:2><click:run_command:\"<command> <prev_page>\">←</click> <fcolor:1>Page: <current_page>/<last_page> <fcolor:2><click:run_command:\"<command> <next_page>\">→";
        }

        @Getter
        public static final class Kick implements SubCommandConfig, Localizable {
            private String nullPlayer = "<color:#ff7171><b>⁉</b> This player does not exist";
            private ReasonMap reasons = new ReasonMap(){
                {
                    put("default", "Kicked by an operator");
                }
            };

            private String server = "<color:#ff7171>🔒 <fcolor:2><moderator> <fcolor:1>kicked <fcolor:2><player> <fcolor:1><hover:show_text:\"<fcolor:1>ID: <id><br>Date: <date><br>Moderator: <moderator><br>Reason: <reason>\">[INFO]</hover>";
            private String person = "<color:#ff7171>🔒 KICK 🔒 <fcolor:1><br><br>ID: <id><br><br>Date: <date><br><br>Moderator: <moderator><br><br>Reason: <reason>";
        }

        @Getter
        public static final class Mail implements SubCommandConfig, Localizable {
            private String nullPlayer = "<color:#ff7171><b>⁉</b> This player does not exist";
            private String onlinePlayer = "<color:#ff7171><b>⁉</b> This player is online";
            private String sender = "<fcolor:2>✉ Mail #<id> for <target> » <fcolor:1><message>";
            private String receiver = "<fcolor:2>✉ Mail from <display_name> » <fcolor:1><message>";
        }

        @Getter
        public static final class Maintenance implements SubCommandConfig, Localizable {
            private String serverDescription = "<color:#ff7171>The server is under maintenance";
            private String serverVersion = "Maintenance";
            private String kick = "<color:#ff7171>★ The server is under maintenance";
            private String formatTrue = "<fcolor:1>★ You have <fcolor:2>enabled <fcolor:1>maintenance on the server";
            private String formatFalse = "<fcolor:1>★ You have <fcolor:2>disabled <fcolor:1>maintenance on the server";
        }

        @Getter
        public static final class Me implements SubCommandConfig, Localizable {
            private String format = "<fcolor:1>✎ <display_name> <fcolor:1><message>";
        }

        @Getter
        public static final class Mute implements SubCommandConfig, Localizable {
            private String nullPlayer = "<color:#ff7171><b>⁉</b> This player does not exist";
            private String nullTime = "<color:#ff7171><b>⁉</b> Incorrect time";
            private ReasonMap reasons = new ReasonMap(){
                {
                    put("default", "You have been muted on this server");
                }
            };
            private String server = "<color:#ff7171>🔒 <fcolor:2><moderator> <fcolor:1>has muted <fcolor:2><player> <fcolor:1><hover:show_text:\"<fcolor:1>ID: <id><br>Date: <date><br>Time: <time><br>Remaining time: <time_left><br>Moderator: <moderator><br>Reason: <reason>\">[INFO]</hover>";
            private String person = "<color:#ff7171>🔒 You are muted, <time_left> left";
        }

        @Getter
        public static final class Mutelist implements SubCommandConfig, Localizable {
            private String empty = "<color:#98FB98>☺ No mutes found";
            private String nullPage = "<color:#ff7171><b>⁉</b> This page doesn't exist";
            private String nullPlayer = "<color:#ff7171><b>⁉</b> This player does not exist";
            private ListTypeMessage global = new ListTypeMessage(
                    "<fcolor:2>▋ Mutes: <count> <br>",
                    "<hover:show_text:\"<fcolor:1>Click to unmute <display_name>\"><click:run_command:\"<command>\"><color:#ff7171>☒ <display_name></click></hover> <fcolor:1><hover:show_text:\"<fcolor:1>ID: <id><br>Date: <date><br>Time: <time><br>Moderator: <moderator><br>Reason: <reason>\">[MORE]</hover>",
                    "<br><fcolor:2>▋ <click:run_command:\"<command> <prev_page>\">←</click> <fcolor:1>Page: <current_page>/<last_page> <fcolor:2><click:run_command:\"<command> <next_page>\">→"
            );

            private ListTypeMessage player = new ListTypeMessage(
                    "<fcolor:2>▋ All mutes: <count> <br>",
                    "<hover:show_text:\"<fcolor:1>Click to unmute <display_name>\"><click:run_command:\"<command>\"><color:#ff7171>☒ <display_name></click></hover> <fcolor:1><hover:show_text:\"<fcolor:1>ID: <id><br>Date: <date><br>Time: <time><br>Moderator: <moderator><br>Reason: <reason>\">[MORE]</hover>",
                    "<br><fcolor:2>▋ <click:run_command:\"<command> <prev_page>\">←</click> <fcolor:1>Page: <current_page>/<last_page> <fcolor:2><click:run_command:\"<command> <next_page>\">→"
            );
        }

        @Getter
        public static final class Online implements SubCommandConfig, Localizable {
            private String nullPlayer = "<color:#ff7171><b>⁉</b> This player does not exist";
            private String formatCurrent = "<fcolor:1>⌛ <display_name> currently on server";
            private String formatFirst = "<fcolor:1>⌛ <display_name> was first on server <time> ago";
            private String formatLast = "<fcolor:1>⌛ <display_name> <fcolor:1>was last on server <time> ago";
            private String formatTotal = "<fcolor:1>⌛ <display_name> <fcolor:1>has spent a total of <time> on server";
        }

        @Getter
        public static final class Ping implements SubCommandConfig, Localizable {
            private String nullPlayer = "<color:#ff7171><b>⁉</b> This player does not exist";
            private String format = "<fcolor:1>🖧 <display_name>'s ping is <ping>";
        }

        @Getter
        public static final class Poll implements SubCommandConfig, Localizable {
            private String nullPoll = "<color:#ff7171><b>⁉</b> Poll not found";
            private String expired = "<color:#ff7171><b>⁉</b> The poll has ended";
            private String already = "<color:#ff7171><b>⁉</b> You have already voted in this poll";
            private String voteTrue = "<color:#4eff52>👍 You voted for option <answer_id> in poll #<id>. There are <count> of you";
            private String voteFalse = "<color:#ff4e4e>🖓 You rejected option <answer_id> in poll #<id>. There are <count> without you";
            private String format = "<br><color:#fce303>│ <status> <br>│ <message><reset> <color:#fce303><br>├─────────────<br><answers>";
            private String answerTemplate = "<color:#fce303>│ <count> → <color:#4eff52><hover:show_text:\"<color:#4eff52>Vote for <bold><answer>\"><click:run_command:\"/pollvote <id> <number>\"><answer> [👍]<br>";
            private Status status = new Status();
            private Modern modern = new Modern();

            @Getter
            public static final class Status {
                private String start = "New poll #<b><id></b> has been created";
                private String run = "Poll #<b><id></b> is in progress";
                private String end = "Poll #<b><id></b> has ended";
            }

            @Getter
            public static final class Modern {
                private String header = "Poll";
                private String inputName = "Name";
                private String inputInitial = "";
                private String multipleName = "Allow multiple answers";
                private String endTimeName = "Duration (min)";
                private String repeatTimeName = "Interval (min)";
                private String newAnswerButtonName = "Add answer";
                private String removeAnswerButtonName = "Remove answer";
                private String inputAnswerName = "Answer <number>";
                private String inputAnswersInitial = "";
                private String createButtonName = "Create poll";
            }
        }

        @Getter
        public static final class Reply implements SubCommandConfig, Localizable {
            private String nullReceiver = "<color:#ff7171><b>⁉</b> No one to answer";
        }

        @Getter
        public static final class Rockpaperscissors implements SubCommandConfig, Localizable {
            private String nullPlayer = "<color:#ff7171><b>⁉</b> This player does not exist";
            private String nullGame = "<color:#ff7171><b>⁉</b> This game does not exist";
            private String wrongMove = "<color:#ff7171><b>⁉</b> This move is not possible";
            private String already = "<color:#ff7171><b>⁉</b> You've already made your move";
            private String myself = "<color:#ff7171><b>⁉</b> You can't play with yourself";
            private String sender = "<fcolor:1>Now goes <target>";
            private String receiver = "<fcolor:2>✂ <display_name> <fcolor:1>suggested a game of rock-paper-scissors";
            private String formatMove = "<fcolor:2>✂ <fcolor:1>Choose your move <fcolor:2><click:run_command:\"/rps <target> rock <uuid>\">[🪨 rock]</click> <click:run_command:\"/rps <target> scissors <uuid>\">[✂ scissors]</click> <click:run_command:\"/rps <target> paper <uuid>\">[🧻 paper]</click>";
            private String formatWin = "<color:#98FB98>✂ Winning <display_name>! <b><sender_move></b> on <b><receiver_move></b>";
            private String formatDraw = "<color:#98FB98>✂ It's a draw! You both chose <b><move>";
            private Map<String, String> strategies = new LinkedHashMap<>(){
                {
                    put("paper", "paper");
                    put("rock", "rock");
                    put("scissors", "scissors");
                }
            };
        }

        @Getter
        public static final class Spy implements SubCommandConfig, Localizable {
            private String formatTrue = "<fcolor:1>[👁] You <color:#98FB98>turned on <fcolor:1>spy mode";
            private String formatFalse = "<fcolor:1>[👁] You <color:#F08080>turned off <fcolor:1>spy mode";
            private String formatLog = "<fcolor:1>[👁] <display_name> <color:#98FB98><action> <fcolor:1>→ <fcolor:2><message>";
        }

        @Getter
        public static final class Stream implements SubCommandConfig, Localizable {
            private String already = "<color:#ff7171><b>⁉</b> You are already streaming";
            private String not = "<color:#ff7171><b>⁉</b> You don't stream";
            private String prefixTrue = "<color:#ff4e4e>⏻</color:#ff4e4e> ";
            private String prefixFalse = "";
            private String urlTemplate = "<color:#ff4e4e>│ <fcolor:2><click:open_url:\"<url>\"><hover:show_text:\"<fcolor:2><url>\"><url></hover></click>";
            private String formatStart = "<br><color:#ff4e4e>│ 🔔 <fcolor:1>Announcement <br><color:#ff4e4e>│<br><color:#ff4e4e>│ <fcolor:1><display_name> started streaming<br><color:#ff4e4e>│<br><urls><br>";
            private String formatEnd = "<fcolor:2>★ Thanks for streaming on our server!";
        }

        @Getter
        public static final class Symbol implements SubCommandConfig, Localizable {
            private String format = "<click:suggest_command:\"<message>\"><fcolor:2>🖥 Click for using: <fcolor:1><message>";
        }

        @Getter
        public static final class Tell implements SubCommandConfig, Localizable {
            private String nullPlayer = "<color:#ff7171><b>⁉</b> This player does not exist";
            private String sender = "<fcolor:2>✉ <display_name> → <target> » <fcolor:1><message>";
            private String receiver = "<fcolor:2>✉ <display_name> → <target> » <fcolor:1><message>";
            private String myself = "<fcolor:2>✉ [Note] <fcolor:1><message>";
        }

        @Getter
        public static final class Tictactoe implements SubCommandConfig, Localizable {
            private String nullPlayer = "<color:#ff7171><b>⁉</b> This player does not exist";
            private String myself = "<color:#ff7171><b>⁉</b> You can't play with yourself";
            private String wrongGame = "<color:#ff7171><b>⁉</b> This game does not exist";
            private String wrongMove = "<color:#ff7171><b>⁉</b> This step is not possible";
            private String wrongByPlayer = "<color:#ff7171><b>⁉</b> This game ended because player quit";
            private Symbol symbol = new Symbol();

            @Getter
            @NoArgsConstructor
            public static final class Symbol {
                private String blank = "<fcolor:1><hover:show_text:\"<fcolor:1>Move <move>\"><click:run_command:\"/tictactoemove %d <move>\">☐</click></hover>";
                private String first = "<fcolor:2>☑";
                private String firstRemove = "<color:#ff7171>☑</color:#ff7171>";
                private String firstWin = "<color:#98FB98>☑</color:#98FB98>";
                private String second = "<fcolor:2>☒";
                private String secondRemove = "<color:#ff7171>☒</color:#ff7171>";
                private String secondWin = "<color:#98FB98>☒</color:#98FB98>";
            }

            private String field = "<fcolor:1><br>|[#][#][#]<fcolor:1>| <title> <current_move> <br><fcolor:1>|[#][#][#]<fcolor:1>| <br>|[#][#][#]<fcolor:1>| <last_move><br>";
            private String currentMove = "<fcolor:2>☐ → <symbol>";
            private String lastMove = "<fcolor:2>Last move (<move>)";
            private String formatMove = "<fcolor:2><target>'s move";
            private String formatWin = "<color:#98FB98><target> won this game</color:#98FB98>";
            private String formatDraw = "<color:#98FB98>The game ended in a draw 👬</color:#98FB98>";
            private String sender = "<fcolor:1>☐ An offer to play was sent to <target>";
            private String receiver = "<click:run_command:\"/tictactoemove %d create\"><fcolor:1>☐ Received an invite to play tic-tac-toe with <display_name>, accept? [+]";
        }

        @Getter
        public static final class Toponline implements SubCommandConfig, Localizable {
            private String nullPage = "<color:#ff7171><b>⁉</b> This page doesn't exist";
            private String header = "<fcolor:2>▋ Players: <count> <br>";
            private String line = "<fcolor:2><time_player> <fcolor:1>played for <fcolor:2><time>";
            private String footer = "<br><fcolor:2>▋ <click:run_command:\"<command> <prev_page>\">←</click> <fcolor:1>Page: <current_page>/<last_page> <fcolor:2><click:run_command:\"<command> <next_page>\">→";
        }

        @Getter
        public static final class Translateto implements SubCommandConfig, Localizable {
            private String nullOrError = "<color:#ff7171><b>⁉</b> Error, you may have specified an unsupported language";
            private String format = "<fcolor:1>📖 Translation to [<language>] → <fcolor:2><message>";
        }

        @Getter
        public static final class Try implements SubCommandConfig, Localizable {
            private String formatTrue = "<color:#98FB98>☺ <display_name> <message><reset> <color:#98FB98><percent>%";
            private String formatFalse = "<color:#F08080>☹ <display_name> <message><reset> <color:#F08080><percent>%";
        }

        @Getter
        public static final class Unban implements SubCommandConfig, Localizable {
            private String nullPlayer = "<color:#ff7171><b>⁉</b> This player does not exist";
            private String notBanned = "<color:#ff7171><b>⁉</b> This player is not banned";
            private String format = "<color:#98FB98>\uD83D\uDD13 <fcolor:2><moderator> <color:#98FB98>unbanned the player <fcolor:2><player>";
        }

        @Getter
        public static final class Unmute implements SubCommandConfig, Localizable {
            private String nullPlayer = "<color:#ff7171><b>⁉</b> This player does not exist";
            private String notMuted = "<color:#ff7171><b>⁉</b> This player is not muted";
            private String format = "<color:#98FB98>\uD83D\uDD13 <fcolor:2><moderator> <color:#98FB98>unmutted the player <fcolor:2><player>";
        }

        @Getter
        public static final class Unwarn implements SubCommandConfig, Localizable {
            private String nullPlayer = "<color:#ff7171><b>⁉</b> This player does not exist";
            private String notWarned = "<color:#ff7171><b>⁉</b> This player is not warned";
            private String format = "<color:#98FB98>\uD83D\uDD13 <fcolor:2><moderator> <color:#98FB98>unwarned the player <fcolor:2><player>";
        }

        @Getter
        public static final class Warn implements SubCommandConfig, Localizable {
            private String nullPlayer = "<color:#ff7171><b>⁉</b> This player does not exist";
            private String nullTime = "<color:#ff7171><b>⁉</b> Incorrect time";
            private ReasonMap reasons = new ReasonMap(){
                {
                    put("default", "You have been warned on this server");
                }
            };

            private String server = "<color:#ff7171>🔒 <fcolor:2><moderator> <fcolor:1>gave a warning to <fcolor:2><player> <fcolor:1><hover:show_text:\"<fcolor:1>ID: <id><br>Date: <date><br>Time: <time><br>Remaining time: <time_left><br>Moderator: <moderator><br>Reason: <reason>\">[INFO]</hover>";
            private String person = "<color:#ff7171>🔒 You are warned a <time>";
        }

        @Getter
        public static final class Warnlist implements SubCommandConfig, Localizable {
            private String empty = "<color:#98FB98>☺ No warns found";
            private String nullPage = "<color:#ff7171><b>⁉</b> This page doesn't exist";
            private String nullPlayer = "<color:#ff7171><b>⁉</b> This player does not exist";
            private ListTypeMessage global = new ListTypeMessage(
                    "<fcolor:2>▋ Warns: <count> <br>",
                    "<hover:show_text:\"<fcolor:1>Click to unwarn <display_name>\"><click:run_command:\"<command>\"><color:#ff7171>☒ <display_name></click></hover> <fcolor:1><hover:show_text:\"<fcolor:1>ID: <id><br>Date: <date><br>Time: <time><br>Moderator: <moderator><br>Reason: <reason>\">[INFO]</hover>",
                    "<br><fcolor:2>▋ <click:run_command:\"<command> <prev_page>\">←</click> <fcolor:1>Page: <current_page>/<last_page> <fcolor:2><click:run_command:\"<command> <next_page>\">→"
            );

            private ListTypeMessage player = new ListTypeMessage(
                    "<fcolor:2>▋ All warns: <count> <br>",
                    "<hover:show_text:\"<fcolor:1>Click to unwarn <display_name>\"><click:run_command:\"<command>\"><color:#ff7171>☒ <display_name></click></hover> <fcolor:1><hover:show_text:\"<fcolor:1>ID: <id><br>Date: <date><br>Time: <time><br>Moderator: <moderator><br>Reason: <reason>\">[INFO]</hover>",
                    "<br><fcolor:2>▋ <click:run_command:\"<command> <prev_page>\">←</click> <fcolor:1>Page: <current_page>/<last_page> <fcolor:2><click:run_command:\"<command> <next_page>\">→"
            );
        }
    }

    @Getter
    public static final class Integration implements IntegrationConfig, Localizable {

        @Comment({@CommentValue(" https://flectone.net/pulse/docs/integration/discord/")})
        private Discord discord = new Discord();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/integration/telegram/")})
        private Telegram telegram = new Telegram();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/integration/twitch/")})
        private Twitch twitch = new Twitch();

        @Override
        public SubIntegrationConfig getAdvancedban() {
            return null;
        }

        @Override
        public SubIntegrationConfig getDeepl() {
            return null;
        }

        @Override
        public SubIntegrationConfig getInteractivechat() {
            return null;
        }

        @Override
        public SubIntegrationConfig getItemsadder() {
            return null;
        }

        @Override
        public SubIntegrationConfig getLitebans() {
            return null;
        }

        @Override
        public SubIntegrationConfig getLuckperms() {
            return null;
        }

        @Override
        public SubIntegrationConfig getMaintenance() {
            return null;
        }

        @Override
        public SubIntegrationConfig getMinimotd() {
            return null;
        }

        @Override
        public SubIntegrationConfig getMiniplaceholders() {
            return null;
        }

        @Override
        public SubIntegrationConfig getMotd() {
            return null;
        }

        @Override
        public SubIntegrationConfig getPlaceholderapi() {
            return null;
        }

        @Override
        public SubIntegrationConfig getPlasmovoice() {
            return null;
        }

        @Override
        public SubIntegrationConfig getSimplevoice() {
            return null;
        }

        @Override
        public SubIntegrationConfig getSkinsrestorer() {
            return null;
        }

        @Override
        public SubIntegrationConfig getSupervanish() {
            return null;
        }

        @Override
        public SubIntegrationConfig getVault() {
            return null;
        }

        @Override
        public SubIntegrationConfig getYandex() {
            return null;
        }

        @Getter
        public static final class Discord implements SubIntegrationConfig, Localizable {
            private String forMinecraft = "<fcolor:2><name> <fcolor:1>» <fcolor:4><message>";
            private Map<String, String> infoChannel = new LinkedHashMap<>(){
                {
                    put("id", "TPS <tps>");
                }
            };
            private Map<String, ChannelEmbed> messageChannel = new LinkedHashMap<>(){
                {
                    put("CHAT_GLOBAL", new ChannelEmbed());
                }
            };

            @Getter
            @NoArgsConstructor
            public final static class ChannelEmbed {
                private String content = "<final_message>";
                private Webhook webhook = new Webhook(
                        false,
                        "https://mc-heads.net/avatar/<skin>/32.png",
                        ""
                );
                private Embed embed = new Embed(
                        false,
                        "",
                        "",
                        "",
                        new Embed.Author("", "", "https://mc-heads.net/avatar/<skin>/16.png"),
                        "",
                        "",
                        "",
                        true,
                        new Embed.Footer("", "https://mc-heads.net/avatar/<skin>/16.png")
                );
            }

            @Getter
            @AllArgsConstructor
            public static final class Webhook {
                private boolean enable;
                private String avatar;
                private String content;
            }

            @Getter
            public static final class Embed {
                private boolean enable;
                private String color;
                private String title;
                private String url;
                private Author author;
                private String description;
                private String thumbnail;
                private String image;
                private boolean timestamp;
                private Footer footer;

                public Embed(boolean enable, String color, String title, String url, Author author, String description, String thumbnail, String image, boolean timestamp, Footer footer) {
                    this.enable = enable;
                    this.color = color;
                    this.title = title;
                    this.url = url;
                    this.author = author;
                    this.description = description;
                    this.thumbnail = thumbnail;
                    this.image = image;
                    this.timestamp = timestamp;
                    this.footer = footer;
                }

                @Getter
                @AllArgsConstructor
                public static final class Author {
                    private String name;
                    private String url;
                    private String iconUrl;
                }

                @Getter
                @AllArgsConstructor
                public static final class Footer {
                    private String text;
                    private String iconUrl;
                }
            }
        }

        @Getter
        public static final class Telegram implements SubIntegrationConfig, Localizable {
            private String forMinecraft = "<fcolor:2><name> <fcolor:1>» <fcolor:4><message>";
            private Map<String, String> messageChannel = new LinkedHashMap<>(){
                {
                    put("CHAT_GLOBAL", "<final_message>");
                }
            };
        }

        @Override
        public SubIntegrationConfig getTriton() {
            return null;
        }

        @Getter
        public static final class Twitch implements SubIntegrationConfig, Localizable {
            private String forMinecraft = "<fcolor:2><name> <fcolor:1>» <fcolor:4><message>";
            private Map<String, String> messageChannel = new LinkedHashMap<>(){
                {
                    put("CHAT_GLOBAL", "<final_message>");
                }
            };
        }
    }

    @Getter
    @Setter
    public static final class Message implements MessageConfig, Localizable {

        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/advancement/")})
        private Advancement advancement = new Advancement();

        @Override
        public SubMessageConfig getAnvil() {
            return null;
        }

        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/afk/")})
        private Afk afk = new Afk();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/attribute/")})
        private Attribute attribute = new Attribute();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/auto/")})
        private Auto auto = new Auto();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/bed/")})
        private Bed bed = new Bed();

        @Override
        public SubMessageConfig getBook() {
            return null;
        }

        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/brand/")})
        private Brand brand = new Brand();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/bubble/")})
        private Bubble bubble = new Bubble();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/chat/")})
        private Chat chat = new Chat();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/clear/")})
        private Clear clear = new Clear();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/clone/")})
        private Clone clone = new Clone();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/commandblock/")})
        private Commandblock commandblock = new Commandblock();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/damage/")})
        private Damage damage = new Damage();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/death/")})
        private Death death = new Death();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/debugstick/")})
        private Debugstick debugstick = new Debugstick();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/deop/")})
        private Deop deop = new Deop();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/dialog/")})
        private Dialog dialog = new Dialog();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/difficulty/")})
        private Difficulty difficulty = new Difficulty();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/effect/")})
        private Effect effect = new Effect();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/enchant/")})
        private Enchant enchant = new Enchant();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/execute/")})
        private Execute execute = new Execute();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/experience/")})
        private Experience experience = new Experience();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/fill/")})
        private Fill fill = new Fill();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/fillbiome/")})
        private Fillbiome fillbiome = new Fillbiome();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/format/")})
        private Format format = new Format();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/gamemode/")})
        private Gamemode gamemode = new Gamemode();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/gamerule/")})
        private Gamerule gamerule = new Gamerule();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/give/")})
        private Give give = new Give();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/greeting/")})
        private Greeting greeting = new Greeting();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/join/")})
        private Join join = new Join();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/kill/")})
        private Kill kill = new Kill();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/locate/")})
        private Locate locate = new Locate();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/objective/")})
        private Objective objective = new Objective();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/op/")})
        private Op op = new Op();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/particle/")})
        private Particle particle = new Particle();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/quit/")})
        private Quit quit = new Quit();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/reload/")})
        private Reload reload = new Reload();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/ride/")})
        private Ride ride = new Ride();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/rightclick/")})
        private Rightclick rightclick = new Rightclick();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/rotate/")})
        private Rotate rotate = new Rotate();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/save/")})
        private Save save = new Save();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/seed/")})
        private Seed seed = new Seed();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/setblock/")})
        private Setblock setblock = new Setblock();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/sidebar/")})
        private Sidebar sidebar = new Sidebar();

        @Override
        public SubMessageConfig getSign() {
            return null;
        }

        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/sleep/")})
        private Sleep sleep = new Sleep();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/spawn/")})
        private Spawn spawn = new Spawn();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/status/")})
        private Status status = new Status();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/stop/")})
        private Stop stop = new Stop();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/summon/")})
        private Summon summon = new Summon();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/tab/")})
        private Tab tab = new Tab();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/teleport/")})
        private Teleport teleport = new Teleport();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/time/")})
        private Time time = new Time();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/update/")})
        private Update update = new Update();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/weather/")})
        private Weather weather = new Weather();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/worldborder/")})
        private Worldborder worldborder = new Worldborder();

        @Getter
        public static final class Advancement implements SubMessageConfig, Localizable {

            private String task = "<fcolor:1>🌠 <display_name> has made the advancement <advancement>";
            private String goal = "<fcolor:1>🌠 <display_name> has reached the goal <advancement>";
            private String challenge = "<fcolor:1>🌠 <display_name> has completed the challenge <advancement>";

            private String taken = "<fcolor:1>🌠 <display_name> has lost the achievement <advancement>";

            private Tag tag = new Tag(
                    "<color:#4eff52>[<hover:show_text:\"<color:#4eff52><advancement>\"><advancement></hover>]",
                    "<color:#834eff>[<hover:show_text:\"<color:#834eff><advancement>\"><advancement></hover>]"
            );

            private Command revoke = new Command(
                    "<fcolor:1>🌠 Revoked <fcolor:2><advancements> <fcolor:1>advancements from <target>",
                    "<fcolor:1>🌠 Revoked the advancement <advancement> from <fcolor:2><players> <fcolor:1>players",
                    "<fcolor:1>🌠 Revoked <fcolor:2><advancements> <fcolor:1>advancements from <fcolor:2><players> <fcolor:1>players",
                    "<fcolor:1>🌠 Revoked the advancement <advancement> <fcolor:1>from <target>",
                    "<fcolor:1>🌠 Revoked criterion '<fcolor:2><criterion><fcolor:1>' of advancement <advancement> from <fcolor:2><players> <fcolor:1>players",
                    "<fcolor:1>🌠 Revoked criterion '<fcolor:2><criterion><fcolor:1>' of advancement <advancement> from <target>"
            );

            private Command grant = new Command(
                    "<fcolor:1>🌠 Granted <fcolor:2><advancements> <fcolor:1>advancements to <target>",
                    "<fcolor:1>🌠 Granted the advancement <advancement> to <fcolor:2><players> <fcolor:1>players",
                    "<fcolor:1>🌠 Granted <fcolor:2><advancements> <fcolor:1>advancements to <fcolor:2><players> <fcolor:1>players",
                    "<fcolor:1>🌠 Granted the advancement <advancement> to <target>",
                    "<fcolor:1>🌠 Granted criterion '<fcolor:2><criterion><fcolor:1>' of advancement <advancement> to <fcolor:2><players> <fcolor:1>players",
                    "<fcolor:1>🌠 Granted criterion '<fcolor:2><criterion><fcolor:1>' of advancement <advancement> to <target>"
            );

            @Getter
            @AllArgsConstructor
            public static final class Tag {
                private String task;
                private String challenge;
            }

            @Getter
            @AllArgsConstructor
            public static final class Command {
                private String manyToOne;
                private String oneToMany;
                private String manyToMany;
                private String oneToOne;
                private String criterionToMany;
                private String criterionToOne;
            }
        }

        @Getter
        public static final class Afk implements SubMessageConfig, Localizable {
            private String suffix = " <color:#FFFF00>⌚</color>";
            private Format formatTrue = new Format(
                    "<gradient:#ffd500:#FFFF00>⌚ <player> is now afk",
                    "<gradient:#ffd500:#FFFF00>⌚ Now you're afk"
            );
            private Format formatFalse = new Format(
                    "<gradient:#ffd500:#FFFF00>⌚ <player> isn't afk now",
                    "<gradient:#ffd500:#FFFF00>⌚ Now you're not afk"
            );

            @Getter
            @AllArgsConstructor
            public static final class Format {
                private String global;
                private String local;
            }
        }

        @Getter
        public static final class Attribute implements SubMessageConfig, Localizable {
            private BaseValue baseValue = new BaseValue(
                    "<fcolor:1>❤ Base value of attribute <fcolor:2><lang:'<attribute>'> <fcolor:1>for entity <target> is <fcolor:2><value>",
                    "<fcolor:1>❤ Base value for attribute <fcolor:2><lang:'<attribute>'> <fcolor:1>for entity <target> reset to default <fcolor:2><value>",
                    "<fcolor:1>❤ Base value for attribute <fcolor:2><lang:'<attribute>'> <fcolor:1>for entity <target> set to <fcolor:2><value>"
            );
            private Modifier modifier = new Modifier(
                    "<fcolor:1>❤ Added modifier <fcolor:2><modifier> <fcolor:1>to attribute <fcolor:2><lang:'<attribute>'> <fcolor:1>for entity <target>",
                    "<fcolor:1>❤ Removed modifier <fcolor:2><modifier> <fcolor:1>from attribute <fcolor:2><lang:'<attribute>'> <fcolor:1>for entity <target>",
                    "<fcolor:1>❤ Value of modifier <fcolor:2><modifier> <fcolor:1>on attribute <fcolor:2><lang:'<attribute>'> <fcolor:1>for entity <target> is <fcolor:2><value>"
            );
            private String valueGet = "<fcolor:1>❤ Value of attribute <fcolor:2><lang:'<attribute>'> <fcolor:1>for entity <target> is <fcolor:2><value>";

            @Getter
            @AllArgsConstructor
            @NoArgsConstructor
            public static final class BaseValue {
                private String get = "";
                private String reset = "";
                private String set = "";
            }

            @Getter
            @AllArgsConstructor
            @NoArgsConstructor
            public static final class Modifier {
                private String add = "";
                private String remove = "";
                private String valueGet = "";
            }
        }

        @Getter
        public static final class Auto implements SubMessageConfig, Localizable {
            private Map<String, List<String>> types = new LinkedHashMap<>(){
                {
                    put("announcement", new LinkedList<>(){
                        {
                            push("<br><fcolor:1>◇ This server uses <click:open_url:\"https://flectone.net/pulse/\"><hover:show_text:\"<fcolor:2>https://flectone.net/pulse/\"><fcolor:2>FlectonePulse</hover></click> :)<br>");
                            push("<br><fcolor:1>      ❝ Join our discord ❠ <br><fcolor:2>   <u><click:open_url:\"https://discord.flectone.net\"><hover:show_text:\"<fcolor:2>https://discord.flectone.net\">https://discord.flectone.net</hover></click></u><br>");
                            push("<br><fcolor:1>⚡ Support <fcolor:2>FlectonePulse <fcolor:1>on Boosty <br><fcolor:1>⚡ <u><click:open_url:\"https://boosty.to/thefaser/\"><hover:show_text:\"<fcolor:2>https://boosty.to/thefaser/\">https://boosty.to/thefaser/</hover></click></u><br>");
                            push("<br><fcolor:1>      ✉ Join our telegram ✉ <br><fcolor:2>    <u><click:open_url:\"https://t.me/flectone\"><hover:show_text:\"<fcolor:2>https://t.me/flectone\">https://t.me/flectone</hover></click></u><br>");
                        }
                    });
                }
            };
        }

        @Getter
        public static final class Bed implements SubMessageConfig, Localizable {
            private String noSleep = "<fcolor:1>\uD83D\uDECC You can sleep only at night or during thunderstorms";
            private String notSafe = "<fcolor:1>\uD83D\uDECC You may not rest now; there are monsters nearby";
            private String obstructed = "<fcolor:1>\uD83D\uDECC This bed is obstructed";
            private String occupied = "<fcolor:1>\uD83D\uDECC This bed is occupied";
            private String tooFarAway = "<fcolor:1>\uD83D\uDECC You may not rest now; the bed is too far away";
        }

        @Getter
        public static final class Brand implements SubMessageConfig, Localizable {
            private List<String> values = new LinkedList<>(){
                {
                    push("<white>Minecraft");
                    push("<aqua>Minecraft");
                }
            };
        }

        @Getter
        public static final class Bubble implements SubMessageConfig, Localizable {
            private String format = "<fcolor:3><message>";
        }

        @Getter
        public static final class Chat implements SubMessageConfig, Localizable {
            private String nullChat = "<color:#ff7171><b>⁉</b> Chat is disabled on this server";
            private String nullReceiver = "<color:#ff7171><b>⁉</b> Nobody heard you";
            private Map<String, String> types = new LinkedHashMap<>(){
                {
                    put("global", "<delete><display_name> <world_prefix>»<fcolor:4> <message><reset><translate>");
                    put("local", "<delete><display_name><fcolor:3>: <message><reset><translate>");
                }
            };
        }

        @Getter
        public static final class Clear implements SubMessageConfig, Localizable {
            private String single = "<fcolor:1>🌊 Removed <fcolor:2><items> <fcolor:1>item(s) from player <target>";
            private String multiple = "<fcolor:1>🌊 Removed <fcolor:2><items> <fcolor:1>item(s) from <fcolor:2><players> <fcolor:1>players";
        }

        @Getter
        public static final class Clone implements SubMessageConfig, Localizable {
            private String format = "<fcolor:1>⏹ Successfully cloned <fcolor:2><blocks> <fcolor:1>block(s)";
        }

        @Getter
        public static final class Commandblock implements SubMessageConfig, Localizable {
            private String notEnabled = "<fcolor:1>\uD83E\uDD16 Command blocks are not enabled on this server";
            private String format = "<fcolor:1>\uD83E\uDD16 Command set: <fcolor:2><command>";
        }

        @Getter
        public static final class Damage implements SubMessageConfig, Localizable {
            private String format = "<fcolor:1>\uD83D\uDDE1 Applied <fcolor:2><amount> <fcolor:1>damage to <target>";
        }

        @Getter
        public static final class Death implements SubMessageConfig, Localizable {
            private Map<String, String> types = new LinkedHashMap<>(){
                {
                    put("death.attack.anvil", "<fcolor:1>☠ <target> was squashed by a falling anvil");
                    put("death.attack.anvil.player", "<fcolor:1>☠ <target> was squashed by a falling anvil while fighting <killer>");
                    put("death.attack.arrow", "<fcolor:1>☠ <fcolor:1><target> was shot by <killer>");
                    put("death.attack.arrow.item", "<fcolor:1>☠ <target> was shot by <killer> using <fcolor:2>[<killer_item>]<fcolor:1>");
                    put("death.attack.badRespawnPoint.message", "<fcolor:1>☠ <target> was killed by <fcolor:2>[<click:open_url:\"https://www.youtube.com/watch?v=dQw4w9WgXcQ\"><hover:show_text:\"<fcolor:2>MCPE-28723\">Intentional Game Design</hover></click>]");
                    put("death.attack.cactus", "<fcolor:1>☠ <target> was pricked to death");
                    put("death.attack.cactus.player", "<fcolor:1>☠ <target> walked into a cactus while trying to escape <killer>");
                    put("death.attack.cramming", "<fcolor:1>☠ <target> was squished too much");
                    put("death.attack.cramming.player", "<fcolor:1>☠ <target> was squashed by <killer>");
                    put("death.attack.dragonBreath", "<fcolor:1>☠ <target> was roasted in dragon's breath");
                    put("death.attack.dragonBreath.player", "<fcolor:1>☠ <target> was roasted in dragon's breath by <killer>");
                    put("death.attack.drown", "<fcolor:1>☠ <target> drowned");
                    put("death.attack.drown.player", "<fcolor:1>☠ <target> drowned while trying to escape <killer>");
                    put("death.attack.dryout", "<fcolor:1>☠ <target> died from dehydration");
                    put("death.attack.dryout.player", "<fcolor:1>☠ <target> died from dehydration while trying to escape <killer>");
                    put("death.attack.even_more_magic", "<fcolor:1>☠ <target> was killed by even more magic");
                    put("death.attack.explosion", "<fcolor:1>☠ <target> blew up");
                    put("death.attack.explosion.player", "<fcolor:1>☠ <target> was blown up by <killer>");
                    put("death.attack.explosion.item", "<fcolor:1>☠ <target> was blown up by <killer> using <fcolor:2>[<killer_item>]<fcolor:1>");
                    put("death.attack.explosion.player.item", "<fcolor:1>☠ <target> was blown up by <killer> using <fcolor:2>[<killer_item>]<fcolor:1>");
                    put("death.attack.fall", "<fcolor:1>☠ <target> hit the ground too hard");
                    put("death.attack.fall.player", "<fcolor:1>☠ <target> hit the ground too hard while trying to escape <killer>");
                    put("death.attack.fallingBlock", "<fcolor:1>☠ <target> was squashed by a falling block");
                    put("death.attack.fallingBlock.player", "<fcolor:1>☠ <target> was squashed by a falling block while fighting <killer>");
                    put("death.attack.fallingStalactite", "<fcolor:1>☠ <target> was skewered by a falling stalactite");
                    put("death.attack.fallingStalactite.player", "<fcolor:1>☠ <target> was skewered by a falling stalactite while fighting <killer>");
                    put("death.attack.fireball", "<fcolor:1>☠ <target> was fireballed by <killer>");
                    put("death.attack.fireball.item", "<fcolor:1>☠ <target> was fireballed by <killer> using <fcolor:2>[<killer_item>]<fcolor:1>");
                    put("death.attack.fireworks", "<fcolor:1>☠ <target> went off with a bang");
                    put("death.attack.fireworks.item", "<fcolor:1>☠ <target> went off with a bang due to a firework fired from <fcolor:2>[<killer_item>]<fcolor:1> by <killer>");
                    put("death.attack.fireworks.player", "<fcolor:1>☠ <target> went off with a bang while fighting <killer>");
                    put("death.attack.flyIntoWall", "<fcolor:1>☠ <target> experienced kinetic energy");
                    put("death.attack.flyIntoWall.player", "<fcolor:1>☠ <target> experienced kinetic energy while trying to escape <killer>");
                    put("death.attack.freeze", "<fcolor:1>☠ <target> froze to death");
                    put("death.attack.freeze.player", "<fcolor:1>☠ <target> was frozen to death by <killer>");
                    put("death.attack.generic", "<fcolor:1>☠ <target> died");
                    put("death.attack.generic.player", "<fcolor:1>☠ <target> died because of <killer>");
                    put("death.attack.genericKill", "<fcolor:1>☠ <target> was killed");
                    put("death.attack.genericKill.player", "<fcolor:1>☠ <target> was killed while fighting <killer>");
                    put("death.attack.hotFloor", "<fcolor:1>☠ <target> discovered the floor was lava");
                    put("death.attack.hotFloor.player", "<fcolor:1>☠ <target> walked into the danger zone due to <killer>");
                    put("death.attack.indirectMagic", "<fcolor:1>☠ <target> was killed by <killer> using magic");
                    put("death.attack.indirectMagic.item", "<fcolor:1>☠ <target> was killed by <killer> using <fcolor:2>[<killer_item>]<fcolor:1>");
                    put("death.attack.inFire", "<fcolor:1>☠ <target> went up in flames");
                    put("death.attack.inFire.player", "<fcolor:1>☠ <target> walked into fire while fighting <killer>");
                    put("death.attack.inWall", "<fcolor:1>☠ <target> suffocated in a wall");
                    put("death.attack.inWall.player", "<fcolor:1>☠ <target> suffocated in a wall while fighting <killer>");
                    put("death.attack.lava", "<fcolor:1>☠ <target> tried to swim in lava");
                    put("death.attack.lava.player", "<fcolor:1>☠ <target> tried to swim in lava to escape <killer>");
                    put("death.attack.lightningBolt", "<fcolor:1>☠ <target> was struck by lightning");
                    put("death.attack.lightningBolt.player", "<fcolor:1☠ <target> was struck by lightning while fighting <killer>");
                    put("death.attack.mace_smash", "<fcolor:1>☠ <target> was smashed by <killer>");
                    put("death.attack.mace_smash.item", "<fcolor:1>☠ <target> was smashed by <killer> with <fcolor:2>[<killer_item>]<fcolor:1>");
                    put("death.attack.magic", "<fcolor:1>☠ <target> was killed by magic");
                    put("death.attack.magic.player", "<fcolor:1>☠ <target> was killed by magic while trying to escape <killer>");
                    put("death.attack.mob", "<fcolor:1>☠ <target> was slain by <killer>");
                    put("death.attack.mob.item", "<fcolor:1>☠ <target> was slain by <killer> using <fcolor:2>[<killer_item>]<fcolor:1>");
                    put("death.attack.onFire", "<fcolor:1>☠ <target> burned to death");
                    put("death.attack.onFire.item", "<fcolor:1>☠ <target> was burned to a crisp while fighting <killer> wielding <fcolor:2>[<killer_item>]<fcolor:1>");
                    put("death.attack.onFire.player", "<fcolor:1>☠ <target> was burned to a crisp while fighting <killer>");
                    put("death.attack.outOfWorld", "<fcolor:1>☠ <target> fell out of the world");
                    put("death.attack.outOfWorld.player", "<fcolor:1>☠ <target> didn't want to live in the same world as <killer>");
                    put("death.attack.outsideBorder", "<fcolor:1>☠ <target> left the confines of this world");
                    put("death.attack.outsideBorder.player", "<fcolor:1>☠ <target> left the confines of this world while fighting <killer>");
                    put("death.attack.player", "<fcolor:1>☠ <target> was slain by <killer>");
                    put("death.attack.player.item", "<fcolor:1>☠ <target> was slain by <killer> using <fcolor:2>[<killer_item>]<fcolor:1>");
                    put("death.attack.sonic_boom", "<fcolor:1>☠ <target> was obliterated by a sonically-charged shriek");
                    put("death.attack.sonic_boom.item", "<fcolor:1>☠ <target> was obliterated by a sonically-charged shriek while trying to escape <killer> wielding <fcolor:2>[<killer_item>]<fcolor:1>");
                    put("death.attack.sonic_boom.player", "<fcolor:1>☠ <target> was obliterated by a sonically-charged shriek while trying to escape <killer>");
                    put("death.attack.stalagmite", "<fcolor:1>☠ <target> was impaled on a stalagmite");
                    put("death.attack.stalagmite.player", "<fcolor:1>☠ <target> was impaled on a stalagmite while fighting <killer>");
                    put("death.attack.starve", "<fcolor:1>☠ <target> starved to death");
                    put("death.attack.starve.player", "<fcolor:1>☠ <target> starved to death while fighting <killer>");
                    put("death.attack.sting", "<fcolor:1>☠ <target> was stung to death");
                    put("death.attack.sting.item", "<fcolor:1>☠ <target> was stung to death by <killer> using <fcolor:2>[<killer_item>]<fcolor:1>");
                    put("death.attack.sting.player", "<fcolor:1>☠ <target> was stung to death by <killer>");
                    put("death.attack.sweetBerryBush", "<fcolor:1>☠ <target> was poked to death by a sweet berry bush");
                    put("death.attack.sweetBerryBush.player", "<fcolor:1>☠ <target> was poked to death by a sweet berry bush while trying to escape <killer>");
                    put("death.attack.thorns", "<fcolor:1>☠ <target> was killed while trying to hurt <killer>");
                    put("death.attack.thorns.item", "<fcolor:1>☠ <target> was killed by <fcolor:2>[<killer_item>]<fcolor:1> while trying to hurt <killer>");
                    put("death.attack.thrown", "<fcolor:1>☠ <target> was pummeled by <killer>");
                    put("death.attack.thrown.item", "<fcolor:1>☠ <target> was pummeled by <killer> using <fcolor:2>[<killer_item>]<fcolor:1>");
                    put("death.attack.trident", "<fcolor:1>☠ <target> was impaled by <killer>");
                    put("death.attack.trident.item", "<fcolor:1>☠ <target> was impaled by <killer> with <fcolor:2>[<killer_item>]<fcolor:1>");
                    put("death.attack.wither", "<fcolor:1>☠ <target> withered away");
                    put("death.attack.wither.player", "<fcolor:1>☠ <target> withered away while fighting <killer>");
                    put("death.attack.witherSkull", "<fcolor:1>☠ <target> was shot by a skull from <killer>");
                    put("death.attack.witherSkull.item", "<fcolor:1>☠ <target> was shot by a skull from <killer> using <fcolor:2>[<killer_item>]<fcolor:1>");
                    put("death.fell.accident.generic", "<fcolor:1>☠ <target> fell from a high place");
                    put("death.fell.accident.ladder", "<fcolor:1>☠ <target> fell off a ladder");
                    put("death.fell.accident.other_climbable", "<fcolor:1>☠ <target> fell while climbing");
                    put("death.fell.accident.scaffolding", "<fcolor:1>☠ <target> fell off scaffolding");
                    put("death.fell.accident.twisting_vines", "<fcolor:1>☠ <target> fell off some twisting vines");
                    put("death.fell.accident.vines", "<fcolor:1>☠ <target> fell off some vines");
                    put("death.fell.accident.weeping_vines", "<fcolor:1>☠ <target> fell off some weeping vines");
                    put("death.fell.assist", "<fcolor:1>☠ <target> was doomed to fall by <killer>");
                    put("death.fell.assist.item", "<fcolor:1>☠ <target> was doomed to fall by <killer> using <fcolor:2>[<killer_item>]<fcolor:1>");
                    put("death.fell.finish", "<fcolor:1>☠ <target> fell too far and was finished by <killer>");
                    put("death.fell.finish.item", "<fcolor:1>☠ <target> fell too far and was finished by <killer> using <fcolor:2>[<killer_item>]<fcolor:1>");
                    put("death.fell.killer", "<fcolor:1>☠ <target> was doomed to fall");
                }
            };
        }

        @Getter
        public static final class Debugstick implements SubMessageConfig, Localizable {
            private String empty = "<fcolor:1>\uD83D\uDD27 <fcolor:2><property> <fcolor:1>has no properties";
            private String select = "<fcolor:1>\uD83D\uDD27 selected \"<fcolor:2><property><fcolor:1>\" (<fcolor:2><value><fcolor:1>)";
            private String update = "<fcolor:1>\uD83D\uDD27 \"<fcolor:2><property><fcolor:1>\" to <fcolor:2><value>";
        }

        @Getter
        public static final class Deop implements SubMessageConfig, Localizable {
            private String format = "<fcolor:1>🤖 Made <target> no longer a server operator";
        }

        @Getter
        public static final class Dialog implements SubMessageConfig, Localizable {
            private SingleMultiple clear = new SingleMultiple(
                    "<fcolor:1>\uD83D\uDDD4 Cleared dialog for <target>",
                    "<fcolor:1>\uD83D\uDDD4 Cleared dialog for <fcolor:2><players> <fcolor:1>players"
            );
            private SingleMultiple show = new SingleMultiple(
                    "<fcolor:1>\uD83D\uDDD4 Displayed dialog to <target",
                    "<fcolor:1>\uD83D\uDDD4 Displayed dialog to <fcolor:2><players> <fcolor:1>players"
            );
        }

        @Getter
        public static final class Difficulty implements SubMessageConfig, Localizable {
            private String query = "<fcolor:1>⚔ The difficulty is <fcolor:2><lang:'<difficulty>'>";
            private String success = "<fcolor:1>⚔ The difficulty has been set to <fcolor:2><lang:'<difficulty>'>";
        }

        @Getter
        public static final class Effect implements SubMessageConfig, Localizable {

            private Clear clear = new Clear();
            private SingleMultiple give = new SingleMultiple(
                    "<fcolor:1>⚗ Applied effect <fcolor:2><lang:'<effect>'> <fcolor:1>to <target>",
                    "<fcolor:1>⚗ Applied effect <fcolor:2><lang:'<effect>'> <fcolor:1>to <fcolor:2><players> <fcolor:1>targets"
            );

            @Getter
            public static final class Clear {
                private SingleMultiple everything = new SingleMultiple(
                        "<fcolor:1>⚗ Removed every effect from <target>",
                        "<fcolor:1>⚗ Removed every effect from <fcolor:2><players> <fcolor:1>targets"
                );

                private SingleMultiple specific = new SingleMultiple(
                        "<fcolor:1>⚗ Removed effect <fcolor:2><lang:'<effect>'> <fcolor:1>from <target>",
                        "<fcolor:1>⚗ Removed effect <fcolor:2><lang:'<effect>'> <fcolor:1>from <fcolor:2><players> <fcolor:1>targets"
                );
            }

        }

        @Getter
        public static final class Enchant implements SubMessageConfig, Localizable {
            private String single = "<fcolor:1>📖 Applied enchantment <fcolor:2><enchantment><fcolor:1> to <target>'s item";
            private String multiple = "<fcolor:1>📖 Applied enchantment <fcolor:2><enchantment><fcolor:1> to <fcolor:2><players><fcolor:1> entities";
        }

        @Getter
        public static final class Execute implements SubMessageConfig, Localizable {
            private String pass = "<fcolor:1>⚡ Test passed";
            private String passCount = "<fcolor:1>⚡ Test passed, count: <fcolor:2><count>";
        }

        @Getter
        public static final class Experience implements SubMessageConfig, Localizable {

            private AddSet add = new AddSet(
                    new SingleMultiple(
                            "<fcolor:1>⏺ Gave <fcolor:2><amount> <fcolor:1>experience levels to <target>",
                            "<fcolor:1>⏺ Gave <fcolor:2><amount> <fcolor:1>experience levels to <fcolor:2><players> <fcolor:1>players"
                    ),
                    new SingleMultiple(
                            "<fcolor:1>⏺ Gave <fcolor:2><amount> <fcolor:1>experience points to <target>",
                            "<fcolor:1>⏺ Gave <fcolor:2><amount> <fcolor:1>experience points to <fcolor:2><players> <fcolor:1>players"
                    )
            );

            private Query query = new Query();

            private AddSet set = new AddSet(
                    new SingleMultiple(
                            "<fcolor:1>⏺ Set <fcolor:2><amount> <fcolor:1>experience levels to <target>",
                            "<fcolor:1>⏺ Set <fcolor:2><amount> <fcolor:1>experience levels to <fcolor:2><players> <fcolor:1>players"
                    ),
                    new SingleMultiple(
                            "<fcolor:1>⏺ Set <fcolor:2><amount> <fcolor:1>experience points to <target>",
                            "<fcolor:1>⏺ Set <fcolor:2><amount> <fcolor:1>experience points to <fcolor:2><players> <fcolor:1>players"
                    )
            );

            @Getter
            public static final class Query {
                private String levels = "<fcolor:1>⏺ <target> has <fcolor:2><amount> <fcolor:1>experience levels";
                private String points = "<fcolor:1>⏺ <target> has <fcolor:2><amount> <fcolor:1>experience points";
            }


            @Getter
            @AllArgsConstructor
            @NoArgsConstructor
            public static final class AddSet {
                private SingleMultiple levels;
                private SingleMultiple points;
            }

        }

        @Getter
        public static final class Fill implements SubMessageConfig, Localizable {
            private String format = "<fcolor:1>⏹ Successfully filled <fcolor:2><blocks> <fcolor:1>block(s)";
        }

        @Getter
        public static final class Fillbiome implements SubMessageConfig, Localizable {
            private String format = "<fcolor:1>⏹ Biomes set between <fcolor:2><x1><fcolor:1>, <fcolor:2><y1><fcolor:1>, <fcolor:2><z1> <fcolor:1>and <fcolor:2><x2><fcolor:1>, <fcolor:2><y2><fcolor:1>, <fcolor:2><z2>";
            private String formatCount = "<fcolor:1>⏹ <fcolor:2><blocks> <fcolor:1>biome entry/entries set between <fcolor:2><x1><fcolor:1>, <fcolor:2><y1><fcolor:1>, <fcolor:2><z1> <fcolor:1>and <fcolor:2><x2><fcolor:1>, <fcolor:2><y2><fcolor:1>, <fcolor:2><z2>";
        }

        @Getter
        public static final class Format implements FormatMessageConfig, Localizable {

            @Override
            public SubFormatMessageConfig getFcolor() {
                return null;
            }

            @Override
            public SubFormatMessageConfig getFixation() {
                return null;
            }

            @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/format/replacement/")})
            private Replacement replacement = new Replacement();

            @Override
            public SubFormatMessageConfig getScoreboard() {
                return null;
            }

            @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/format/mention/")})
            private Mention mention = new Mention();
            @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/format/moderation/")})
            private Moderation moderation = new Moderation();
            @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/format/name_/")})
            private Name name_ = new Name();
            @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/format/questionanswer/")})
            private QuestionAnswer questionAnswer = new QuestionAnswer();
            @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/format/translate/")})
            private Translate translate = new Translate();

            @Override
            public SubFormatMessageConfig getWorld() {
                return null;
            }

            @Getter
            public static final class Replacement implements SubFormatMessageConfig, Localizable {

                private String spoilerSymbol = "█";

                private Map<String, String> values = new LinkedHashMap<>() {
                    {
                        // emoticons
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

                        // ascii art
                        put("ascii_idk", "<click:suggest_command:\":idk:\"><hover:show_text:\":idk:\">¯\\_(ツ)_/¯</hover></click>");
                        put("ascii_angry", "<click:suggest_command:\":angry:\"><hover:show_text:\":angry:\">(╯°□°)╯︵ ┻━┻</hover></click>");
                        put("ascii_happy", "<click:suggest_command:\":happy:\"><hover:show_text:\":happy:\">＼(＾O＾)／</hover></click>");

                        // dynamic placeholders
                        put("ping", "<fcolor:2><ping>");
                        put("tps", "<fcolor:2><tps>");
                        put("online", "<fcolor:2><online>");
                        put("coords", "<fcolor:2><x> <y> <z>");
                        put("stats", "<color:#ff7171><hp>♥</color> <color:#3de0d8><armor>🛡 <color:#e33059><attack>🗡 <color:#4eff52><exp>⏺ <color:#f0a01f><food>🍖");
                        put("skin", "<click:open_url:\"<message_1>\"><hover:show_text:\"<fcolor:2><pixels>\"><fcolor:2><u>👨 Skin</u></hover></click>");
                        put("item", "<fcolor:2>[<message_1>]");

                        // text formatting
                        put("url", "<click:open_url:\"<message_1>\"><hover:show_text:\"<fcolor:2>Open url <br><u><message_1>\"><fcolor:2><u>🗗 Url</u></hover></click>");
                        put("image", "<click:open_url:\"<message_1>\"><hover:show_text:\"<fcolor:2><pixels>\"><fcolor:2><u>🖃 Image</u></hover></click>");
                        put("spoiler", "<hover:show_text:\"<fcolor:2><message_1>\"><fcolor:2><symbols></hover>");
                        put("bold", "<b><message_1></b>");
                        put("italic", "<i><message_1></i>");
                        put("underline", "<u><message_1></u>");
                        put("obfuscated", "<obf><message_1></obf>");
                        put("strikethrough", "<st><message_1></st>");
                    }
                };
            }

            @Getter
            public static final class Mention implements SubFormatMessageConfig, Localizable {
                private String person = "<fcolor:2>You were mentioned";
                private String format = "<fcolor:2>@<target>";
            }

            @Getter
            public static final class Moderation implements ModerationFormatMessageConfig, Localizable {

                @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/format/moderation/delete/")})
                private Delete delete = new Delete();

                @Override
                public SubModerationFormatMessageConfig getCaps() {
                    return null;
                }

                @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/format/moderation/newbie/")})
                private Newbie newbie = new Newbie();

                @Override
                public SubModerationFormatMessageConfig getFlood() {
                    return null;
                }

                @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/format/moderation/swear/")})
                private Swear swear = new Swear();

                @Getter
                public static final class Delete implements SubModerationFormatMessageConfig, Localizable {
                    private String placeholder = "<color:#ff7171><hover:show_text:\"<color:#ff7171>Click to delete message\"><click:run_command:\"/deletemessage <uuid>\">[x] ";
                    private String format = "<fcolor:3><i>Message deleted</i>";
                }

                @Getter
                public static final class Newbie implements SubModerationFormatMessageConfig, Localizable {
                    private String reason = "You're still too new";
                }

                @Getter
                public static final class Swear implements SubModerationFormatMessageConfig, Localizable {
                    private String symbol = "❤";
                }

            }

            @Getter
            @Setter
            public static final class Name implements SubFormatMessageConfig, Localizable {
                private String constant = "";
                private String display = "<click:suggest_command:\"/msg <player> \"><hover:show_text:\"<fcolor:2>Write to <player>\"><vault_prefix><stream_prefix><fcolor:2><player><afk_suffix><vault_suffix></hover></click>";
                private String entity = "<fcolor:2><hover:show_text:\"<fcolor:2><name> <br><fcolor:1>Type <fcolor:2><lang:'<type>'> <br><fcolor:1>ID <fcolor:2><uuid>\"><name></hover>";
                private String unknown = "<fcolor:2><name>";
                private String invisible = "<fcolor:2>\uD83D\uDC7B Invisible";
            }

            @Getter
            public static final class QuestionAnswer implements SubFormatMessageConfig, Localizable {
                private Map<String, String> questions = new LinkedHashMap<>(){
                    {
                        put("server", "<fcolor:2>[Answer] @<player><fcolor:1>, this is a vanilla server in minecraft!");
                        put("flectone", "<fcolor:2>[Answer] @<player><fcolor:1>, this is a brand and projects created by TheFaser");
                    }
                };
            }

            @Getter
            public static final class Translate implements SubFormatMessageConfig, Localizable {
                private String action = " <click:run_command:\"/translateto <language> <language> <message>\"><hover:show_text:\"<fcolor:2>Translate message\"><fcolor:1>⇄";
            }
        }

        @Getter
        public static final class Gamemode implements SubMessageConfig, Localizable {
            private String setDefault = "<fcolor:1>🗘 The default game mode is now <fcolor:2><lang:'<gamemode>'>";
            private String self = "<fcolor:1>🗘 Set own game mode to <fcolor:2><lang:'<gamemode>'>";
            private String other = "<fcolor:1>🗘 Set <target>'s game mode to <fcolor:2><lang:'<gamemode>'>";
        }

        @Getter
        public static final class Gamerule implements SubMessageConfig, Localizable {
            private String formatQuery = "<fcolor:1>\uD83D\uDDD0 Gamerule <fcolor:2><gamerule> <fcolor:1>is currently set to: <fcolor:2><value>";
            private String formatSet = "<fcolor:1>\uD83D\uDDD0 Gamerule <fcolor:2><gamerule> <fcolor:1>is now set to: <fcolor:2><value>";
        }

        @Getter
        public static final class Give implements SubMessageConfig, Localizable {
            private String single = "<fcolor:1>⛏ Gave <fcolor:2><items> <fcolor:1>[<fcolor:2><hover:show_text:\"<fcolor:2><give_item>\"><give_item></hover><fcolor:1>] to <target>";
            private String multiple = "<fcolor:1>⛏ Gave <fcolor:2><items> <fcolor:1>[<fcolor:2><hover:show_text:\"<fcolor:2><give_item>\"><give_item></hover><fcolor:1>] to <players> players";
        }

        @Getter
        public static final class Greeting implements SubMessageConfig, Localizable {
            private String format = "<br>[#][#][#][#][#][#][#][#]<br>[#][#][#][#][#][#][#][#]<br>[#][#][#][#][#][#][#][#]<br>[#][#][#][#][#][#][#][#]<br>[#][#][#][#][#][#][#][#]  <fcolor:1>Hello,<br>[#][#][#][#][#][#][#][#]  <player><br>[#][#][#][#][#][#][#][#]<br>[#][#][#][#][#][#][#][#]<br>";
        }

        @Getter
        public static final class Join implements SubMessageConfig, Localizable {
            private String format = "<color:#4eff52>→ <display_name>";
            private String formatFirstTime = "<color:#4eff52>→ <display_name> <fcolor:1>welcome!";
        }

        @Getter
        public static final class Kill implements SubMessageConfig, Localizable {
            private String single = "<color:#778899>☠ <fcolor:1>Killed <target>";
            private String multiple = "<color:#778899>☠ <fcolor:1>Killed <fcolor:2><entities> <fcolor:1>entities";
        }

        @Getter
        public static final class Locate implements SubMessageConfig, Localizable {
            private String biome = "<fcolor:1>\uD83D\uDDFA The nearest <fcolor:2><value> <fcolor:1>is at <fcolor:2><hover:show_text:\"<fcolor:2>Click to teleport\"><click:suggest_command:\"/tp @s <x> <y> <z>\">[<x>, <y>, <z>]</click></hover> <fcolor:1>(<fcolor:2><blocks> <fcolor:1>blocks away)";
            private String poi = "<fcolor:1>\uD83D\uDDFA The nearest <fcolor:2><value> <fcolor:1>is at <fcolor:2><hover:show_text:\"<fcolor:2>Click to teleport\"><click:suggest_command:\"/tp @s <x> <y> <z>\">[<x>, <y>, <z>]</click></hover> <fcolor:1>(<fcolor:2><blocks> <fcolor:1>blocks away)";
            private String structure = "<fcolor:1>\uD83D\uDDFA The nearest <fcolor:2><value> <fcolor:1>is at <fcolor:2><hover:show_text:\"<fcolor:2>Click to teleport\"><click:suggest_command:\"/tp @s <x> <y> <z>\">[<x>, <y>, <z>]</click></hover> <fcolor:1>(<fcolor:2><blocks> <fcolor:1>blocks away)";
        }

        @Getter
        public static final class Objective implements ObjectiveMessageConfig, Localizable {


            @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/objective/belowname/")})
            private Belowname belowname = new Belowname();

            @Override
            public SubObjectiveMessageConfig getTabname() {
                return null;
            }

            @Getter
            public static final class Belowname implements SubObjectiveMessageConfig, Localizable {
                private String format = "<fcolor:1>ms";
            }

        }

        @Getter
        public static final class Op implements SubMessageConfig, Localizable {
            private String format = "<fcolor:1>🤖 Made <target> a server operator";
        }

        @Getter
        public static final class Particle implements SubMessageConfig, Localizable {
            private String format = "<fcolor:1>❄ Displaying particle <fcolor:2><lang:'<particle>'>";
        }

        @Getter
        public static final class Quit implements SubMessageConfig, Localizable {
            private String format = "<color:#ff4e4e>← <display_name>";
        }

        @Getter
        public static final class Reload implements SubMessageConfig, Localizable {
            private String format = "<fcolor:1>\uD83D\uDEC8 Reloading!";
        }

        @Getter
        public static final class Ride implements SubMessageConfig, Localizable {
            private String dismount = "<fcolor:1>\uD83C\uDFC7 <target> stopped riding <second_target>";
            private String mount = "<fcolor:1>\uD83C\uDFC7 <target> started riding <second_target>";
        }

        @Getter
        public static final class Rightclick implements SubMessageConfig, Localizable {
            private String format = "<fcolor:1>◁ <display_name> ▷";
        }

        @Getter
        public static final class Rotate implements SubMessageConfig, Localizable {
            private String format = "<fcolor:1>\uD83E\uDD38 Rotated <target>";
        }

        @Getter
        public static final class Save implements SubMessageConfig, Localizable {
            private String disabled = "<fcolor:1>\uD83D\uDEC8 Automatic saving is now disabled";
            private String enabled = "<fcolor:1>\uD83D\uDEC8 Automatic saving is now enabled";
            private String saving = "<fcolor:1>\uD83D\uDEC8 Saving the game (this may take a moment!)";
            private String success = "<fcolor:1>\uD83D\uDEC8 Saved the game";
        }

        @Getter
        public static final class Seed implements SubMessageConfig, Localizable {
            private String format = "<fcolor:1>\uD83D\uDD11 Seed: [<fcolor:2><hover:show_text:'<fcolor:2>Click to Copy to Clipboard'><click:copy_to_clipboard:<seed>><seed></click></hover><fcolor:1>]";
        }

        @Getter
        public static final class Setblock implements SubMessageConfig, Localizable {
            private String format = "<fcolor:1>⏹ Changed the block at <fcolor:2><x><fcolor:1>, <fcolor:2><y><fcolor:1>, <fcolor:2><z>";
        }

        @Getter
        public static final class Sidebar implements SubMessageConfig, Localizable {
            private List<List<String>> values = new LinkedList<>(){
                {
                    push(new LinkedList<>(){
                        {
                            push(" ");
                            push("<fcolor:1>Ping <ping>");
                            push(" ");
                            push("<fcolor:1>FlectonePulse");
                        }
                    });
                    push(new LinkedList<>(){
                        {
                            push(" ");
                            push("<fcolor:2>TPS <tps>");
                            push(" ");
                            push("<fcolor:2>FlectonePulse");
                        }
                    });
                }
            };
        }

        @Getter
        public static final class Sleep implements SubMessageConfig, Localizable {
            private String notPossible = "<fcolor:1>\uD83D\uDECC No amount of rest can pass this night";
            private String playersSleeping = "<fcolor:1>\uD83D\uDECC <fcolor:2><players_sleeping><fcolor:1>/<fcolor:2><players><fcolor:1> players sleeping";
            private String skippingNight = "<fcolor:1>\uD83D\uDECC Sleeping through this night";
        }

        @Getter
        public static final class Spawn implements SubMessageConfig, Localizable {
            private String notValid = "<fcolor:1>\uD83D\uDECC You have no home bed or charged respawn anchor, or it was obstructed";
            private String set = "<fcolor:1>\uD83D\uDECC Respawn point set";
            private String setWorld = "<fcolor:1>\uD83D\uDECC Set the world spawn point to <fcolor:2><x><fcolor:1>, <fcolor:2><y><fcolor:1>, <fcolor:2><z><fcolor:1> [<fcolor:2><angle><fcolor:1>]";
            private String single = "<fcolor:1>\uD83D\uDECC Set spawn point to <fcolor:2><x><fcolor:1>, <fcolor:2><y><fcolor:1>, <fcolor:2><z><fcolor:1> [<fcolor:2><angle><fcolor:1>] in <fcolor:2><world><fcolor:1> for <target>";
            private String multiple = "<fcolor:1>\uD83D\uDECC Set spawn point to <fcolor:2><x><fcolor:1>, <fcolor:2><y><fcolor:1>, <fcolor:2><z><fcolor:1> [<fcolor:2><angle><fcolor:1>] in <fcolor:2><world><fcolor:1> for <fcolor:2><players><fcolor:1> players";
        }

        @Getter
        public static final class Status implements StatusMessageConfig, Localizable {

            @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/status/motd/")})
            private MOTD motd = new MOTD();
            @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/status/players/")})
            private Players players = new Players();
            @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/status/version/")})
            private Version version = new Version();

            @Override
            public SubStatusMessageConfig getIcon() {
                return null;
            }

            @Getter
            public static final class MOTD implements SubStatusMessageConfig, Localizable {
                private List<String> values = new LinkedList<>(){
                    {
                        push("<fcolor:1>Welcome to our server!");
                        push("<fcolor:1>Join us and enjoy a unique game experience!");
                        push("<fcolor:1>We have a friendly community - be polite and respect each other!");
                        push("<fcolor:1>Enjoy the game! If you have any questions, feel free to contact the administration");
                    }
                };
            }

            @Getter
            public static final class Players implements SubStatusMessageConfig, Localizable {
                private List<Sample> samples = new LinkedList<>(List.of(
                        new Sample()
                ));
                private String full = "<color:#ff7171>The server is full";

                @Getter
                @NoArgsConstructor
                @AllArgsConstructor
                public static final class Sample {
                    private String name = "<players>";
                    private String id = null;
                }
            }

            @Getter
            public static final class Version implements SubStatusMessageConfig, Localizable {
                private String name = "Minecraft server";
            }

        }

        @Getter
        public static final class Stop implements SubMessageConfig, Localizable {
            private String format = "<fcolor:1>\uD83D\uDEC8 Stopping the server";
        }

        @Getter
        public static final class Summon implements SubMessageConfig, Localizable {
            private String format = "<fcolor:1>\uD83D\uDC3A Summoned new <target>";
        }

        @Getter
        public static final class Tab implements TabMessageConfig, Localizable {

            @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/tab/header/")})
            private Header header = new Header();
            @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/tab/footer/")})
            private Footer footer = new Footer();
            @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/tab/playerlistname/")})
            private Playerlistname playerlistname = new Playerlistname();

            @Getter
            public static final class Footer implements SubTabMessageConfig, Localizable {
                private List<List<String>> lists = new LinkedList<>(){
                    {
                        push(new LinkedList<>(){
                            {
                                push(" ");
                                push("<fcolor:1>Hello <fcolor:2><player><fcolor:1>!");
                                push(" ");
                            }
                        });
                        push(new LinkedList<>(){
                            {
                                push(" ");
                                push("<fcolor:1>TPS <tps>, Online <online>");
                                push(" ");
                            }
                        });
                    }
                };
            }

            @Getter
            public static final class Header implements SubTabMessageConfig, Localizable {
                private List<List<String>> lists = new LinkedList<>(){
                    {
                        push(new LinkedList<>(){
                            {
                                push(" ");
                                push("<fcolor:1>❤");
                                push(" ");
                            }
                        });
                        push(new LinkedList<>(){
                            {
                                push(" ");
                                push("<fcolor:1>\uD83D\uDC7E");
                                push(" ");
                            }
                        });
                    }
                };
            }

            @Getter
            public static final class Playerlistname implements SubTabMessageConfig, Localizable {
                private String format = "<world_prefix>▋ <reset><vault_prefix><stream_prefix><fcolor:2><player><afk_suffix><vault_suffix>";
            }
        }

        @Getter
        public static final class Teleport implements SubMessageConfig, Localizable {
            private SingleMultiple entity = new SingleMultiple(
                    "<fcolor:1>\uD83C\uDF00 Teleported <target> to <second_target>",
                    "<fcolor:1>\uD83C\uDF00 Teleported <fcolor:2><entities> <fcolor:1>entities to <second_target>"
            );

            private SingleMultiple location = new SingleMultiple(
                    "<fcolor:1>\uD83C\uDF00 Teleported <target> to <fcolor:2><x><fcolor:1>, <fcolor:2><y><fcolor:1>, <fcolor:2><z><fcolor:1>",
                    "<fcolor:1>\uD83C\uDF00 Teleported <fcolor:2><entities> <fcolor:1>to <fcolor:2><x><fcolor:1>, <fcolor:2><y><fcolor:1>, <fcolor:2><z><fcolor:1>"
            );
        }

        @Getter
        public static final class Time implements SubMessageConfig, Localizable {
            private String query = "<fcolor:1>☽ The time is <fcolor:2><time>";
            private String set = "<fcolor:1>☽ Set the time to <fcolor:2><time>";
        }

        @Getter
        public static final class Update implements SubMessageConfig, Localizable {
            private String formatPlayer = "<fcolor:1><fcolor:2>(FlectonePulse)<fcolor:1> Your version <fcolor:2><current_version><fcolor:1> is outdated! Update to <fcolor:2><latest_version><fcolor:1> at <url:https://github.com/Flectone/FlectonePulse/releases> for the latest features!";
            private String formatConsole = "<fcolor:1>Your version <fcolor:2><current_version><fcolor:1> is outdated! Update to <fcolor:2><latest_version><fcolor:1> at <click:open_url:https://github.com/Flectone/FlectonePulse/releases>https://github.com/Flectone/FlectonePulse/releases";
        }

        @Getter
        public static final class Weather implements SubMessageConfig, Localizable {
            private String formatClear = "<fcolor:1>☀ Set the weather to <fcolor:2>clear";
            private String formatRain = "<fcolor:1>\uD83C\uDF27 Set the weather to <fcolor:2>rain";
            private String formatThunder = "<fcolor:1>⛈ Set the weather to <fcolor:2>rain & thunder";
        }

        @Getter
        public static final class Worldborder implements SubMessageConfig, Localizable {
            private String center = "<fcolor:1>\uD83D\uDEAB Set the center of the world border to <fcolor:2><value><fcolor:1>, <fcolor:2><second_value>";
            private Damage damage = new Damage(
                    "<fcolor:1>\uD83D\uDEAB Set the world border damage to <fcolor:2><value> <fcolor:1>per block each second",
                    "<fcolor:1>\uD83D\uDEAB Set the world border damage buffer to <fcolor:2><value> <fcolor:1>block(s)"
            );
            private String get = "<fcolor:1>\uD83D\uDEAB The world border is currently <fcolor:2><value> <fcolor:1>block(s) wide";
            private Set set = new Set(
                    "<fcolor:1>\uD83D\uDEAB Growing the world border to <fcolor:2><value> <fcolor:1>blocks wide over <fcolor:2><second_value> <fcolor:1>seconds",
                    "<fcolor:1>\uD83D\uDEAB Set the world border to <fcolor:2><value> <fcolor:1>block(s) wide",
                    "<fcolor:1>\uD83D\uDEAB Shrinking the world border to <fcolor:2><value> <fcolor:1>block(s) wide over <fcolor:2><second_value> <fcolor:1>second(s)"
            );
            private Warning warning = new Warning(
                    "<fcolor:1>\uD83D\uDEAB Set the world border warning distance to <fcolor:2><value> <fcolor:1>block(s)",
                    "<fcolor:1>\uD83D\uDEAB Set the world border warning time to <fcolor:2><value> <fcolor:1>second(s)"
            );

            @Getter
            @AllArgsConstructor
            @NoArgsConstructor
            public static final class Damage {
                private String amount = "";
                private String buffer = "";
            }

            @Getter
            @AllArgsConstructor
            @NoArgsConstructor
            public static final class Set {
                private String grow = "";
                private String immediate = "";
                private String shrink = "";
            }

            @Getter
            @AllArgsConstructor
            @NoArgsConstructor
            public static final class Warning {
                private String distance = "";
                private String time = "";
            }
        }

    }

    public interface Localizable {}

    @Getter
    @AllArgsConstructor
    public static final class ListTypeMessage {
        private String header;
        private String line;
        private String footer;
    }

    public static class ReasonMap extends LinkedHashMap<String, String> {

        public String getConstant(String reason) {
            if (StringUtils.isEmpty(reason)) {
                return super.getOrDefault("default", "UNKNOWN");
            }

            return super.getOrDefault(reason, reason);
        }
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static final class SingleMultiple {
        private String single = "";
        private String multiple = "";
    }

}
