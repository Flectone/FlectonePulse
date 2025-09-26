package net.flectone.pulse.module.command.ping;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageType;
import org.incendo.cloud.context.CommandContext;

import java.util.Optional;

@Singleton
public class PingModule extends AbstractModuleCommand<Localization.Command.Ping> {

    private final FileResolver fileResolver;
    private final FPlayerService fPlayerService;
    private final CommandParserProvider commandParserProvider;
    private final IntegrationModule integrationModule;
    private final PlatformPlayerAdapter platformPlayerAdapter;

    @Inject
    public PingModule(FileResolver fileResolver,
                      FPlayerService fPlayerService,
                      CommandParserProvider commandParserProvider,
                      IntegrationModule integrationModule,
                      PlatformPlayerAdapter platformPlayerAdapter) {
        super(MessageType.COMMAND_PING);

        this.fileResolver = fileResolver;
        this.fPlayerService = fPlayerService;
        this.commandParserProvider = commandParserProvider;
        this.integrationModule = integrationModule;
        this.platformPlayerAdapter = platformPlayerAdapter;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission());

        createCooldown(config().getCooldown(), permission().getCooldownBypass());
        createSound(config().getSound(), permission().getSound());

        String promptPlayer = addPrompt(0, Localization.Command.Prompt::getPlayer);
        registerCommand(commandBuilder -> commandBuilder
                .permission(permission().getName())
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
            sendErrorMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Ping::getNullPlayer)
                    .build()
            );

            return;
        }

        sendMessage(metadataBuilder()
                .sender(fTarget)
                .filterPlayer(fPlayer)
                .format(Localization.Command.Ping::getFormat)
                .destination(config().getDestination())
                .sound(getModuleSound())
                .build()
        );

    }

    @Override
    public Command.Ping config() {
        return fileResolver.getCommand().getPing();
    }

    @Override
    public Permission.Command.Ping permission() {
        return fileResolver.getPermission().getCommand().getPing();
    }

    @Override
    public Localization.Command.Ping localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getCommand().getPing();
    }
}
