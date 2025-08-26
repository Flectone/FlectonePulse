package net.flectone.pulse.module.command.chatsetting;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.NonNull;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.chatsetting.builder.DialogMenuBuilder;
import net.flectone.pulse.module.command.chatsetting.builder.InventoryMenuBuilder;
import net.flectone.pulse.module.command.chatsetting.builder.MenuBuilder;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.incendo.cloud.suggestion.Suggestion;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

@Singleton
public class ChatsettingModule extends AbstractModuleCommand<Localization.Command.Chatsetting> {

    private final Command.Chatsetting command;
    private final Permission.Command.Chatsetting permission;
    private final FPlayerService fPlayerService;
    private final PermissionChecker permissionChecker;
    private final CommandParserProvider commandParserProvider;
    private final ProxySender proxySender;
    private final ProxyRegistry proxyRegistry;
    private final Provider<DialogMenuBuilder> dialogMenuBuilderProvider;
    private final Provider<InventoryMenuBuilder> inventoryMenuBuilderProvider;
    private final boolean isNewerThanOrEqualsV_1_21_6;

    @Inject
    public ChatsettingModule(FileResolver fileResolver,
                             FPlayerService fPlayerService,
                             PermissionChecker permissionChecker,
                             CommandParserProvider commandParserProvider,
                             ProxySender proxySender,
                             ProxyRegistry proxyRegistry,
                             Provider<DialogMenuBuilder> dialogMenuBuilderProvider,
                             Provider<InventoryMenuBuilder> inventoryMenuBuilderProvider,
                             PacketProvider packetProvider) {
        super(localization -> localization.getCommand().getChatsetting(), Command::getChatsetting, MessageType.COMMAND_CHATSETTING);

        this.command = fileResolver.getCommand().getChatsetting();
        this.permission = fileResolver.getPermission().getCommand().getChatsetting();
        this.fPlayerService = fPlayerService;
        this.permissionChecker = permissionChecker;
        this.commandParserProvider = commandParserProvider;
        this.proxySender = proxySender;
        this.proxyRegistry = proxyRegistry;
        this.dialogMenuBuilderProvider = dialogMenuBuilderProvider;
        this.inventoryMenuBuilderProvider = inventoryMenuBuilderProvider;
        this.isNewerThanOrEqualsV_1_21_6 = packetProvider.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21_6);
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        permission.getSettings().values().forEach(this::registerPermission);
        permission.getFcolors().values().forEach(this::registerPermission);

        String promptPlayer = addPrompt(0, Localization.Command.Prompt::getPlayer);
        String promptType = addPrompt(1, Localization.Command.Prompt::getType);
        String promptValue = addPrompt(2, Localization.Command.Prompt::getValue);
        registerCommand(commandBuilder -> commandBuilder
                .permission(permission.getName())
                .optional(promptPlayer, commandParserProvider.offlinePlayerParser(), commandParserProvider.playerSuggestionPermission(true, permission.getOther()))
                .optional(promptType, commandParserProvider.singleMessageParser(), typeSuggestion())
                .optional(promptValue, commandParserProvider.messageParser())
        );

        addPredicate(this::checkCooldown);
    }

    private @NonNull BlockingSuggestionProvider<FPlayer> typeSuggestion() {
        return (context, input) -> {
            if (!permissionChecker.check(context.sender(), permission.getOther())) return Collections.emptyList();

            return Arrays.stream(FPlayer.Setting.values())
                    .map(setting -> Suggestion.suggestion(setting.name()))
                    .toList();
        };
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer)) return;

        if (permissionChecker.check(fPlayer, permission.getOther())) {
            String promptPlayer = getPrompt(0);
            Optional<String> optionalPlayer = commandContext.optional(promptPlayer);
            if (optionalPlayer.isPresent()) {
                executeOther(fPlayer, optionalPlayer.get(), commandContext);
                return;
            }
        }

        open(fPlayer, fPlayer);

        playSound(fPlayer);
    }

    private void executeOther(FPlayer fPlayer, String target, CommandContext<FPlayer> commandContext) {
        FPlayer fTarget = fPlayerService.getFPlayer(target);
        if (fTarget.isUnknown()) return;

        fPlayerService.loadSettings(fTarget);

        String promptType = getPrompt(1);
        Optional<String> optionalType = commandContext.optional(promptType);

        if (optionalType.isEmpty()) {
            open(fPlayer, fTarget);
            return;
        }

        FPlayer.Setting setting = FPlayer.Setting.fromString(optionalType.get());
        if (setting == null) return;

        String promptValue = getPrompt(2);
        Optional<String> optionalValue = commandContext.optional(promptValue);

        if (fTarget.isSetting(setting) && optionalValue.isEmpty()) {
            fTarget.removeSetting(setting);
        } else {
            fTarget.setSetting(setting, optionalValue.orElse(""));
        }

        updateSettings(fTarget);
    }

    private void open(FPlayer fPlayer, FPlayer fTarget) {
        MenuBuilder menuBuilder = command.getModern().isEnable() && isNewerThanOrEqualsV_1_21_6
                ? dialogMenuBuilderProvider.get()
                : inventoryMenuBuilderProvider.get();
        menuBuilder.open(fPlayer, fTarget);
    }

    public void updateSettings(FPlayer fPlayer) {
        fPlayerService.saveSettings(fPlayer);

        if (proxyRegistry.hasEnabledProxy()) {
            proxySender.send(fPlayer, MessageType.COMMAND_CHATSETTING, dataOutputStream -> {});
        }
    }

    public String getPlayerChat(FPlayer fTarget) {
        String currentChat = fTarget.getSettingValue(FPlayer.Setting.CHAT);
        if (StringUtils.isEmpty(currentChat)) return "default";

        return currentChat;
    }

    public String getCheckboxMaterial(boolean enabled) {
        Command.Chatsetting.Checkbox checkbox = command.getCheckbox();
        return enabled ? checkbox.getEnabledMaterial() : checkbox.getDisabledMaterial();
    }

    public String getCheckboxTitle(FPlayer fPlayer, FPlayer.Setting setting, boolean enabled) {
        Localization.Command.Chatsetting.Checkbox localizationCheckbox = resolveLocalization(fPlayer).getCheckbox();
        String statusColor = enabled ? localizationCheckbox.getEnabledColor() : localizationCheckbox.getDisabledColor();

        return Strings.CS.replace(
                localizationCheckbox.getTypes().getOrDefault(setting, ""),
                "<status_color>",
                statusColor
        );
    }

    public String getCheckboxLore(FPlayer fPlayer, boolean enabled) {
        Localization.Command.Chatsetting.Checkbox localizationCheckbox = resolveLocalization(fPlayer).getCheckbox();
        String statusColor = enabled ? localizationCheckbox.getEnabledColor() : localizationCheckbox.getDisabledColor();

        return Strings.CS.replace(
                enabled ? localizationCheckbox.getEnabledHover() : localizationCheckbox.getDisabledHover(),
                "<status_color>",
                statusColor
        );
    }
}