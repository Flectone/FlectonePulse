package net.flectone.pulse.module.command.chatstyle;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.configuration.Command;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.converter.ColorConverter;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.registry.CommandRegistry;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.sender.ProxySender;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.constant.MessageType;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.meta.CommandMeta;

import java.util.Optional;

@Singleton
public class ChatstyleModule extends AbstractModuleCommand<Localization.Command.Chatstyle> {

    private final Command.Chatstyle command;
    private final Permission.Command.Chatstyle permission;
    private final FPlayerService fPlayerService;
    private final PermissionChecker permissionChecker;
    private final ProxySender proxySender;
    private final CommandRegistry commandRegistry;
    private final ColorConverter colorConverter;

    @Inject
    public ChatstyleModule(FileResolver fileResolver,
                           FPlayerService fPlayerService,
                           PermissionChecker permissionChecker,
                           ProxySender proxySender,
                           CommandRegistry commandRegistry,
                           ColorConverter colorConverter) {
        super(localization -> localization.getCommand().getChatstyle(), null);
        this.fPlayerService = fPlayerService;
        this.permissionChecker = permissionChecker;
        this.proxySender = proxySender;
        this.commandRegistry = commandRegistry;
        this.colorConverter = colorConverter;
        this.command = fileResolver.getCommand().getChatstyle();
        this.permission = fileResolver.getPermission().getCommand().getChatstyle();
    }

    @Override
    protected boolean isConfigEnable() {
        return command.isEnable();
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        registerPermission(permission.getOther());

        String commandName = getName(command);
        String promptPlayer = getPrompt().getPlayer();
        String promptColor = getPrompt().getColor();
        commandRegistry.registerCommand(manager ->  manager.commandBuilder(commandName, command.getAliases(), CommandMeta.empty())
                .permission(permission.getName())
                .required(promptColor, commandRegistry.colorParser())
                .optional(promptPlayer, commandRegistry.offlinePlayerParser(), commandRegistry.playerSuggestionPermission(permission.getOther()))
                .handler(this)
        );

        addPredicate(this::checkCooldown);
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkModulePredicates(fPlayer)) return;

        String promptPlayer = getPrompt().getPlayer();
        Optional<String> optionalPlayer = commandContext.optional(promptPlayer);

        String promptColor = getPrompt().getColor();
        Optional<String> optionalStyle = commandContext.optional(promptColor);
        String style = optionalStyle.orElse(null);

        FPlayer fTarget = fPlayer;

        if (optionalPlayer.isPresent() && permissionChecker.check(fPlayer, permission.getOther())) {
            String player = optionalPlayer.get();
            fTarget = fPlayerService.getFPlayer(player);

            if (fTarget.isUnknown()) {
                builder(fPlayer)
                        .format(Localization.Command.Chatstyle::getNullPlayer)
                        .sendBuilt();
                return;
            }

            proxySender.send(fTarget, MessageType.COMMAND_CHATSTYLE, dataOutputStream ->
                    dataOutputStream.writeUTF(String.valueOf(style))
            );
        }

        if (style == null || style.equalsIgnoreCase("clear")) {
            fPlayerService.deleteSetting(fTarget, FPlayer.Setting.STYLE);
        } else {
            String convertedStyle = colorConverter.convertOrDefault(style, style);
            fPlayerService.saveOrUpdateSetting(fTarget, FPlayer.Setting.STYLE, convertedStyle);
        }

        builder(fTarget)
                .destination(command.getDestination())
                .format((fResolver, s) -> s.getFormat())
                .sound(getSound())
                .sendBuilt();
    }
}
