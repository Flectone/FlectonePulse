package net.flectone.pulse.module.command.ban;

import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.wrapper.login.server.WrapperLoginServerDisconnect;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.registry.CommandRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.util.*;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.meta.CommandMeta;

import java.util.Optional;
import java.util.function.BiFunction;

@Singleton
public class BanModule extends AbstractModuleCommand<Localization.Command.Ban> {

    @Getter private final Command.Ban command;
    private final Permission.Command.Ban permission;

    private final FPlayerService fPlayerService;
    private final ModerationService moderationService;
    private final CommandRegistry commandRegistry;
    private final ModerationUtil moderationUtil;
    private final PermissionUtil permissionUtil;
    private final ComponentUtil componentUtil;
    private final PacketEventsUtil packetEventsUtil;
    private final Gson gson;

    @Inject
    public BanModule(FileManager fileManager,
                     FPlayerService fPlayerService,
                     ModerationService moderationService,
                     CommandRegistry commandRegistry,
                     ModerationUtil moderationUtil,
                     PermissionUtil permissionUtil,
                     ComponentUtil componentUtil,
                     PacketEventsUtil packetEventsUtil,
                     Gson gson) {
        super(localization -> localization.getCommand().getBan(), fPlayer -> fPlayer.isSetting(FPlayer.Setting.BAN));

        this.fPlayerService = fPlayerService;
        this.moderationService = moderationService;
        this.commandRegistry = commandRegistry;
        this.moderationUtil = moderationUtil;
        this.permissionUtil = permissionUtil;
        this.componentUtil = componentUtil;
        this.packetEventsUtil = packetEventsUtil;
        this.gson = gson;

        command = fileManager.getCommand().getBan();
        permission = fileManager.getPermission().getCommand().getBan();
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
        String promptReason = getPrompt().getReason();
        String promptTime = getPrompt().getTime();

        commandRegistry.registerCommand(manager ->
                manager.commandBuilder(commandName, command.getAliases(), CommandMeta.empty())
                        .permission(permission.getName())
                        .required(promptPlayer, commandRegistry.playerParser(command.isSuggestOfflinePlayers()))
                        .optional(promptTime + " " + promptReason, commandRegistry.durationReasonParser())
                        .handler(this)
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkCooldown(fPlayer)) return;
        if (checkMute(fPlayer)) return;
        if (checkModulePredicates(fPlayer)) return;

        String promptReason = getPrompt().getReason();
        String promptTime = getPrompt().getTime();

        Optional<Pair<Long, String>> optionalTime = commandContext.optional(promptTime + " " + promptReason);
        Pair<Long, String> timeReasonPair = optionalTime.orElse(new Pair<>(-1L, null));

        long time = timeReasonPair.left();
        String reason = timeReasonPair.right();

        if (time != -1 && time < 1) {
            builder(fPlayer)
                    .format(Localization.Command.Ban::getNullTime)
                    .sendBuilt();
            return;
        }

        String promptPlayer = getPrompt().getPlayer();
        String target = commandContext.get(promptPlayer);

        ban(fPlayer, target, time, reason);
    }

    public void ban(FPlayer fPlayer, String target, long time, String reason) {
        if (checkModulePredicates(fPlayer)) return;

        FPlayer fTarget = fPlayerService.getFPlayer(target);
        if (fTarget.isUnknown()) {
            builder(fPlayer)
                    .format(Localization.Command.Ban::getNullPlayer)
                    .sendBuilt();
            return;
        }

        long databaseTime = time != -1 ? time + System.currentTimeMillis() : -1;

        Moderation ban = moderationService.ban(fTarget, databaseTime, reason, fPlayer.getId());
        if (ban == null) return;

        kick(fPlayer, fTarget, ban);

        builder(fTarget)
                .range(command.getRange())
                .destination(command.getDestination())
                .tag(MessageTag.COMMAND_BAN)
                .format(buildFormat(ban))
                .proxy(output -> {
                    output.writeUTF(gson.toJson(fPlayer));
                    output.writeUTF(gson.toJson(ban));
                })
                .integration(s -> moderationUtil.replacePlaceholders(s, FPlayer.UNKNOWN, ban))
                .sound(getSound())
                .sendBuilt();
    }

    public BiFunction<FPlayer, Localization.Command.Ban, String> buildFormat(Moderation ban) {
        return (fReceiver, message) -> {
            String format = message.getServer();

            return moderationUtil.replacePlaceholders(format, fReceiver, ban);
        };
    }

    public void kick(FEntity fModerator, FPlayer fTarget, Moderation ban) {
        if (checkModulePredicates(fModerator)) return;
        if (fModerator == null) return;

        Localization.Command.Ban localization = resolveLocalization(fTarget);

        String formatPlayer = localization.getPerson();
        formatPlayer = moderationUtil.replacePlaceholders(formatPlayer, fTarget, ban);

        fPlayerService.kick(fTarget, componentUtil.builder(fModerator, fTarget, formatPlayer).build());
    }

    public boolean isKicked(UserProfile userProfile) {
        if (!isEnable()) return false;

        FPlayer fPlayer = fPlayerService.getFPlayer(userProfile.getUUID());

        for (Moderation ban : moderationService.getValid(fPlayer, Moderation.Type.BAN)) {
            FPlayer fModerator = fPlayerService.getFPlayer(ban.getModerator());

            Localization.Command.Ban localization = resolveLocalization();

            String formatPlayer = localization.getPerson();
            formatPlayer = moderationUtil.replacePlaceholders(formatPlayer, fPlayer, ban);

            Component reason = componentUtil.builder(fModerator, fPlayer, formatPlayer).build();
            packetEventsUtil.sendPacket(userProfile.getUUID(), new WrapperLoginServerDisconnect(reason));

            if (command.isShowConnectionAttempts()) {
                builder(fPlayer)
                        .range(Range.SERVER)
                        .filter(filter -> permissionUtil.has(filter, getModulePermission()))
                        .format((fReceiver, message) -> {
                            String format = message.getConnectionAttempt();
                            return moderationUtil.replacePlaceholders(format, fReceiver, ban);
                        })
                        .sendBuilt();
            }

            return true;
        }

        return false;
    }
}
