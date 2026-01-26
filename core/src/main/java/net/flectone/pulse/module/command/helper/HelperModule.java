package net.flectone.pulse.module.command.helper;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.file.FileFacade;
import org.incendo.cloud.context.CommandContext;

import java.util.List;
import java.util.function.Predicate;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class HelperModule extends AbstractModuleCommand<Localization.Command.Helper> {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final ProxyRegistry proxyRegistry;
    private final PermissionChecker permissionChecker;
    private final CommandParserProvider commandParserProvider;

    @Override
    public void onEnable() {
        super.onEnable();

        String promptMessage = addPrompt(0, Localization.Command.Prompt::message);
        registerCommand(commandBuilder -> commandBuilder
                .permission(permission().name())
                .required(promptMessage, commandParserProvider.nativeMessageParser())
        );
    }

    @Override
    public ImmutableList.Builder<PermissionSetting> permissionBuilder() {
        return super.permissionBuilder()
                .add(permission().see());
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        Predicate<FPlayer> filter = getFilterSee();

        List<FPlayer> recipients = fPlayerService.getVisibleFPlayersFor(fPlayer).stream().filter(filter).toList();
        if (recipients.isEmpty() && config().nullHelper()) {
            boolean nullHelper = !proxyRegistry.hasEnabledProxy() || fPlayerService.findOnlineFPlayers().stream()
                    .noneMatch(online -> permissionChecker.check(online, permission().see()));

            if (nullHelper) {
                sendErrorMessage(EventMetadata.<Localization.Command.Helper>builder()
                        .sender(fPlayer)
                        .format(Localization.Command.Helper::nullHelper)
                        .build()
                );

                return;
            }
        }

        String message = getArgument(commandContext, 0);

        sendMessage(EventMetadata.<Localization.Command.Helper>builder()
                .sender(fPlayer)
                .format(Localization.Command.Helper::player)
                .destination(config().destination())
                .build()
        );

        sendMessage(EventMetadata.<Localization.Command.Helper>builder()
                .sender(fPlayer)
                .format(Localization.Command.Helper::global)
                .destination(config().destination())
                .range(config().range())
                .message(message)
                .filter(filter)
                .proxy(dataOutputStream -> dataOutputStream.writeString(message))
                .integration()
                .sound(soundOrThrow())
                .build()
        );
    }

    @Override
    public MessageType messageType() {
        return MessageType.COMMAND_HELPER;
    }

    @Override
    public Command.Helper config() {
        return fileFacade.command().helper();
    }

    @Override
    public Permission.Command.Helper permission() {
        return fileFacade.permission().command().helper();
    }

    @Override
    public Localization.Command.Helper localization(FEntity sender) {
        return fileFacade.localization(sender).command().helper();
    }

    public Predicate<FPlayer> getFilterSee() {
        return fPlayer -> permissionChecker.check(fPlayer, permission().see());
    }
}
