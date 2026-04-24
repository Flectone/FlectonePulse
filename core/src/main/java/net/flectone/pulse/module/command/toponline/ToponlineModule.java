package net.flectone.pulse.module.command.toponline;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.MessageSendEvent;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.util.PlayTime;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.module.command.toponline.listener.PulseToponlineListener;
import net.flectone.pulse.platform.controller.ModuleCommandController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.formatter.TimeFormatter;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.sender.SoundPlayer;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ToponlineModule implements ModuleCommand<Localization.Command.Toponline> {

    private final FileFacade fileFacade;
    private final CommandParserProvider commandParserProvider;
    private final MessagePipeline messagePipeline;
    private final MessageDispatcher messageDispatcher;
    private final EventDispatcher eventDispatcher;
    private final TimeFormatter timeFormatter;
    private final SoundPlayer soundPlayer;
    private final FPlayerService fPlayerService;
    private final ModuleController moduleController;
    private final ModuleCommandController commandModuleController;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        String promptNumber = commandModuleController.addPrompt(this, 0, Localization.Command.Prompt::number);
        commandModuleController.registerCommand(this, manager -> manager
                .permission(permission().name())
                .optional(promptNumber, commandParserProvider.integerParser())
        );

        listenerRegistry.register(PulseToponlineListener.class);
    }

    @Override
    public void onDisable() {
        commandModuleController.clearPrompts(this);
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (moduleController.isDisabledFor(this, fPlayer, true)) return;

        String promptNumber = commandModuleController.getPrompt(this, 0);
        Optional<Integer> optionalNumber = commandContext.optional(promptNumber);
        int page = optionalNumber.orElse(1);

        int size = fPlayerService.getPlayTimesCount();
        int perPage = config().perPage();
        int countPage = (int) Math.ceil((double) size / perPage);

        if (page > countPage || page < 1) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Toponline>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Toponline::nullPage)
                    .build()
            );

            return;
        }

        List<PlayTime> finalPlayedTimePlayers = fPlayerService.getAllPlayTimes(perPage, (page - 1) * perPage);

        Localization.Command.Toponline localization = localization(fPlayer);

        String header = Strings.CS.replace(localization.header(), "<count>", String.valueOf(size));
        MessageContext headerContext = messagePipeline.createContext(fPlayer, header);
        Component component = messagePipeline.build(headerContext).append(Component.newline());

        for (PlayTime timePlayer : finalPlayedTimePlayers) {
            FPlayer fTarget = fPlayerService.getFPlayer(timePlayer.playerId());

            // take only time that is saved in database and do not check player online,
            // otherwise there will be an incorrect order
            String line = Strings.CS.replace(
                    localization.line(),
                    "<time>",
                    timeFormatter.format(fPlayer, timePlayer.total())
            );

            MessageContext lineContext = messagePipeline.createContext(fPlayer, line)
                    .addTagResolvers(
                            messagePipeline.targetTag("time_player", fPlayer, fTarget)
                    );

            component = component
                    .append(messagePipeline.build(lineContext))
                    .append(Component.newline());
        }

        String footer = StringUtils.replaceEach(localization.footer(),
                new String[]{"<command>", "<prev_page>", "<next_page>", "<current_page>", "<last_page>"},
                new String[]{"/" + commandModuleController.getCommandName(this), String.valueOf(page - 1), String.valueOf(page + 1), String.valueOf(page), String.valueOf(countPage)}
        );

        MessageContext footerContext = messagePipeline.createContext(fPlayer, footer);
        component = component.append(messagePipeline.build(footerContext));

        eventDispatcher.dispatch(new MessageSendEvent(name(), fPlayer, component));

        soundPlayer.play(soundOrThrow(), fPlayer);
    }

    @Override
    public ModuleName name() {
        return ModuleName.COMMAND_TOPONLINE;
    }

    @Override
    public Command.Toponline config() {
        return fileFacade.command().toponline();
    }

    @Override
    public Permission.Command.Toponline permission() {
        return fileFacade.permission().command().toponline();
    }

    @Override
    public Localization.Command.Toponline localization(FEntity sender) {
        return fileFacade.localization(sender).command().toponline();
    }

    public MessageContext addTag(MessageContext messageContext) {
        FEntity sender = messageContext.sender();
        if (moduleController.isDisabledFor(this, sender)) return messageContext;
        if (!(sender instanceof FPlayer)) return messageContext;

        return messageContext.addTagResolver(TagResolver.resolver(MessagePipeline.ReplacementTag.TOPONLINE.getTagName(), (argumentQueue, _) -> {
            if (!argumentQueue.hasNext()) return MessagePipeline.ReplacementTag.emptyTag();

            OptionalInt optionalInt = argumentQueue.pop().asInt();
            if (optionalInt.isEmpty()) return MessagePipeline.ReplacementTag.emptyTag();

            Optional<FPlayer> fTarget = getPlayerByPosition(optionalInt.getAsInt());
            if (fTarget.isEmpty()) return MessagePipeline.ReplacementTag.emptyTag();

            MessageContext toponlineContext = messagePipeline.createContext(fTarget.get(), messageContext.receiver(), "<display_name>")
                    .withFlags(messageContext.flags())
                    .addFlag(MessageFlag.PLAYER_MESSAGE, false);

            return Tag.selfClosingInserting(messagePipeline.build(toponlineContext));
        }));
    }

    public Optional<FPlayer> getPlayerByPosition(String rawPosition) {
        int position;
        try {
            position = Integer.parseInt(rawPosition);
        } catch (NumberFormatException _) {
            return Optional.empty();
        }

        return getPlayerByPosition(position);
    }

    public Optional<FPlayer> getPlayerByPosition(int position) {
        if (position < 1) return Optional.empty();

        List<PlayTime> playTimeList = fPlayerService.getAllPlayTimes(1, position - 1);
        if (playTimeList.isEmpty()) return Optional.empty();

        return Optional.of(fPlayerService.getFPlayer(playTimeList.getFirst().playerId()));
    }

}
