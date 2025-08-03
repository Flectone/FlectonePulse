package net.flectone.pulse.module.command.spy;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import org.incendo.cloud.context.CommandContext;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

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
        super(localization -> localization.getCommand().getSpy(), Command::getSpy);

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

        if (fPlayer.isSetting(FPlayer.Setting.SPY)) {
            fPlayerService.deleteSetting(fPlayer, FPlayer.Setting.SPY);
        } else {
            fPlayerService.saveOrUpdateSetting(fPlayer, FPlayer.Setting.SPY, "");
        }

        builder(fPlayer)
                .destination(command.getDestination())
                .format(s -> fPlayer.isSetting(FPlayer.Setting.SPY) ? s.getFormatTrue() : s.getFormatFalse())
                .sound(getSound())
                .sendBuilt();
    }

    public void checkChat(FPlayer fPlayer, String chat, String message) {
        if (!isEnable()) return;

        Map<String, List<String>> categories = command.getCategories();
        if (categories.get("action") == null) return;
        if (!categories.get("action").contains(chat)) return;

        spy(fPlayer, chat, message);
    }

    public void spy(FPlayer fPlayer, String action, String string) {
        if (!isEnable()) return;

        builder(fPlayer)
                .range(command.getRange())
                .destination(command.getDestination())
                .filter(fReceiver -> !fPlayer.equals(fReceiver))
                .filter(fReceiver -> permissionChecker.check(fReceiver, getModulePermission()))
                .filter(fReceiver -> fReceiver.isSetting(FPlayer.Setting.SPY))
                .filter(FPlayer::isOnline)
                .tag(MessageType.COMMAND_SPY)
                .format(replaceAction(action))
                .message((fResolver, s) -> string)
                .proxy(output -> {
                    output.writeUTF(action);
                    output.writeUTF(string);
                })
                .integration(s -> s
                        .replace("<action>", action)
                        .replace("<message>", string)
                )
                .sendBuilt();
    }

    public Function<Localization.Command.Spy, String> replaceAction(String action) {
        return message -> message.getFormatLog().replace("<action>", action);
    }
}
