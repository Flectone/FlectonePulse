package net.flectone.pulse.module.command.clearchat;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageType;
import org.incendo.cloud.context.CommandContext;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ClearchatModule extends AbstractModuleCommand<Localization.Command.Clearchat> {

    private final FileResolver fileResolver;
    private final FPlayerService fPlayerService;
    private final PermissionChecker permissionChecker;
    private final CommandParserProvider commandParserProvider;
    private final ProxySender proxySender;
    private final PlatformPlayerAdapter platformPlayerAdapter;

    @Override
    public void onEnable() {
        super.onEnable();

        registerPermission(permission().getOther());

        String promptPlayer = addPrompt(0, Localization.Command.Prompt::getPlayer);
        registerCommand(commandBuilder -> commandBuilder
                        .permission(permission().getName())
                        .optional(promptPlayer, commandParserProvider.playerParser(), commandParserProvider.playerSuggestionPermission(false, permission().getOther()))
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        String promptPlayer = getPrompt(0);
        Optional<String> optionalPlayer = commandContext.optional(promptPlayer);

        FPlayer fTarget = fPlayer;

        if (optionalPlayer.isPresent() && permissionChecker.check(fPlayer, permission().getOther())) {
            String player = optionalPlayer.get();
            if (player.equals("all")) {
                fPlayerService.findOnlineFPlayers().forEach(this::clearChat);
                return;
            }

            fTarget = fPlayerService.getFPlayer(player);
            if (fTarget.isUnknown()) {
                sendErrorMessage(metadataBuilder()
                        .sender(fPlayer)
                        .format(Localization.Command.Clearchat::getNullPlayer)
                        .build()
                );

                return;
            }
        }

        clearChat(fTarget);
    }

    @Override
    public MessageType messageType() {
        return MessageType.COMMAND_CLEARCHAT;
    }

    @Override
    public Command.Clearchat config() {
        return fileResolver.getCommand().getClearchat();
    }

    @Override
    public Permission.Command.Clearchat permission() {
        return fileResolver.getPermission().getCommand().getClearchat();
    }

    @Override
    public Localization.Command.Clearchat localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getCommand().getClearchat();
    }

    public void clearChat(FPlayer fPlayer) {
        clearChat(fPlayer, true);
    }

    public void clearChat(FPlayer fPlayer, boolean checkProxy) {
        if (checkProxy
                && !platformPlayerAdapter.isOnline(fPlayer)
                && proxySender.send(fPlayer, MessageType.COMMAND_CLEARCHAT)) {
            return;
        }

        sendMessage(metadataBuilder()
                .sender(fPlayer)
                .format("<br> ".repeat(100))
                .destination(config().getDestination())
                .build()
        );

        sendMessage(metadataBuilder()
                .sender(fPlayer)
                .format(Localization.Command.Clearchat::getFormat)
                .destination(config().getDestination())
                .sound(getModuleSound())
                .build()
        );
    }
}
