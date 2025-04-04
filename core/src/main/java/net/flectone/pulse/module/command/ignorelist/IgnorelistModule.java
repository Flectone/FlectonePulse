package net.flectone.pulse.module.command.ignorelist;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.ignore.model.Ignore;
import net.flectone.pulse.platform.MessageSender;
import net.flectone.pulse.registry.CommandRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.ComponentUtil;
import net.flectone.pulse.util.TimeUtil;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.meta.CommandMeta;

import java.util.List;
import java.util.Optional;

@Singleton
public class IgnorelistModule extends AbstractModuleCommand<Localization.Command.Ignorelist> {

    private final Command.Ignorelist command;
    private final Permission.Command.Ignorelist permission;

    private final FPlayerService fPlayerService;
    private final MessageSender messageSender;
    private final ComponentUtil componentUtil;
    private final CommandRegistry commandRegistry;
    private final TimeUtil timeUtil;

    @Inject
    public IgnorelistModule(FileManager fileManager,
                            FPlayerService fPlayerService,
                            MessageSender messageSender,
                            ComponentUtil componentUtil,
                            CommandRegistry commandRegistry,
                            TimeUtil timeUtil) {
        super(localization -> localization.getCommand().getIgnorelist(), null);

        this.fPlayerService = fPlayerService;
        this.messageSender = messageSender;
        this.componentUtil = componentUtil;
        this.commandRegistry = commandRegistry;
        this.timeUtil = timeUtil;

        command = fileManager.getCommand().getIgnorelist();
        permission = fileManager.getPermission().getCommand().getIgnorelist();

        addPredicate(this::checkCooldown);
    }

    @Override
    public boolean isConfigEnable() {
        return command.isEnable();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        String commandName = getName(command);
        String promptNumber = getPrompt().getNumber();
        commandRegistry.registerCommand(manager ->
                manager.commandBuilder(commandName, command.getAliases(), CommandMeta.empty())
                        .permission(permission.getName())
                        .optional(promptNumber, commandRegistry.integerParser())
                        .handler(this)
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
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

        String prompt = getPrompt().getNumber();
        Optional<Integer> optionalPage = commandContext.optional(prompt);
        Integer page = optionalPage.orElse(1);

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

            FPlayer fTarget = fPlayerService.getFPlayer(ignore.target());
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

        messageSender.sendMessage(fPlayer, component);

        playSound(fPlayer);
    }
}
