package net.flectone.pulse.module.command.helper;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageType;
import org.incendo.cloud.context.CommandContext;

import java.util.List;
import java.util.function.Predicate;

@Singleton
public class HelperModule extends AbstractModuleCommand<Localization.Command.Helper> {

    private final FileResolver fileResolver;
    private final FPlayerService fPlayerService;
    private final ProxyRegistry proxyRegistry;
    private final PermissionChecker permissionChecker;
    private final CommandParserProvider commandParserProvider;

    @Inject
    public HelperModule(FileResolver fileResolver,
                        FPlayerService fPlayerService,
                        ProxyRegistry proxyRegistry,
                        PermissionChecker permissionChecker,
                        CommandParserProvider commandParserProvider) {
        super(MessageType.COMMAND_HELPER);

        this.fileResolver = fileResolver;
        this.fPlayerService = fPlayerService;
        this.proxyRegistry = proxyRegistry;
        this.permissionChecker = permissionChecker;
        this.commandParserProvider = commandParserProvider;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission());

        createCooldown(config().getCooldown(), permission().getCooldownBypass());
        createSound(config().getSound(), permission().getSound());

        registerPermission(permission().getSee());

        String promptMessage = addPrompt(0, Localization.Command.Prompt::getMessage);
        registerCommand(commandBuilder -> commandBuilder
                .permission(permission().getName())
                .required(promptMessage, commandParserProvider.nativeMessageParser())
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        Predicate<FPlayer> filter = getFilterSee();

        List<FPlayer> recipients = fPlayerService.getVisibleFPlayersFor(fPlayer).stream().filter(filter).toList();
        if (recipients.isEmpty() && config().isNullHelper()) {
            boolean nullHelper = !proxyRegistry.hasEnabledProxy() || fPlayerService.findOnlineFPlayers().stream()
                    .noneMatch(online -> permissionChecker.check(online, permission().getSee()));

            if (nullHelper) {
                sendErrorMessage(metadataBuilder()
                        .sender(fPlayer)
                        .format(Localization.Command.Helper::getNullHelper)
                        .build()
                );

                return;
            }
        }

        String message = getArgument(commandContext, 0);

        sendMessage(metadataBuilder()
                .sender(fPlayer)
                .format(Localization.Command.Helper::getPlayer)
                .destination(config().getDestination())
                .build()
        );

        sendMessage(metadataBuilder()
                .sender(fPlayer)
                .format(Localization.Command.Helper::getGlobal)
                .destination(config().getDestination())
                .range(config().getRange())
                .message(message)
                .filter(filter)
                .proxy(dataOutputStream -> dataOutputStream.writeString(message))
                .integration()
                .build()
        );
    }

    @Override
    public Command.Helper config() {
        return fileResolver.getCommand().getHelper();
    }

    @Override
    public Permission.Command.Helper permission() {
        return fileResolver.getPermission().getCommand().getHelper();
    }

    @Override
    public Localization.Command.Helper localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getCommand().getHelper();
    }

    public Predicate<FPlayer> getFilterSee() {
        return fPlayer -> permissionChecker.check(fPlayer, permission().getSee());
    }
}
