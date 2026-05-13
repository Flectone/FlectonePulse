package net.flectone.pulse.module.command.toggleoriginal;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.module.message.format.moderation.delete.DeleteModule;
import net.flectone.pulse.platform.controller.ModuleCommandController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import org.incendo.cloud.context.CommandContext;

import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ToggleOriginalModule implements ModuleCommand<Localization.Command.Deletemessage> {

    private final FileFacade fileFacade;
    private final CommandParserProvider commandParserProvider;
    private final DeleteModule deleteModule;
    private final ModuleController moduleController;
    private final ModuleCommandController commandModuleController;

    @Override
    public void onEnable() {
        commandModuleController.registerCustomCommand(manager -> manager
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
            deleteModule.toggleOriginal(fPlayer, messageUuid);
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
    public Localization.Command.Deletemessage localization(FEntity sender) {
        return fileFacade.localization(sender).command().deletemessage();
    }
}
