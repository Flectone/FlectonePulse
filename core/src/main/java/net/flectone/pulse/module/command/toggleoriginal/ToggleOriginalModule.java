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
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.registry.CommandRegistry;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import org.incendo.cloud.context.CommandContext;

import java.util.UUID;

/**
 * /toggleoriginal &lt;message-uuid&gt; — flip the displayed version of a chat message
 * between auto-translated and original.
 *
 * <p>Wired to {@link TranslateModule#toggleOriginal} only — no DeleteModule reference.
 * Reuses the Deletemessage command config (cooldown / aliases / permission) since
 * the plugin doesn't have a dedicated config record for this command.
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ToggleOriginalModule implements ModuleCommand<Localization.Command.Deletemessage> {

    private final FileFacade fileFacade;
    private final CommandParserProvider commandParserProvider;
    private final TranslateModule translateModule;
    private final ModuleController moduleController;
    private final CommandRegistry commandRegistry;
    private final SocialService socialService;

    @Override
    public void onEnable() {
        commandRegistry.registerCommand(manager -> manager
                .commandBuilder("toggleoriginal")
                .required("message", commandParserProvider.singleMessageParser())
                .handler(this)
        );
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (moduleController.isDisabledFor(this, fPlayer)) return;

        String messageUuidString = commandContext.get("message");

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
    public Command.Deletemessage config() {
        return fileFacade.command().deletemessage();
    }

    @Override
    public Permission.Command.Deletemessage permission() {
        return fileFacade.permission().command().deletemessage();
    }

    @Override
    public Localization.Command.Deletemessage localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).command().deletemessage();
    }
}
