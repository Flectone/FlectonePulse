package net.flectone.pulse.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.util.MessageTag;
import net.flectone.pulse.util.TagType;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@SuppressWarnings({"FieldMayBeFinal", "unused"})
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

    public Localization(Path projectPath, String language) {
        super(Paths.get(projectPath.toString(), "localizations", language + ".yml"));

        if (language.equalsIgnoreCase("ru_ru")) {
            initRU_RU();
        }
    }

    private void initRU_RU() {
        cooldown = "<color:#ff7171><b>⁉</b> Слишком быстро, попробуй через <time>";

        time.format = "dd'д' HH'ч' mm'м' ss.SSS'с'";
        time.permanent = "НАВСЕГДА";
        time.zero = "0с";

        command.dice.format = "<fcolor:1>✎ <display_name> кинул кубики <message> (<sum>)";

        command.ball.format = "<color:#9370DB>❓ <display_name> спросил: <message> <br>🔮 Магический шар: <u><answer></u>";
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
        command.ping.format = "<fcolor:1>🖧 Пинг игрока <fcolor:2><player></fcolor:2> равен <ping>";

        command.coin.head = "орёл";
        command.coin.tail = "решка";
        command.coin.format = "<fcolor:1>✎ <display_name> подбросил монетку - <result>";
        command.coin.formatDraw = "<fcolor:1>✎ <display_name> неудачно подбросил монетку ребром :)";

        command.translateto.nullOrError = "<color:#ff7171><b>⁉</b> Ошибка, возможно указан неправильный язык";
        command.translateto.format = "<fcolor:1>📖 Перевод на [<language>] → <fcolor:2><message>";

        command.clearchat.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        command.clearchat.format = "<fcolor:1>\uD83D\uDCAC Чат очищен";

        command.geolocate.nullOrError = "<color:#ff7171><b>⁉</b> Ошибка, попробуй чуть позже";
        command.geolocate.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        command.geolocate.format = "<fcolor:1>Геолокация <display_name><br>Страна: <fcolor:2><country><br><fcolor:1>Регион: <fcolor:2><region_name><br><fcolor:1>Город: <fcolor:2><city><br><fcolor:1>Часовой пояс: <fcolor:2><timezone><br><fcolor:1>Мобильный интернет? <fcolor:2><mobile><br><fcolor:1>ВПН? <fcolor:2><proxy><br><fcolor:1>Хостинг? <fcolor:2><hosting><br><fcolor:1>Айпи: <fcolor:2><query>";

        command.broadcast.format = "<color:#ffd500>🕫 Сообщение для всех от <display_name> <br>❝ <message> ❠";

        command.spy.formatLog = "<fcolor:1>[👁] <display_name> <color:#98FB98><action> <fcolor:1>→ <fcolor:2><message>";
        command.spy.formatTrue = "<fcolor:1>[👁] Ты <color:#98FB98>включил <fcolor:1>слежку";
        command.spy.formatFalse = "<fcolor:1>[👁] Ты <color:#F08080>выключил <fcolor:1>слежку";

        command.stream.not = "<color:#ff7171><b>⁉</b> Ты не включил трансляцию";
        command.stream.formatEnd = "<fcolor:2>★ Спасибо за трансляцию на нашем сервере!";
        command.stream.already = "<color:#ff7171><b>⁉</b> Ты уже включил трансляцию";
        command.stream.formatStart = "<br><color:#ff4e4e>\uD83D\uDD14 <fcolor:1>Объявление <color:#ff4e4e>\uD83D\uDD14<br><br><fcolor:1><display_name> начал трансляцию<br><br><urls><br>";

        command.kick.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        command.kick.reasons.clear();
        command.kick.reasons.put("default", "Исключён модератором");
        command.kick.server = "<color:#ff7171>🔒 <fcolor:2><moderator></fcolor> исключил <fcolor:2><player></fcolor> <fcolor:1><hover:show_text:\"<fcolor:1>Айди: <id><br>Дата: <date><br>Модератор: <moderator><br>Причина: <reason>\">[ПОДРОБНЕЕ]</hover>";
        command.kick.person = "<color:#ff7171>🔒 КИК 🔒 <fcolor:1><br><br>Айди: <id><br><br>Дата: <date><br><br>Модератор: <moderator><br><br>Причина: <reason>";

        command.helper.nullHelper = "<color:#ff7171><b>⁉</b> Сейчас нет людей, кто бы смог помочь";
        command.helper.global = "<fcolor:2>👤 <display_name> просит помощи ⏩ <fcolor:1><message>";
        command.helper.player = "<fcolor:2>👤 Запрос отправлен, ожидай ответа";

        command.tell.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        command.tell.sender = "<fcolor:2>✉ Ты → <display_name> » <fcolor:1><message>";
        command.tell.receiver = "<fcolor:2>✉ <display_name> → Тебе » <fcolor:1><message>";
        command.tell.myself = "<fcolor:2>✉ [Заметка] <fcolor:1><message>";

        command.reply.nullReceiver = "<color:#ff7171><b>⁉</b> Некому отвечать";

        command.poll.formatStart = "<br><color:#fce303>🗐 Создано голосование #<id> <br>❓ <message> <br><answers>";
        command.poll.formatOver = "<br><color:#fce303>🗐 Голосование #<id> завершено <br>❓ <message> <br>Результат: <br><answers>";
        command.poll.voteTrue = "<color:#4eff52>👍 Ты выбрал <answer_id> в голосовании #<id>. Всего таких голосов <count>";
        command.poll.voteFalse = "<color:#ff4e4e>\uD83D\uDD93 Ты передумал об <answer_id> в голосовании #<id>. Всего таких голосов <count> без тебя";
        command.poll.countAnswers = "<color:#4eff52><bold><count></bold> за [<answer_key>] - <answer_value> <br>";
        command.poll.voteButton = "<color:#4eff52><hover:show_text:\"<color:#4eff52>Проголосовать за <bold><answer_key>\"><click:run_command:\"/poll vote <id> <number>\">[<answer_key>] - <answer_value> <br>";
        command.poll.expired = "<color:#ff7171><b>⁉</b> Голосование завершено";
        command.poll.already = "<color:#ff7171><b>⁉</b> Ты уже проголосовал в этом голосовании";
        command.poll.nullPoll = "<color:#ff7171><b>⁉</b> Голосование не найдено";

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
        command.ban.server = "<color:#ff7171>🔒 <fcolor:2><moderator></fcolor> заблокировал игрока <fcolor:2><player></fcolor> <fcolor:1><hover:show_text:\"<fcolor:1>Айди: <id><br>Дата: <date><br>Время: <time><br>Осталось: <time_left><br>Модератор: <moderator><br>Причина: <reason>\">[ПОДРОБНЕЕ]</hover>";
        command.ban.person = "<color:#ff7171>🔒 БАН 🔒 <fcolor:1><br><br>Дата: <date><br><br>Время: <time><br><br>Осталось: <time_left><br><br>Модератор: <moderator><br><br>Причина: <reason>";
        command.ban.connectionAttempt = "<color:#ff7171>🔒 Заблокированный <fcolor:2><player></fcolor> попытался подключиться <fcolor:1><hover:show_text:\"<fcolor:1>Айди: <id><br>Дата: <date><br>Время: <time><br>Осталось: <time_left><br>Модератор: <moderator><br>Причина: <reason>\">[ПОДРОБНЕЕ]</hover>";

        command.unban.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        command.unban.notBanned = "<color:#ff7171><b>⁉</b> Игрок не заблокирован";
        command.unban.format = "<color:#98FB98>\uD83D\uDD13 <fcolor:2><moderator></fcolor> разблокировал игрока <fcolor:2><player></fcolor>";

        command.banlist.empty = "<color:#98FB98>☺ Блокировки не найдены";
        command.banlist.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        command.banlist.nullPage = "<color:#ff7171><b>⁉</b> Страница не найдена";
        command.banlist.global.header = "<fcolor:2>▋ Блокировки: <count> <br>";
        command.banlist.global.line = "<hover:show_text:\"<fcolor:1>Разблокировать <display_name>\"><click:run_command:\"<command>\"><color:#ff7171>☒ <display_name></click></hover> <fcolor:1><hover:show_text:\"<fcolor:1>Айди: <id><br>Дата: <date><br>Время: <time><br>Модератор: <moderator><br>Причина: <reason>\">[ПОДРОБНЕЕ]</hover>";
        command.banlist.global.footer = "<br>▋ <fcolor:2><click:run_command:\"<command> <prev_page>\">←</click> <fcolor:1>Page: <current_page>/<last_page> <fcolor:2><click:run_command:\"<command> <next_page>\">→";
        command.banlist.player.header = "<fcolor:2>▋ Все блокировки: <count> <br>";
        command.banlist.player.line = "<hover:show_text:\"<fcolor:1>Разблокировать <display_name>\"><click:run_command:\"<command>\"><color:#ff7171>☒ <display_name></click></hover> <fcolor:1><hover:show_text:\"<fcolor:1>Айди: <id><br>Дата: <date><br>Время: <time><br>Модератор: <moderator><br>Причина: <reason>\">[ПОДРОБНЕЕ]</hover>";
        command.banlist.player.footer = "<br>▋ <fcolor:2><click:run_command:\"<command> <prev_page>\">←</click> <fcolor:1>Page: <current_page>/<last_page> <fcolor:2><click:run_command:\"<command> <next_page>\">→";

        command.mute.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        command.mute.nullTime = "<color:#ff7171><b>⁉</b> Невозможное время";
        command.mute.reasons.clear();
        command.mute.reasons.put("default", "Ты был замучен на сервере");
        command.mute.server = "<color:#ff7171>🔒 <fcolor:2><moderator></fcolor> выдал мут игроку <fcolor:2><player></fcolor> <fcolor:1><hover:show_text:\"<fcolor:1>Айди: <id><br>Дата: <date><br>Время: <time><br>Осталось: <time_left><br>Модератор: <moderator><br>Причина: <reason>\">[ПОДРОБНЕЕ]</hover>";
        command.mute.person = "<color:#ff7171>🔒 Ты замучен, осталось <time_left>";

        command.unmute.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        command.unmute.notMuted = "<color:#ff7171><b>⁉</b> Игрок не замучен";
        command.unmute.format = "<color:#98FB98>\uD83D\uDD13 <fcolor:2><moderator></fcolor> размутил игрока <fcolor:2><player></fcolor>";

        command.mutelist.empty = "<color:#98FB98>☺ Муты не найдены";
        command.mutelist.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        command.mutelist.nullPage = "<color:#ff7171><b>⁉</b> Страница не найдена";
        command.mutelist.global.header = "<fcolor:2>▋ Муты: <count> <br>";
        command.mutelist.global.line = "<hover:show_text:\"<fcolor:1>Размутить <display_name>\"><click:run_command:\"<command>\"><color:#ff7171>☒ <display_name></click></hover> <fcolor:1><hover:show_text:\"<fcolor:1>Айди: <id><br>Дата: <date><br>Время: <time><br>Модератор: <moderator><br>Причина: <reason>\">[ПОДРОБНЕЕ]</hover>";
        command.mutelist.global.footer = "<br>▋ <fcolor:2><click:run_command:\"<command> <prev_page>\">←</click> <fcolor:1>Страница: <current_page>/<last_page> <fcolor:2><click:run_command:\"<command> <next_page>\">→";
        command.mutelist.player.header = "<fcolor:2>▋ Все муты: <count> <br>";
        command.mutelist.player.line = "<hover:show_text:\"<fcolor:1>Размутить <display_name>\"><click:run_command:\"<command>\"><color:#ff7171>☒ <display_name></click></hover> <fcolor:1><hover:show_text:\"<fcolor:1>Айди: <id><br>Дата: <date><br>Время: <time><br>Модератор: <moderator><br>Причина: <reason>\">[ПОДРОБНЕЕ]</hover>";
        command.mutelist.player.footer = "<br>▋ <fcolor:2><click:run_command:\"<command> <prev_page>\">←</click> <fcolor:1>Страница: <current_page>/<last_page> <fcolor:2><click:run_command:\"<command> <next_page>\">→";

        command.warn.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        command.warn.nullTime = "<color:#ff7171><b>⁉</b> Невозможное время";
        command.warn.reasons.clear();
        command.warn.reasons.put("default", "Ты получил предупреждение");
        command.warn.server = "<color:#ff7171>🔒 <fcolor:2><moderator></fcolor> выдал предупреждение игроку <fcolor:2><player></fcolor> <fcolor:1><hover:show_text:\"<fcolor:1>Айди: <id><br>Дата: <date><br>Время: <time><br>Осталось: <time_left><br>Модератор: <moderator><br>Причина: <reason>\">[ПОДРОБНЕЕ]</hover>";
        command.warn.person = "<color:#ff7171>🔒 Ты получил предупреждение на <time>";

        command.unwarn.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        command.unwarn.notWarned = "<color:#ff7171><b>⁉</b> Игрок не имеет предупреждений";
        command.unwarn.format = "<color:#98FB98>\uD83D\uDD13 <fcolor:2><moderator></fcolor> снял предупреждение с игрока <fcolor:2><player></fcolor>";

        command.warnlist.empty = "<color:#98FB98>☺ Предупреждения не найдены";
        command.warnlist.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        command.warnlist.nullPage = "<color:#ff7171><b>⁉</b> Страница не найдена";
        command.warnlist.global.header = "<fcolor:2>▋ Предупреждения: <count> <br>";
        command.warnlist.global.line = "<hover:show_text:\"<fcolor:1>Снять предупреждение <display_name>\"><click:run_command:\"<command>\"><color:#ff7171>☒ <display_name></click></hover> <fcolor:1><hover:show_text:\"<fcolor:1>Айди: <id><br>Дата: <date><br>Время: <time><br>Модератор: <moderator><br>Причина: <reason>\">[ПОДРОБНЕЕ]</hover>";
        command.warnlist.global.footer = "<br>▋ <fcolor:2><click:run_command:\"<command> <prev_page>\">←</click> <fcolor:1>Страница: <current_page>/<last_page> <fcolor:2><click:run_command:\"<command> <next_page>\">→";
        command.warnlist.player.header = "<fcolor:2>▋ Все предупреждения: <count> <br>";
        command.warnlist.player.line = "<hover:show_text:\"<fcolor:1>Снять предупреждение <display_name>\"><click:run_command:\"<command>\"><color:#ff7171>☒ <display_name></click></hover> <fcolor:1><hover:show_text:\"<fcolor:1>Айди: <id><br>Дата: <date><br>Время: <time><br>Модератор: <moderator><br>Причина: <reason>\">[ПОДРОБНЕЕ]</hover>";
        command.warnlist.player.footer = "<br>▋ <fcolor:2><click:run_command:\"<command> <prev_page>\">←</click> <fcolor:1>Страница: <current_page>/<last_page> <fcolor:2><click:run_command:\"<command> <next_page>\">→";

        command.flectonepulse.formatFalse = "<color:#ff7171>★ Возникла проблема при перезагрузке";
        command.flectonepulse.formatTrue = "<fcolor:2>★ <u>FlectonePulse</u> успешно перезагружен! (<i><time></i>)";
        command.flectonepulse.formatTrueText = "<fcolor:2>★ ВАЖНО! Перезагружен только текст, для ПОЛНОЙ перезагрузки нужно использовать: <br><fcolor:1>/flectonepulse reload";

        command.chatcolor.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        command.chatcolor.format = "<color:#98FB98>★ Теперь цвета <fcolor:1>сообщение</fcolor> <fcolor:2>сообщение</fcolor> <fcolor:3>сообщение <fcolor:4>сообщение";
        command.chatcolor.nullColor = "<color:#ff7171><b>⁉</b> Цвета введены неверно";

        command.chatsetting.noPermission = "<color:#ff7171><b>⁉</b> Нет разрешения на изменение этой настройки";
        command.chatsetting.settings.clear();
        command.chatsetting.settings.put(FPlayer.Setting.CHAT, List.of(List.of("<fcolor:2>Чат", "<fcolor:1>Выбран чат <bold><chat>")));
        command.chatsetting.settings.put(FPlayer.Setting.COLOR, List.of(List.of("<fcolor:2>Цвета", "<fcolor:1>сообщение", "<fcolor:2>сообщение", "<fcolor:3>сообщение", "<fcolor:4>сообщение")));
        command.chatsetting.settings.put(FPlayer.Setting.STREAM, List.of(List.of("<fcolor:2>Режим стримера", "<color:#98FB98>Включён"), List.of("<fcolor:2>Режим стримера", "<color:#ff7171>Выключен")));
        command.chatsetting.settings.put(FPlayer.Setting.SPY, List.of(List.of("<fcolor:2>Режим слежки", "<color:#98FB98>Включён"), List.of("<fcolor:2>Режим слежки", "<color:#ff7171>Выключен")));
        command.chatsetting.settings.put(FPlayer.Setting.ADVANCEMENT, List.of(List.of("<fcolor:2>Достижения", "<color:#98FB98>Показываются"), List.of("<fcolor:2>Достижения", "<color:#ff7171>Скрыты")));
        command.chatsetting.settings.put(FPlayer.Setting.DEATH, List.of(List.of("<fcolor:2>Смерти", "<color:#98FB98>Показываются"), List.of("<fcolor:2>Смерти", "<color:#ff7171>Скрыты")));
        command.chatsetting.settings.put(FPlayer.Setting.JOIN, List.of(List.of("<fcolor:2>Оповещения о входе", "<color:#98FB98>Показываются"), List.of("<fcolor:2>Оповещения о входе", "<color:#ff7171>Скрыты")));
        command.chatsetting.settings.put(FPlayer.Setting.QUIT, List.of(List.of("<fcolor:2>Оповещения о выходе", "<color:#98FB98>Показываются"), List.of("<fcolor:2>Оповещения о выходе", "<color:#ff7171>Скрыты")));
        command.chatsetting.settings.put(FPlayer.Setting.AUTO, List.of(List.of("<fcolor:2>Авто сообщения", "<color:#98FB98>Показываются"), List.of("<fcolor:2>Авто сообщения", "<color:#ff7171>Скрыты")));
        command.chatsetting.settings.put(FPlayer.Setting.ME, List.of(List.of("<fcolor:2>Команда /me", "<color:#98FB98>Показывается"), List.of("<fcolor:2>Команда/me", "<color:#ff7171>Скрыта")));
        command.chatsetting.settings.put(FPlayer.Setting.TRY, List.of(List.of("<fcolor:2>Команда /try", "<color:#98FB98>Показывается"), List.of("<fcolor:2>Команда /try", "<color:#ff7171>Скрыта")));
        command.chatsetting.settings.put(FPlayer.Setting.DICE, List.of(List.of("<fcolor:2>Команда /dice", "<color:#98FB98>Показывается"), List.of("<fcolor:2>Команда /dice", "<color:#ff7171>Скрыта")));
        command.chatsetting.settings.put(FPlayer.Setting.BALL, List.of(List.of("<fcolor:2>Команда /ball", "<color:#98FB98>Показывается"), List.of("<fcolor:2>Команда /ball", "<color:#ff7171>Скрыта")));
        command.chatsetting.settings.put(FPlayer.Setting.MUTE, List.of(List.of("<fcolor:2>Команда /mute", "<color:#98FB98>Показывается"), List.of("<fcolor:2>Команда /mute", "<color:#ff7171>Скрыта")));
        command.chatsetting.settings.put(FPlayer.Setting.BAN, List.of(List.of("<fcolor:2>Команда /ban", "<color:#98FB98>Показывается"), List.of("<fcolor:2>Команда /ban", "<color:#ff7171>Скрыта")));
        command.chatsetting.settings.put(FPlayer.Setting.WARN, List.of(List.of("<fcolor:2>Команда /warn", "<color:#98FB98>Показывается"), List.of("<fcolor:2>Команда /warn", "<color:#ff7171>Скрыта")));
        command.chatsetting.settings.put(FPlayer.Setting.TELL, List.of(List.of("<fcolor:2>Команда /tell", "<color:#98FB98>Показывается"), List.of("<fcolor:2>Команда /tell", "<color:#ff7171>Скрыта")));
        command.chatsetting.settings.put(FPlayer.Setting.REPLY, List.of(List.of("<fcolor:2>Команда /reply", "<color:#98FB98>Показывается"), List.of("<fcolor:2>Команда /reply", "<color:#ff7171>Скрыта")));
        command.chatsetting.settings.put(FPlayer.Setting.MAIL, List.of(List.of("<fcolor:2>Команда /mail", "<color:#98FB98>Показывается"), List.of("<fcolor:2>Команда /mail", "<color:#ff7171>Скрыта")));
        command.chatsetting.settings.put(FPlayer.Setting.TICTACTOE, List.of(List.of("<fcolor:2>Команда /tictactoe", "<color:#98FB98>Показывается"), List.of("<fcolor:2>Команда /tictactoe", "<color:#ff7171>Скрыта")));
        command.chatsetting.settings.put(FPlayer.Setting.KICK, List.of(List.of("<fcolor:2>Команда /kick", "<color:#98FB98>Показывается"), List.of("<fcolor:2>Команда /kick", "<color:#ff7171>Скрыта")));
        command.chatsetting.settings.put(FPlayer.Setting.TRANSLATETO, List.of(List.of("<fcolor:2>Команда /translate", "<color:#98FB98>Показывается"), List.of("<fcolor:2>Команда /translate", "<color:#ff7171>Скрыта")));
        command.chatsetting.settings.put(FPlayer.Setting.BROADCAST, List.of(List.of("<fcolor:2>Команда /broadcast", "<color:#98FB98>Показывается"), List.of("<fcolor:2>Команда /broadcast", "<color:#ff7171>Скрыта")));
        command.chatsetting.settings.put(FPlayer.Setting.DO, List.of(List.of("<fcolor:2>Команда /do", "<color:#98FB98>Показывается"), List.of("<fcolor:2>Команда /do", "<color:#ff7171>Скрыта")));
        command.chatsetting.settings.put(FPlayer.Setting.COIN, List.of(List.of("<fcolor:2>Команда /coin", "<color:#98FB98>Показывается"), List.of("<fcolor:2>Команда /coin", "<color:#ff7171>Скрыта")));
        command.chatsetting.settings.put(FPlayer.Setting.AFK, List.of(List.of("<fcolor:2>Команда /afk", "<color:#98FB98>Показывается"), List.of("<fcolor:2>Команда /afk", "<color:#ff7171>Скрыта")));
        command.chatsetting.settings.put(FPlayer.Setting.POLL, List.of(List.of("<fcolor:2>Команда /poll", "<color:#98FB98>Показывается"), List.of("<fcolor:2>Команда /poll", "<color:#ff7171>Скрыта")));
        command.chatsetting.settings.put(FPlayer.Setting.SPIT, List.of(List.of("<fcolor:2>Команда /spit", "<color:#98FB98>Показывается"), List.of("<fcolor:2>Команда /spit", "<color:#ff7171>Скрыта")));
        command.chatsetting.settings.put(FPlayer.Setting.GREETING, List.of(List.of("<fcolor:2>Приветственное сообщение", "<color:#98FB98>Показывается"), List.of("<fcolor:2>Приветственное сообщение", "<color:#ff7171>Скрыта")));
        command.chatsetting.settings.put(FPlayer.Setting.ROCKPAPERSCISSORS, List.of(List.of("<fcolor:2>Команда /rockpaperscissors", "<color:#98FB98>Показывается"), List.of("<fcolor:2>Команда /rockpaperscissors", "<color:#ff7171>Скрыта")));
        command.chatsetting.settings.put(FPlayer.Setting.DISCORD, List.of(List.of("<fcolor:2>Сообщения для/из Discord", "<color:#98FB98>Показываются"), List.of("<fcolor:2>Сообщения для/из Discord", "<color:#ff7171>Скрыты")));
        command.chatsetting.settings.put(FPlayer.Setting.TELEGRAM, List.of(List.of("<fcolor:2>Сообщения для/из Telegram", "<color:#98FB98>Показываются"), List.of("<fcolor:2>Сообщения для/из Telegram", "<color:#ff7171>Скрыты")));
        command.chatsetting.settings.put(FPlayer.Setting.TWITCH, List.of(List.of("<fcolor:2>Сообщения для/из Twitch", "<color:#98FB98>Показываются"), List.of("<fcolor:2>Сообщения для/из Twitch", "<color:#ff7171>Скрыты")));
        command.chatsetting.header = "          Настройки чата";
        command.chatsetting.disable.you = "<color:#ff7171><b>⁉</b> Команда скрыта, проверь /chatsetting";
        command.chatsetting.disable.he = "<color:#ff7171><b>⁉</b> Он выключил команду с помощью /chatsetting";
        command.chatsetting.disable.server = "<color:#ff7171><b>⁉</b> Команда отключена на сервере";

        command.symbol.format = "<click:suggest_command:\"<message>\"><fcolor:2>\uD83D\uDDA5 Нажми, чтобы использовать: <fcolor:1><message>";
        command.symbol.categories.clear();
        command.symbol.categories.put("activities", "мероприятие");
        command.symbol.categories.put("animals", "животные");
        command.symbol.categories.put("arrows", "стрелки");
        command.symbol.categories.put("body", "тело");
        command.symbol.categories.put("clothes", "одежда");
        command.symbol.categories.put("environment", "окружение");
        command.symbol.categories.put("faces", "лицо");
        command.symbol.categories.put("food", "еда");
        command.symbol.categories.put("greenery", "зелень");
        command.symbol.categories.put("hands", "руки");
        command.symbol.categories.put("misc", "разное");
        command.symbol.categories.put("numbers", "числа");
        command.symbol.categories.put("people", "люди");
        command.symbol.categories.put("shapes", "фигуры");
        command.symbol.categories.put("symbols", "символы");
        command.symbol.categories.put("things", "вещи");
        command.symbol.categories.put("transport", "транспорт");

        command.mail.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        command.mail.sender = "<fcolor:2>✉ Письмо #<id> для <display_name> » <fcolor:1><message>";
        command.mail.receiver = "<fcolor:2>✉ Письмо от <display_name> » <fcolor:1><message>";

        command.clearmail.nullMail = "<color:#ff7171><b>⁉</b> Письма не найдено";
        command.clearmail.format = "<fcolor:2>✉ [УДАЛЕНО] Письмо #<id> для <display_name> » <fcolor:1><message>";

        command.tictactoe.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        command.tictactoe.myself = "<color:#ff7171><b>⁉</b> Ты не можешь играть с самим собой";
        command.tictactoe.sender = "<fcolor:1>☐ Предложение сыграть в крестики-нолики отправлено для <display_name>";
        command.tictactoe.formatCreate = "<click:run_command:\"/tictactoe %d create\"><fcolor:1>☐ Есть предложение сыграть в крестики-нолики от <display_name>, принять? [+]";
        command.tictactoe.wrongGame = "<color:#ff7171><b>⁉</b> Этой игры не существует";
        command.tictactoe.wrongByPlayer = "<color:#ff7171><b>⁉</b> Игра закончена, потому что один из игроков не в сети";
        command.tictactoe.wrongMove = "<color:#ff7171><b>⁉</b> Такой ход невозможен";
        command.tictactoe.formatMove = "<fcolor:2>Ход <player> </fcolor:2>";
        command.tictactoe.lastMove = "<fcolor:2>Последний ход (<move>)</fcolor:2>";
        command.tictactoe.formatWin = "<color:#98FB98><player> выйграл</color:#98FB98>";
        command.tictactoe.formatDraw = "<color:#98FB98>Ничья \uD83D\uDC6C</color:#98FB98>";
        command.tictactoe.symbol.empty = "<hover:show_text:\"<fcolor:1>Ход <move>\"><click:run_command:\"/tictactoe %d <move>\">☐</click></hover>";

        command.maintenance.already = "<color:#ff7171><b>⁉</b> Технические работы уже идут";
        command.maintenance.not = "<color:#ff7171><b>⁉</b> Технические работы не идут";
        command.maintenance.kick = "<color:#ff7171>★ На сервере ведутся технические работы";
        command.maintenance.serverDescription = "<color:#ff7171>В настоящее время проводятся технические работы";
        command.maintenance.serverVersion = "Технические работы";
        command.maintenance.formatTrue = "<fcolor:1>★ Ты <fcolor:2>включил</fcolor:2> технические работы на сервере";
        command.maintenance.formatFalse = "<fcolor:1>★ Ты <fcolor:2>выключил</fcolor:2> технические работы на сервере";

        command.rockpaperscissors.nullPlayer = "<color:#ff7171><b>⁉</b> Игрок не найден";
        command.rockpaperscissors.wrongMove = "<color:#ff7171><b>⁉</b> Такой ход невозможен";
        command.rockpaperscissors.already = "<color:#ff7171><b>⁉</b> Ты уже сходил";
        command.rockpaperscissors.nullGame = "<color:#ff7171><b>⁉</b> Этой игры не существует";
        command.rockpaperscissors.myself = "<color:#ff7171><b>⁉</b> Ты не можешь играть с самим собой";
        command.rockpaperscissors.formatMove = "<fcolor:2>✂ <fcolor:1>Выбери свой ход <fcolor:2><click:run_command:\"/rps <target> rock <uuid>\">[\uD83E\uDEA8 камень]</click> <click:run_command:\"/rps <target> scissors <uuid>\">[✂ ножницы]</click> <click:run_command:\"/rps <target> paper <uuid>\">[\uD83E\uDDFB бумага]</click>";
        command.rockpaperscissors.sender = "<fcolor:2>✂ <fcolor:1>Теперь ходит <display_name>";
        command.rockpaperscissors.receiver = "<fcolor:2>✂ <display_name> <fcolor:1>предложил сыграть в камень-ножницы-бумага";
        command.rockpaperscissors.formatDraw = "<color:#98FB98>✂ Ничья! Вы оба выбрали <b><move>";
        command.rockpaperscissors.formatWin = "<color:#98FB98>✂ Выйграл <display_name>! <b><sender_move></b> на <b><receiver_move></b>";
        command.rockpaperscissors.strategies.clear();
        command.rockpaperscissors.strategies.putAll(Map.of(
                "paper", "бумага",
                "rock", "камень",
                "scissors", "ножницы"
        ));

        command.prompt.message = "сообщение";
        command.prompt.hard = "сложно?";
        command.prompt.accept = "принять";
        command.prompt.turn = "включить";
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

        integration.discord.infoChannel.clear();
        integration.discord.infoChannel.put("айди", "ТПС <tps>");

        message.advancement.task.format = "<color:#4eff52>🌠 <fcolor:2><display_name> <fcolor:1>получил достижение <advancement>";
        message.advancement.task.tag = "<color:#4eff52>[<hover:show_text:\"<color:#4eff52><lang:<title>> <br><lang:<description>>\"><lang:<title>></hover>]";
        message.advancement.goal.format = "<color:#FFFF00>🌠 <fcolor:2><display_name> <fcolor:1>выполнил цель <advancement>";
        message.advancement.goal.tag = "<color:#FFFF00>[<hover:show_text:\"<color:#FFFF00><lang:<title>> <br><lang:<description>>\"><lang:<title>></hover>]";
        message.advancement.challenge.format = "<color:#834eff>🌠 <fcolor:2><display_name> <fcolor:1>завершил испытание <advancement>";
        message.advancement.challenge.tag = "<color:#834eff>[<hover:show_text:\"<color:#834eff><lang:<title>> <br><lang:<description>>\"><lang:<title>></hover>]";
        message.advancement.revoke.manyToOne = "<fcolor:1>🌠 Отозвано <fcolor:2><number></fcolor:2> достижений у игрока <display_name>";
        message.advancement.revoke.oneToOne = "<fcolor:1>🌠 Отозвано достижение <fcolor:2><advancement></fcolor:2> у игрока <display_name>";
        message.advancement.grant.manyToOne = "<fcolor:1>🌠 Выдано <fcolor:2><number></fcolor:2> достижений игроку <display_name>";
        message.advancement.grant.oneToOne = "<fcolor:1>🌠 Достижение <fcolor:2><advancement></fcolor:2> выдано игроку <display_name>";

        message.auto.types.clear();
        message.auto.types.put("announcement", new LinkedList<>(){
            {
                push("<br><fcolor:1>◇ Сервер использует <click:open_url:\"https://flectone.net/pulse/\"><hover:show_text:\"<fcolor:2>https://flectone.net/pulse/\"><fcolor:2>FlectonePulse</hover></click> :)<br>");
                push("<br><fcolor:1>      ❝ Заходи в дискорд ❠<br><fcolor:2>    <u><click:open_url:\"https://discord.flectone.net\"><hover:show_text:\"<fcolor:2>https://discord.flectone.net\">https://discord.flectone.net</hover></click></u><br>");
                push("<br><fcolor:1>⚡ Поддержи <fcolor:2>FlectonePulse</fcolor:2> на Boosty <br><fcolor:1>⚡ <u><click:open_url:\"https://boosty.to/thefaser/\"><hover:show_text:\"<fcolor:2>https://boosty.to/thefaser/\">https://boosty.to/thefaser/</hover></click></u><br>");
                push("<br><fcolor:1>   ✉ Заходи в телеграм ✉ <br><fcolor:2>    <u><click:open_url:\"https://t.me/flectone\"><hover:show_text:\"<fcolor:2>https://t.me/flectone\">https://t.me/flectone</hover></click></u><br>");
            }
        });

        message.brand.values.clear();
        message.brand.values.addAll(Arrays.asList("<white>Майнкрафт", "<aqua>Майнкрафт"));

        message.chat.types.clear();
        message.chat.types.putAll(Map.of(
                "local", "<display_name><fcolor:3>: <message> <translateto:\"<message_to_translate>\">",
                "global", "<display_name> <world_prefix>»<fcolor:4> <message> <translateto:\"<message_to_translate>\">"
        ));
        message.chat.nullChat = "<color:#ff7171><b>⁉</b> На сервер выключен чат";
        message.chat.nullRecipient = "<color:#ff7171><b>⁉</b> Тебя никто не услышал";

        message.clear.single = "<fcolor:1>\uD83C\uDF0A Удалено <fcolor:2><number></fcolor:2> предметов у игрока <display_name>";
        message.clear.multiple = "<fcolor:1>\uD83C\uDF0A Удалено <fcolor:2><number></fcolor:2> предметов у <fcolor:2><count></fcolor:2> игроков";

        message.contact.afk.formatTrue.global = "<gradient:#ffd500:#FFFF00>⌚ <player> отошёл";
        message.contact.afk.formatTrue.local = "<gradient:#ffd500:#FFFF00>⌚ Ты отошёл от игры";
        message.contact.afk.formatFalse.global = "<gradient:#ffd500:#FFFF00>⌚ <player> вернулся";
        message.contact.afk.formatFalse.local = "<gradient:#ffd500:#FFFF00>⌚ Ты вернулся в игру";

        message.contact.spit.format = "<fcolor:1>🫐 Тебя обплевал <display_name>!";

        message.death.types.put("death.attack.anvil", "<color:#778899>🪦 <fcolor:1><display_name> раздавлен упавшей наковальней");
        message.death.types.put("death.attack.anvil.player", "<color:#778899>🪦 <fcolor:1><display_name> был раздавлен упавшей наковальней, пока боролся с <killer>");
        message.death.types.put("death.attack.arrow", "<color:#778899>🪦 <fcolor:1><display_name> застрелен <killer>");
        message.death.types.put("death.attack.arrow.item", "<color:#778899>🪦 <fcolor:1><display_name> застрелен <killer> с помощью <fcolor:2>[<i><by_item></i>]");
        message.death.types.put("death.attack.badRespawnPoint.message", "<color:#778899>🪦 <fcolor:1><display_name> стал жертвой <fcolor:2>[<click:open_url:\"https://www.youtube.com/watch?v=dQw4w9WgXcQ\"><hover:show_text:\"<fcolor:2>MCPE-28723\">жестоких правил игры</hover></click>]");
        message.death.types.put("death.attack.cactus", "<color:#778899>🪦 <fcolor:1><display_name> исколот до смерти");
        message.death.types.put("death.attack.cactus.player", "<color:#778899>🪦 <fcolor:1><display_name> наткнулся на кактус, спасаясь от <killer>");
        message.death.types.put("death.attack.cramming", "<color:#778899>🪦 <fcolor:1><display_name> расплющен в лепёшку");
        message.death.types.put("death.attack.cramming.player", "<color:#778899>🪦 <fcolor:1><display_name> расплющен <killer>");
        message.death.types.put("death.attack.dragonBreath", "<color:#778899>🪦 <fcolor:1><display_name> испепелён дыханием дракона");
        message.death.types.put("death.attack.dragonBreath.player", "<color:#778899>🪦 <fcolor:1><display_name> сварился заживо в драконьем дыхании из-за <killer>");
        message.death.types.put("death.attack.drown", "<color:#778899>🪦 <fcolor:1><display_name> утонул");
        message.death.types.put("death.attack.drown.player", "<color:#778899>🪦 <fcolor:1><display_name> утонул, спасаясь от <killer>");
        message.death.types.put("death.attack.dryout", "<color:#778899>🪦 <fcolor:1><display_name> умер от обезвоживания");
        message.death.types.put("death.attack.dryout.player", "<color:#778899>🪦 <fcolor:1><display_name> умер от обезвоживания, спасаясь от <killer>");
        message.death.types.put("death.attack.even_more_magic", "<color:#778899>🪦 <fcolor:1><display_name> был убит неизведанной магией");
        message.death.types.put("death.attack.explosion", "<color:#778899>🪦 <fcolor:1><display_name> взорвался");
        message.death.types.put("death.attack.explosion.player", "<color:#778899>🪦 <fcolor:1><display_name> был взорван <killer>");
        message.death.types.put("death.attack.explosion.item", "<color:#778899>🪦 <fcolor:1><display_name> был взорван <killer> с помощью <fcolor:2>[<i><by_item></i>]");
        message.death.types.put("death.attack.explosion.player.item", "<color:#778899>🪦 <fcolor:1><display_name> был взорван <killer> с помощью <fcolor:2>[<i><by_item></i>]");
        message.death.types.put("death.attack.fall", "<color:#778899>🪦 <fcolor:1><display_name> разбился вдребезги");
        message.death.types.put("death.attack.fall.player", "<color:#778899>🪦 <fcolor:1><display_name> разбился вдребезги, спасаясь от <killer>");
        message.death.types.put("death.attack.fallingBlock", "<color:#778899>🪦 <fcolor:1><display_name> раздавлен упавшим блоком");
        message.death.types.put("death.attack.fallingBlock.player", "<color:#778899>🪦 <fcolor:1><display_name> был раздавлен упавшим блоком, пока боролся с <killer>");
        message.death.types.put("death.attack.fallingStalactite", "<color:#778899>🪦 <fcolor:1><display_name> был пронзён обрушившимся сталактитом");
        message.death.types.put("death.attack.fallingStalactite.player", "<color:#778899>🪦 <fcolor:1><display_name> был пронзён обрушившимся сталактитом, пока боролся с <killer>");
        message.death.types.put("death.attack.fireball", "<color:#778899>🪦 <fcolor:1><display_name> убит файерболом <killer>");
        message.death.types.put("death.attack.fireball.item", "<color:#778899>🪦 <fcolor:1><display_name> убит файерболом <killer> с помощью <fcolor:2>[<i><by_item></i>]");
        message.death.types.put("death.attack.fireworks", "<color:#778899>🪦 <fcolor:1><display_name> с треском разлетелся");
        message.death.types.put("death.attack.fireworks.item", "<color:#778899>🪦 <fcolor:1><display_name> с треском разлетелся из-за фейерверка <killer>, выпущенного из <fcolor:2>[<i><by_item></i>]");
        message.death.types.put("death.attack.fireworks.player", "<color:#778899>🪦 <fcolor:1><display_name> с треском разлетелся, пока боролся с <killer>");
        message.death.types.put("death.attack.flyIntoWall", "<color:#778899>🪦 <fcolor:1><display_name> преобразовал кинетическую энергию во внутреннюю");
        message.death.types.put("death.attack.flyIntoWall.player", "<color:#778899>🪦 <fcolor:1><display_name> преобразовал кинетическую энергию во внутреннюю, спасаясь от <killer>");
        message.death.types.put("death.attack.freeze", "<color:#778899>🪦 <fcolor:1><display_name> замёрз насмерть");
        message.death.types.put("death.attack.freeze.player", "<color:#778899>🪦 <fcolor:1><display_name> замёрз насмерть благодаря <killer>");
        message.death.types.put("death.attack.generic", "<color:#778899>🪦 <fcolor:1><display_name> умер");
        message.death.types.put("death.attack.generic.player", "<color:#778899>🪦 <fcolor:1><display_name> умер из-за <killer>");
        message.death.types.put("death.attack.genericKill", "<color:#778899>🪦 <fcolor:1><display_name> убит");
        message.death.types.put("death.attack.genericKill.player", "<color:#778899>🪦 <fcolor:1><display_name> был убит, сражаясь с <killer>");
        message.death.types.put("death.attack.hotFloor", "<color:#778899>🪦 <fcolor:1><display_name> обнаружил, что пол — это лава");
        message.death.types.put("death.attack.hotFloor.player", "<color:#778899>🪦 <fcolor:1><display_name> зашёл в опасную зону из-за <killer>");
        message.death.types.put("death.attack.inFire", "<color:#778899>🪦 <fcolor:1><display_name> умер в огне");
        message.death.types.put("death.attack.inFire.player", "<color:#778899>🪦 <fcolor:1><display_name> сгорел в огне, пока боролся с <killer>");
        message.death.types.put("death.attack.inWall", "<color:#778899>🪦 <fcolor:1><display_name> погребён заживо");
        message.death.types.put("death.attack.inWall.player", "<color:#778899>🪦 <fcolor:1><display_name> был погребён заживо, пока боролся с <killer>");
        message.death.types.put("death.attack.indirectMagic", "<color:#778899>🪦 <fcolor:1><display_name> был убит <killer> с помощью магии");
        message.death.types.put("death.attack.indirectMagic.item", "<color:#778899>🪦 <fcolor:1><display_name> был убит <killer> с помощью <fcolor:2>[<i><by_item></i>]");
        message.death.types.put("death.attack.lava", "<color:#778899>🪦 <fcolor:1><display_name> решил поплавать в лаве");
        message.death.types.put("death.attack.lava.player", "<color:#778899>🪦 <fcolor:1><display_name> упал в лаву, убегая от <killer>");
        message.death.types.put("death.attack.lightningBolt", "<color:#778899>🪦 <fcolor:1><display_name> был поражён молнией");
        message.death.types.put("death.attack.lightningBolt.player", "<color:#778899>🪦 <fcolor:1><display_name> был поражён молнией, пока боролся с <killer>");
        message.death.types.put("death.attack.mace_smash", "<color:#778899>🪦 <fcolor:1><display_name> был сокрушён <killer>");
        message.death.types.put("death.attack.mace_smash.item", "<color:#778899>🪦 <fcolor:1><display_name> был сокрушён <killer> с помощью <fcolor:2>[<i><by_item></i>]");
        message.death.types.put("death.attack.magic", "<color:#778899>🪦 <fcolor:1><display_name> был убит магией");
        message.death.types.put("death.attack.magic.player", "<color:#778899>🪦 <fcolor:1><display_name> был убит магией, убегая от <killer>");
        message.death.types.put("death.attack.mob", "<color:#778899>🪦 <fcolor:1><display_name> был убит <killer>");
        message.death.types.put("death.attack.mob.item", "<color:#778899>🪦 <fcolor:1><display_name> был убит <killer> с помощью <fcolor:2>[<i><by_item></i>]");
        message.death.types.put("death.attack.onFire", "<color:#778899>🪦 <fcolor:1><display_name> сгорел заживо");
        message.death.types.put("death.attack.onFire.item", "<color:#778899>🪦 <fcolor:1><display_name> был сожжён дотла, пока боролся с <killer>, держащим <fcolor:2>[<i><by_item></i>]");
        message.death.types.put("death.attack.onFire.player", "<color:#778899>🪦 <fcolor:1><display_name> был сожжён дотла, пока боролся с <killer>");
        message.death.types.put("death.attack.outOfWorld", "<color:#778899>🪦 <fcolor:1><display_name> выпал из мира");
        message.death.types.put("death.attack.outOfWorld.player", "<color:#778899>🪦 <fcolor:1><display_name> не захотел жить в том же мире, что и <killer>");
        message.death.types.put("death.attack.outsideBorder", "<color:#778899>🪦 <fcolor:1><display_name> покинул пределы этого мира");
        message.death.types.put("death.attack.outsideBorder.player", "<color:#778899>🪦 <fcolor:1><display_name> покинул пределы этого мира, пока боролся с <killer>");
        message.death.types.put("death.attack.player", "<color:#778899>🪦 <fcolor:1><display_name> был убит <killer>");
        message.death.types.put("death.attack.player.item", "<color:#778899>🪦 <fcolor:1><display_name> был убит <killer> с помощью <fcolor:2>[<i><by_item></i>]");
        message.death.types.put("death.attack.sonic_boom", "<color:#778899>🪦 <fcolor:1><display_name> был уничтожен звуковым зарядом");
        message.death.types.put("death.attack.sonic_boom.item", "<color:#778899>🪦 <fcolor:1><display_name> был уничтожен звуковым зарядом, спасаясь от <killer>, держащего <fcolor:2>[<i><by_item></i>]");
        message.death.types.put("death.attack.sonic_boom.player", "<color:#778899>🪦 <fcolor:1><display_name> был уничтожен звуковым зарядом, спасаясь от <killer>");
        message.death.types.put("death.attack.stalagmite", "<color:#778899>🪦 <fcolor:1><display_name> пронзён сталагмитом");
        message.death.types.put("death.attack.stalagmite.player", "<color:#778899>🪦 <fcolor:1><display_name> был пронзён сталагмитом, пока боролся с <killer>");
        message.death.types.put("death.attack.starve", "<color:#778899>🪦 <fcolor:1><display_name> умер от голода");
        message.death.types.put("death.attack.starve.player", "<color:#778899>🪦 <fcolor:1><display_name> умер от голода, пока боролся с <killer>");
        message.death.types.put("death.attack.sting", "<color:#778899>🪦 <fcolor:1><display_name> изжален до смерти");
        message.death.types.put("death.attack.sting.item", "<color:#778899>🪦 <fcolor:1><display_name> был изжален до смерти <killer> с помощью <fcolor:2>[<i><by_item></i>]");
        message.death.types.put("death.attack.sting.player", "<color:#778899>🪦 <fcolor:1><display_name> изжален до смерти <killer>");
        message.death.types.put("death.attack.sweetBerryBush", "<color:#778899>🪦 <fcolor:1><display_name> искололся до смерти в кустах сладких ягод");
        message.death.types.put("death.attack.sweetBerryBush.player", "<color:#778899>🪦 <fcolor:1><display_name> искололся до смерти в кустах сладких ягод, спасаясь от <killer>");
        message.death.types.put("death.attack.thorns", "<color:#778899>🪦 <fcolor:1><display_name> был убит, пытаясь навредить <killer>");
        message.death.types.put("death.attack.thorns.item", "<color:#778899>🪦 <fcolor:1><display_name> был убит <fcolor:2>[<i><by_item></i>]</fcolor:2>, пытаясь навредить <killer>");
        message.death.types.put("death.attack.thrown", "<color:#778899>🪦 <fcolor:1><display_name> был избит <killer>");
        message.death.types.put("death.attack.thrown.item", "<color:#778899>🪦 <fcolor:1><display_name> был избит <killer> с помощью <fcolor:2>[<i><by_item></i>]");
        message.death.types.put("death.attack.trident", "<color:#778899>🪦 <fcolor:1><display_name> был пронзён <killer>");
        message.death.types.put("death.attack.trident.item", "<color:#778899>🪦 <fcolor:1><display_name> пронзён <killer> с помощью <fcolor:2>[<i><by_item></i>]");
        message.death.types.put("death.attack.wither", "<color:#778899>🪦 <fcolor:1><display_name> иссушён");
        message.death.types.put("death.attack.wither.player", "<color:#778899>🪦 <fcolor:1><display_name> был иссушён, пока боролся с <killer>");
        message.death.types.put("death.attack.witherSkull", "<color:#778899>🪦 <fcolor:1><display_name> был поражён черепом из <killer>");
        message.death.types.put("death.attack.witherSkull.item", "<color:#778899>🪦 <fcolor:1><display_name> был поражён черепом из <killer> с помощью <fcolor:2>[<i><by_item></i>]");
        message.death.types.put("death.fell.accident.generic", "<color:#778899>🪦 <fcolor:1><display_name> разбился насмерть");
        message.death.types.put("death.fell.accident.ladder", "<color:#778899>🪦 <fcolor:1><display_name> свалился с лестницы");
        message.death.types.put("death.fell.accident.other_climbable", "<color:#778899>🪦 <fcolor:1><display_name> сорвался");
        message.death.types.put("death.fell.accident.scaffolding", "<color:#778899>🪦 <fcolor:1><display_name> сорвался с подмосток");
        message.death.types.put("death.fell.accident.twisting_vines", "<color:#778899>🪦 <fcolor:1><display_name> сорвался с вьющейся лозы");
        message.death.types.put("death.fell.accident.vines", "<color:#778899>🪦 <fcolor:1><display_name> сорвался с лианы");
        message.death.types.put("death.fell.accident.weeping_vines", "<color:#778899>🪦 <fcolor:1><display_name> сорвался с плакучей лозы");
        message.death.types.put("death.fell.assist", "<color:#778899>🪦 <fcolor:1><display_name> свалился благодаря <killer>");
        message.death.types.put("death.fell.assist.item", "<color:#778899>🪦 <fcolor:1><display_name> был обречён на падение <killer> с помощью <fcolor:2>[<i><by_item></i>]");
        message.death.types.put("death.fell.finish", "<color:#778899>🪦 <fcolor:1><display_name> упал с высоты и был добит <killer>");
        message.death.types.put("death.fell.finish.item", "<color:#778899>🪦 <fcolor:1><display_name> упал с высоты и был добит <killer> с помощью <fcolor:2>[<i><by_item></i>]");
        message.death.types.put("death.fell.killer", "<color:#778899>🪦 <fcolor:1><display_name> был обречён на падение");

        message.deop.format = "<fcolor:1>\uD83E\uDD16 <display_name> больше не является оператором сервера";

        message.enchant.single = "<fcolor:1>\uD83D\uDCD6 Наложены чары «<fcolor:2><lang:<enchant>> <lang:<level>></fcolor:2>» на предмет <display_name>";
        message.enchant.multiple = "<fcolor:1>\uD83D\uDCD6 Наложены чары «<fcolor:2><lang:<enchant>> <lang:<level>></fcolor:2>» на предмет <fcolor:2><count></fcolor:2> сущностей";

        message.format.mention.person = "<fcolor:2>Тебя упомянули!";

        message.format.tags.put(TagType.URL, "<click:open_url:\"<message>\"><hover:show_text:\"<fcolor:2>Открыть ссылку <br><u><message>\"><fcolor:2><u>🗗 Ссылка</u></fcolor:2></hover></click>");
        message.format.tags.put(TagType.IMAGE, "<image:\"<message>\"><u>🖃 Картинка</u></image>");
        message.format.tags.put(TagType.SKIN, "<image:\"<message>\"><u>👨 Скин</u></image>");

        message.format.name_.display = "<click:suggest_command:\"/msg <player> \"><hover:show_text:\"<fcolor:2>Написать <player>\"><vault_prefix><stream_prefix><fcolor:2><player></fcolor><afk_suffix><vault_suffix></hover></click>";
        message.format.name_.entity = "<fcolor:2><hover:show_text:\"<fcolor:2><lang:<name>> <br><fcolor:1>Тип <fcolor:2><lang:<type>> <br><fcolor:1>Айди <fcolor:2><uuid>\"><lang:<name>></hover></fcolor:2>";
        message.format.name_.unknown = "<fcolor:2><name></fcolor:2>";

        message.format.translate.action = "<click:run_command:\"/translateto <language> <language> <message>\"><hover:show_text:\"<fcolor:2>Перевести сообщение\"><fcolor:1>[📖]";

        message.format.questionAnswer.questions.clear();
        message.format.questionAnswer.questions.put("server", "<fcolor:2>[Вопрос-Ответ] @<player><fcolor:1>, это ванильный сервер в Майнкрафте!");
        message.format.questionAnswer.questions.put("flectone", "<fcolor:2>[Вопрос-Ответ] @<player><fcolor:1>, это бренд и проекты созданные TheFaser'ом");

        message.gamemode.self.creative = "<fcolor:1>\uD83D\uDDD8 Твой режим игры изменён на <fcolor:2>Творческий режим";
        message.gamemode.self.survival = "<fcolor:1>\uD83D\uDDD8 Твой режим игры изменён на <fcolor:2>Режим выживания";
        message.gamemode.self.adventure = "<fcolor:1>\uD83D\uDDD8 Твой режим игры изменён на <fcolor:2>Режим приключения";
        message.gamemode.self.spectator = "<fcolor:1>\uD83D\uDDD8 Твой режим игры изменён на <fcolor:2>Режим наблюдателя";
        message.gamemode.other.creative = "<fcolor:1>\uD83D\uDDD8 Режим игры игрока <display_name> изменён на <fcolor:2>Творческий режим";
        message.gamemode.other.survival = "<fcolor:1>\uD83D\uDDD8 Режим игры игрока <display_name> изменён на <fcolor:2>Режим выживания";
        message.gamemode.other.adventure = "<fcolor:1>\uD83D\uDDD8 Режим игры игрока <display_name> изменён на <fcolor:2>Режим приключения";
        message.gamemode.other.spectator = "<fcolor:1>\uD83D\uDDD8 Режим игры игрока <display_name> изменён на <fcolor:2>Режим наблюдателя";

        message.greeting.format = "<br>[#][#][#][#][#][#][#][#]<br>[#][#][#][#][#][#][#][#]<br>[#][#][#][#][#][#][#][#]<br>[#][#][#][#][#][#][#][#]<br>[#][#][#][#][#][#][#][#]  <fcolor:1>Привет,<br>[#][#][#][#][#][#][#][#]  <player><br>[#][#][#][#][#][#][#][#]<br>[#][#][#][#][#][#][#][#]<br>";

        message.join.formatFirstTime = "<color:#4eff52>→ <display_name> <fcolor:1>впервые тут!";

        message.op.format = "<fcolor:1>\uD83E\uDD16 <display_name> назначен оператором сервера";

        message.scoreboard.values.clear();
        message.scoreboard.values.addAll(new LinkedList<>(){
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

        message.seed.format = "<fcolor:1>\uD83C\uDF10 Ключ генератора: [<fcolor:2><hover:show_text:'<fcolor:2>Нажми, чтобы скопировать в буфер обмена'><click:copy_to_clipboard:<seed>><seed></click></fcolor:2>]";

        message.setblock.format = "<fcolor:1>⏹ Изменён блок в точке <fcolor:2><x></fcolor:2>, <fcolor:2><y></fcolor:2>, <fcolor:2><z></fcolor:2>";

        message.spawnpoint.single = "<fcolor:1>\uD83D\uDECC Установлена точка возрождения <fcolor:2><x></fcolor:2>, <fcolor:2><y></fcolor:2>, <fcolor:2><z></fcolor:2> [<fcolor:2><angle></fcolor:2>] в <fcolor:2><world></fcolor:2> для <display_name>";
        message.spawnpoint.multiple = "<fcolor:1>\uD83D\uDECC Установлена точка возрождения <fcolor:2><x></fcolor:2>, <fcolor:2><y></fcolor:2>, <fcolor:2><z></fcolor:2> [<fcolor:2><angle></fcolor:2>] в <fcolor:2><world></fcolor:2> для <fcolor:2><count></fcolor:2> игроков";

        message.status.motd.values.clear();
        message.status.motd.values.addAll(List.of(
                "<fcolor:1>Добро пожаловать на сервер!",
                "<fcolor:1>Присоединяйся и наслаждайся уникальным опытом игры!",
                "<fcolor:1>У нас дружелюбное сообщество - будь вежлив и уважай других!",
                "<fcolor:1>Приятной игры! Если есть вопросы, обращайся к администрации"
        ));

        message.status.version.name = "Майнкрафт сервер";

        message.status.players.full = "<color:#ff7171>Сервер полон";

        message.tab.footer.lists.clear();
        message.tab.footer.lists.addAll(new LinkedList<>(){
            {
                push(new LinkedList<>(){
                    {
                        push(" ");
                        push("<fcolor:1>Привет <fcolor:2><player></fcolor:2>!");
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
    public static final class Command implements CommandConfig, Localizable {

        private Prompt prompt = new Prompt();

        @Override
        public SubCommandConfig getAfk() {
            return null;
        }

        @Override
        public SubCommandConfig getMark() {
            return null;
        }

        @Override
        public SubCommandConfig getSpit() {
            return null;
        }

        @Getter
        @NoArgsConstructor
        public static final class Prompt {
            private String message = "message";
            private String hard = "hard";
            private String accept = "accept";
            private String turn = "turn on";
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
        }

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
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/command/maintenace/")})
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
        public static final class Ball implements SubCommandConfig, Localizable {
            private String format = "<color:#9370DB>❓ <display_name> asked: <message> <br>\uD83D\uDD2E Ball answered: <u><answer></u>";
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
            private String server = "<color:#ff7171>🔒 <fcolor:2><moderator></fcolor> was banned player <fcolor:2><player></fcolor> <fcolor:1><hover:show_text:\"<fcolor:1>ID: <id><br>Date: <date><br>Time: <time><br>Remaining time: <time_left><br>Moderator: <moderator><br>Reason: <reason>\">[INFO]</hover>";
            private String person = "<color:#ff7171>🔒 BAN 🔒<fcolor:1><br><br>Date: <date><br><br>Time: <time><br><br>Remaining time: <time_left><br><br>Moderator: <moderator><br><br>Reason: <reason>";
            private String connectionAttempt = "<color:#ff7171>🔒 Banned <fcolor:2><player></fcolor> tried to log in <fcolor:1><hover:show_text:\"<fcolor:1>ID: <id><br>Date: <date><br>Time: <time><br>Remaining time: <time_left><br>Moderator: <moderator><br>Reason: <reason>\">[INFO]</hover>";

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
                    "<br>▋ <fcolor:2><click:run_command:\"<command> <prev_page>\">←</click> <fcolor:1>Page: <current_page>/<last_page> <fcolor:2><click:run_command:\"<command> <next_page>\">→"
            );

            private ListTypeMessage player = new ListTypeMessage(
                    "<fcolor:2>▋ All bans: <count> <br>",
                    "<hover:show_text:\"<fcolor:1>Click to unban <display_name>\"><click:run_command:\"<command>\"><color:#ff7171>☒ <display_name></click></hover> <fcolor:1><hover:show_text:\"<fcolor:1>ID: <id><br>Date: <date><br>Time: <time><br>Moderator: <moderator><br>Reason: <reason>\">[INFO]</hover>",
                    "<br>▋ <fcolor:2><click:run_command:\"<command> <prev_page>\">←</click> <fcolor:1>Page: <current_page>/<last_page> <fcolor:2><click:run_command:\"<command> <next_page>\">→"
            );
        }

        @Getter
        public static final class Broadcast implements SubCommandConfig, Localizable {
            private String format = "<color:#fce303>\uD83D\uDD6B Message for all from <display_name> <br>❝ <message> ❠";
        }

        @Getter
        public static final class Chatcolor implements SubCommandConfig, Localizable {
            private String nullPlayer = "<color:#ff7171><b>⁉</b> This player does not exist";
            private String nullColor = "<color:#ff7171><b>⁉</b> Incorrect colors entered";
            private String format = "<color:#98FB98>★ You set <fcolor:1>message</fcolor> <fcolor:2>message</fcolor> <fcolor:3>message <fcolor:4>message";
        }

        @Getter
        public static final class Chatsetting implements SubCommandConfig, Localizable {
            private String noPermission = "<color:#ff7171><b>⁉</b> No permission to change this setting";
            private Disable disable = new Disable();

            private String header = "            Chat Settings";

            @Getter
            @NoArgsConstructor
            public static final class Disable {
                private String you = "<color:#ff7171><b>⁉</b> Display of this command is disabled, check /chatsetting";
                private String he = "<color:#ff7171><b>⁉</b> He disabled this option via /chatsetting";
                private String server = "<color:#ff7171><b>⁉</b> This command disabled on server";
            }

            private Map<FPlayer.Setting, List<List<String>>> settings = new LinkedHashMap<>(){
                {
                    put(FPlayer.Setting.CHAT, List.of(List.of("<fcolor:2>Chat", "<fcolor:1>Your chat <bold><chat>")));
                    put(FPlayer.Setting.COLOR, List.of(List.of("<fcolor:2>Colors", "<fcolor:1>message", "<fcolor:2>message", "<fcolor:3>message", "<fcolor:4>message")));
                    put(FPlayer.Setting.STREAM, List.of(List.of("<fcolor:2>Stream", "<color:#98FB98>You're streaming"), List.of("<fcolor:2>Stream", "<color:#ff7171>You don't stream")));
                    put(FPlayer.Setting.SPY, List.of(List.of("<fcolor:2>Spy", "<color:#98FB98>You're spying"), List.of("<fcolor:2>Spy", "<color:#ff7171>You're not spying")));
                    put(FPlayer.Setting.ADVANCEMENT, List.of(List.of("<fcolor:2>Advancement", "<color:#98FB98>Display enabled"), List.of("<fcolor:2>Advancement", "<color:#ff7171>Display disabled")));
                    put(FPlayer.Setting.DEATH, List.of(List.of("<fcolor:2>Death", "<color:#98FB98>Display enabled"), List.of("<fcolor:2>Death", "<color:#ff7171>Display disabled")));
                    put(FPlayer.Setting.JOIN, List.of(List.of("<fcolor:2>Join", "<color:#98FB98>Display enabled"), List.of("<fcolor:2>Join", "<color:#ff7171>Display disabled")));
                    put(FPlayer.Setting.QUIT, List.of(List.of("<fcolor:2>Quit", "<color:#98FB98>Display enabled"), List.of("<fcolor:2>Quit", "<color:#ff7171>Display disabled")));
                    put(FPlayer.Setting.AUTO, List.of(List.of("<fcolor:2>Auto Message", "<color:#98FB98>Display enabled"), List.of("<fcolor:2>Auto Message", "<color:#ff7171>Display disabled")));
                    put(FPlayer.Setting.ME, List.of(List.of("<fcolor:2>Command /me", "<color:#98FB98>Display enabled"), List.of("<fcolor:2>Command /me", "<color:#ff7171>Display disabled")));
                    put(FPlayer.Setting.TRY, List.of(List.of("<fcolor:2>Command /try", "<color:#98FB98>Display enabled"), List.of("<fcolor:2>Command /try", "<color:#ff7171>Display disabled")));
                    put(FPlayer.Setting.DICE, List.of(List.of("<fcolor:2>Command /dice", "<color:#98FB98>Display enabled"), List.of("<fcolor:2>Command /dice", "<color:#ff7171>Display disabled")));
                    put(FPlayer.Setting.BALL, List.of(List.of("<fcolor:2>Command /ball", "<color:#98FB98>Display enabled"), List.of("<fcolor:2>Command /ball", "<color:#ff7171>Display disabled")));
                    put(FPlayer.Setting.MUTE, List.of(List.of("<fcolor:2>Command /mute", "<color:#98FB98>Display enabled"), List.of("<fcolor:2>Command /mute", "<color:#ff7171>Display disabled")));
                    put(FPlayer.Setting.BAN, List.of(List.of("<fcolor:2>Command /ban", "<color:#98FB98>Display enabled"), List.of("<fcolor:2>Command /ban", "<color:#ff7171>Display disabled")));
                    put(FPlayer.Setting.WARN, List.of(List.of("<fcolor:2>Command /warn", "<color:#98FB98>Display enabled"), List.of("<fcolor:2>Command /warn", "<color:#ff7171>Display disabled")));
                    put(FPlayer.Setting.TELL, List.of(List.of("<fcolor:2>Command /tell", "<color:#98FB98>Display enabled"), List.of("<fcolor:2>Command /tell", "<color:#ff7171>Display disabled")));
                    put(FPlayer.Setting.REPLY, List.of(List.of("<fcolor:2>Command /reply", "<color:#98FB98>Display enabled"), List.of("<fcolor:2>Command /reply", "<color:#ff7171>Display disabled")));
                    put(FPlayer.Setting.MAIL, List.of(List.of("<fcolor:2>Command /mail", "<color:#98FB98>Display enabled"), List.of("<fcolor:2>Command /mail", "<color:#ff7171>Display disabled")));
                    put(FPlayer.Setting.TICTACTOE, List.of(List.of("<fcolor:2>Command /tictactoe", "<color:#98FB98>Display enabled"), List.of("<fcolor:2>Command /tictactoe", "<color:#ff7171>Display disabled")));
                    put(FPlayer.Setting.KICK, List.of(List.of("<fcolor:2>Command /kick", "<color:#98FB98>Display enabled"), List.of("<fcolor:2>Command /kick", "<color:#ff7171>Display disabled")));
                    put(FPlayer.Setting.TRANSLATETO, List.of(List.of("<fcolor:2>Command /translateto", "<color:#98FB98>Display enabled"), List.of("<fcolor:2>Command /translateto", "<color:#ff7171>Display disabled")));
                    put(FPlayer.Setting.BROADCAST, List.of(List.of("<fcolor:2>Command /broadcast", "<color:#98FB98>Display enabled"), List.of("<fcolor:2>Command /broadcast", "<color:#ff7171>Display disabled")));
                    put(FPlayer.Setting.DO, List.of(List.of("<fcolor:2>Command /do", "<color:#98FB98>Display enabled"), List.of("<fcolor:2>Command /do", "<color:#ff7171>Display disabled")));
                    put(FPlayer.Setting.COIN, List.of(List.of("<fcolor:2>Command /coin", "<color:#98FB98>Display enabled"), List.of("<fcolor:2>Command /coin", "<color:#ff7171>Display disabled")));
                    put(FPlayer.Setting.AFK, List.of(List.of("<fcolor:2>Command /afk", "<color:#98FB98>Display enabled"), List.of("<fcolor:2>Command /afk", "<color:#ff7171>Display disabled")));
                    put(FPlayer.Setting.POLL, List.of(List.of("<fcolor:2>Command /poll", "<color:#98FB98>Display enabled"), List.of("<fcolor:2>Command /poll", "<color:#ff7171>Display disabled")));
                    put(FPlayer.Setting.SPIT, List.of(List.of("<fcolor:2>Command /spit", "<color:#98FB98>Display enabled"), List.of("<fcolor:2>Command /spit", "<color:#ff7171>Display disabled")));
                    put(FPlayer.Setting.GREETING, List.of(List.of("<fcolor:2>Greeting message", "<color:#98FB98>Display enabled"), List.of("<fcolor:2>Greeting message", "<color:#ff7171>Display disabled")));
                    put(FPlayer.Setting.ROCKPAPERSCISSORS, List.of(List.of("<fcolor:2>Command /rockpaperscissors", "<color:#98FB98>Display enabled"), List.of("<fcolor:2>Command /rockpaperscissors", "<color:#ff7171>Display disabled")));
                    put(FPlayer.Setting.DISCORD, List.of(List.of("<fcolor:2>Messages for/from Discord", "<color:#98FB98>Display enabled"), List.of("<fcolor:2>Messages for/from Discord", "<color:#ff7171>Display disabled")));
                    put(FPlayer.Setting.TELEGRAM, List.of(List.of("<fcolor:2>Messages for/from Telegram", "<color:#98FB98>Display enabled"), List.of("<fcolor:2>Messages for/from Telegram", "<color:#ff7171>Display disabled")));
                    put(FPlayer.Setting.TWITCH, List.of(List.of("<fcolor:2>Messages for/from Twitch", "<color:#98FB98>Display enabled"), List.of("<fcolor:2>Messages for/from Twitch", "<color:#ff7171>Display disabled")));
                }
            };
        }

        @Getter
        public static final class Clearchat implements SubCommandConfig, Localizable {
            private String nullPlayer = "<color:#ff7171><b>⁉</b> This player does not exist";
            private String format = "<fcolor:1>💬 Chat is cleared";
        }

        @Getter
        public static final class Clearmail implements SubCommandConfig, Localizable {
            private String nullMail = "<color:#ff7171><b>⁉</b> This mail does not exist";
            private String format = "<fcolor:2>✉ [REMOVED] Mail #<id> for <display_name> » <fcolor:1><message>";
        }

        @Getter
        public static final class Coin implements SubCommandConfig, Localizable {
            private String head = "heads";
            private String tail = "tails";
            private String format = "<fcolor:1>✎ <display_name> player got <result>";
            private String formatDraw = "<fcolor:1>✎ <display_name> player got edge :)";
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
            private String format = "<fcolor:1>✎ <display_name> roll <message> (<sum>)";
        }

        @Getter
        public static final class Do implements SubCommandConfig, Localizable {
            private String format = "<fcolor:1>✎ <message> (<i><display_name></i>)";
        }

        @Getter
        public static final class Flectonepulse implements SubCommandConfig, Localizable {
            private String formatFalse = "<color:#ff7171>★ An has error occurred while reloading";
            private String formatTrue = "<fcolor:2>★ <u>FlectonePulse</u> successfully reloaded! (<i><time></i>)";
            private String formatTrueText = "<fcolor:2>★ IMPORTANT! <br>Only texts have been updated, for a FULL reload you need to use: <br><fcolor:1>/flectonepulse reload";
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

            private String server = "<color:#ff7171>🔒 <fcolor:2><moderator></fcolor> was kicked player <fcolor:2><player></fcolor> <fcolor:1><hover:show_text:\"<fcolor:1>ID: <id><br>Date: <date><br>Moderator: <moderator><br>Reason: <reason>\">[INFO]</hover>";
            private String person = "<color:#ff7171>🔒 KICK 🔒 <fcolor:1><br><br>ID: <id><br><br>Date: <date><br><br>Moderator: <moderator><br><br>Reason: <reason>";
        }

        @Getter
        public static final class Mail implements SubCommandConfig, Localizable {
            private String nullPlayer = "<color:#ff7171><b>⁉</b> This player does not exist";
            private String sender = "<fcolor:2>✉ Mail #<id> for <display_name> » <fcolor:1><message>";
            private String receiver = "<fcolor:2>✉ Mail from <display_name> » <fcolor:1><message>";
        }

        @Getter
        public static final class Maintenance implements SubCommandConfig, Localizable {
            private String not = "<color:#ff7171><b>⁉</b> You have not maintenance";
            private String already = "<color:#ff7171><b>⁉</b> You have already maintenance";
            private String serverDescription = "<color:#ff7171>The server is under maintenance";
            private String serverVersion = "Maintenance";
            private String kick = "<color:#ff7171>★ The server is under maintenance";
            private String formatTrue = "<fcolor:1>★ You have <fcolor:2>enabled</fcolor:2> maintenance on the server";
            private String formatFalse = "<fcolor:1>★ You have <fcolor:2>disabled</fcolor:2> maintenance on the server";
        }

        @Getter
        public static final class Me implements SubCommandConfig, Localizable {
            private String format = "<fcolor:1>✎ <display_name> <message>";
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
            private String server = "<color:#ff7171>🔒 <fcolor:2><moderator></fcolor> was muted player <fcolor:2><player></fcolor> <fcolor:1><hover:show_text:\"<fcolor:1>ID: <id><br>Date: <date><br>Time: <time><br>Remaining time: <time_left><br>Moderator: <moderator><br>Reason: <reason>\">[INFO]</hover>";
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
                    "<br>▋ <fcolor:2><click:run_command:\"<command> <prev_page>\">←</click> <fcolor:1>Page: <current_page>/<last_page> <fcolor:2><click:run_command:\"<command> <next_page>\">→"
            );

            private ListTypeMessage player = new ListTypeMessage(
                    "<fcolor:2>▋ All mutes: <count> <br>",
                    "<hover:show_text:\"<fcolor:1>Click to unmute <display_name>\"><click:run_command:\"<command>\"><color:#ff7171>☒ <display_name></click></hover> <fcolor:1><hover:show_text:\"<fcolor:1>ID: <id><br>Date: <date><br>Time: <time><br>Moderator: <moderator><br>Reason: <reason>\">[MORE]</hover>",
                    "<br>▋ <fcolor:2><click:run_command:\"<command> <prev_page>\">←</click> <fcolor:1>Page: <current_page>/<last_page> <fcolor:2><click:run_command:\"<command> <next_page>\">→"
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
            private String format = "<fcolor:1>🖧 <fcolor:2><player>'s</fcolor:2> ping is <ping>";
        }

        @Getter
        public static final class Poll implements SubCommandConfig, Localizable {
            private String nullPoll = "<color:#ff7171><b>⁉</b> This poll does not exist";
            private String expired = "<color:#ff7171><b>⁉</b> This poll has already ended";
            private String already = "<color:#ff7171><b>⁉</b> You've already voted in this poll";
            private String voteTrue = "<color:#4eff52>👍 You voted for <answer_id> in poll #<id>. There are <count> of you";
            private String voteFalse = "<color:#ff4e4e>🖓 You rejected <answer_id> in poll #<id>. There are <count> without you";
            private String countAnswers = "<color:#4eff52><bold><count></bold> for [<answer_key>] - <answer_value> <br>";
            private String voteButton = "<color:#4eff52><hover:show_text:\"<color:#4eff52>Vote for <bold><answer_key>\"><click:run_command:\"/poll vote <id> <number>\">[<answer_key>] - <answer_value> <br>";
            private String formatStart = "<br><color:#fce303>🗐 There's a poll #<id> going on right now <br>❓ <message> <br><answers>";
            private String formatOver = "<br><color:#fce303>🗐 Poll #<id> is over <br>❓ <message> <br>Votes: <br><answers>";
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
            private String sender = "<fcolor:1>Now goes <display_name>";
            private String receiver = "<fcolor:2>✂ <display_name> <fcolor:1>suggested a game of rock-paper-scissors";
            private String formatMove = "<fcolor:2>✂ <fcolor:1>Choose your move <fcolor:2><click:run_command:\"/rps <target> rock <uuid>\">[🪨 rock]</click> <click:run_command:\"/rps <target> scissors <uuid>\">[✂ scissors]</click> <click:run_command:\"/rps <target> paper <uuid>\">[🧻 paper]</click>";
            private String formatWin = "<color:#98FB98>✂ Winning <player>! <b><sender_move></b> on <b><receiver_move></b>";
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
            private String urlTag = "<fcolor:2><click:open_url:\"<url>\"><hover:show_text:\"<fcolor:2><url>\"><url></hover></click>";
            private String formatStart = "<br><color:#ff4e4e>\uD83D\uDD14 <fcolor:1>Announcement <color:#ff4e4e>\uD83D\uDD14 <br><br><fcolor:1><display_name> started stream <br><br><urls>";
            private String formatEnd = "<fcolor:2>★ Thanks for streaming on our server!";
        }

        @Getter
        public static final class Symbol implements SubCommandConfig, Localizable {
            private String format = "<click:suggest_command:\"<message>\"><fcolor:2>🖥 Click for using: <fcolor:1><message>";
            private Map<String, String> categories = new LinkedHashMap<>(){
                {
                    put("activities", "activities");
                    put("animals", "animals");
                    put("arrows", "arrows");
                    put("body", "body");
                    put("clothes", "clothes");
                    put("environment", "environment");
                    put("faces", "faces");
                    put("food", "food");
                    put("greenery", "greenery");
                    put("hands", "hands");
                    put("misc", "misc");
                    put("numbers", "numbers");
                    put("people", "people");
                    put("shapes", "shapes");
                    put("symbols", "symbols");
                    put("things", "things");
                    put("transport", "transport");
                }
            };
        }

        @Getter
        public static final class Tell implements SubCommandConfig, Localizable {
            private String nullPlayer = "<color:#ff7171><b>⁉</b> This player does not exist";
            private String sender = "<fcolor:2>✉ You → <display_name> » <fcolor:1><message>";
            private String receiver = "<fcolor:2>✉ <display_name> → You » <fcolor:1><message>";
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
                private String empty = "<hover:show_text:\"<fcolor:1>Move <move>\"><click:run_command:\"/tictactoe %d <move>\">☐</click></hover>";
                private String first = "<fcolor:2>☑</fcolor:2>";
                private String firstRemove = "<color:#ff7171>☑</color:#ff7171>";
                private String firstWin = "<color:#98FB98>☑</color:#98FB98>";
                private String second = "<fcolor:2>☒</fcolor:2>";
                private String secondRemove = "<color:#ff7171>☒</color:#ff7171>";
                private String secondWin = "<color:#98FB98>☒</color:#98FB98>";
            }

            private String field = "<fcolor:1><br>|[#][#][#]| <title> <current_move> <br>|[#][#][#]| <br>|[#][#][#]| <last_move><br>";
            private String currentMove = "<fcolor:2>☐ → <symbol></fcolor:2>";
            private String lastMove = "<fcolor:2>Last move (<move>)</fcolor:2>";
            private String formatMove = "<fcolor:2><player>'s move</fcolor:2>";
            private String formatWin = "<color:#98FB98><player> won this game</color:#98FB98>";
            private String formatDraw = "<color:#98FB98>The game ended in a draw 👬</color:#98FB98>";
            private String sender = "<fcolor:1>☐ An offer to play was sent to <display_name>";
            private String formatCreate = "<click:run_command:\"/tictactoe %d create\"><fcolor:1>☐ Received an invite to play tic-tac-toe with <display_name>, accept? [+]";
        }

        @Getter
        public static final class Translateto implements SubCommandConfig, Localizable {
            private String nullOrError = "<color:#ff7171><b>⁉</b> Error, you may have specified an unsupported language";
            private String format = "<fcolor:1>📖 Translation to [<language>] → <fcolor:2><message>";
        }

        @Getter
        public static final class Try implements SubCommandConfig, Localizable {
            private String formatTrue = "<color:#98FB98>☺ <display_name> <message> <percent>%";
            private String formatFalse = "<color:#F08080>☹ <display_name> <message> <percent>%";
        }

        @Getter
        public static final class Unban implements SubCommandConfig, Localizable {
            private String nullPlayer = "<color:#ff7171><b>⁉</b> This player does not exist";
            private String notBanned = "<color:#ff7171><b>⁉</b> This player is not banned";
            private String format = "<color:#98FB98>\uD83D\uDD13 <fcolor:2><moderator></fcolor> unbanned the player <fcolor:2><player></fcolor>";
        }

        @Getter
        public static final class Unmute implements SubCommandConfig, Localizable {
            private String nullPlayer = "<color:#ff7171><b>⁉</b> This player does not exist";
            private String notMuted = "<color:#ff7171><b>⁉</b> This player is not muted";
            private String format = "<color:#98FB98>\uD83D\uDD13 <fcolor:2><moderator></fcolor> unmutted the player <fcolor:2><player></fcolor>";
        }

        @Getter
        public static final class Unwarn implements SubCommandConfig, Localizable {
            private String nullPlayer = "<color:#ff7171><b>⁉</b> This player does not exist";
            private String notWarned = "<color:#ff7171><b>⁉</b> This player is not warned";
            private String format = "<color:#98FB98>\uD83D\uDD13 <fcolor:2><moderator></fcolor> unwarned the player <fcolor:2><player></fcolor>";
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

            private String server = "<color:#ff7171>🔒 <fcolor:2><moderator></fcolor> was warned player <fcolor:2><player></fcolor> <fcolor:1><hover:show_text:\"<fcolor:1>ID: <id><br>Date: <date><br>Time: <time><br>Remaining time: <time_left><br>Moderator: <moderator><br>Reason: <reason>\">[INFO]</hover>";
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
                    "<br>▋ <fcolor:2><click:run_command:\"<command> <prev_page>\">←</click> <fcolor:1>Page: <current_page>/<last_page> <fcolor:2><click:run_command:\"<command> <next_page>\">→"
            );

            private ListTypeMessage player = new ListTypeMessage(
                    "<fcolor:2>▋ All warns: <count> <br>",
                    "<hover:show_text:\"<fcolor:1>Click to unwarn <display_name>\"><click:run_command:\"<command>\"><color:#ff7171>☒ <display_name></click></hover> <fcolor:1><hover:show_text:\"<fcolor:1>ID: <id><br>Date: <date><br>Time: <time><br>Moderator: <moderator><br>Reason: <reason>\">[INFO]</hover>",
                    "<br>▋ <fcolor:2><click:run_command:\"<command> <prev_page>\">←</click> <fcolor:1>Page: <current_page>/<last_page> <fcolor:2><click:run_command:\"<command> <next_page>\">→"
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
        public SubIntegrationConfig getDeepl() {
            return null;
        }

        @Override
        public SubIntegrationConfig getLuckperms() {
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
            private Map<MessageTag, ChannelEmbed> messageChannel = new LinkedHashMap<>(){
                {
                    put(MessageTag.CHAT, new ChannelEmbed());
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
            private Map<MessageTag, String> messageChannel = new LinkedHashMap<>(){
                {
                    put(MessageTag.CHAT, "<final_message>");
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
            private Map<MessageTag, String> messageChannel = new LinkedHashMap<>(){
                {
                    put(MessageTag.CHAT, "<final_message>");
                }
            };
        }
    }

    @Getter
    public static final class Message implements MessageConfig, Localizable {

        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/advancement/")})
        private Advancement advancement = new Advancement();

        @Override
        public SubMessageConfig getAnvil() {
            return null;
        }

        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/auto/")})
        private Auto auto = new Auto();

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
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/contact/")})
        private Contact contact = new Contact();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/death/")})
        private Death death = new Death();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/deop/")})
        private Deop deop = new Deop();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/enchant/")})
        private Enchant enchant = new Enchant();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/format/")})
        private Format format = new Format();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/gamemode/")})
        private Gamemode gamemode = new Gamemode();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/greeting/")})
        private Greeting greeting = new Greeting();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/join/")})
        private Join join = new Join();

        @Override
        public ObjectiveMessageConfig getObjective() {
            return null;
        }

        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/op/")})
        private Op op = new Op();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/scoreboard/")})
        private Scoreboard scoreboard = new Scoreboard();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/quit/")})
        private Quit quit = new Quit();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/seed/")})
        private Seed seed = new Seed();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/setblock/")})
        private Setblock setblock = new Setblock();

        @Override
        public SubMessageConfig getSign() {
            return null;
        }

        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/spawnpoint/")})
        private Spawnpoint spawnpoint = new Spawnpoint();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/status/")})
        private Status status = new Status();
        @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/tab/")})
        private Tab tab = new Tab();

        @Getter
        public static final class Advancement implements SubMessageConfig, Localizable {

            private Type task = new Type(
                    "<color:#4eff52>🌠 <fcolor:2><display_name> <fcolor:1>has completed the task <advancement>",
                    "<color:#4eff52>[<hover:show_text:\"<color:#4eff52><lang:<title>> <br><lang:<description>>\"><lang:<title>></hover>]"
            );

            private Type goal = new Type(
                    "<color:#FFFF00>🌠 <fcolor:2><display_name> <fcolor:1>has completed the goal <advancement>",
                    "<color:#FFFF00>[<hover:show_text:\"<color:#FFFF00><lang:<title>> <br><lang:<description>>\"><lang:<title>></hover>]"
            );

            private Type challenge = new Type(
                    "<color:#834eff>🌠 <fcolor:2><display_name> <fcolor:1>has completed the challenge <color:#834eff><advancement>",
                    "<color:#834eff>[<hover:show_text:\"<color:#834eff><lang:<title>> <br><lang:<description>>\"><lang:<title>></hover>]"
            );

            private Command revoke = new Command(
                    "<fcolor:1>🌠 Revoked <fcolor:2><number></fcolor:2> advancements from <display_name>",
                    "<fcolor:1>🌠 Revoked the advancement <fcolor:2><advancement></fcolor:2> from <display_name>"
            );

            private Command grant = new Command(
                    "<fcolor:1>🌠 Granted <fcolor:2><number></fcolor:2> advancements to <display_name>",
                    "<fcolor:1>🌠 Granted the advancement <fcolor:2><advancement></fcolor:2> to <display_name>"
            );

            @Getter
            @AllArgsConstructor
            public static final class Type {
                private String format;
                private String tag;
            }

            @Getter
            @AllArgsConstructor
            public static final class Command {
                private String manyToOne;
                private String oneToOne;
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
                            push("<br><fcolor:1>⚡ Support <fcolor:2>FlectonePulse</fcolor:2> on Boosty <br><fcolor:1>⚡ <u><click:open_url:\"https://boosty.to/thefaser/\"><hover:show_text:\"<fcolor:2>https://boosty.to/thefaser/\">https://boosty.to/thefaser/</hover></click></u><br>");
                            push("<br><fcolor:1>      ✉ Join our telegram ✉ <br><fcolor:2>    <u><click:open_url:\"https://t.me/flectone\"><hover:show_text:\"<fcolor:2>https://t.me/flectone\">https://t.me/flectone</hover></click></u><br>");
                        }
                    });
                }
            };
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
            private String nullRecipient = "<color:#ff7171><b>⁉</b> Nobody heard you";
            private Map<String, String> types = new LinkedHashMap<>(){
                {
                    put("global", "<display_name> <world_prefix>»<fcolor:4> <message> <translateto:\"<message_to_translate>\">");
                    put("local", "<display_name><fcolor:3>: <message> <translateto:\"<message_to_translate>\">");
                }
            };
        }

        @Getter
        public static final class Clear implements SubMessageConfig, Localizable {
            private String single = "<fcolor:1>🌊 Removed <fcolor:2><number></fcolor:2> item(s) from player <display_name>";
            private String multiple = "<fcolor:1>🌊 Removed <fcolor:2><number></fcolor:2> item(s) from <fcolor:2><count></fcolor:2> players";
        }

        @Getter
        public static final class Contact implements ContactMessageConfig, Localizable {

            @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/contact/afk/")})
            private Afk afk = new Afk();

            @Override
            public SubContactMessageConfig getKnock() {
                return null;
            }

            @Override
            public SubContactMessageConfig getMark() {
                return null;
            }

            @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/contact/rightclick/")})
            private Rightclick rightclick = new Rightclick();
            @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/contact/spit/")})
            private Spit spit = new Spit();

            @Getter
            public static final class Afk implements SubContactMessageConfig, Localizable {
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
            public static final class Rightclick implements SubContactMessageConfig, Localizable {
                private String format = "<fcolor:1>◁ <display_name> ▷";
            }

            @Getter
            public static final class Spit implements SubContactMessageConfig, Localizable {
                private String format = "<fcolor:1>🫐 You were spat on by <display_name>!";
            }
        }

        @Getter
        public static final class Death implements SubMessageConfig, Localizable {
            private Map<String, String> types = new LinkedHashMap<>(){
                {
                    put("death.attack.anvil", "<color:#778899>🪦 <fcolor:1><display_name> was squashed by a falling anvil");
                    put("death.attack.anvil.player", "<color:#778899>🪦 <fcolor:1><color:#778899>🪦 <fcolor:1> <display_name> was squashed by a falling anvil while fighting <killer>");
                    put("death.attack.arrow", "<color:#778899>🪦 <fcolor:1><display_name> was shot by <killer>");
                    put("death.attack.arrow.item", "<color:#778899>🪦 <fcolor:1><display_name> was shot by <killer> using <fcolor:2>[<i><by_item></i>]");
                    put("death.attack.badRespawnPoint.message", "<color:#778899>🪦 <fcolor:1><display_name> was killed by <fcolor:2>[<click:open_url:\"https://www.youtube.com/watch?v=dQw4w9WgXcQ\"><hover:show_text:\"<fcolor:2>MCPE-28723\">Intentional Game Design</hover></click>]");
                    put("death.attack.cactus", "<color:#778899>🪦 <fcolor:1><display_name> was pricked to death");
                    put("death.attack.cactus.player", "<color:#778899>🪦 <fcolor:1><display_name> walked into a cactus while trying to escape <killer>");
                    put("death.attack.cramming", "<color:#778899>🪦 <fcolor:1><display_name> was squished too much");
                    put("death.attack.cramming.player", "<color:#778899>🪦 <fcolor:1><display_name> was squashed by <killer>");
                    put("death.attack.dragonBreath", "<color:#778899>🪦 <fcolor:1><display_name> was roasted in dragon's breath");
                    put("death.attack.dragonBreath.player", "<color:#778899>🪦 <fcolor:1><display_name> was roasted in dragon's breath by <killer>");
                    put("death.attack.drown", "<color:#778899>🪦 <fcolor:1><display_name> drowned");
                    put("death.attack.drown.player", "<color:#778899>🪦 <fcolor:1><display_name> drowned while trying to escape <killer>");
                    put("death.attack.dryout", "<color:#778899>🪦 <fcolor:1><display_name> died from dehydration");
                    put("death.attack.dryout.player", "<color:#778899>🪦 <fcolor:1><display_name> died from dehydration while trying to escape <killer>");
                    put("death.attack.even_more_magic", "<color:#778899>🪦 <fcolor:1><display_name> was killed by even more magic");
                    put("death.attack.explosion", "<color:#778899>🪦 <fcolor:1><display_name> blew up");
                    put("death.attack.explosion.player", "<color:#778899>🪦 <fcolor:1><display_name> was blown up by <killer>");
                    put("death.attack.explosion.item", "<color:#778899>🪦 <fcolor:1><display_name> was blown up by <killer> using <fcolor:2>[<i><by_item></i>]");
                    put("death.attack.explosion.player.item", "<color:#778899>🪦 <fcolor:1><display_name> was blown up by <killer> using <fcolor:2>[<i><by_item></i>]");
                    put("death.attack.fall", "<color:#778899>🪦 <fcolor:1><display_name> hit the ground too hard");
                    put("death.attack.fall.player", "<color:#778899>🪦 <fcolor:1><display_name> hit the ground too hard while trying to escape <killer>");
                    put("death.attack.fallingBlock", "<color:#778899>🪦 <fcolor:1><display_name> was squashed by a falling block");
                    put("death.attack.fallingBlock.player", "<color:#778899>🪦 <fcolor:1><display_name> was squashed by a falling block while fighting <killer>");
                    put("death.attack.fallingStalactite", "<color:#778899>🪦 <fcolor:1><display_name> was skewered by a falling stalactite");
                    put("death.attack.fallingStalactite.player", "<color:#778899>🪦 <fcolor:1><display_name> was skewered by a falling stalactite while fighting <killer>");
                    put("death.attack.fireball", "<color:#778899>🪦 <fcolor:1><display_name> was fireballed by <killer>");
                    put("death.attack.fireball.item", "<color:#778899>🪦 <fcolor:1><display_name> was fireballed by <killer> using <fcolor:2>[<i><by_item></i>]");
                    put("death.attack.fireworks", "<color:#778899>🪦 <fcolor:1><display_name> went off with a bang");
                    put("death.attack.fireworks.item", "<color:#778899>🪦 <fcolor:1><display_name> went off with a bang due to a firework fired from <fcolor:2>[<i><by_item></i>] by <killer>");
                    put("death.attack.fireworks.player", "<color:#778899>🪦 <fcolor:1><display_name> went off with a bang while fighting <killer>");
                    put("death.attack.flyIntoWall", "<color:#778899>🪦 <fcolor:1><display_name> experienced kinetic energy");
                    put("death.attack.flyIntoWall.player", "<color:#778899>🪦 <fcolor:1><display_name> experienced kinetic energy while trying to escape <killer>");
                    put("death.attack.freeze", "<color:#778899>🪦 <fcolor:1><display_name> froze to death");
                    put("death.attack.freeze.player", "<color:#778899>🪦 <fcolor:1><display_name> was frozen to death by <killer>");
                    put("death.attack.generic", "<color:#778899>🪦 <fcolor:1><display_name> died");
                    put("death.attack.generic.player", "<color:#778899>🪦 <fcolor:1><display_name> died because of <killer>");
                    put("death.attack.genericKill", "<color:#778899>🪦 <fcolor:1><display_name> was killed");
                    put("death.attack.genericKill.player", "<color:#778899>🪦 <fcolor:1><display_name> was killed while fighting <killer>");
                    put("death.attack.hotFloor", "<color:#778899>🪦 <fcolor:1><display_name> discovered the floor was lava");
                    put("death.attack.hotFloor.player", "<color:#778899>🪦 <fcolor:1><display_name> walked into the danger zone due to <killer>");
                    put("death.attack.indirectMagic", "<color:#778899>🪦 <fcolor:1><display_name> was killed by <killer> using magic");
                    put("death.attack.indirectMagic.item", "<color:#778899>🪦 <fcolor:1><display_name> was killed by <killer> using <fcolor:2>[<i><by_item></i>]");
                    put("death.attack.inFire", "<color:#778899>🪦 <fcolor:1><display_name> went up in flames");
                    put("death.attack.inFire.player", "<color:#778899>🪦 <fcolor:1><display_name> walked into fire while fighting <killer>");
                    put("death.attack.inWall", "<color:#778899>🪦 <fcolor:1><display_name> suffocated in a wall");
                    put("death.attack.inWall.player", "<color:#778899>🪦 <fcolor:1><display_name> suffocated in a wall while fighting <killer>");
                    put("death.attack.lava", "<color:#778899>🪦 <fcolor:1><display_name> tried to swim in lava");
                    put("death.attack.lava.player", "<color:#778899>🪦 <fcolor:1><display_name> tried to swim in lava to escape <killer>");
                    put("death.attack.lightningBolt", "<color:#778899>🪦 <fcolor:1><display_name> was struck by lightning");
                    put("death.attack.lightningBolt.player", "<color:#778899>🪦<fcolor:1> <display_name> was struck by lightning while fighting <killer>");
                    put("death.attack.mace_smash", "<color:#778899>🪦 <fcolor:1><display_name> was smashed by <killer>");
                    put("death.attack.mace_smash.item", "<color:#778899>🪦 <fcolor:1><display_name> was smashed by <killer> with <fcolor:2>[<i><by_item></i>]");
                    put("death.attack.magic", "<color:#778899>🪦 <fcolor:1><display_name> was killed by magic");
                    put("death.attack.magic.player", "<color:#778899>🪦 <fcolor:1><display_name> was killed by magic while trying to escape <killer>");
                    put("death.attack.mob", "<color:#778899>🪦 <fcolor:1><display_name> was slain by <killer>");
                    put("death.attack.mob.item", "<color:#778899>🪦 <fcolor:1><display_name> was slain by <killer> using <fcolor:2>[<i><by_item></i>]");
                    put("death.attack.onFire", "<color:#778899>🪦 <fcolor:1><display_name> burned to death");
                    put("death.attack.onFire.item", "<color:#778899>🪦 <fcolor:1><display_name> was burned to a crisp while fighting <killer> wielding <fcolor:2>[<i><by_item></i>]");
                    put("death.attack.onFire.player", "<color:#778899>🪦 <fcolor:1><display_name> was burned to a crisp while fighting <killer>");
                    put("death.attack.outOfWorld", "<color:#778899>🪦 <fcolor:1><display_name> fell out of the world");
                    put("death.attack.outOfWorld.player", "<color:#778899>🪦 <fcolor:1><display_name> didn't want to live in the same world as <killer>");
                    put("death.attack.outsideBorder", "<color:#778899>🪦 <fcolor:1><display_name> left the confines of this world");
                    put("death.attack.outsideBorder.player", "<color:#778899>🪦 <fcolor:1><display_name> left the confines of this world while fighting <killer>");
                    put("death.attack.player", "<color:#778899>🪦 <fcolor:1><display_name> was slain by <killer>");
                    put("death.attack.player.item", "<color:#778899>🪦 <fcolor:1><display_name> was slain by <killer> using <fcolor:2>[<i><by_item></i>]");
                    put("death.attack.sonic_boom", "<color:#778899>🪦 <fcolor:1><display_name> was obliterated by a sonically-charged shriek");
                    put("death.attack.sonic_boom.item", "<color:#778899>🪦 <fcolor:1><display_name> was obliterated by a sonically-charged shriek while trying to escape <killer> wielding <fcolor:2>[<i><by_item></i>]");
                    put("death.attack.sonic_boom.player", "<color:#778899>🪦 <fcolor:1><display_name> was obliterated by a sonically-charged shriek while trying to escape <killer>");
                    put("death.attack.stalagmite", "<color:#778899>🪦 <fcolor:1><display_name> was impaled on a stalagmite");
                    put("death.attack.stalagmite.player", "<color:#778899>🪦 <fcolor:1><display_name> was impaled on a stalagmite while fighting <killer>");
                    put("death.attack.starve", "<color:#778899>🪦 <fcolor:1><display_name> starved to death");
                    put("death.attack.starve.player", "<color:#778899>🪦 <fcolor:1><display_name> starved to death while fighting <killer>");
                    put("death.attack.sting", "<color:#778899>🪦 <fcolor:1><display_name> was stung to death");
                    put("death.attack.sting.item", "<color:#778899>🪦 <fcolor:1><display_name> was stung to death by <killer> using <fcolor:2>[<i><by_item></i>]");
                    put("death.attack.sting.player", "<color:#778899>🪦 <fcolor:1><display_name> was stung to death by <killer>");
                    put("death.attack.sweetBerryBush", "<color:#778899>🪦 <fcolor:1><display_name> was poked to death by a sweet berry bush");
                    put("death.attack.sweetBerryBush.player", "<color:#778899>🪦 <fcolor:1><display_name> was poked to death by a sweet berry bush while trying to escape <killer>");
                    put("death.attack.thorns", "<color:#778899>🪦 <fcolor:1><display_name> was killed while trying to hurt <killer>");
                    put("death.attack.thorns.item", "<color:#778899>🪦 <fcolor:1><display_name> was killed by <fcolor:2>[<i><by_item></i>]</fcolor:2> while trying to hurt <killer>");
                    put("death.attack.thrown", "<color:#778899>🪦 <fcolor:1><display_name> was pummeled by <killer>");
                    put("death.attack.thrown.item", "<color:#778899>🪦 <fcolor:1><display_name> was pummeled by <killer> using <fcolor:2>[<i><by_item></i>]");
                    put("death.attack.trident", "<color:#778899>🪦 <fcolor:1><display_name> was impaled by <killer>");
                    put("death.attack.trident.item", "<color:#778899>🪦 <fcolor:1><display_name> was impaled by <killer> with <fcolor:2>[<i><by_item></i>]");
                    put("death.attack.wither", "<color:#778899>🪦 <fcolor:1><display_name> withered away");
                    put("death.attack.wither.player", "<color:#778899>🪦 <fcolor:1><display_name> withered away while fighting <killer>");
                    put("death.attack.witherSkull", "<color:#778899>🪦 <fcolor:1><display_name> was shot by a skull from <killer>");
                    put("death.attack.witherSkull.item", "<color:#778899>🪦 <fcolor:1><display_name> was shot by a skull from <killer> using <fcolor:2>[<i><by_item></i>]");
                    put("death.fell.accident.generic", "<color:#778899>🪦 <fcolor:1><display_name> fell from a high place");
                    put("death.fell.accident.ladder", "<color:#778899>🪦 <fcolor:1><display_name> fell off a ladder");
                    put("death.fell.accident.other_climbable", "<color:#778899>🪦 <fcolor:1><display_name> fell while climbing");
                    put("death.fell.accident.scaffolding", "<color:#778899>🪦 <fcolor:1><display_name> fell off scaffolding");
                    put("death.fell.accident.twisting_vines", "<color:#778899>🪦 <fcolor:1><display_name> fell off some twisting vines");
                    put("death.fell.accident.vines", "<color:#778899>🪦 <fcolor:1><display_name> fell off some vines");
                    put("death.fell.accident.weeping_vines", "<color:#778899>🪦 <fcolor:1><display_name> fell off some weeping vines");
                    put("death.fell.assist", "<color:#778899>🪦 <fcolor:1><display_name> was doomed to fall by <killer>");
                    put("death.fell.assist.item", "<color:#778899>🪦 <fcolor:1><display_name> was doomed to fall by <killer> using <fcolor:2>[<i><by_item></i>]");
                    put("death.fell.finish", "<color:#778899>🪦 <fcolor:1><display_name> fell too far and was finished by <killer>");
                    put("death.fell.finish.item", "<color:#778899>🪦 <fcolor:1><display_name> fell too far and was finished by <killer> using <fcolor:2>[<i><by_item></i>]");
                    put("death.fell.killer", "<color:#778899>🪦 <fcolor:1><display_name> was doomed to fall");
                }
            };
        }

        @Getter
        public static final class Deop implements SubMessageConfig, Localizable {
            private String format = "<fcolor:1>🤖 Made <display_name> no longer a server operator";
        }

        @Getter
        public static final class Enchant implements SubMessageConfig, Localizable {
            private String single = "<fcolor:1>📖 Applied enchantment «<fcolor:2><lang:<enchant>> <lang:<level>></fcolor:2>» to <display_name>'s item";
            private String multiple = "<fcolor:1>📖 Applied enchantment «<fcolor:2><lang:<enchant>> <lang:<level>></fcolor:2>» to <fcolor:2><count></fcolor:2> entities";
        }

        @Getter
        public static final class Format implements FormatMessageConfig, Localizable {

            private Map<TagType, String> tags = new LinkedHashMap<>(){
                {
                    put(TagType.PING, "<fcolor:2><ping></fcolor>");
                    put(TagType.TPS, "<fcolor:2><tps></fcolor>");
                    put(TagType.ONLINE, "<fcolor:2><online></fcolor>");
                    put(TagType.COORDS, "<fcolor:2><x> <y> <z></fcolor>");
                    put(TagType.STATS, "<color:#ff7171><hp>♥</color> <color:#3de0d8><armor>🛡 <color:#e33059><attack>🗡 <color:#4eff52><exp>⏺ <color:#f0a01f><food>🍖");
                    put(TagType.SKIN, "<image:\"<message>\"><u>👨 Skin</u></image>");
                    put(TagType.ITEM, "<fcolor:2>[<message>]</fcolor>");
                    put(TagType.URL, "<click:open_url:\"<message>\"><hover:show_text:\"<fcolor:2>Open url <br><u><message>\"><fcolor:2><u>🗗 Url</u></fcolor:2></hover></click>");
                    put(TagType.IMAGE, "<image:\"<message>\"><u>🖃 Image</u></image>");
                }
            };

            @Override
            public SubFormatMessageConfig getColor() {
                return null;
            }

            @Override
            public SubFormatMessageConfig getEmoji() {
                return null;
            }

            @Override
            public SubFormatMessageConfig getFixation() {
                return null;
            }

            @Override
            public SubFormatMessageConfig getImage() {
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
            @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/format/spoiler/")})
            private Spoiler spoiler = new Spoiler();
            @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/format/translate/")})
            private Translate translate = new Translate();

            @Override
            public SubFormatMessageConfig getWorld() {
                return null;
            }

            @Getter
            public static final class Mention implements SubFormatMessageConfig, Localizable {
                private String person = "<fcolor:2>You were mentioned";
                private String format = "<fcolor:2>@<target></fcolor>";
            }

            @Getter
            public static final class Moderation implements ModerationFormatMessageConfig, Localizable {

                @Override
                public SubModerationFormatMessageConfig getCaps() {
                    return null;
                }

                @Override
                public SubModerationFormatMessageConfig getFlood() {
                    return null;
                }

                @Comment({@CommentValue(" https://flectone.net/pulse/docs/message/format/moderation/swear/")})
                private Swear swear = new Swear();

                @Getter
                public static final class Swear implements SubModerationFormatMessageConfig, Localizable {
                    private String symbol = "❤";
                }

            }

            @Getter
            public static final class Name implements SubFormatMessageConfig, Localizable {
                private String display = "<click:suggest_command:\"/msg <player> \"><hover:show_text:\"<fcolor:2>Write to <player>\"><vault_prefix><stream_prefix><fcolor:2><player></fcolor><afk_suffix><vault_suffix></hover></click>";
                private String entity = "<fcolor:2><hover:show_text:\"<fcolor:2><lang:<name>> <br><fcolor:1>Type <fcolor:2><lang:<type>> <br><fcolor:1>ID <fcolor:2><uuid>\"><lang:<name>></hover></fcolor:2>";
                private String unknown = "<fcolor:2><name></fcolor:2>";
                private String prefix = "<vault_prefix><stream_prefix>";
                private String suffix = "<afk_suffix><vault_suffix>";
            }

            @Getter
            public static final class QuestionAnswer implements SubFormatMessageConfig, Localizable {
                private Map<String, String> questions = new LinkedHashMap<>(){
                    {
                        put("server", "<fcolor:2>[Q&A] @<player><fcolor:1>, this is a vanilla server in minecraft!");
                        put("flectone", "<fcolor:2>[Q&A] @<player><fcolor:1>, this is a brand and projects created by TheFaser");
                    }
                };
            }

            @Getter
            public static final class Spoiler implements SubFormatMessageConfig, Localizable {
                private String symbol = "█";
                private String hover = "<fcolor:2><message>";
            }

            @Getter
            public static final class Translate implements SubFormatMessageConfig, Localizable {
                private String action = "<click:run_command:\"/translateto <language> <language> <message>\"><hover:show_text:\"<fcolor:2>Translate message\"><fcolor:1>[📖]";
            }
        }

        @Getter
        public static final class Gamemode implements SubMessageConfig, Localizable {

            private Type self = new Type(
                    "<fcolor:1>🗘 Set own game mode to <fcolor:2>Creative Mode",
                    "<fcolor:1>🗘 Set own game mode to <fcolor:2>Survival Mode",
                    "<fcolor:1>🗘 Set own game mode to <fcolor:2>Adventure Mode",
                    "<fcolor:1>🗘 Set own game mode to <fcolor:2>Spectator Mode"
            );

            private Type other = new Type(
                    "<fcolor:1>🗘 Set <display_name>'s game mode to <fcolor:2>Creative Mode",
                    "<fcolor:1>🗘 Set <display_name>'s game mode to <fcolor:2>Survival Mode",
                    "<fcolor:1>🗘 Set <display_name>'s game mode to <fcolor:2>Adventure Mode",
                    "<fcolor:1>🗘 Set <display_name>'s game mode to <fcolor:2>Spectator Mode"
            );

            @Getter
            @AllArgsConstructor
            public static final class Type {
                private String creative;
                private String survival;
                private String adventure;
                private String spectator;
            }
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
        public static final class Op implements SubMessageConfig, Localizable {
            private String format = "<fcolor:1>🤖 Made <display_name> a server operator";
        }

        @Getter
        public static final class Scoreboard implements SubMessageConfig, Localizable {
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
        public static final class Quit implements SubMessageConfig, Localizable {
            private String format = "<color:#ff4e4e>← <display_name>";
        }

        @Getter
        public static final class Seed implements SubMessageConfig, Localizable {
            private String format = "<fcolor:1>🌐 Seed: [<fcolor:2><hover:show_text:'<fcolor:2>Click to Copy to Clipboard'><click:copy_to_clipboard:<seed>><seed></click></fcolor:2>]";
        }

        @Getter
        public static final class Setblock implements SubMessageConfig, Localizable {
            private String format = "<fcolor:1>⏹ Changed the block at <fcolor:2><x></fcolor:2>, <fcolor:2><y></fcolor:2>, <fcolor:2><z></fcolor:2>";
        }

        @Getter
        public static final class Spawnpoint implements SubMessageConfig, Localizable {
            private String single = "<fcolor:1>\uD83D\uDECC Set spawn point to <fcolor:2><x></fcolor:2>, <fcolor:2><y></fcolor:2>, <fcolor:2><z></fcolor:2> [<fcolor:2><angle></fcolor:2>] in <fcolor:2><world></fcolor:2> for <display_name>";
            private String multiple = "<fcolor:1>\uD83D\uDECC Set spawn point to <fcolor:2><x></fcolor:2>, <fcolor:2><y></fcolor:2>, <fcolor:2><z></fcolor:2> [<fcolor:2><angle></fcolor:2>] in <fcolor:2><world></fcolor:2> for <fcolor:2><count></fcolor:2> players";
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
                                push("<fcolor:1>Hello <fcolor:2><player></fcolor:2>!");
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
            if (reason == null || reason.isEmpty()) {
                return super.getOrDefault("default", "UNKNOWN");
            }

            return super.getOrDefault(reason, reason);
        }
    }

}
