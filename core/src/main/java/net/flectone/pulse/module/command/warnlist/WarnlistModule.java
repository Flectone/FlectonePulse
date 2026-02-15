package net.flectone.pulse.module.command.warnlist;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.MessageSendEvent;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.unwarn.UnwarnModule;
import net.flectone.pulse.platform.formatter.ModerationMessageFormatter;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.sender.SoundPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.util.constant.MessageType;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;

import java.util.List;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class WarnlistModule extends AbstractModuleCommand<Localization.Command.Warnlist> {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final ModerationService moderationService;
    private final ModerationMessageFormatter moderationMessageFormatter;
    private final UnwarnModule unwarnModule;
    private final MessagePipeline messagePipeline;
    private final CommandParserProvider commandParserProvider;
    private final EventDispatcher eventDispatcher;
    private final SoundPlayer soundPlayer;

    @Override
    public void onEnable() {
        super.onEnable();

        String promptPlayer = addPrompt(0, Localization.Command.Prompt::player);
        String promptNumber = addPrompt(1, Localization.Command.Prompt::number);
        registerCommand(manager -> manager
                .permission(permission().name())
                .optional(promptPlayer, commandParserProvider.warnedParser())
                .optional(promptNumber, commandParserProvider.integerParser())
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        Localization.Command.Warnlist localization = localization(fPlayer);
        Localization.ListTypeMessage localizationType = localization.global();

        String commandLine = "/" + getCommandName();

        FPlayer targetFPlayer = null;
        int page = 1;

        String promptPlayer = getPrompt(0);
        Optional<String> optionalPlayer = commandContext.optional(promptPlayer);
        if (optionalPlayer.isPresent()) {
            String playerName = optionalPlayer.get();

            if (StringUtils.isNumeric(playerName)) {
                page = Integer.parseInt(playerName);
            } else {
                String promptNumber = getPrompt(1);
                Optional<Integer> optionalNumber = commandContext.optional(promptNumber);
                page = optionalNumber.orElse(page);

                targetFPlayer = fPlayerService.getFPlayer(playerName);
                if (targetFPlayer.isUnknown()) {
                    sendErrorMessage(EventMetadata.<Localization.Command.Warnlist>builder()
                            .sender(fPlayer)
                            .format(Localization.Command.Warnlist::nullPlayer)
                            .build()
                    );

                    return;
                }

                commandLine += " " + playerName;
                localizationType = localization.player();
            }
        }

        List<Moderation> moderationList = targetFPlayer == null
                ? moderationService.getValidWarns()
                : moderationService.getValidWarns(targetFPlayer);

        if (moderationList.isEmpty()) {
            sendErrorMessage(EventMetadata.<Localization.Command.Warnlist>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Warnlist::empty)
                    .build()
            );

            return;
        }

        int size = moderationList.size();
        int perPage = config().perPage();
        int countPage = (int) Math.ceil((double) size / perPage);

        if (page > countPage || page < 1) {
            sendErrorMessage(EventMetadata.<Localization.Command.Warnlist>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Warnlist::nullPage)
                    .build()
            );

            return;
        }

        List<Moderation> finalModerationList = moderationList.stream()
                .skip((long) (page - 1) * perPage)
                .limit(perPage)
                .toList();

        String header = Strings.CS.replace(localizationType.header(), "<count>", String.valueOf(size));
        MessageContext headerContext = messagePipeline.createContext(fPlayer, header);
        Component component = messagePipeline.build(headerContext).append(Component.newline());

        for (Moderation moderation : finalModerationList) {
            FPlayer fTarget = fPlayerService.getFPlayer(moderation.player());

            String line = Strings.CS.replace(localizationType.line(), "<command>", "/" + unwarnModule.getCommandName() + " <player> <id>");
            line = moderationMessageFormatter.replacePlaceholders(line, fPlayer, moderation);

            MessageContext lineContext = messagePipeline.createContext(fPlayer, line)
                    .addTagResolver(targetTag(fPlayer, fTarget));

            component = component
                    .append(messagePipeline.build(lineContext))
                    .append(Component.newline());
        }

        String footer = StringUtils.replaceEach(
                localizationType.footer(),
                new String[]{"<command>", "<prev_page>", "<next_page>", "<current_page>", "<last_page>"},
                new String[]{
                        commandLine,
                        String.valueOf(page - 1),
                        String.valueOf(page + 1),
                        String.valueOf(page),
                        String.valueOf(countPage)
                }
        );

        MessageContext footerContext = messagePipeline.createContext(fPlayer, footer);
        component = component.append(messagePipeline.build(footerContext));

        eventDispatcher.dispatch(new MessageSendEvent(MessageType.COMMAND_WARNLIST, fPlayer, component));

        soundPlayer.play(soundOrThrow(), fPlayer);
    }

    @Override
    public MessageType messageType() {
        return MessageType.COMMAND_WARNLIST;
    }

    @Override
    public Command.Warnlist config() {
        return fileFacade.command().warnlist();
    }

    @Override
    public Permission.Command.Warnlist permission() {
        return fileFacade.permission().command().warnlist();
    }

    @Override
    public Localization.Command.Warnlist localization(FEntity sender) {
        return fileFacade.localization(sender).command().warnlist();
    }
}
