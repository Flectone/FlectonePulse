package net.flectone.pulse.module.command.poll;

import com.google.gson.Gson;
import lombok.Getter;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.file.Command;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.connector.ProxyConnector;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.poll.model.Poll;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.util.ComponentUtil;
import net.flectone.pulse.util.DisableAction;
import net.flectone.pulse.util.MessageTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public abstract class PollModule extends AbstractModuleCommand<Localization.Command.Poll> {

    private final HashMap<Integer, Poll> pollMap = new HashMap<>();

    @Getter private final Command.Poll command;
    @Getter private final Permission.Command.Poll permission;

    private final FileManager fileManager;
    private final ProxyConnector proxyConnector;
    private final TaskScheduler taskScheduler;
    private final CommandUtil commandUtil;
    private final ComponentUtil componentUtil;
    private final Gson gson;

    public PollModule(FileManager fileManager,
                      ProxyConnector proxyConnector,
                      TaskScheduler taskScheduler,
                      CommandUtil commandUtil,
                      ComponentUtil componentUtil,
                      Gson gson) {
        super(localization -> localization.getCommand().getPoll(), fPlayer -> fPlayer.is(FPlayer.Setting.POLL));

        this.fileManager = fileManager;
        this.proxyConnector = proxyConnector;
        this.taskScheduler = taskScheduler;
        this.commandUtil = commandUtil;
        this.componentUtil = componentUtil;
        this.gson = gson;

        command = fileManager.getCommand().getPoll();
        permission = fileManager.getPermission().getCommand().getPoll();
    }

    @Override
    public void onCommand(FPlayer fPlayer, Object arguments) {
        if (checkModulePredicates(fPlayer)) return;

        Optional<Object> objectId = commandUtil.getOptional(0, arguments);
        if (objectId.isEmpty() || !(objectId.get() instanceof Integer id)) return;

        Optional<Object> objectNumberVote = commandUtil.getOptional(1, arguments);
        if (objectNumberVote.isEmpty() || !(objectNumberVote.get() instanceof Integer numberVote)) return;

        boolean isSent = proxyConnector.sendMessage(fPlayer, MessageTag.COMMAND_POLL_VOTE, byteArrayDataOutput -> {
            byteArrayDataOutput.writeInt(id);
            byteArrayDataOutput.writeInt(numberVote);
        });

        if (isSent) return;

        vote(fPlayer, id, numberVote);
    }

    @Async
    public void onCommandCreate(FPlayer fPlayer, Object arguments) {
        if (!isEnable()) return;
        if (checkDisable(fPlayer, fPlayer, DisableAction.YOU)) return;
        if (checkCooldown(fPlayer)) return;
        if (checkMute(fPlayer)) return;

        int time = commandUtil.getInteger(0, arguments);
        int repeatTime = commandUtil.getInteger(1, arguments);
        boolean multipleVote = commandUtil.getBoolean(2, arguments);
        String title = commandUtil.getText(3, arguments);
        Map<String, String> answerSet = ((Map<String, String>) commandUtil.getOptional(4, arguments).get());

        Poll poll = new Poll(command.getLastId(), answerSet.size(), multipleVote);
        put(poll);

        int range = command.getRange();

        Runnable sendRunnable = () -> sendMessage(fPlayer, poll, answerSet, range, title);

        sendRunnable.run();

        if (repeatTime != -1) {
            recursiveSend(repeatTime * 20L, poll, sendRunnable);
            taskScheduler.runAsync(() -> {});
        }

        taskScheduler.runAsyncLater(() -> {
            Poll expiredPoll = pollMap.get(poll.getId());
            expiredPoll.setExpired(true);

            sendMessage(fPlayer, expiredPoll, answerSet, range, title);

        }, time * 20L);
    }

    public void put(Poll poll) {
        pollMap.put(poll.getId(), poll);
        command.setLastId(poll.getId() + 1);
        fileManager.getCommand().save();
    }

    public void vote(FEntity fPlayer, int id, int numberVote) {
        if (checkModulePredicates(fPlayer)) return;

        Poll poll = pollMap.get(id);
        if (poll == null) {
            builder(fPlayer)
                    .format(Localization.Command.Poll::getNullPoll)
                    .sendBuilt();
            return;
        }

        if (poll.isExpired()) {
            builder(fPlayer)
                    .format(Localization.Command.Poll::getExpired)
                    .sendBuilt();
            return;
        }

        int voteType = poll.vote(fPlayer, numberVote);

        if (voteType == -1) {
            builder(fPlayer)
                    .format(Localization.Command.Poll::getAlready)
                    .sendBuilt();
            return;
        }

        int count = poll.getCountAnswers()[numberVote];
        int pollID = poll.getId();

        builder(fPlayer)
                .format(replaceAnswer(voteType, numberVote, pollID, count))
                .sound(getSound())
                .sendBuilt();
    }

    public Function<Localization.Command.Poll, String> replaceAnswer(int voteType, int answerID, int pollID, int count) {
        return message -> (voteType == 1 ? message.getVoteTrue() : message.getVoteFalse())
                .replace("<answer_id>", String.valueOf(answerID + 1))
                .replace("<id>", String.valueOf(pollID))
                .replace("<count>", String.valueOf(count));
    }

    private void sendMessage(FPlayer fPlayer, Poll poll, Map<String, String> answerSet, int range, String title) {
        builder(fPlayer)
                .range(range)
                .tag(MessageTag.COMMAND_POLL_CREATE_MESSAGE)
                .format(createFormat(fPlayer, answerSet, poll))
                .message((fResolver, s) -> title)
                .proxy(output -> {
                    output.writeUTF(gson.toJson(poll));
                    output.writeUTF(title);
                    output.writeUTF(gson.toJson(answerSet));
                })
                .integration(s -> {
                    s = s.replace("<title>", title);

                    for (var answer : answerSet.entrySet()) {
                        s = s.replaceFirst("<answer_key>", answer.getKey())
                                .replaceFirst("<answer_value>", answer.getValue());
                    }

                    return s;
                })
                .sendBuilt();
    }

    public Function<Localization.Command.Poll, String> createFormat(FEntity fPlayer,
                                                                    Map<String, String> answerSet,
                                                                    Poll poll) {
        return message -> {
            StringBuilder answersBuilder = new StringBuilder();

            int k = 0;
            for (var answer : answerSet.entrySet()) {

                String finalButton = message.getVoteButton();

                if (poll.isExpired()) {
                    finalButton = message.getCountAnswers().replace("<count>", String.valueOf(poll.getCountAnswers()[k]));
                }

                Component answerKey = componentUtil.builder(fPlayer, FPlayer.UNKNOWN, answer.getKey())
                        .build();

                Component answerValue = componentUtil.builder(fPlayer, FPlayer.UNKNOWN, answer.getValue())
                        .build();

                answersBuilder.append(finalButton
                        .replace("<id>", String.valueOf(poll.getId()))
                        .replace("<number>", String.valueOf(k))
                        .replace("<answer_key>", PlainTextComponentSerializer.plainText().serialize(answerKey))
                        .replace("<answer_value>", PlainTextComponentSerializer.plainText().serialize(answerValue))
                );

                k++;
            }

            return (poll.isExpired() ? message.getFormatOver() : message.getFormatStart())
                    .replace("<id>", String.valueOf(poll.getId()))
                    .replace("<answers>", answersBuilder.toString());
        };
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        registerPermission(permission.getCreate());

        getCommand().getAliases().forEach(commandUtil::unregister);

        createCommand();
    }

    @Override
    public boolean isConfigEnable() {
        return command.isEnable();
    }

    public void recursiveSend(long repeatTime, Poll poll, Runnable runnable) {
        taskScheduler.runAsyncLater(() -> {
            if (poll.isExpired()) return;
            runnable.run();

            recursiveSend(repeatTime, poll, runnable);
        }, repeatTime);
    }
}
