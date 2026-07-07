package net.flectone.pulse.module.command.toggleoriginal;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.module.message.format.translate.TranslateModule;
import net.flectone.pulse.platform.controller.ModuleCommandController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import org.incendo.cloud.context.CommandContext;

import java.util.UUID;

// /toggleoriginal <message-uuid> — flips a chat message between translated and original.
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ToggleOriginalModule implements ModuleCommand<Localization.Command.Toggleoriginal> {

    private final FileFacade fileFacade;
    private final CommandParserProvider commandParserProvider;
    private final TranslateModule translateModule;
    private final ModuleController moduleController;
    private final ModuleCommandController commandModuleController;
    private final SocialService socialService;

    @Override
    public void onEnable() {
        String promptId = commandModuleController.addPrompt(this, 0, Localization.Command.Prompt::id);
        commandModuleController.registerCommand(this, commandBuilder -> commandBuilder
                .permission(permission().name())
                .required(promptId, commandParserProvider.singleMessageParser())
        );
    }

    @Override
    public void onDisable() {
        commandModuleController.clearPrompts(this);
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (moduleController.isDisabledFor(this, fPlayer)) return;

        String messageUuidString = commandModuleController.getArgument(this, commandContext, 0);

        try {
            UUID messageUuid = UUID.fromString(messageUuidString);
            translateModule.toggleOriginal(fPlayer, messageUuid);
        } catch (IllegalArgumentException _) {
            // Invalid UUID, ignore
        }
    }

    @Override
    public ModuleName name() {
        return ModuleName.COMMAND_TOGGLEORIGINAL;
    }

    @Override
    public Command.Toggleoriginal config() {
        return fileFacade.command().toggleoriginal();
    }

    @Override
    public Permission.Command.Toggleoriginal permission() {
        return fileFacade.permission().command().toggleoriginal();
    }

    @Override
    public Localization.Command.Toggleoriginal localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).command().toggleoriginal();
    }
}
