package net.flectone.pulse.module.command.banlist;

import lombok.Getter;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.file.Command;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.platform.MessageSender;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.util.ComponentUtil;
import net.flectone.pulse.util.TimeUtil;
import net.kyori.adventure.text.Component;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public abstract class BanlistModule extends AbstractModuleCommand<Localization.Command.Banlist> {

    @Getter
    private final Command.Banlist command;
    @Getter
    private final Permission.Command.Banlist permission;

    private final FileManager fileManager;
    private final CommandUtil commandUtil;
    private final ComponentUtil componentUtil;
    private final TimeUtil timeUtil;
    private final MessageSender messageSender;

    public BanlistModule(FileManager fileManager,
                         CommandUtil commandUtil,
                         ComponentUtil componentUtil,
                         TimeUtil timeUtil,
                         MessageSender messageSender) {
        super(localization -> localization.getCommand().getBanlist(), null);

        this.fileManager = fileManager;
        this.commandUtil = commandUtil;
        this.componentUtil = componentUtil;
        this.timeUtil = timeUtil;
        this.messageSender = messageSender;

        command = fileManager.getCommand().getBanlist();
        permission = fileManager.getPermission().getCommand().getBanlist();

        addPredicate(this::checkCooldown);
    }

    @Override
    public void onCommand(Database database, FPlayer fPlayer, Object arguments) throws SQLException {
        if (checkModulePredicates(fPlayer)) return;

        Localization.Command.Banlist localization = resolveLocalization(fPlayer);
        Localization.ListTypeMessage localizationType = localization.getGlobal();

        String commandLine = "/" + command.getAliases().get(0);

        int page;

        Optional<Object> optionalObject = commandUtil.getOptional(0, arguments);

        FPlayer targetFPlayer = null;

        if (optionalObject.isPresent() && optionalObject.get() instanceof String playerName) {
            targetFPlayer = database.getFPlayer(playerName);

            if (targetFPlayer.isUnknown()) {
                builder(fPlayer)
                        .format(Localization.Command.Banlist::getNullPlayer)
                        .sendBuilt();
                return;
            }

            optionalObject = commandUtil.getOptional(1, arguments);

            commandLine += " " + playerName;
            localizationType = localization.getPlayer();
        }

        page = optionalObject.map(o -> (int) o).orElse(1);

        List<Moderation> moderationList = targetFPlayer == null
                ? database.getValidModerations(Moderation.Type.BAN)
                : database.getModerations(targetFPlayer, Moderation.Type.BAN);

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
        Component component = componentUtil.builder(fPlayer, header)
                .build()
                .append(Component.newline());

        for (Moderation moderation : finalModerationList) {

            Localization.Command.Ban localizationBan = fileManager.getLocalization(fPlayer).getCommand().getBan();
            Localization.ReasonMap constantReasons = moderation.getTime() == -1
                    ? localizationBan.getPermanent().getReasons()
                    : localizationBan.getTemporarily().getReasons();

            FPlayer fTarget = database.getFPlayer(moderation.getPlayer());
            String line = localizationType.getLine()
                    .replace("<command>", "/unban <player> <id>")
                    .replace("<player>", fTarget.getName())
                    .replace("<id>", String.valueOf(moderation.getId()))
                    .replace("<reason>", constantReasons.getConstant(moderation.getReason()))
                    .replace("<date>", timeUtil.formatDate(moderation.getDate()))
                    .replace("<time>", moderation.isPermanent()
                            ? localization.getPermanentName()
                            : timeUtil.format(fPlayer, moderation.getOriginalTime())
                    );

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
