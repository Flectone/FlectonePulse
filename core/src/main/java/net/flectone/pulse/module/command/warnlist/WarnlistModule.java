package net.flectone.pulse.module.command.warnlist;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.SenderToReceiverMessageEvent;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.unwarn.UnwarnModule;
import net.flectone.pulse.platform.formatter.ModerationMessageFormatter;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.processing.resolver.FileResolver;
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
public class WarnlistModule extends AbstractModuleCommand<Localization.Command.Warnlist> {

    private final Command.Warnlist command;
    private final Permission.Command.Warnlist permission;
    private final FPlayerService fPlayerService;
    private final ModerationService moderationService;
    private final ModerationMessageFormatter moderationMessageFormatter;
    private final UnwarnModule unwarnModule;
    private final MessagePipeline messagePipeline;
    private final CommandParserProvider commandParserProvider;
    private final EventDispatcher eventDispatcher;

    @Inject
    public WarnlistModule(FileResolver fileResolver,
                          FPlayerService fPlayerService,
                          ModerationService moderationService,
                          ModerationMessageFormatter moderationMessageFormatter,
                          UnwarnModule unwarnModule,
                          MessagePipeline messagePipeline,
                          CommandParserProvider commandParserProvider,
                          EventDispatcher eventDispatcher) {
        super(localization -> localization.getCommand().getWarnlist(), Command::getWarnlist);

        this.command = fileResolver.getCommand().getWarnlist();
        this.permission = fileResolver.getPermission().getCommand().getWarnlist();
        this.fPlayerService = fPlayerService;
        this.moderationService = moderationService;
        this.moderationMessageFormatter = moderationMessageFormatter;
        this.unwarnModule = unwarnModule;
        this.messagePipeline = messagePipeline;
        this.eventDispatcher = eventDispatcher;
        this.commandParserProvider = commandParserProvider;
    }

    @Override
    public void onEnable() {
        // if FPlayer.UNKNOWN (all-permissions) fails check (method will return true),
        // a moderation plugin is intercepting this command
        if (isModuleDisabledFor(FPlayer.UNKNOWN)) return;

        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        String promptPlayer = addPrompt(0, Localization.Command.Prompt::getPlayer);
        String promptNumber = addPrompt(1, Localization.Command.Prompt::getNumber);
        registerCommand(manager -> manager
                .permission(permission.getName())
                .optional(promptPlayer, commandParserProvider.warnedParser())
                .optional(promptNumber, commandParserProvider.integerParser())
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer)) return;
        if (checkCooldown(fPlayer)) return;

        Localization.Command.Warnlist localization = resolveLocalization(fPlayer);
        Localization.ListTypeMessage localizationType = localization.getGlobal();

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
                    builder(fPlayer)
                            .format(Localization.Command.Warnlist::getNullPlayer)
                            .sendBuilt();
                    return;
                }

                commandLine += " " + playerName;
                localizationType = localization.getPlayer();
            }
        }

        List<Moderation> moderationList = targetFPlayer == null
                ? moderationService.getValidWarns()
                : moderationService.getValidWarns(targetFPlayer);

        if (moderationList.isEmpty()) {
            builder(fPlayer)
                    .format((fResolver, s) -> s.getEmpty())
                    .sendBuilt();
            return;
        }

        int size = moderationList.size();
        int perPage = command.getPerPage();
        int countPage = (int) Math.ceil((double) size / perPage);

        if (page > countPage || page < 1) {
            builder(fPlayer)
                    .format((fResolver, s) -> s.getNullPage())
                    .sendBuilt();
            return;
        }

        List<Moderation> finalModerationList = moderationList.stream()
                .skip((long) (page - 1) * perPage)
                .limit(perPage)
                .toList();

        String header = Strings.CS.replace(localizationType.getHeader(), "<count>", String.valueOf(size));
        Component component = messagePipeline.builder(fPlayer, header)
                .build()
                .append(Component.newline());

        for (Moderation moderation : finalModerationList) {

            FPlayer fTarget = fPlayerService.getFPlayer(moderation.getPlayer());

            String line = Strings.CS.replace(localizationType.getLine(), "<command>", "/" + unwarnModule.getCommandName() + " <player> <id>");
            line = moderationMessageFormatter.replacePlaceholders(line, fPlayer, moderation);

            component = component
                    .append(messagePipeline.builder(fTarget, fPlayer, line).build())
                    .append(Component.newline());
        }

        String footer = StringUtils.replaceEach(
                localizationType.getFooter(),
                new String[]{"<command>", "<prev_page>", "<next_page>", "<current_page>", "<last_page>"},
                new String[]{
                        commandLine,
                        String.valueOf(page - 1),
                        String.valueOf(page + 1),
                        String.valueOf(page),
                        String.valueOf(countPage)
                }
        );

        component = component.append(messagePipeline.builder(fPlayer, footer).build());

        eventDispatcher.dispatch(new SenderToReceiverMessageEvent(MessageType.COMMAND_WARNLIST, fPlayer, component));

        playSound(fPlayer);
    }
}
