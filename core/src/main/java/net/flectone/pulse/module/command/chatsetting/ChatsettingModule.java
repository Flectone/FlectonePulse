package net.flectone.pulse.module.command.chatsetting;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.chatsetting.builder.DialogMenuBuilder;
import net.flectone.pulse.module.command.chatsetting.builder.InventoryMenuBuilder;
import net.flectone.pulse.module.command.chatsetting.builder.MenuBuilder;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.platform.sender.SoundPlayer;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.SettingText;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.incendo.cloud.suggestion.Suggestion;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ChatsettingModule extends AbstractModuleCommand<Localization.Command.Chatsetting> {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final PermissionChecker permissionChecker;
    private final CommandParserProvider commandParserProvider;
    private final ProxySender proxySender;
    private final ProxyRegistry proxyRegistry;
    private final Provider<DialogMenuBuilder> dialogMenuBuilderProvider;
    private final Provider<InventoryMenuBuilder> inventoryMenuBuilderProvider;
    private final SoundPlayer soundPlayer;
    private final @Named("isNewerThanOrEqualsV_1_21_6") boolean isNewerThanOrEqualsV_1_21_6;

    @Override
    public void onEnable() {
        super.onEnable();

        String promptPlayer = addPrompt(0, Localization.Command.Prompt::player);
        String promptType = addPrompt(1, Localization.Command.Prompt::type);
        String promptValue = addPrompt(2, Localization.Command.Prompt::value);
        registerCommand(commandBuilder -> commandBuilder
                .permission(permission().name())
                .optional(promptPlayer, commandParserProvider.offlinePlayerParser(), commandParserProvider.playerSuggestionPermission(true, permission().other()))
                .optional(promptType, commandParserProvider.singleMessageParser(), typeSuggestion())
                .optional(promptValue, commandParserProvider.messageParser())
        );
    }

    @Override
    public ImmutableList.Builder<PermissionSetting> permissionBuilder() {
        return super.permissionBuilder()
                .add(permission().other())
                .addAll(permission().settings().values());
    }

    private @NonNull BlockingSuggestionProvider<FPlayer> typeSuggestion() {
        return (context, input) -> {
            if (!permissionChecker.check(context.sender(), permission().other())) return Collections.emptyList();

            return Arrays.stream(MessageType.values())
                    .map(setting -> Suggestion.suggestion(setting.name()))
                    .toList();
        };
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        if (permissionChecker.check(fPlayer, permission().other())) {
            String promptPlayer = getPrompt(0);
            Optional<String> optionalPlayer = commandContext.optional(promptPlayer);
            if (optionalPlayer.isPresent()) {
                executeOther(fPlayer, optionalPlayer.get(), commandContext);
                return;
            }
        }

        open(fPlayer, fPlayer);

        soundPlayer.play(soundOrThrow(), fPlayer);
    }

    @Override
    public MessageType messageType() {
        return MessageType.COMMAND_CHATSETTING;
    }

    @Override
    public Command.Chatsetting config() {
        return fileFacade.command().chatsetting();
    }

    @Override
    public Permission.Command.Chatsetting permission() {
        return fileFacade.permission().command().chatsetting();
    }

    @Override
    public Localization.Command.Chatsetting localization(FEntity sender) {
        return fileFacade.localization(sender).command().chatsetting();
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

        SettingText settingText = SettingText.fromString(optionalType.get());
        if (settingText != null) {
            String promptValue = getPrompt(2);
            Optional<String> optionalValue = commandContext.optional(promptValue);

            fTarget.setSetting(settingText, optionalValue.orElse(null));
            saveSetting(fTarget, settingText);
            return;
        }

        String messageType = optionalType.get().toUpperCase();

        fTarget.setSetting(messageType, !fTarget.isSetting(messageType));
        saveSetting(fTarget, messageType);
    }

    private void open(FPlayer fPlayer, FPlayer fTarget) {
        MenuBuilder menuBuilder = config().modern().enable() && isNewerThanOrEqualsV_1_21_6
                ? dialogMenuBuilderProvider.get()
                : inventoryMenuBuilderProvider.get();
        menuBuilder.open(fPlayer, fTarget);
    }

    public void saveSetting(FPlayer fPlayer, String messageType) {
        fPlayerService.saveOrUpdateSetting(fPlayer, messageType);

        if (proxyRegistry.hasEnabledProxy()) {
            proxySender.send(fPlayer, MessageType.COMMAND_CHATSETTING);
        }
    }


    public void saveSetting(FPlayer fPlayer, SettingText settingText) {
        fPlayerService.saveOrUpdateSetting(fPlayer, settingText);

        if (proxyRegistry.hasEnabledProxy()) {
            proxySender.send(fPlayer, MessageType.COMMAND_CHATSETTING);
        }
    }


    public String getPlayerChat(FPlayer fTarget) {
        String currentChat = fTarget.getSetting(SettingText.CHAT_NAME);
        if (StringUtils.isEmpty(currentChat)) return "default";

        return currentChat;
    }

    public String getCheckboxMaterial(boolean enabled) {
        Command.Chatsetting.Checkbox checkbox = config().checkbox();
        return enabled ? checkbox.enabledMaterial() : checkbox.disabledMaterial();
    }

    public String getCheckboxTitle(FPlayer fPlayer, String setting, boolean enabled) {
        Localization.Command.Chatsetting.Checkbox localizationCheckbox = localization(fPlayer).checkbox();
        String statusColor = enabled ? localizationCheckbox.enabledColor() : localizationCheckbox.disabledColor();

        return Strings.CS.replace(
                localizationCheckbox.types().getOrDefault(setting, ""),
                "<status_color>",
                statusColor
        );
    }

    public String getCheckboxLore(FPlayer fPlayer, boolean enabled) {
        Localization.Command.Chatsetting.Checkbox localizationCheckbox = localization(fPlayer).checkbox();
        String statusColor = enabled ? localizationCheckbox.enabledColor() : localizationCheckbox.disabledColor();

        return Strings.CS.replace(
                enabled ? localizationCheckbox.enabledHover() : localizationCheckbox.disabledHover(),
                "<status_color>",
                statusColor
        );
    }
}