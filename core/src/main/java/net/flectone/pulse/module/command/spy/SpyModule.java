package net.flectone.pulse.module.command.spy;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
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
            fPlayer.removeSetting(SettingText.SPY_STATUS);
        } else {
            fPlayer.setSetting(SettingText.SPY_STATUS, "1");
        }

        fPlayerService.saveOrUpdateSetting(fPlayer, SettingText.SPY_STATUS);

        sendMessage(SpyMetadata.<Localization.Command.Spy>builder()
                .sender(fPlayer)
                .format(s -> !turnedBefore ? s.formatTrue() : s.formatFalse())
                .turned(!turnedBefore)
                .action("turning")
                .destination(config().destination())
                .sound(getModuleSound())
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

    public void checkChat(FPlayer fPlayer, String chat, String message) {
        if (!isEnable()) return;

        Map<String, List<String>> categories = config().categories();
        if (categories.get("action") == null) return;
        if (!categories.get("action").contains(chat)) return;

        spy(fPlayer, chat, message);
    }

    public void spy(FPlayer fPlayer, String action, String message) {
        if (!isEnable()) return;

        sendMessage(SpyMetadata.<Localization.Command.Spy>builder()
                .sender(fPlayer)
                .format(replaceAction(action))
                .turned(true)
                .action(action)
                .range(config().range())
                .destination(config().destination())
                .message(message)
                .filter(createFilter(fPlayer))
                .proxy(dataOutputStream -> {
                    dataOutputStream.writeString(action);
                    dataOutputStream.writeString(message);
                })
                .integration(string -> Strings.CS.replace(string, "<action>", action))
                .build()
        );
    }

    public Predicate<FPlayer> createFilter(FPlayer fPlayer) {
        return fReceiver -> !fPlayer.equals(fReceiver)
                && permissionChecker.check(fReceiver, getModulePermission())
                && fReceiver.getSetting(SettingText.SPY_STATUS) != null
                && fReceiver.isOnline();
    }

    public Function<Localization.Command.Spy, String> replaceAction(String action) {
        return message -> Strings.CS.replace(message.formatLog(), "<action>", action);
    }
}
