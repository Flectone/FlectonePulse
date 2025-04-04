package net.flectone.pulse.module.command.spy;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.registry.CommandRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.MessageTag;
import net.flectone.pulse.util.PermissionUtil;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.meta.CommandMeta;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Singleton
public class SpyModule extends AbstractModuleCommand<Localization.Command.Spy> {

    @Getter private final Command.Spy command;
    private final Permission.Command.Spy permission;

    private final CommandRegistry commandRegistry;
    private final FPlayerService fPlayerService;
    private final PermissionUtil permissionUtil;

    @Inject
    public SpyModule(FileManager fileManager,
                     CommandRegistry commandRegistry,
                     FPlayerService fPlayerService,
                     PermissionUtil permissionUtil) {
        super(localization -> localization.getCommand().getSpy(), null);

        this.commandRegistry = commandRegistry;
        this.fPlayerService = fPlayerService;
        this.permissionUtil = permissionUtil;

        command = fileManager.getCommand().getSpy();
        permission = fileManager.getPermission().getCommand().getSpy();
    }

    @Override
    public boolean isConfigEnable() {
        return command.isEnable();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        String commandName = getName(command);
        commandRegistry.registerCommand(manager ->
                manager.commandBuilder(commandName, command.getAliases(), CommandMeta.empty())
                        .permission(permission.getName())
                        .handler(this)
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkCooldown(fPlayer)) return;
        if (checkModulePredicates(fPlayer)) return;

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

        Map<String, List<String>> categories = getCommand().getCategories();
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
                .filter(fReceiver -> permissionUtil.has(fReceiver, getModulePermission()))
                .filter(fReceiver -> fReceiver.isSetting(FPlayer.Setting.SPY))
                .filter(FPlayer::isOnline)
                .tag(MessageTag.COMMAND_SPY)
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
