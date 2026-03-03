package net.flectone.pulse.module.command.banlist;

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
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.module.command.unban.UnbanModule;
import net.flectone.pulse.platform.controller.CommandModuleController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.formatter.ModerationMessageFormatter;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.sender.SoundPlayer;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;

import java.util.List;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BanlistModule implements ModuleCommand<Localization.Command.Banlist> {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final ModerationService moderationService;
    private final ModerationMessageFormatter moderationMessageFormatter;
    private final UnbanModule unbanModule;
    private final MessagePipeline messagePipeline;
    private final MessageDispatcher messageDispatcher;
    private final EventDispatcher eventDispatcher;
    private final CommandParserProvider commandParserProvider;
    private final SoundPlayer soundPlayer;
    private final ModuleController moduleController;
    private final CommandModuleController commandModuleController;

    @Override
    public void onEnable() {
        String promptPlayer = commandModuleController.addPrompt(this, 0, Localization.Command.Prompt::player);
        String promptNumber = commandModuleController.addPrompt(this, 1, Localization.Command.Prompt::number);
        commandModuleController.registerCommand(this, commandBuilder -> commandBuilder
                .permission(permission().name())
                .optional(promptPlayer, commandParserProvider.bannedParser())
                .optional(promptNumber, commandParserProvider.integerParser())
        );
    }

    @Override
    public void onDisable() {
        commandModuleController.clearPrompts(this);
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (moduleController.isDisabledFor(this, fPlayer, true)) return;

        Localization.Command.Banlist localization = localization(fPlayer);
        Localization.ListTypeMessage localizationType = localization.global();

        String commandLine = "/" + commandModuleController.getCommandName(this);

        int page = 1;
        FPlayer targetFPlayer = null;

        String promptPlayer = commandModuleController.getPrompt(this, 0);
        Optional<String> optionalPlayer = commandContext.optional(promptPlayer);
        if (optionalPlayer.isPresent()) {
            String playerName = optionalPlayer.get();

            if (StringUtils.isNumeric(playerName)) {
                page = Integer.parseInt(playerName);
            } else {
                String promptNumber = commandModuleController.getPrompt(this, 1);
                Optional<Integer> optionalNumber = commandContext.optional(promptNumber);
                page = optionalNumber.orElse(page);

                targetFPlayer = fPlayerService.getFPlayer(playerName);
                if (targetFPlayer.isUnknown()) {
                    messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Banlist>builder()
                            .sender(fPlayer)
                            .format(Localization.Command.Banlist::nullPlayer)
                            .build()
                    );

                    return;
                }

                commandLine += " " + playerName;
                localizationType = localization.player();
            }
        }

        List<Moderation> moderationList = targetFPlayer == null
                ? moderationService.getValidBans()
                : moderationService.getValidBans(targetFPlayer);

        if (moderationList.isEmpty()) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Banlist>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Banlist::empty)
                    .build()
            );

            return;
        }

        int size = moderationList.size();
        int perPage = config().perPage();
        int countPage = (int) Math.ceil((double) size / perPage);

        if (page > countPage || page < 1) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Banlist>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Banlist::nullPage)
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

            String line = Strings.CS.replace(localizationType.line(), "<command>", "/" + commandModuleController.getCommandName(unbanModule) + " <player> <id>");
            line = moderationMessageFormatter.replacePlaceholders(line, fPlayer, moderation);

            MessageContext lineContext = messagePipeline.createContext(fPlayer, line)
                    .addTagResolvers(
                            messagePipeline.targetTag(fPlayer, fTarget),
                            messagePipeline.targetTag("moderator", fPlayer, fPlayerService.getFPlayer(moderation.moderator()))
                    );

            component = component
                    .append(messagePipeline.build(lineContext))
                    .append(Component.newline());
        }

        String footer = StringUtils.replaceEach(
                localizationType.footer(),
                new String[]{"<command>", "<prev_page>", "<next_page>", "<current_page>", "<last_page>"},
                new String[]{
                        StringUtils.defaultString(commandLine),
                        String.valueOf(page - 1),
                        String.valueOf(page + 1),
                        String.valueOf(page),
                        String.valueOf(countPage)
                }
        );

        MessageContext footerContext = messagePipeline.createContext(fPlayer, footer);
        component = component.append(messagePipeline.build(footerContext));

        eventDispatcher.dispatch(new MessageSendEvent(ModuleName.COMMAND_BANLIST, fPlayer, component));

        soundPlayer.play(soundOrThrow(), fPlayer);
    }

    @Override
    public ModuleName name() {
        return ModuleName.COMMAND_BANLIST;
    }

    @Override
    public Command.Banlist config() {
        return fileFacade.command().banlist();
    }

    @Override
    public Permission.Command.Banlist permission() {
        return fileFacade.permission().command().banlist();
    }

    @Override
    public Localization.Command.Banlist localization(FEntity sender) {
        return fileFacade.localization(sender).command().banlist();
    }
}
