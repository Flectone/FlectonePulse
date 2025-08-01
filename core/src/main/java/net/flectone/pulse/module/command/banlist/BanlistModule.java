package net.flectone.pulse.module.command.banlist;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.platform.formatter.ModerationMessageFormatter;
import net.flectone.pulse.listener.MessagePulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.unban.UnbanModule;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.context.CommandContext;

import java.util.List;
import java.util.Optional;

@Singleton
public class BanlistModule extends AbstractModuleCommand<Localization.Command.Banlist> {

    private final Command.Banlist command;
    private final Permission.Command.Banlist permission;
    private final FPlayerService fPlayerService;
    private final ModerationService moderationService;
    private final ModerationMessageFormatter moderationMessageFormatter;
    private final UnbanModule unbanModule;
    private final MessagePipeline messagePipeline;
    private final MessagePulseListener messagePulseListener;
    private final CommandParserProvider commandParserProvider;

    @Inject
    public BanlistModule(FileResolver fileResolver,
                         FPlayerService fPlayerService,
                         ModerationService moderationService,
                         ModerationMessageFormatter moderationMessageFormatter,
                         UnbanModule unbanModule,
                         MessagePipeline messagePipeline,
                         MessagePulseListener messagePulseListener,
                         CommandParserProvider commandParserProvider) {
        super(localization -> localization.getCommand().getBanlist(), Command::getBanlist);

        this.command = fileResolver.getCommand().getBanlist();
        this.permission = fileResolver.getPermission().getCommand().getBanlist();
        this.fPlayerService = fPlayerService;
        this.moderationService = moderationService;
        this.moderationMessageFormatter = moderationMessageFormatter;
        this.unbanModule = unbanModule;
        this.messagePipeline = messagePipeline;
        this.messagePulseListener = messagePulseListener;
        this.commandParserProvider = commandParserProvider;
    }

    @Override
    public void onEnable() {
        // if FPlayer.UNKNOWN (all-permissions) fails check (method will return true),
        // a moderation plugin is intercepting this command
        if (checkModulePredicates(FPlayer.UNKNOWN)) return;

        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        String promptPlayer = addPrompt(0, Localization.Command.Prompt::getPlayer);
        String promptNumber = addPrompt(1, Localization.Command.Prompt::getNumber);
        registerCommand(commandBuilder -> commandBuilder
                .permission(permission.getName())
                .optional(promptPlayer, commandParserProvider.bannedParser())
                .optional(promptNumber, commandParserProvider.integerParser())
        );

        addPredicate(this::checkCooldown);
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkModulePredicates(fPlayer)) return;

        Localization.Command.Banlist localization = resolveLocalization(fPlayer);
        Localization.ListTypeMessage localizationType = localization.getGlobal();

        String commandLine = "/" + getCommandName();

        int page = 1;
        FPlayer targetFPlayer = null;

        String promptPlayer = getPrompt(0);
        Optional<String> optionalPlayer = commandContext.optional(promptPlayer);
        if (optionalPlayer.isPresent()) {
            String playerName = optionalPlayer.get();

            try {
                page = Integer.parseInt(playerName);
            } catch (NumberFormatException e) {
                String promptNumber = getPrompt(1);
                Optional<Integer> optionalNumber = commandContext.optional(promptNumber);
                page = optionalNumber.orElse(page);

                targetFPlayer = fPlayerService.getFPlayer(playerName);
                if (targetFPlayer.isUnknown()) {
                    builder(fPlayer)
                            .format(Localization.Command.Banlist::getNullPlayer)
                            .sendBuilt();
                    return;
                }

                commandLine += " " + playerName;
                localizationType = localization.getPlayer();
            }
        }

        List<Moderation> moderationList = targetFPlayer == null
                ? moderationService.getValidBans()
                : moderationService.getValidBans(targetFPlayer);

        if (moderationList.isEmpty()) {
            builder(fPlayer)
                    .format(Localization.Command.Banlist::getEmpty)
                    .sendBuilt();
            return;
        }

        int size = moderationList.size();
        int perPage = command.getPerPage();
        int countPage = (int) Math.ceil((double) size / perPage);

        if (page > countPage || page < 1) {
            builder(fPlayer)
                    .format(Localization.Command.Banlist::getNullPage)
                    .sendBuilt();
            return;
        }

        List<Moderation> finalModerationList = moderationList.stream()
                .skip((long) (page - 1) * perPage)
                .limit(perPage)
                .toList();

        String header = localizationType.getHeader().replace("<count>", String.valueOf(size));
        Component component = messagePipeline.builder(fPlayer, header)
                .build()
                .append(Component.newline());

        for (Moderation moderation : finalModerationList) {
            FPlayer fTarget = fPlayerService.getFPlayer(moderation.getPlayer());

            String line = localizationType.getLine().replace("<command>", "/" + unbanModule.getCommandName() + " <player> <id>");
            line = moderationMessageFormatter.replacePlaceholders(line, fPlayer, moderation);

            component = component
                    .append(messagePipeline.builder(fTarget, fPlayer, line).build())
                    .append(Component.newline());
        }

        String footer = localizationType.getFooter()
                .replace("<command>", commandLine)
                .replace("<prev_page>", String.valueOf(page-1))
                .replace("<next_page>", String.valueOf(page+1))
                .replace("<current_page>", String.valueOf(page))
                .replace("<last_page>", String.valueOf(countPage));

        component = component.append(messagePipeline.builder(fPlayer, footer).build());

        messagePulseListener.sendMessage(fPlayer, component);

        playSound(fPlayer);
    }
}
