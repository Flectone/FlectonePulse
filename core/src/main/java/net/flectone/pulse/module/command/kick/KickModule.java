package net.flectone.pulse.module.command.kick;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.configuration.Command;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.formatter.MessageFormatter;
import net.flectone.pulse.formatter.ModerationMessageFormatter;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.registry.CommandRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.util.*;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.meta.CommandMeta;

import java.util.Optional;
import java.util.function.BiFunction;

@Singleton
public class KickModule extends AbstractModuleCommand<Localization.Command.Kick> {

    @Getter private final Command.Kick command;
    private final Permission.Command.Kick permission;

    private final FPlayerService fPlayerService;
    private final ModerationService moderationService;
    private final ModerationMessageFormatter moderationMessageFormatter;
    private final CommandRegistry commandRegistry;
    private final MessageFormatter messageFormatter;
    private final Gson gson;

    @Inject
    public KickModule(FileManager fileManager,
                      FPlayerService fPlayerService,
                      ModerationService moderationService,
                      ModerationMessageFormatter moderationMessageFormatter,
                      CommandRegistry commandRegistry,
                      MessageFormatter messageFormatter,
                      Gson gson) {
        super(localization -> localization.getCommand().getKick(), fPlayer -> fPlayer.isSetting(FPlayer.Setting.KICK));

        this.fPlayerService = fPlayerService;
        this.moderationService = moderationService;
        this.moderationMessageFormatter = moderationMessageFormatter;
        this.commandRegistry = commandRegistry;
        this.messageFormatter = messageFormatter;
        this.gson = gson;

        command = fileManager.getCommand().getKick();
        permission = fileManager.getPermission().getCommand().getKick();
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
        String promptMessage = getPrompt().getMessage();
        commandRegistry.registerCommand(manager ->
                manager.commandBuilder(commandName, command.getAliases(), CommandMeta.empty())
                        .permission(permission.getName())
                        .required(promptPlayer, commandRegistry.playerParser())
                        .optional(promptMessage, commandRegistry.nativeMessageParser())
                        .handler(this)
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkCooldown(fPlayer)) return;
        if (checkDisable(fPlayer, fPlayer, DisableAction.YOU)) return;
        if (checkMute(fPlayer)) return;
        if (checkModulePredicates(fPlayer)) return;

        String promptPlayer = getPrompt().getPlayer();
        String playerName = commandContext.get(promptPlayer);

        FPlayer fTarget = fPlayerService.getFPlayer(playerName);
        if (!fTarget.isOnline()) {
            builder(fPlayer)
                    .format(Localization.Command.Kick::getNullPlayer)
                    .sendBuilt();
            return;
        }

        String promptMessage = getPrompt().getMessage();
        Optional<String> optionalReason = commandContext.optional(promptMessage);
        String reason = optionalReason.orElse(null);

        Moderation kick = moderationService.kick(fTarget, reason, fPlayer.getId());
        if (kick == null) return;

        kick(fPlayer, fTarget, kick);

        builder(fTarget)
                .destination(command.getDestination())
                .range(command.getRange())
                .tag(MessageTag.COMMAND_KICK)
                .format(buildFormat(kick))
                .proxy(output -> {
                    output.writeUTF(gson.toJson(fTarget));
                    output.writeUTF(gson.toJson(kick));
                })
                .integration(s -> moderationMessageFormatter.replacePlaceholders(s, FPlayer.UNKNOWN, kick))
                .sound(getSound())
                .sendBuilt();
    }

    public BiFunction<FPlayer, Localization.Command.Kick, String> buildFormat(Moderation kick) {
        return (fReceiver, message) -> moderationMessageFormatter.replacePlaceholders(message.getServer(), fReceiver, kick);
    }

    public void kick(FEntity fModerator, FPlayer fReceiver, Moderation kick) {
        if (checkModulePredicates(fModerator)) return;

        String format = moderationMessageFormatter.replacePlaceholders(resolveLocalization(fReceiver).getPerson(), fReceiver, kick);

        fPlayerService.kick(fReceiver, messageFormatter.builder(fReceiver, format).build());
    }
}
