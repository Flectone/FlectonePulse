package net.flectone.pulse.module.command.unmute;

import com.google.gson.Gson;
import lombok.Getter;
import net.flectone.pulse.file.Command;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.manager.ProxyManager;
import net.flectone.pulse.manager.ThreadManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.util.MessageTag;

import java.util.ArrayList;
import java.util.List;

public abstract class UnmuteModule extends AbstractModuleCommand<Localization.Command.Unmute> {

    @Getter
    private final Command.Unmute command;

    @Getter
    private final Permission.Command.Unmute permission;

    private final ThreadManager threadManager;
    private final ProxyManager proxyManager;
    private final CommandUtil commandUtil;
    private final Gson gson;

    public UnmuteModule(FileManager fileManager,
                        ThreadManager threadManager,
                        ProxyManager proxyManager,
                        CommandUtil commandUtil,
                        Gson gson) {
        super(localization -> localization.getCommand().getUnmute(), null);

        this.threadManager = threadManager;
        this.proxyManager = proxyManager;
        this.commandUtil = commandUtil;
        this.gson = gson;

        command = fileManager.getCommand().getUnmute();
        permission = fileManager.getPermission().getCommand().getUnmute();
    }

    @Override
    public void onCommand(FPlayer fPlayer, Object arguments) {
        if (checkModulePredicates(fPlayer)) return;
        if (checkCooldown(fPlayer)) return;

        String target = commandUtil.getString(0, arguments);
        int id = commandUtil.getByClassOrDefault(1, Integer.class, -1, arguments);

        unmute(fPlayer, target, id);
    }

    public void unmute(FPlayer fPlayer, String target, int id) {
        if (checkModulePredicates(fPlayer)) return;

        threadManager.runDatabase(database -> {
            FPlayer fTarget = database.getFPlayer(target);
            if (fTarget.isUnknown()) {
                builder(fPlayer)
                        .format(Localization.Command.Unmute::getNullPlayer)
                        .sendBuilt();
                return;
            }

            List<Moderation> mutes = new ArrayList<>();

            if (id == -1) {
                mutes.addAll(database.getValidModerations(fTarget, Moderation.Type.MUTE));
            } else {
                database.getValidModerations(fTarget, Moderation.Type.MUTE).stream()
                        .filter(moderation -> moderation.getId() == id)
                        .findAny()
                        .ifPresent(mutes::add);
            }

            if (mutes.isEmpty()) {
                builder(fPlayer)
                        .format(Localization.Command.Unmute::getNotMuted)
                        .sendBuilt();
                return;
            }

            for (Moderation mute : mutes) {
                database.setInvalidModeration(mute);
            }

            unmute(fPlayer, fTarget);

            proxyManager.sendMessage(fTarget, MessageTag.COMMAND_UNMUTE, byteArrayDataOutput ->
                    byteArrayDataOutput.writeUTF(gson.toJson(fTarget)));
        });
    }

    public void unmute(FEntity moderator, FPlayer fTarget) {
        if (checkModulePredicates(moderator)) return;

        builder(fTarget)
                .destination(command.getDestination())
                .format(Localization.Command.Unmute::getFormat)
                .sound(getSound())
                .sendBuilt();

        if (moderator.equals(fTarget)) return;
        if (!(moderator instanceof FPlayer fPlayer)) return;

        builder(fTarget)
                .destination(command.getDestination())
                .receiver(fPlayer)
                .format(Localization.Command.Unmute::getFormat)
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
