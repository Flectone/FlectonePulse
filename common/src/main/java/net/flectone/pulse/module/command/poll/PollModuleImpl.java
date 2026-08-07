package net.flectone.pulse.module.command.poll;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.IntegrationMessageFormat;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.command.poll.listener.PollProxyMessageListener;
import net.flectone.pulse.module.command.poll.model.Poll;
import net.flectone.pulse.module.command.poll.model.PollMessageContext;
import net.flectone.pulse.platform.controller.ModuleCommandController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.processing.serializer.ComponentSerializer;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.incendo.cloud.suggestion.Suggestion;
import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.*;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PollModuleImpl implements PollModule {

    private final LinkedHashMap<Integer, Poll> pollMap = new LinkedHashMap<>();

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final ProxySender proxySender;
    private final TaskScheduler taskScheduler;
    private final CommandParserProvider commandParserProvider;
    private final MessagePipeline messagePipeline;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;
    private final ModuleCommandController commandModuleController;
    private final ComponentSerializer componentSerializer;
    private final FLogger fLogger;
    private final ProxyRegistry proxyRegistry;
    private final ListenerRegistry listenerRegistry;
    private final SocialService socialService;
    private final Gson gson;

    @Override
    public void onEnable() {
        String promptTime = commandModuleController.addPrompt(this, 0, Localization.Command.Prompt::time);
        String promptRepeatTime = commandModuleController.addPrompt(this, 1, Localization.Command.Prompt::repeatTime);
        String promptMultipleVote = commandModuleController.addPrompt(this, 2, Localization.Command.Prompt::multipleVote);
        String promptMessage = commandModuleController.addPrompt(this, 3, Localization.Command.Prompt::message);
        commandModuleController.registerCommand(this, commandBuilder -> commandBuilder
                .permission(permission().create().name())
                .required(promptTime, commandParserProvider.durationParser())
                .required(promptRepeatTime, commandParserProvider.durationParser())
                .required(promptMultipleVote, commandParserProvider.booleanParser())
                .required(promptMessage, commandParserProvider.messageParser(), mapSuggestion())
        );

        String promptId = commandModuleController.addPrompt(this, 4, Localization.Command.Prompt::id);
        String promptNumber = commandModuleController.addPrompt(this, 5, Localization.Command.Prompt::number);
        commandModuleController.registerSubCommand(this, config().subCommandVote(), commandBuilder -> commandBuilder
                .permission(permission().name())
                .required(promptId, commandParserProvider.integerParser())
                .required(promptNumber, commandParserProvider.integerParser())
                .handler(commandContext -> executeVote(commandContext.sender(), commandContext))
        );

        taskScheduler.runAsyncTimer(() -> {
            HashSet toRemove = new HashSet();

            pollMap.forEach((id, poll) -> {
                Status status;

                if (poll.isEnded()) {
                    toRemove.add(id);
                    status = Status.END;
                } else if (poll.repeat()) {
                    status = Status.RUN;
                } else {
                    return;
                }

                FPlayer fPlayer = fPlayerService.getFPlayer(poll.getCreator());
                Range range = config().range();

                messageDispatcher.dispatch(this, EventMetadata.builder()
                        .range(range)
                        .messageContext(fResolver -> PollMessageContext.builder()
                                .base(MessageContext.builder()
                                        .sender(fPlayer)
                                        .receiver(fResolver)
                                        .message(resolvePollFormat(fResolver, poll, status))
                                        .tagResolver(messagePipeline.messageTag(fPlayer, fResolver, poll.getTitle()))
                                        .build()
                                )
                                .string(poll.getTitle())
                                .poll(poll)
                                .status(status)
                                .action(Action.REPEAT)
                                .build()
                        )
                        .integration(() -> IntegrationMessageFormat.builder()
                                .messageNames(List.of(name().name() + "_" + status, name().name() + "_REPEAT"))
                                .build()
                        )
                        .build()
                );
            });

            toRemove.forEach(pollMap::remove);
        }, 20L);

        if (proxyRegistry.hasEnabledProxy()) {
            listenerRegistry.register(PollProxyMessageListener.class);
        }
    }

    @Override
    public Set<PermissionSetting> permissions() {
        Set<PermissionSetting> permissions = new LinkedHashSet<>(PollModule.super.permissions());
        permissions.add(permission().create());
        return Collections.unmodifiableSet(permissions);
    }

    @Override
    public void onDisable() {
        pollMap.clear();
        commandModuleController.clearPrompts(this);
    }

    private @NonNull BlockingSuggestionProvider<FPlayer> mapSuggestion() {
        return (_, input) -> {
            String[] words = input.input().split(" ");
            if (words.length < 5) return List.of(Suggestion.suggestion("title="));

            String string = String.join(" ", Arrays.copyOfRange(words, 4, words.length));
            if (!string.contains("title=")) return List.of(Suggestion.suggestion("title="), Suggestion.suggestion(string + ";"));

            return List.of(Suggestion.suggestion(string + ";"));
        };
    }

    @Override
    public void executeVote(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (moduleController.isDisabledFor(this, fPlayer, true)) return;

        int id = commandModuleController.getArgument(this, commandContext, 4);
        int numberVote = commandModuleController.getArgument(this, commandContext, 5);

        UUID metadataUUID = UUID.randomUUID();
        boolean isSent = proxySender.send(fPlayer, ModuleName.COMMAND_POLL, dataOutputStream -> {
            dataOutputStream.writeUTF(Action.VOTE.name());
            dataOutputStream.writeInt(id);
            dataOutputStream.writeInt(numberVote);
        }, metadataUUID);

        if (isSent) return;

        vote(fPlayer, id, numberVote, metadataUUID);
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (moduleController.isDisabledFor(this, fPlayer, true)) return;

        String promptTime = commandModuleController.getPrompt(this, 0);
        long time = ((Duration) commandContext.get(promptTime)).toMillis();

        String promptRepeatTime = commandModuleController.getPrompt(this, 1);
        long repeatTime = ((Duration) commandContext.get(promptRepeatTime)).toMillis();

        String promptMultipleVote = commandModuleController.getPrompt(this, 2);
        boolean multipleVote = commandContext.get(promptMultipleVote);

        String promptMessage = commandModuleController.getPrompt(this, 3);
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

    @Override
    public ModuleName name() {
        return ModuleName.COMMAND_POLL;
    }

    @Override
    public Command.Poll config() {
        return fileFacade.command().poll();
    }

    @Override
    public Permission.Command.Poll permission() {
        return fileFacade.permission().command().poll();
    }

    @Override
    public Localization.Command.Poll localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).command().poll();
    }

    @Override
    public void createPoll(FPlayer fPlayer, String title, boolean multipleValue, long endTimeValue, long repeatTimeValue, List<String> answers) {
        Poll poll = new Poll(config().lastId(),
                fPlayer.id(),
                endTimeValue + System.currentTimeMillis(),
                repeatTimeValue,
                multipleValue,
                title,
                answers
        );

        saveAndUpdateLast(poll);

        messageDispatcher.dispatch(this, EventMetadata.builder()
                .range(config().range())
                .sound(soundOrThrow())
                .messageContext(fResolver -> PollMessageContext.builder()
                        .base(MessageContext.builder()
                                .sender(fPlayer)
                                .receiver(fResolver)
                                .message(resolvePollFormat(fResolver, poll, Status.START))
                                .tagResolver(messagePipeline.messageTag(fPlayer, fResolver, poll.getTitle()))
                                .build()
                        )
                        .string(poll.getTitle())
                        .poll(poll)
                        .status(Status.START)
                        .action(Action.CREATE)
                        .build()
                )
                .proxy(dataOutputStream -> {
                    dataOutputStream.writeUTF(Action.CREATE.name());
                    dataOutputStream.writeUTF(gson.toJson(poll));
                })
                .integration(() -> IntegrationMessageFormat.builder()
                        .messageNames(List.of(name().name() + "_START", name().name() + "_CREATE"))
                        .build()
                )
                .build()
        );
    }

    @Override
    public void saveAndUpdateLast(Poll poll) {
        pollMap.put(poll.getId(), poll);

        fileFacade.updateFilePack(filePack -> filePack.withCommand(filePack.command().withPoll(filePack.command().poll().withLastId(poll.getId() + 1))));

        try {
            fileFacade.saveFiles();
        } catch (RuntimeException e) {
            fLogger.warning(e);
        }
    }

    @Override
    public void vote(FEntity fPlayer, int id, int numberVote, UUID metadataUUID) {
        if (moduleController.isDisabledFor(this, fPlayer)) return;

        Poll poll = pollMap.get(id);
        if (poll == null) {
            messageDispatcher.dispatch(ModuleName.ERROR, EventMetadata.builder()
                    .messageContext(fResolver -> MessageContext.builder()
                            .sender(fPlayer)
                            .receiver(fResolver)
                            .message(localization(fResolver).nullPoll())
                            .build()
                    )
                    .build()
            );

            return;
        }

        if (poll.isEnded()) {
            messageDispatcher.dispatch(ModuleName.ERROR, EventMetadata.builder()
                    .messageContext(fResolver -> MessageContext.builder()
                            .sender(fPlayer)
                            .receiver(fResolver)
                            .message(localization(fResolver).expired())
                            .build()
                    )
                    .build()
            );

            return;
        }

        int voteType = poll.vote(fPlayer, numberVote);

        if (voteType == -1) {
            messageDispatcher.dispatch(ModuleName.ERROR, EventMetadata.builder()
                    .messageContext(fResolver -> MessageContext.builder()
                            .sender(fPlayer)
                            .receiver(fResolver)
                            .message(localization(fResolver).already())
                            .build()
                    )
                    .build()
            );

            return;
        }

        int count = poll.getCountAnswers()[numberVote];
        int pollID = poll.getId();

        messageDispatcher.dispatch(this, EventMetadata.builder()
                .messageContext(fResolver -> PollMessageContext.builder()
                        .base(MessageContext.builder()
                                .uuid(metadataUUID)
                                .sender(fPlayer)
                                .receiver(fResolver)
                                .message(resolveVote(fResolver, voteType, numberVote, pollID, count))
                                .build()
                        )
                        .poll(poll)
                        .status(Status.RUN)
                        .action(Action.VOTE)
                        .build()
                )
                .build()
        );
    }

    @Override
    public String resolveVote(FPlayer fPlayer, int voteType, int answerID, int pollID, int count) {
        return StringUtils.replaceEach(
                voteType == 1 ? localization(fPlayer).voteTrue() : localization(fPlayer).voteFalse(),
                new String[]{"<answer_id>", "<id>", "<count>"},
                new String[]{String.valueOf(answerID + 1), String.valueOf(pollID), String.valueOf(count)}
        );
    }

    @Override
    public String resolvePollFormat(FPlayer fPlayer, Poll poll, Status status) {
        Localization.Command.Poll localization = localization(fPlayer);

        StringBuilder answersBuilder = new StringBuilder();

        int k = 0;
        for (String answer : poll.getAnswers()) {

            Component answerComponent = messagePipeline.build(MessageContext.builder()
                    .sender(fPlayer)
                    .receiver(FPlayer.UNKNOWN)
                    .message(answer)
                    .build()
            );

            answersBuilder.append(StringUtils.replaceEach(
                    localization.answerTemplate(),
                    new String[]{"<command>", "<id>", "<number>", "<answer>", "<count>"},
                    new String[]{commandModuleController.getCommandName(this) + config().subCommandVote(), String.valueOf(poll.getId()), String.valueOf(k), componentSerializer.toPlain(answerComponent), String.valueOf(poll.getCountAnswers()[k])}
            ));

            k++;
        }

        String messageStatus = Strings.CS.replace(
                switch (status) {
                    case START -> localization.status().start();
                    case RUN -> localization.status().run();
                    case END -> localization.status().end();
                },
                "<id>",
                String.valueOf(poll.getId())
        );

        return StringUtils.replaceEach(
                localization.format(),
                new String[]{"<status>", "<answers>"},
                new String[]{messageStatus, answersBuilder.toString()}
        );
    }

}
