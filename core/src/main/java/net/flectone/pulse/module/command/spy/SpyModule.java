package net.flectone.pulse.module.command.spy;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.spy.model.SpyMetadata;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageType;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

@Singleton
public class SpyModule extends AbstractModuleCommand<Localization.Command.Spy> {

    private final Command.Spy command;
    private final Permission.Command.Spy permission;
    private final FPlayerService fPlayerService;
    private final PermissionChecker permissionChecker;

    @Inject
    public SpyModule(FileResolver fileResolver,
                     FPlayerService fPlayerService,
                     PermissionChecker permissionChecker) {
        super(localization -> localization.getCommand().getSpy(), Command::getSpy, MessageType.COMMAND_SPY);

        this.command = fileResolver.getCommand().getSpy();
        this.permission = fileResolver.getPermission().getCommand().getSpy();
        this.fPlayerService = fPlayerService;
        this.permissionChecker = permissionChecker;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        registerCommand(manager -> manager
                .permission(permission.getName())
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkCooldown(fPlayer)) return;
        if (isModuleDisabledFor(fPlayer)) return;

        boolean turnedBefore = fPlayer.isSetting(FPlayer.Setting.SPY);
        if (turnedBefore) {
            fPlayerService.deleteSetting(fPlayer, FPlayer.Setting.SPY);
        } else {
            fPlayerService.saveOrUpdateSetting(fPlayer, FPlayer.Setting.SPY, "");
        }

        sendMessage(SpyMetadata.<Localization.Command.Spy>builder()
                .sender(fPlayer)
                .format(s -> !turnedBefore ? s.getFormatTrue() : s.getFormatFalse())
                .turned(!turnedBefore)
                .action("turning")
                .destination(command.getDestination())
                .sound(getModuleSound())
                .build()
        );
    }

    public void checkChat(FPlayer fPlayer, String chat, String message) {
        if (!isEnable()) return;

        Map<String, List<String>> categories = command.getCategories();
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
                .range(command.getRange())
                .destination(command.getDestination())
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

    private Predicate<FPlayer> createFilter(FPlayer fPlayer) {
        return fReceiver -> !fPlayer.equals(fReceiver)
                && permissionChecker.check(fReceiver, getModulePermission())
                && fReceiver.isSetting(FPlayer.Setting.SPY)
                && fPlayer.isOnline();
    }

    public Function<Localization.Command.Spy, String> replaceAction(String action) {
        return message -> Strings.CS.replace(message.getFormatLog(), "<action>", action);
    }
}
