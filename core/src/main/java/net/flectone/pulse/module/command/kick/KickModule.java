package net.flectone.pulse.module.command.kick;

import com.google.gson.Gson;
import lombok.Getter;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.file.Command;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.util.ComponentUtil;
import net.flectone.pulse.util.DisableAction;
import net.flectone.pulse.util.MessageTag;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.function.Function;

public abstract class KickModule extends AbstractModuleCommand<Localization.Command.Kick> {

    @Getter
    private final Command.Kick command;
    @Getter
    private final Permission.Command.Kick permission;

    private final FPlayerManager fPlayerManager;
    private final CommandUtil commandUtil;
    private final ComponentUtil componentUtil;
    private final Gson gson;

    public KickModule(FileManager fileManager,
                      FPlayerManager fPlayerManager,
                      CommandUtil commandUtil,
                      ComponentUtil componentUtil,
                      Gson gson) {
        super(localization -> localization.getCommand().getKick(), fPlayer -> fPlayer.is(FPlayer.Setting.KICK));

        this.fPlayerManager = fPlayerManager;
        this.commandUtil = commandUtil;
        this.componentUtil = componentUtil;
        this.gson = gson;

        command = fileManager.getCommand().getKick();
        permission = fileManager.getPermission().getCommand().getKick();
    }

    @Override
    public void onCommand(Database database, FPlayer fPlayer, Object arguments) throws SQLException {
        if (checkCooldown(fPlayer)) return;
        if (checkDisable(fPlayer, fPlayer, DisableAction.YOU)) return;
        if (checkMute(fPlayer)) return;
        if (checkModulePredicates(fPlayer)) return;

        String playerName = commandUtil.getString(0, arguments);

        FPlayer fTarget = database.getFPlayer(playerName);
        if (!fTarget.isOnline()) {
            builder(fPlayer)
                    .format(Localization.Command.Kick::getNullPlayer)
                    .sendBuilt();
            return;
        }

        String reason = commandUtil.getString(1, arguments);

        kick(fPlayer, fTarget, reason);

        builder(fPlayer)
                .destination(command.getDestination())
                .range(command.getRange())
                .tag(MessageTag.COMMAND_KICK)
                .format(replaceTarget(fTarget.getName()))
                .message((fResolver, s) -> s.getReasons().getConstant(reason))
                .proxy(output -> {
                    output.writeUTF(gson.toJson(fTarget));
                    output.writeUTF(reason == null ? "" : reason);
                })
                .integration(s -> s
                        .replace("<reason>", resolveLocalization().getReasons().getConstant(reason))
                        .replace("<target>", fTarget.getName())
                )
                .sound(getSound())
                .sendBuilt();
    }

    public Function<Localization.Command.Kick, String> replaceTarget(String target) {
        return message -> message.getGlobal().replace("<target>", target);
    }

    public void kick(FEntity fPlayer, FPlayer fTarget, @Nullable String reason) {
        if (checkModulePredicates(fPlayer)) return;

        Localization.Command.Kick localization = resolveLocalization(fTarget);

        String format = localization.getPlayer()
                .replace("<message>",localization.getReasons().getConstant(reason))
                .replace("<moderator>", fPlayer.getName());

        Component component = componentUtil.builder(fPlayer, format).build();
        fPlayerManager.kick(fTarget, component);
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
