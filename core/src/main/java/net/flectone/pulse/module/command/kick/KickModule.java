package net.flectone.pulse.module.command.kick;

import com.google.gson.Gson;
import lombok.Getter;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.util.*;

import java.util.function.BiFunction;

public abstract class KickModule extends AbstractModuleCommand<Localization.Command.Kick> {

    @Getter private final Command.Kick command;
    @Getter private final Permission.Command.Kick permission;

    private final FPlayerService fPlayerService;
    private final ModerationService moderationService;
    private final ModerationUtil moderationUtil;
    private final CommandUtil commandUtil;
    private final ComponentUtil componentUtil;
    private final Gson gson;

    public KickModule(FileManager fileManager,
                      FPlayerService fPlayerService,
                      ModerationService moderationService,
                      ModerationUtil moderationUtil,
                      CommandUtil commandUtil,
                      ComponentUtil componentUtil,
                      Gson gson) {
        super(localization -> localization.getCommand().getKick(), fPlayer -> fPlayer.isSetting(FPlayer.Setting.KICK));

        this.fPlayerService = fPlayerService;
        this.moderationService = moderationService;
        this.moderationUtil = moderationUtil;
        this.commandUtil = commandUtil;
        this.componentUtil = componentUtil;
        this.gson = gson;

        command = fileManager.getCommand().getKick();
        permission = fileManager.getPermission().getCommand().getKick();
    }

    @Override
    public void onCommand(FPlayer fPlayer, Object arguments) {
        if (checkCooldown(fPlayer)) return;
        if (checkDisable(fPlayer, fPlayer, DisableAction.YOU)) return;
        if (checkMute(fPlayer)) return;
        if (checkModulePredicates(fPlayer)) return;

        String playerName = commandUtil.getString(0, arguments);

        FPlayer fTarget = fPlayerService.getFPlayer(playerName);
        if (!fTarget.isOnline()) {
            builder(fPlayer)
                    .format(Localization.Command.Kick::getNullPlayer)
                    .sendBuilt();
            return;
        }

        String reason = commandUtil.getString(1, arguments);
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
                .integration(s -> moderationUtil.replacePlaceholders(s, FPlayer.UNKNOWN, kick))
                .sound(getSound())
                .sendBuilt();
    }

    public BiFunction<FPlayer, Localization.Command.Kick, String> buildFormat(Moderation kick) {
        return (fReceiver, message) -> moderationUtil.replacePlaceholders(message.getServer(), fReceiver, kick);
    }

    public void kick(FEntity fModerator, FPlayer fReceiver, Moderation kick) {
        if (checkModulePredicates(fModerator)) return;

        String format = moderationUtil.replacePlaceholders(resolveLocalization(fReceiver).getPerson(), fReceiver, kick);

        fPlayerService.kick(fReceiver, componentUtil.builder(fReceiver, format).build());
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        getCommand().getAliases().forEach(commandUtil::unregister);

        createCommand();
    }

    @Override
    public boolean isConfigEnable() {
        return command.isEnable();
    }
}
