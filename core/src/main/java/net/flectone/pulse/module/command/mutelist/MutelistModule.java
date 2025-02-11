package net.flectone.pulse.module.command.mutelist;

import lombok.Getter;
import net.flectone.pulse.database.dao.FPlayerDAO;
import net.flectone.pulse.database.dao.ModerationDAO;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.unmute.UnmuteModule;
import net.flectone.pulse.platform.MessageSender;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.util.ComponentUtil;
import net.flectone.pulse.util.ModerationUtil;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.Optional;

public abstract class MutelistModule extends AbstractModuleCommand<Localization.Command.Mutelist> {

    @Getter private final Command.Mutelist command;
    @Getter private final Permission.Command.Mutelist permission;

    private final FPlayerDAO fPlayerDAO;
    private final ModerationDAO moderationDAO;
    private final UnmuteModule unmuteModule;
    private final ComponentUtil componentUtil;
    private final CommandUtil commandUtil;
    private final ModerationUtil moderationUtil;
    private final MessageSender messageSender;

    public MutelistModule(FileManager fileManager,
                          FPlayerDAO fPlayerDAO,
                          ModerationDAO moderationDAO,
                          UnmuteModule unmuteModule,
                          ComponentUtil componentUtil,
                          CommandUtil commandUtil,
                          ModerationUtil moderationUtil,
                          MessageSender messageSender) {
        super(localization -> localization.getCommand().getMutelist(), null);

        this.fPlayerDAO = fPlayerDAO;
        this.moderationDAO = moderationDAO;
        this.unmuteModule = unmuteModule;
        this.componentUtil = componentUtil;
        this.commandUtil = commandUtil;
        this.moderationUtil = moderationUtil;
        this.messageSender = messageSender;

        command = fileManager.getCommand().getMutelist();
        permission = fileManager.getPermission().getCommand().getMutelist();

        addPredicate(this::checkCooldown);
    }

    @Override
    public void onCommand(FPlayer fPlayer, Object arguments) {
        if (checkModulePredicates(fPlayer)) return;

        Localization.Command.Mutelist localization = resolveLocalization(fPlayer);
        Localization.ListTypeMessage localizationType = localization.getGlobal();

        String commandLine = "/" + command.getAliases().get(0);

        int page = 1;

        Optional<Object> optionalObject = commandUtil.getOptional(0, arguments);

        FPlayer targetFPlayer = null;

        if (optionalObject.isPresent() && optionalObject.get() instanceof String playerName) {
            targetFPlayer = fPlayerDAO.getFPlayer(playerName);

            if (targetFPlayer.isUnknown()) {
                builder(fPlayer)
                        .format(Localization.Command.Mutelist::getNullPlayer)
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
                ? moderationDAO.getValidModerations(Moderation.Type.MUTE)
                : moderationDAO.getModerations(targetFPlayer, Moderation.Type.MUTE);

        if (moderationList.isEmpty()) {
            builder(fPlayer)
                    .format(Localization.Command.Mutelist::getEmpty)
                    .sendBuilt();
            return;
        }

        int size = moderationList.size();
        int perPage = command.getPerPage();
        int countPage = (int) Math.ceil((double) size / perPage);

        if (page > countPage || page < 1) {
            builder(fPlayer)
                    .format(Localization.Command.Mutelist::getNullPage)
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

            String line = localizationType.getLine().replace("<command>", "/" + unmuteModule.getName(unmuteModule.getCommand()) + " <player> <id>");
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
