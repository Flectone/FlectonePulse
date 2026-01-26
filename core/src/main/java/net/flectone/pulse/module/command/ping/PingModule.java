package net.flectone.pulse.module.command.ping;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageType;
import org.incendo.cloud.context.CommandContext;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PingModule extends AbstractModuleCommand<Localization.Command.Ping> {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final CommandParserProvider commandParserProvider;
    private final IntegrationModule integrationModule;
    private final PlatformPlayerAdapter platformPlayerAdapter;

    @Override
    public void onEnable() {
        super.onEnable();

        String promptPlayer = addPrompt(0, Localization.Command.Prompt::player);
        registerCommand(commandBuilder -> commandBuilder
                .permission(permission().name())
                .optional(promptPlayer, commandParserProvider.platformPlayerParser())
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        String promptPlayer = getPrompt(0);
        Optional<String> optionalTarget = commandContext.optional(promptPlayer);

        FPlayer fTarget = optionalTarget.isPresent() ? fPlayerService.getFPlayer(optionalTarget.get()) : fPlayer;
        if (!platformPlayerAdapter.isOnline(fTarget)
                || (!integrationModule.canSeeVanished(fTarget, fPlayer) && !fPlayer.equals(fTarget))) {
            sendErrorMessage(EventMetadata.<Localization.Command.Ping>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Ping::nullPlayer)
                    .build()
            );

            return;
        }

        sendMessage(EventMetadata.<Localization.Command.Ping>builder()
                .sender(fTarget)
                .filterPlayer(fPlayer)
                .format(Localization.Command.Ping::format)
                .destination(config().destination())
                .sound(soundOrThrow())
                .build()
        );

    }

    @Override
    public MessageType messageType() {
        return MessageType.COMMAND_PING;
    }

    @Override
    public Command.Ping config() {
        return fileFacade.command().ping();
    }

    @Override
    public Permission.Command.Ping permission() {
        return fileFacade.permission().command().ping();
    }

    @Override
    public Localization.Command.Ping localization(FEntity sender) {
        return fileFacade.localization(sender).command().ping();
    }
}
