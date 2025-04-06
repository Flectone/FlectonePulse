package net.flectone.pulse.module.command.poll;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.NonNull;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.configuration.Command;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.sender.ProxySender;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.poll.model.Poll;
import net.flectone.pulse.registry.CommandRegistry;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.formatter.MessageFormatter;
import net.flectone.pulse.util.DisableAction;
import net.flectone.pulse.util.MessageTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.meta.CommandMeta;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.incendo.cloud.suggestion.Suggestion;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;

@Singleton
public class PollModule extends AbstractModuleCommand<Localization.Command.Poll> {

    private final HashMap<Integer, Poll> pollMap = new HashMap<>();

    private final Command.Poll command;
    private final Permission.Command.Poll permission;

    private final FileManager fileManager;
    private final FPlayerService fPlayerService;
    private final ProxySender proxySender;
    private final TaskScheduler taskScheduler;
    private final CommandRegistry commandRegistry;
    private final MessageFormatter messageFormatter;
    private final Gson gson;

    @Inject
    public PollModule(FileManager fileManager,
                      FPlayerService fPlayerService,
                      ProxySender proxySender,
                      TaskScheduler taskScheduler,
                      CommandRegistry commandRegistry,
                      MessageFormatter messageFormatter,
                      Gson gson) {
        super(localization -> localization.getCommand().getPoll(), fPlayer -> fPlayer.isSetting(FPlayer.Setting.POLL));

        this.fileManager = fileManager;
        this.fPlayerService = fPlayerService;
        this.proxySender = proxySender;
        this.taskScheduler = taskScheduler;
        this.commandRegistry = commandRegistry;
        this.messageFormatter = messageFormatter;
        this.gson = gson;

        command = fileManager.getCommand().getPoll();
        permission = fileManager.getPermission().getCommand().getPoll();
    }

    @Override
    public boolean isConfigEnable() {
        return command.isEnable();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        registerPermission(permission.getCreate());

        String commandName = getName(command);

        String promptTime = getPrompt().getTime();
        String promptRepeatTime = getPrompt().getRepeatTime();
        String promptMultipleVote = getPrompt().getMultipleVote();
        String promptMessage = getPrompt().getMessage();

        commandRegistry.registerCommand(manager ->
                manager.commandBuilder(commandName, command.getAliases(), CommandMeta.empty())
                        .permission(permission.getCreate().getName())
                        .required(promptTime, commandRegistry.durationParser())
                        .required(promptRepeatTime, commandRegistry.durationParser())
                        .required(promptMultipleVote, commandRegistry.booleanParser())
                        .required(promptMessage, commandRegistry.messageParser(), mapSuggestion())
                        .handler(commandContext -> executeCreate(commandContext.sender(), commandContext))
        );

        String promptId = getPrompt().getId();
        String promptNumber = getPrompt().getNumber();

        commandRegistry.registerCommand(manager ->
                manager.commandBuilder(commandName + "vote", CommandMeta.empty())
                        .permission(permission.getName())
                        .required(promptId, commandRegistry.integerParser())
                        .required(promptNumber, commandRegistry.integerParser())
                        .handler(this)
        );

        taskScheduler.runAsyncTimer(() -> {
            Set<Integer> toRemove = new HashSet<>();

            pollMap.forEach((id, poll) -> {
                Status status = null;

                if (poll.isEnded()) {
                    toRemove.add(id);
                    status = Status.END;
                } else if (poll.repeat()) {
                    status = Status.RUN;
                }

                if (status == null) return;

                FPlayer fPlayer = fPlayerService.getFPlayer(poll.getCreator());
                int range = command.getRange();

                builder(fPlayer)
                        .range(range)
                        .tag(MessageTag.COMMAND_POLL_CREATE_MESSAGE)
                        .format(resolvePollFormat(fPlayer, poll, status))
                        .message((fResolver, s) -> poll.getTitle())
                        .sendBuilt();
            });

            toRemove.forEach(pollMap::remove);
        }, 20L);
    }

    private @NonNull BlockingSuggestionProvider<FPlayer> mapSuggestion() {
        return (context, input) -> {
            String[] words = input.input().split(" ");
            if (words.length < 5) return List.of(Suggestion.suggestion("title="));

            String string = String.join(" ", Arrays.copyOfRange(words, 4, words.length));
            if (!string.contains("title=")) return List.of(Suggestion.suggestion("title="), Suggestion.suggestion(string + ";"));

            return List.of(Suggestion.suggestion(string + ";"));
        };
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkModulePredicates(fPlayer)) return;

