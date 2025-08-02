package net.flectone.pulse.module.command.chatstyle;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.processing.converter.ColorConverter;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.service.FPlayerService;
import org.incendo.cloud.context.CommandContext;

import java.util.Optional;

@Singleton
public class ChatstyleModule extends AbstractModuleCommand<Localization.Command.Chatstyle> {

    private final Command.Chatstyle command;
    private final Permission.Command.Chatstyle permission;
    private final FPlayerService fPlayerService;
    private final PermissionChecker permissionChecker;
    private final ProxySender proxySender;
    private final ColorConverter colorConverter;
    private final CommandParserProvider commandParserProvider;

    @Inject
    public ChatstyleModule(FileResolver fileResolver,
                           FPlayerService fPlayerService,
                           PermissionChecker permissionChecker,
                           ProxySender proxySender,
                           ColorConverter colorConverter,
                           CommandParserProvider commandParserProvider) {
        super(localization -> localization.getCommand().getChatstyle(), Command::getChatstyle);
        this.fPlayerService = fPlayerService;
        this.permissionChecker = permissionChecker;
        this.proxySender = proxySender;
        this.colorConverter = colorConverter;
        this.command = fileResolver.getCommand().getChatstyle();
        this.permission = fileResolver.getPermission().getCommand().getChatstyle();
        this.commandParserProvider = commandParserProvider;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        registerPermission(permission.getOther());

        String promptPlayer = addPrompt(0, Localization.Command.Prompt::getPlayer);
        String promptColor = addPrompt(1, Localization.Command.Prompt::getColor);
        registerCommand(commandBuilder -> commandBuilder
                .permission(permission.getName())
                .required(promptColor, commandParserProvider.colorParser())
                .optional(promptPlayer, commandParserProvider.offlinePlayerParser(), commandParserProvider.playerSuggestionPermission(permission.getOther()))
        );

        addPredicate(this::checkCooldown);
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkModulePredicates(fPlayer)) return;

        String promptPlayer = getPrompt(0);
        Optional<String> optionalPlayer = commandContext.optional(promptPlayer);

        String promptColor = getPrompt(1);
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
