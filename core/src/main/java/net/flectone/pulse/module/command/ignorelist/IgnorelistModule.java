package net.flectone.pulse.module.command.ignorelist;

import lombok.Getter;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.file.Command;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.ignore.model.Ignore;
import net.flectone.pulse.platform.PlatformSender;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.util.ComponentUtil;
import net.flectone.pulse.util.TimeUtil;
import net.kyori.adventure.text.Component;

import java.sql.SQLException;
import java.util.List;

public abstract class IgnorelistModule extends AbstractModuleCommand<Localization.Command.Ignorelist> {

    @Getter
    private final Command.Ignorelist command;
    @Getter
    private final Permission.Command.Ignorelist permission;

    private final PlatformSender platformSender;
    private final ComponentUtil componentUtil;
    private final CommandUtil commandUtil;
    private final TimeUtil timeUtil;

    public IgnorelistModule(FileManager fileManager,
                            PlatformSender platformSender,
                            ComponentUtil componentUtil,
                            CommandUtil commandUtil,
                            TimeUtil timeUtil) {
        super(localization -> localization.getCommand().getIgnorelist(), null);

        this.platformSender = platformSender;
        this.componentUtil = componentUtil;
        this.commandUtil = commandUtil;
        this.timeUtil = timeUtil;

        command = fileManager.getCommand().getIgnorelist();
        permission = fileManager.getPermission().getCommand().getIgnorelist();

        addPredicate(this::checkCooldown);
    }

    @Override
    public void onCommand(Database database, FPlayer fPlayer, Object arguments) throws SQLException {
        if (checkModulePredicates(fPlayer)) return;

        List<Ignore> ignoreList = fPlayer.getIgnores();
        if (ignoreList.isEmpty()) {
            builder(fPlayer)
                    .format(Localization.Command.Ignorelist::getEmpty)
                    .sendBuilt();
            return;
        }

        Localization.Command.Ignorelist localization = resolveLocalization(fPlayer);

        int size = ignoreList.size();
        int perPage = command.getPerPage();
        int countPage = (int) Math.ceil((double) size / perPage);

        Integer page = commandUtil.getByClassOrDefault(0, Integer.class, 1, arguments);

        if (page > countPage || page < 1) {
            builder(fPlayer)
                    .format(Localization.Command.Ignorelist::getNullPage)
                    .sendBuilt();
            return;
        }

        String commandLine = "/" + command.getAliases().get(0);

        List<Ignore> finalIgnoreList = ignoreList.stream()
                 .skip((long) (page - 1) * perPage)
                 .limit(perPage)
                 .toList();
        String header = localization.getHeader().replace("<count>", String.valueOf(size));
        Component component = componentUtil.builder(fPlayer, header)
                .build()
                .append(Component.newline());

        for (Ignore ignore : finalIgnoreList) {

            FPlayer fTarget = database.getFPlayer(ignore.target());
            String line = localization.getLine()
                    .replace("<command>", "/ignore " + fTarget.getName())
                    .replace("<date>", timeUtil.formatDate(ignore.date()));

            component = component
                    .append(componentUtil.builder(fTarget, fPlayer, line).build())
                    .append(Component.newline());
        }

        String footer = localization.getFooter()
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

        getCommand().getAliases().forEach(commandUtil::unregister);

        createCommand();
    }

    @Override
    public boolean isConfigEnable() {
        return command.isEnable();
    }
}
