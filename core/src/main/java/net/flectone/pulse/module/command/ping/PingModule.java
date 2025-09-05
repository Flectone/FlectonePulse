package net.flectone.pulse.module.command.ping;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
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

    private final Command.Ping command;
    private final Permission.Command.Ping permission;
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
        super(localization -> localization.getCommand().getPing(), Command::getPing, MessageType.COMMAND_PING);

        this.command = fileResolver.getCommand().getPing();
        this.permission = fileResolver.getPermission().getCommand().getPing();
        this.fPlayerService = fPlayerService;
        this.commandParserProvider = commandParserProvider;
        this.integrationModule = integrationModule;
        this.platformPlayerAdapter = platformPlayerAdapter;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        String promptPlayer = addPrompt(0, Localization.Command.Prompt::getPlayer);
        registerCommand(commandBuilder -> commandBuilder
                .permission(permission.getName())
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
                .destination(command.getDestination())
                .sound(getModuleSound())
                .build()
        );

    }
}
