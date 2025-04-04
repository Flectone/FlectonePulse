package net.flectone.pulse.module.command.mutelist;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.unmute.UnmuteModule;
import net.flectone.pulse.message.MessageSender;
import net.flectone.pulse.registry.CommandRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.util.ComponentUtil;
import net.flectone.pulse.util.ModerationUtil;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.meta.CommandMeta;

import java.util.List;
import java.util.Optional;

@Singleton
public class MutelistModule extends AbstractModuleCommand<Localization.Command.Mutelist> {

    private final Command.Mutelist command;
    private final Permission.Command.Mutelist permission;

    private final FPlayerService fPlayerService;
    private final ModerationService moderationService;
    private final ModerationUtil moderationUtil;
    private final UnmuteModule unmuteModule;
    private final ComponentUtil componentUtil;
    private final CommandRegistry commandRegistry;
    private final MessageSender messageSender;

    @Inject
    public MutelistModule(FileManager fileManager,
                          FPlayerService fPlayerService,
                          ModerationService moderationService,
                          ModerationUtil moderationUtil,
                          UnmuteModule unmuteModule,
                          ComponentUtil componentUtil,
                          CommandRegistry commandRegistry,
                          MessageSender messageSender) {
        super(localization -> localization.getCommand().getMutelist(), null);

        this.fPlayerService = fPlayerService;
        this.moderationService = moderationService;
        this.moderationUtil = moderationUtil;
        this.unmuteModule = unmuteModule;
        this.componentUtil = componentUtil;
        this.commandRegistry = commandRegistry;
        this.messageSender = messageSender;

        command = fileManager.getCommand().getMutelist();
        permission = fileManager.getPermission().getCommand().getMutelist();

        addPredicate(this::checkCooldown);
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

        String commandName = getName(command);
        String promptPlayer = getPrompt().getPlayer();
        String promptNumber = getPrompt().getNumber();
        commandRegistry.registerCommand(manager ->
                manager.commandBuilder(commandName, command.getAliases(), CommandMeta.empty())
                        .permission(permission.getName())
                        .optional(promptPlayer, commandRegistry.mutedParser())
                        .optional(promptNumber, commandRegistry.integerParser())
                        .handler(this)
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkModulePredicates(fPlayer)) return;

        Localization.Command.Mutelist localization = resolveLocalization(fPlayer);
        Localization.ListTypeMessage localizationType = localization.getGlobal();

        String commandLine = "/" + getName(command);

        FPlayer targetFPlayer = null;
        int page = 1;

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
                            .format(Localization.Command.Mutelist::getNullPlayer)
                            .sendBuilt();
                    return;
                }

                commandLine += " " + playerName;
                localizationType = localization.getPlayer();
            }
        }

        List<Moderation> moderationList = targetFPlayer == null
                ? moderationService.getValid(Moderation.Type.MUTE)
                : moderationService.getValid(targetFPlayer, Moderation.Type.MUTE);

        if (moderationList.isEmpty()) {
            builder(fPlayer)
                    .format(Localization.Command.Mutelist::getEmpty)
                    .sendBuilt();
            return;
        }

        int size = moderationList.size();
        int perPage = command.getPerPage();
        int countPage = (int) Math.ceil((double) size / perPage);

        if (page > countPage || page < 1) {
            builder(fPlayer)
                    .format(Localization.Command.Mutelist::getNullPage)
                    .sendBuilt();
            return;
        }

        List<Moderation> finalModerationList = moderationList.stream()
                .skip((long) (page - 1) * perPage)
                .limit(perPage)
                .toList();

        String header = localizationType.getHeader().replace("<count>", String.valueOf(size));
        Component component = componentUtil.builder(fPlayer, header)
                .build()
                .append(Component.newline());

        for (Moderation moderation : finalModerationList) {

            FPlayer fTarget = fPlayerService.getFPlayer(moderation.getPlayer());

            String line = localizationType.getLine().replace("<command>", "/" + unmuteModule.getName(unmuteModule.getCommand()) + " <player> <id>");
            line = moderationUtil.replacePlaceholders(line, fPlayer, moderation);

            component = component
                    .append(componentUtil.builder(fTarget, fPlayer, line).build())
                    .append(Component.newline());
        }

        String footer = localizationType.getFooter()
                .replace("<command>", commandLine)
                .replace("<prev_page>", String.valueOf(page-1))
                .replace("<next_page>", String.valueOf(page+1))
                .replace("<current_page>", String.valueOf(page))
                .replace("<last_page>", String.valueOf(countPage));

        component = component.append(componentUtil.builder(fPlayer, footer).build());

        messageSender.sendMessage(fPlayer, component);

        playSound(fPlayer);
    }
}
