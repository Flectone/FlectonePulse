package net.flectone.pulse.module.command.unban;

import com.google.gson.Gson;
import lombok.Getter;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.util.MessageTag;

import java.util.ArrayList;
import java.util.List;

public abstract class UnbanModule extends AbstractModuleCommand<Localization.Command.Unban> {

    @Getter private final Command.Unban command;
    @Getter private final Permission.Command.Unban permission;

    private final FPlayerService fPlayerService;
    private final ModerationService moderationService;
    private final CommandUtil commandUtil;
    private final Gson gson;

    public UnbanModule(FileManager fileManager,
                       FPlayerService fPlayerService,
                       ModerationService moderationService,
                       CommandUtil commandUtil,
                       Gson gson) {
        super(localization -> localization.getCommand().getUnban(), null);

        this.fPlayerService = fPlayerService;
        this.moderationService = moderationService;
        this.commandUtil = commandUtil;
        this.gson = gson;

        command = fileManager.getCommand().getUnban();
        permission = fileManager.getPermission().getCommand().getUnban();
    }

    @Override
    public void onCommand(FPlayer fPlayer, Object arguments) {
        if (checkModulePredicates(fPlayer)) return;
        if (checkCooldown(fPlayer)) return;

        String target = commandUtil.getString(0, arguments);
        int id = commandUtil.getByClassOrDefault(1, Integer.class, -1, arguments);

        unban(fPlayer, target, id);
    }

    public void unban(FPlayer fPlayer, String target, int id) {
        if (checkModulePredicates(fPlayer)) return;

        FPlayer fTarget = fPlayerService.getFPlayer(target);
        if (fTarget.isUnknown()) {
            builder(fPlayer)
                    .format(Localization.Command.Unban::getNullPlayer)
                    .sendBuilt();
            return;
        }

        List<Moderation> bans = new ArrayList<>();

        if (id == -1) {
            bans.addAll(moderationService.getValid(fTarget, Moderation.Type.BAN));
        } else {
            moderationService.getValid(fTarget, Moderation.Type.BAN).stream()
                    .filter(moderation -> moderation.getId() == id)
                    .findAny()
                    .ifPresent(bans::add);
        }

        if (bans.isEmpty()) {
            builder(fPlayer)
                    .format(Localization.Command.Unban::getNotBanned)
                    .sendBuilt();
            return;
        }

        for (Moderation ban : bans) {
            moderationService.setInvalid(ban);
        }

        builder(fTarget)
                .tag(MessageTag.COMMAND_UNBAN)
                .destination(command.getDestination())
                .range(command.getRange())
                .filter(filter -> filter.isSetting(FPlayer.Setting.BAN))
                .format(unwarn -> unwarn.getFormat().replace("<moderator>", fPlayer.getName()))
                .proxy(output -> output.writeUTF(gson.toJson(fPlayer)))
                .integration(s -> s.replace("<moderator>", fPlayer.getName()))
                .sound(getSound())
                .sendBuilt();
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
