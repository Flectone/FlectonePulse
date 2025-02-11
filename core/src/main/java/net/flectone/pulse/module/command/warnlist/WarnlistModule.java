package net.flectone.pulse.module.command.warnlist;

import com.google.inject.Inject;
import lombok.Getter;
import net.flectone.pulse.database.dao.FPlayerDAO;
import net.flectone.pulse.database.dao.ModerationDAO;
import net.flectone.pulse.file.Command;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.unwarn.UnwarnModule;
import net.flectone.pulse.platform.MessageSender;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.util.ComponentUtil;
import net.flectone.pulse.util.ModerationUtil;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.Optional;

public abstract class WarnlistModule extends AbstractModuleCommand<Localization.Command.Warnlist> {

    @Getter private final Command.Warnlist command;
    @Getter private final Permission.Command.Warnlist permission;

    private final FPlayerDAO fPlayerDAO;
    private final ModerationDAO moderationDAO;
    private final UnwarnModule unwarnModule;
    private final ComponentUtil componentUtil;
    private final CommandUtil commandUtil;
    private final ModerationUtil moderationUtil;
    private final MessageSender messageSender;

    @Inject
    public WarnlistModule(FileManager fileManager,
                          FPlayerDAO fPlayerDAO,
                          ModerationDAO moderationDAO,
                          UnwarnModule unwarnModule,
                          ComponentUtil componentUtil,
                          CommandUtil commandUtil,
                          ModerationUtil moderationUtil,
                          MessageSender messageSender) {
        super(localization -> localization.getCommand().getWarnlist(), null);

        this.fPlayerDAO = fPlayerDAO;
        this.moderationDAO = moderationDAO;
        this.unwarnModule = unwarnModule;
        this.componentUtil = componentUtil;
        this.moderationUtil = moderationUtil;
        this.messageSender = messageSender;
        this.commandUtil = commandUtil;

        command = fileManager.getCommand().getWarnlist();
        permission = fileManager.getPermission().getCommand().getWarnlist();
    }

    @Override
    public void onCommand(FPlayer fPlayer, Object arguments) {
        if (checkModulePredicates(fPlayer)) return;
        if (checkCooldown(fPlayer)) return;

        Localization.Command.Warnlist localization = resolveLocalization(fPlayer);
        Localization.ListTypeMessage localizationType = localization.getGlobal();

        String commandLine = "/" + command.getAliases().get(0);

        int page = 1;

        Optional<Object> optionalObject = commandUtil.getOptional(0, arguments);

        FPlayer targetFPlayer = null;

        if (optionalObject.isPresent() && optionalObject.get() instanceof String playerName) {
            targetFPlayer = fPlayerDAO.getFPlayer(playerName);

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
                ? moderationDAO.getValidModerations(Moderation.Type.WARN)
                : moderationDAO.getModerations(targetFPlayer, Moderation.Type.WARN);

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

        for (Moderation moderation : finalModerationList) {

            FPlayer fTarget = fPlayerDAO.getFPlayer(moderation.getPlayer());

            String line = localizationType.getLine().replace("<command>", "/" + unwarnModule.getName(unwarnModule.getCommand()) + " <player> <id>");
            line = moderationUtil.replacePlaceholders(line, fPlayer, moderation);

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
