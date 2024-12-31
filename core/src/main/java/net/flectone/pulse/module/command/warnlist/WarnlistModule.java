package net.flectone.pulse.module.command.warnlist;

import com.google.inject.Inject;
import lombok.Getter;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.file.Command;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.platform.PlatformSender;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.util.ComponentUtil;
import net.flectone.pulse.util.TimeUtil;
import net.kyori.adventure.text.Component;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public abstract class WarnlistModule extends AbstractModuleCommand<Localization.Command.Warnlist> {

    @Getter
    private final Command.Warnlist command;
    @Getter
    private final Permission.Command.Warnlist permission;

    private final FileManager fileManager;
    private final ComponentUtil componentUtil;
    private final CommandUtil commandUtil;
    private final TimeUtil timeUtil;
    private final PlatformSender platformSender;

    @Inject
    public WarnlistModule(FileManager fileManager,
                          ComponentUtil componentUtil,
                          CommandUtil commandUtil,
                          TimeUtil timeUtil,
                          PlatformSender platformSender) {
        super(localization -> localization.getCommand().getWarnlist(), null);

        this.fileManager = fileManager;
        this.componentUtil = componentUtil;
        this.timeUtil = timeUtil;
        this.platformSender = platformSender;
        this.commandUtil = commandUtil;

        command = fileManager.getCommand().getWarnlist();
        permission = fileManager.getPermission().getCommand().getWarnlist();
    }

    @Override
    public void onCommand(Database database, FPlayer fPlayer, Object arguments) throws SQLException {
        if (checkModulePredicates(fPlayer)) return;
        if (checkCooldown(fPlayer)) return;

        Localization.Command.Warnlist localization = resolveLocalization(fPlayer);
        Localization.ListTypeMessage localizationType = localization.getGlobal();

        String commandLine = "/" + command.getAliases().get(0);

        int page = 1;

        Optional<Object> optionalObject = commandUtil.getOptional(0, arguments);

        FPlayer targetFPlayer = null;

        if (optionalObject.isPresent() && optionalObject.get() instanceof String playerName) {
            targetFPlayer = database.getFPlayer(playerName);

            if (targetFPlayer.isUnknown()) {
                builder(fPlayer)
                        .format((fResolver, s) -> s.getNullPlayer())
                        .sendBuilt();
                return;
            }

            optionalObject = commandUtil.getOptional(1, arguments);

            commandLine += " " + playerName;

            localizationType = localization.getPlayer();
        }

        if (optionalObject.isPresent()) {
            page = (int) optionalObject.get();
        }

        List<Moderation> moderationList = targetFPlayer == null
                ? database.getValidModerations(Moderation.Type.WARN)
                : database.getModerations(targetFPlayer, Moderation.Type.WARN);

        if (moderationList.isEmpty()) {
            builder(fPlayer)
                    .format((fResolver, s) -> s.getEmpty())
                    .sendBuilt();
            return;
        }

        int size = moderationList.size();
        int perPage = command.getPerPage();
        int countPage = (int) Math.ceil((double) size / perPage);

        if (page > countPage || page < 1) {
            builder(fPlayer)
                    .format((fResolver, s) -> s.getNullPage())
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

        Localization.ReasonMap constantReasons = fileManager.getLocalization(fPlayer).getCommand().getWarn().getReasons();

        for (Moderation moderation : finalModerationList) {

            FPlayer fTarget = database.getFPlayer(moderation.getPlayer());
            String line = localizationType.getLine()
                    .replace("<command>", "/unwarn <player> <id>")
                    .replace("<player>", fTarget.getName())
                    .replace("<id>", String.valueOf(moderation.getId()))
                    .replace("<reason>", constantReasons.getConstant(moderation.getReason()))
                    .replace("<date>", timeUtil.formatDate(moderation.getDate()))
                    .replace("<time>", timeUtil.format(fPlayer, moderation.getOriginalTime()));

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

        platformSender.sendMessage(fPlayer, component);

        playSound(fPlayer);
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        createCommand();
    }

    @Override
    public boolean isConfigEnable() {
        return command.isEnable();
    }
}
