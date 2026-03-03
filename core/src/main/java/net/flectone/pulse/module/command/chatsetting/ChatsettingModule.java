package net.flectone.pulse.module.command.chatsetting;

import com.google.common.collect.ImmutableList;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.module.command.chatsetting.builder.MenuBuilder;
import net.flectone.pulse.platform.controller.ModuleCommandController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.platform.sender.SoundPlayer;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.incendo.cloud.suggestion.Suggestion;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

public abstract class ChatsettingModule implements ModuleCommand<Localization.Command.Chatsetting> {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final PermissionChecker permissionChecker;
    private final CommandParserProvider commandParserProvider;
    private final ProxySender proxySender;
    private final ProxyRegistry proxyRegistry;
    private final SoundPlayer soundPlayer;
    private final TaskScheduler taskScheduler;
    private final ModuleController moduleController;
    private final ModuleCommandController commandModuleController;

    protected ChatsettingModule(FileFacade fileFacade,
                                FPlayerService fPlayerService,
                                PermissionChecker permissionChecker,
                                CommandParserProvider commandParserProvider,
                                ProxySender proxySender,
                                ProxyRegistry proxyRegistry,
                                SoundPlayer soundPlayer,
                                TaskScheduler taskScheduler,
                                ModuleController moduleController,
                                ModuleCommandController commandModuleController) {
        this.fileFacade = fileFacade;
        this.fPlayerService = fPlayerService;
        this.permissionChecker = permissionChecker;
        this.commandParserProvider = commandParserProvider;
        this.proxySender = proxySender;
        this.proxyRegistry = proxyRegistry;
        this.soundPlayer = soundPlayer;
        this.taskScheduler = taskScheduler;
        this.moduleController = moduleController;
        this.commandModuleController = commandModuleController;
    }

    @Override
    public void onEnable() {
        String promptPlayer = commandModuleController.addPrompt(this, 0, Localization.Command.Prompt::player);
        String promptType = commandModuleController.addPrompt(this, 1, Localization.Command.Prompt::type);
        String promptValue = commandModuleController.addPrompt(this, 2, Localization.Command.Prompt::value);
        commandModuleController.registerCommand(this, commandBuilder -> commandBuilder
                .permission(permission().name())
                .optional(promptPlayer, commandParserProvider.offlinePlayerParser(), commandParserProvider.playerSuggestionPermission(true, permission().other()))
                .optional(promptType, commandParserProvider.singleMessageParser(), typeSuggestion())
                .optional(promptValue, commandParserProvider.messageParser())
        );
    }

    @Override
    public void onDisable() {
        commandModuleController.clearPrompts(this);
    }

    @Override
    public ImmutableList.Builder<PermissionSetting> permissionBuilder() {
        return ModuleCommand.super.permissionBuilder()
                .add(permission().other())
                .addAll(permission().settings().values());
    }

    private @NonNull BlockingSuggestionProvider<FPlayer> typeSuggestion() {
        return (context, input) -> {
            if (!permissionChecker.check(context.sender(), permission().other())) return Collections.emptyList();

            return Arrays.stream(ModuleName.values())
                    .map(setting -> Suggestion.suggestion(setting.name()))
                    .toList();
        };
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (moduleController.isDisabledFor(this, fPlayer, true)) return;

        if (permissionChecker.check(fPlayer, permission().other())) {
            String promptPlayer = commandModuleController.getPrompt(this, 0);
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
    public ModuleName name() {
        return ModuleName.COMMAND_CHATSETTING;
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

        fTarget = fPlayerService.loadSettings(fTarget);

        String promptType = commandModuleController.getPrompt(this, 1);
        Optional<String> optionalType = commandContext.optional(promptType);

        if (optionalType.isEmpty()) {
            open(fPlayer, fTarget);
            return;
        }

        SettingText settingText = SettingText.fromString(optionalType.get());
        if (settingText != null) {
            String promptValue = commandModuleController.getPrompt(this, 2);
            Optional<String> optionalValue = commandContext.optional(promptValue);

            saveSetting(fTarget.withSetting(settingText, optionalValue.orElse(null)), settingText);
            return;
        }

        String messageType = optionalType.get().toUpperCase();
        saveSetting(fTarget.withSetting(messageType, !fTarget.isSetting(messageType)), messageType);
    }

    protected abstract MenuBuilder getMenuBuilder();

    private void open(FPlayer fPlayer, FPlayer fTarget) {
        getMenuBuilder().open(fPlayer, fTarget.uuid());
    }

    public void saveSetting(FPlayer fPlayer, String messageType) {
        taskScheduler.runAsync(() -> {
            fPlayerService.saveOrUpdateSetting(fPlayer, messageType);

            if (proxyRegistry.hasEnabledProxy()) {
                proxySender.send(fPlayer, ModuleName.COMMAND_CHATSETTING);
            }
        }, true);
    }

    public void saveSetting(FPlayer fPlayer, SettingText settingText) {
        taskScheduler.runAsync(() -> {
            fPlayerService.saveOrUpdateSetting(fPlayer, settingText);

            if (proxyRegistry.hasEnabledProxy()) {
                proxySender.send(fPlayer, ModuleName.COMMAND_CHATSETTING);
            }
        }, true);
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