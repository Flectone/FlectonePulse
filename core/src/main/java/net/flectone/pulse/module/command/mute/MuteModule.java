package net.flectone.pulse.module.command.mute;

import com.google.gson.Gson;
import lombok.Getter;
import net.flectone.pulse.file.Command;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.manager.ThreadManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.util.MessageTag;
import net.flectone.pulse.util.ModerationUtil;

import java.util.function.BiFunction;

public abstract class MuteModule extends AbstractModuleCommand<Localization.Command.Mute> {

    @Getter private final Command.Mute command;
    @Getter private final Permission.Command.Mute permission;

    private final ThreadManager threadManager;
    private final FPlayerManager fPlayerManager;
    private final CommandUtil commandUtil;
    private final ModerationUtil moderationUtil;
    private final Gson gson;

    public MuteModule(FileManager fileManager,
                      ThreadManager threadManager,
                      FPlayerManager fPlayerManager,
                      CommandUtil commandUtil,
                      ModerationUtil moderationUtil,
                      Gson gson) {
        super(localization -> localization.getCommand().getMute(), fPlayer -> fPlayer.is(FPlayer.Setting.MUTE));

        this.threadManager = threadManager;
        this.fPlayerManager = fPlayerManager;
        this.commandUtil = commandUtil;
        this.moderationUtil = moderationUtil;
        this.gson = gson;

        command = fileManager.getCommand().getMute();
        permission = fileManager.getPermission().getCommand().getMute();
    }

    @Override
    public void onCommand(FPlayer fPlayer, Object arguments) {
        if (checkModulePredicates(fPlayer)) return;
        if (checkCooldown(fPlayer)) return;
        if (checkMute(fPlayer)) return;

        String target = commandUtil.getString(0, arguments);
        long time = commandUtil.getInteger(1, arguments) * 1000L;

        if (time < 1) {
            builder(fPlayer)
                    .format(Localization.Command.Mute::getNullTime)
                    .sendBuilt();
            return;
        }

        String reason =  commandUtil.getString(2, arguments);
        threadManager.runDatabase(database -> {

            FPlayer fTarget = database.getFPlayer(target);
            if (fTarget.isUnknown()) {
                builder(fPlayer)
                        .format(Localization.Command.Mute::getNullPlayer)
                        .sendBuilt();
                return;
            }

            long databaseTime = time != -1 ? time + System.currentTimeMillis() : -1;

            Moderation mute = database.insertModeration(fTarget, databaseTime, reason, fPlayer.getId(), Moderation.Type.MUTE);
            if (mute == null) return;

            if (!fPlayerManager.get(fTarget.getUuid()).isUnknown()) {
                fPlayerManager.get(fTarget.getUuid()).updateMutes(database.getValidModerations(Moderation.Type.MUTE));
            }

            builder(fTarget)
                    .range(command.getRange())
                    .destination(command.getDestination())
                    .tag(MessageTag.COMMAND_MUTE)
                    .format(buildFormat(mute))
                    .proxy(output -> {
                        output.writeUTF(gson.toJson(fPlayer));
                        output.writeUTF(gson.toJson(mute));
                    })
                    .integration(s -> moderationUtil.replacePlaceholders(s, FPlayer.UNKNOWN, mute))
                    .sound(getSound())
                    .sendBuilt();

            sendForTarget(fPlayer, fTarget, mute);
        });
    }

    public BiFunction<FPlayer, Localization.Command.Mute, String> buildFormat(Moderation mute) {
        return (fReceiver, message) -> moderationUtil.replacePlaceholders(message.getServer(), fReceiver, mute);
    }

    public void sendForTarget(FEntity fModerator, FPlayer fReceiver, Moderation mute) {
        if (checkModulePredicates(fModerator)) return;

        builder(fReceiver)
                .format(s -> moderationUtil.replacePlaceholders(s.getPerson(), fReceiver, mute))
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
