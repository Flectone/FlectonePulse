package net.flectone.pulse.module.command.poll;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.NonNull;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.poll.builder.DialogPollBuilder;
import net.flectone.pulse.module.command.poll.model.Poll;
import net.flectone.pulse.module.command.poll.model.PollMetadata;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
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
    private final FileResolver fileResolver;
    private final FPlayerService fPlayerService;
    private final ProxySender proxySender;
    private final TaskScheduler taskScheduler;
    private final CommandParserProvider commandParserProvider;
    private final MessagePipeline messagePipeline;
    private final PacketProvider packetProvider;
    private final Provider<DialogPollBuilder> dialogPollBuilderProvider;

    @Inject
    public PollModule(FileResolver fileResolver,
                      FPlayerService fPlayerService,
                      ProxySender proxySender,
                      TaskScheduler taskScheduler,
                      CommandParserProvider commandParserProvider,
                      MessagePipeline messagePipeline,
                      PacketProvider packetProvider,
                      Provider<DialogPollBuilder> dialogPollBuilderProvider) {
        super(localization -> localization.getCommand().getPoll(), Command::getPoll, MessageType.COMMAND_POLL);

        this.command = fileResolver.getCommand().getPoll();
        this.permission = fileResolver.getPermission().getCommand().getPoll();
        this.fileResolver = fileResolver;
        this.fPlayerService = fPlayerService;
        this.proxySender = proxySender;
        this.taskScheduler = taskScheduler;
        this.commandParserProvider = commandParserProvider;
        this.messagePipeline = messagePipeline;
        this.packetProvider = packetProvider;
        this.dialogPollBuilderProvider = dialogPollBuilderProvider;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        registerPermission(permission.getCreate());

        String promptTime = addPrompt(0, Localization.Command.Prompt::getTime);
        String promptRepeatTime = addPrompt(1, Localization.Command.Prompt::getRepeatTime);
        String promptMultipleVote = addPrompt(2, Localization.Command.Prompt::getMultipleVote);
        String promptMessage = addPrompt(3, Localization.Command.Prompt::getMessage);
        registerCommand(manager -> manager
                .permission(permission.getCreate().getName())
                .required(promptTime, commandParserProvider.durationParser())
                .required(promptRepeatTime, commandParserProvider.durationParser())
                .required(promptMultipleVote, commandParserProvider.booleanParser())
                .required(promptMessage, commandParserProvider.messageParser(), mapSuggestion())
        );

        if (command.isEnableGui() && packetProvider.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21_6)) {
            registerCustomCommand(manager ->
                    manager.commandBuilder(getCommandName() + "gui", CommandMeta.empty())
                            .permission(permission.getCreate().getName())
                            .handler(commandContext -> dialogPollBuilderProvider.get().openDialog(commandContext.sender()))
            );
        }

        String promptId = addPrompt(4, Localization.Command.Prompt::getId);
        String promptNumber = addPrompt(5, Localization.Command.Prompt::getNumber);
        registerCustomCommand(manager ->
                manager.commandBuilder(getCommandName() + "vote", CommandMeta.empty())
                        .permission(permission.getName())
                        .required(promptId, commandParserProvider.integerParser())
                        .required(promptNumber, commandParserProvider.integerParser())
                        .handler(commandContext -> executeVote(commandContext.sender(), commandContext))
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
                Range range = command.getRange();

                sendMessage(PollMetadata.<Localization.Command.Poll>builder()
                        .sender(fPlayer)
                        .format(resolvePollFormat(fPlayer, poll, status))
                        .poll(poll)
                        .status(status)
                        .action(Action.REPEAT)
                        .range(range)
                        .message(poll.getTitle())
                        .build()
                );
            });

            toRemove.forEach(pollMap::remove);
        }, 20L);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        pollMap.clear();
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

    public void executeVote(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        int id = getArgument(commandContext, 4);
        int numberVote = getArgument(commandContext, 5);

        UUID metadataUUID = UUID.randomUUID();
        boolean isSent = proxySender.send(fPlayer, MessageType.COMMAND_POLL, dataOutputStream -> {
            dataOutputStream.writeUTF(Action.VOTE.name());
            dataOutputStream.writeInt(id);
            dataOutputStream.writeInt(numberVote);
        }, metadataUUID);

        if (isSent) return;

        vote(fPlayer, id, numberVote, metadataUUID);
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        String promptTime = getPrompt(0);
        long time = ((Duration) commandContext.get(promptTime)).toMillis();

        String promptRepeatTime = getPrompt(1);
        long repeatTime = ((Duration) commandContext.get(promptRepeatTime)).toMillis();

        String promptMultipleVote = getPrompt(2);
        boolean multipleVote = commandContext.get(promptMultipleVote);

        String promptMessage = getPrompt(3);
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

        createPoll(fPlayer, title, multipleVote, time, repeatTime, answers);
    }

    public void createPoll(FPlayer fPlayer, String title, boolean multipleValue, long endTimeValue, long repeatTimeValue, List<String> answers) {
        Poll poll = new Poll(command.getLastId(),
                fPlayer.getId(),
                endTimeValue + System.currentTimeMillis(),
                repeatTimeValue,
                multipleValue,
                title,
                answers
        );

        saveAndUpdateLast(poll);

        Range range = command.getRange();

        sendMessage(PollMetadata.<Localization.Command.Poll>builder()
                .sender(fPlayer)
                .format(resolvePollFormat(fPlayer, poll, Status.START))
                .poll(poll)
                .status(Status.START)
                .action(Action.CREATE)
                .range(range)
                .message(poll.getTitle())
                .sound(getModuleSound())
                .proxy(dataOutputStream -> {
                    dataOutputStream.writeUTF(Action.CREATE.name());
                    dataOutputStream.writeAsJson(poll);
                })
                .integration()
                .build()
        );
    }

    public void saveAndUpdateLast(Poll poll) {
        pollMap.put(poll.getId(), poll);
        command.setLastId(poll.getId() + 1);
        fileResolver.getCommand().save();
    }

    public void vote(FEntity fPlayer, int id, int numberVote, UUID metadataUUID) {
        if (isModuleDisabledFor(fPlayer)) return;

        Poll poll = pollMap.get(id);
        if (poll == null) {
            sendMessage(MessageType.ERROR, metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Poll::getNullPoll)
                    .build()
            );

            return;
        }

        if (poll.isEnded()) {
            sendMessage(MessageType.ERROR, metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Poll::getExpired)
                    .build()
            );

            return;
        }

        int voteType = poll.vote(fPlayer, numberVote);

        if (voteType == -1) {
            sendMessage(MessageType.ERROR, metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Poll::getAlready)
                    .build()
            );

            return;
        }

        int count = poll.getCountAnswers()[numberVote];
        int pollID = poll.getId();

        sendMessage(PollMetadata.<Localization.Command.Poll>builder()
                .uuid(metadataUUID)
                .sender(fPlayer)
                .format(resolveVote(voteType, numberVote, pollID, count))
                .poll(poll)
                .status(Status.RUN)
                .action(Action.VOTE)
                .build()
        );
    }

    public Function<Localization.Command.Poll, String> resolveVote(int voteType, int answerID, int pollID, int count) {
        return message -> StringUtils.replaceEach(
                voteType == 1 ? message.getVoteTrue() : message.getVoteFalse(),
                new String[]{"<answer_id>", "<id>", "<count>"},
                new String[]{String.valueOf(answerID + 1), String.valueOf(pollID), String.valueOf(count)}
        );
    }

    public Function<Localization.Command.Poll, String> resolvePollFormat(FEntity fPlayer, Poll poll, Status status) {
        return message -> {
            StringBuilder answersBuilder = new StringBuilder();

            int k = 0;
            for (String answer : poll.getAnswers()) {

                Component answerComponent = messagePipeline.builder(fPlayer, FPlayer.UNKNOWN, answer).build();

                answersBuilder.append(StringUtils.replaceEach(
                        message.getAnswerTemplate(),
                        new String[]{"<id>", "<number>", "<answer>", "<count>"},
                        new String[]{
                                String.valueOf(poll.getId()),
                                String.valueOf(k),
                                PlainTextComponentSerializer.plainText().serialize(answerComponent),
                                String.valueOf(poll.getCountAnswers()[k])
                        }
                ));

                k++;
            }

            String messageStatus = Strings.CS.replace(
                    switch (status) {
                        case START -> message.getStatus().getStart();
                        case RUN -> message.getStatus().getRun();
                        case END -> message.getStatus().getEnd();
                    },
                    "<id>",
                    String.valueOf(poll.getId())
            );

            return StringUtils.replaceEach(
                    message.getFormat(),
                    new String[]{"<status>", "<answers>"},
                    new String[]{messageStatus, answersBuilder.toString()}
            );
        };
    }

    public enum Status {
        START,
        RUN,
        END
    }

    public enum Action {
        CREATE,
        REPEAT,
        VOTE
    }
}
