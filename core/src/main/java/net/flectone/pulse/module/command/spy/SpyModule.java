package net.flectone.pulse.module.command.spy;

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
import net.flectone.pulse.module.command.spy.model.SpyMetadata;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.SettingText;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SpyModule extends AbstractModuleCommand<Localization.Command.Spy> {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final PermissionChecker permissionChecker;

    @Override
    public void onEnable() {
        super.onEnable();

        registerCommand(manager -> manager
                .permission(permission().name())
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        boolean turnedBefore = fPlayer.getSetting(SettingText.SPY_STATUS) != null;
        if (turnedBefore) {
            fPlayer = fPlayer.withoutSetting(SettingText.SPY_STATUS);
        } else {
            fPlayer = fPlayer.withSetting(SettingText.SPY_STATUS, "1");
        }

        fPlayerService.saveOrUpdateSetting(fPlayer, SettingText.SPY_STATUS);

        sendMessage(SpyMetadata.<Localization.Command.Spy>builder()
                .base(EventMetadata.<Localization.Command.Spy>builder()
                        .sender(fPlayer)
                        .format(s -> !turnedBefore ? s.formatTrue() : s.formatFalse())
                        .destination(config().destination())
                        .sound(soundOrThrow())
                        .build()
                )
                .turned(!turnedBefore)
                .action("turning")
                .build()
        );
    }

    @Override
    public MessageType messageType() {
        return MessageType.COMMAND_SPY;
    }

    @Override
    public Command.Spy config() {
        return fileFacade.command().spy();
    }

    @Override
    public Permission.Command.Spy permission() {
        return fileFacade.permission().command().spy();
    }

    @Override
    public Localization.Command.Spy localization(FEntity sender) {
        return fileFacade.localization(sender).command().spy();
    }

    public void check(FPlayer fPlayer, String chat, String message, List<FPlayer> receivers) {
        if (!isEnable()) return;
        if (!needToSpy("action", chat)) return;

        spy(fPlayer, chat, message, receivers);
    }

    public void spy(FPlayer fPlayer, String action, String message) {
        spy(fPlayer, action, message, Collections.emptyList());
    }

    public void spy(FPlayer fPlayer, String action, String message, List<FPlayer> receivers) {
        if (!isEnable()) return;

        sendMessage(SpyMetadata.<Localization.Command.Spy>builder()
                .base(EventMetadata.<Localization.Command.Spy>builder()
                        .sender(fPlayer)
                        .format(replaceAction(action))
                        .range(config().range())
                        .destination(config().destination())
                        .message(message)
                        .filter(createFilter(fPlayer, receivers))
                        .proxy(dataOutputStream -> {
                            dataOutputStream.writeString(action);
                            dataOutputStream.writeString(message);
                        })
                        .integration(string -> Strings.CS.replace(string, "<action>", action))
                        .build()
                )
                .turned(true)
                .action(action)
                .build()
        );
    }

    public Predicate<FPlayer> createFilter(FPlayer fPlayer, List<FPlayer> receivers) {
        return fReceiver -> !fPlayer.equals(fReceiver)
                && !receivers.contains(fReceiver)
                && permissionChecker.check(fReceiver, permission())
                && fReceiver.getSetting(SettingText.SPY_STATUS) != null
                && fReceiver.isOnline();
    }

    protected boolean needToSpy(String category, String value) {
        Map<String, List<String>> categories = config().categories();

        List<String> values = categories.get(category);

        return values != null && values.contains(value);
    }

}
