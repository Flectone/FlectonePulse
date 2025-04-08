package net.flectone.pulse.module.command.banlist;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Command;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.unban.UnbanModule;
import net.flectone.pulse.sender.MessageSender;
import net.flectone.pulse.registry.CommandRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.formatter.ModerationMessageFormatter;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.meta.CommandMeta;

import java.util.List;
import java.util.Optional;

@Singleton
public class BanlistModule extends AbstractModuleCommand<Localization.Command.Banlist> {

    private final Command.Banlist command;
    private final Permission.Command.Banlist permission;

    private final FPlayerService fPlayerService;
    private final ModerationService moderationService;
    private final CommandRegistry commandRegistry;
    private final ModerationMessageFormatter moderationMessageFormatter;
    private final UnbanModule unbanModule;
    private final MessagePipeline messagePipeline;
    private final MessageSender messageSender;

    @Inject
    public BanlistModule(FileManager fileManager,
                         FPlayerService fPlayerService,
                         ModerationService moderationService,
                         CommandRegistry commandRegistry,
                         ModerationMessageFormatter moderationMessageFormatter,
                         UnbanModule unbanModule,
                         MessagePipeline messagePipeline,
                         MessageSender messageSender) {
        super(localization -> localization.getCommand().getBanlist(), null);

        this.fPlayerService = fPlayerService;
        this.moderationService = moderationService;
        this.commandRegistry = commandRegistry;
        this.moderationMessageFormatter = moderationMessageFormatter;
        this.unbanModule = unbanModule;
        this.messagePipeline = messagePipeline;
        this.messageSender = messageSender;

        command = fileManager.getCommand().getBanlist();
        permission = fileManager.getPermission().getCommand().getBanlist();

        addPredicate(this::checkCooldown);
    }

    @Override
    public boolean isConfigEnable() {
        return command.isEnable();
    }

    @Override
    public void reload() {
        if (checkModulePredicates(FPlayer.UNKNOWN)) return;

        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        String commandName = getName(command);
        String promptPlayer = getPrompt().getPlayer();
        String promptNumber = getPrompt().getNumber();

        commandRegistry.registerCommand(manager ->
                manager.commandBuilder(commandName, command.getAliases(), CommandMeta.empty())
                        .permission(permission.getName())
                        .optional(promptPlayer, commandRegistry.bannedParser())
                        .optional(promptNumber, commandRegistry.integerParser())
                        .handler(this)
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkModulePredicates(fPlayer)) return;

        Localization.Command.Banlist localization = resolveLocalization(fPlayer);
        Localization.ListTypeMessage localizationType = localization.getGlobal();

        String commandLine = "/" + getName(command);

        int page = 1;
        FPlayer targetFPlayer = null;

        String promptPlayer = getPrompt().getPlayer();
        Optional<String> optionalPlayer = commandContext.optional(promptPlayer);
        if (optionalPlayer.isPresent()) {
            String playerName = optionalPlayer.get();

            try {
                page = Integer.parseInt(playerName);
            } catch (NumberFormatException e) {
                String promptNumber = getPrompt().getNumber();
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

            String line = localizationType.getLine().replace("<command>", "/" + unbanModule.getName(unbanModule.getCommand()) + " <player> <id>");
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

        messageSender.sendMessage(fPlayer, component);

        playSound(fPlayer);
    }
}