        String promptId = getPrompt().getId();
        int id = commandContext.get(promptId);

        String promptNumber = getPrompt().getNumber();
        int numberVote = commandContext.get(promptNumber);

        boolean isSent = proxySender.sendMessage(fPlayer, MessageTag.COMMAND_POLL_VOTE, byteArrayDataOutput -> {
            byteArrayDataOutput.writeInt(id);
            byteArrayDataOutput.writeInt(numberVote);
        });

        if (isSent) return;

        vote(fPlayer, id, numberVote);
    }

    @Async
    public void executeCreate(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (!isEnable()) return;
        if (checkDisable(fPlayer, fPlayer, DisableAction.YOU)) return;
        if (checkCooldown(fPlayer)) return;
        if (checkMute(fPlayer)) return;

        String promptTime = getPrompt().getTime();
        long time = ((Duration) commandContext.get(promptTime)).toMillis();

        String promptRepeatTime = getPrompt().getRepeatTime();
        long repeatTime = ((Duration) commandContext.get(promptRepeatTime)).toMillis();

        String promptMultipleVote = getPrompt().getMultipleVote();
        boolean multipleVote = commandContext.get(promptMultipleVote);

        String promptMessage = getPrompt().getMessage();
        String rawPoll = commandContext.get(promptMessage);

        boolean hasTitle = rawPoll.startsWith("title=");
        if (hasTitle) {
            rawPoll = rawPoll.substring(6);
        }

        String[] parts = rawPoll.split(";");
        String title = hasTitle && parts.length > 0 ? parts[0] : "";

        int firstAnswerIndex = hasTitle ? 1 : 0;
        List<String> answers = parts.length > firstAnswerIndex
                ? List.of(Arrays.copyOfRange(parts, firstAnswerIndex, parts.length))
                : List.of();

        Poll poll = new Poll(command.getLastId(),
                fPlayer.getId(),
                time + System.currentTimeMillis(),
                repeatTime,
                multipleVote,
                title,
                answers
        );

        saveAndUpdateLast(poll);

        int range = command.getRange();

        builder(fPlayer)
                .range(range)
                .tag(MessageTag.COMMAND_POLL_CREATE_MESSAGE)
                .format(resolvePollFormat(fPlayer, poll, Status.START))
                .message((fResolver, s) -> poll.getTitle())
                .proxy(output -> output.writeUTF(gson.toJson(poll)))
                .integration()
                .sendBuilt();
    }

    public void saveAndUpdateLast(Poll poll) {
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

        if (poll.isEnded()) {
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
                .format(resolveVote(voteType, numberVote, pollID, count))
                .sound(getSound())
                .sendBuilt();
    }

    public Function<Localization.Command.Poll, String> resolveVote(int voteType, int answerID, int pollID, int count) {
        return message -> (voteType == 1 ? message.getVoteTrue() : message.getVoteFalse())
                .replace("<answer_id>", String.valueOf(answerID + 1))
                .replace("<id>", String.valueOf(pollID))
                .replace("<count>", String.valueOf(count));
    }

    public Function<Localization.Command.Poll, String> resolvePollFormat(FEntity fPlayer, Poll poll, Status status) {
        return message -> {
            StringBuilder answersBuilder = new StringBuilder();

            int k = 0;
            for (String answer : poll.getAnswers()) {

                Component answerComponent = messageFormatter.builder(fPlayer, FPlayer.UNKNOWN, answer).build();

                answersBuilder.append(message.getAnswerTemplate()
                        .replace("<id>", String.valueOf(poll.getId()))
                        .replace("<number>", String.valueOf(k))
                        .replace("<answer>", PlainTextComponentSerializer.plainText().serialize(answerComponent))
                        .replace("<count>", String.valueOf(poll.getCountAnswers()[k]))
                );

                k++;
            }

            String messageStatus = switch (status) {
                case START -> message.getStatus().getStart();
                case RUN -> message.getStatus().getRun();
                case END -> message.getStatus().getEnd();
            };

            return message.getFormat()
                    .replace("<status>", messageStatus)
                    .replace("<id>", String.valueOf(poll.getId()))
                    .replace("<answers>", answersBuilder.toString());
        };
    }

    public enum Status {
        START,
        RUN,
        END
    }
}
