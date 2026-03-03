package net.flectone.pulse.module.command.me;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.platform.controller.ModuleCommandController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import org.incendo.cloud.context.CommandContext;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MeModule implements ModuleCommand<Localization.Command.Me> {

    private final FileFacade fileFacade;
    private final CommandParserProvider commandParserProvider;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;
    private final ModuleCommandController commandModuleController;

    @Override
    public void onEnable() {
        String promptMessage = commandModuleController.addPrompt(this, 0, Localization.Command.Prompt::message);
        commandModuleController.registerCommand(this, commandBuilder -> commandBuilder
                .permission(permission().name())
                .required(promptMessage, commandParserProvider.nativeMessageParser())
        );
    }

    @Override
    public void onDisable() {
        commandModuleController.clearPrompts(this);
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (moduleController.isDisabledFor(this, fPlayer, true)) return;

        String message = commandModuleController.getArgument(this, commandContext, 0);

        messageDispatcher.dispatch(this, EventMetadata.<Localization.Command.Me>builder()
                .sender(fPlayer)
                .format(Localization.Command.Me::format)
                .destination(config().destination())
                .range(config().range())
                .message(message)
                .sound(soundOrThrow())
                .proxy(dataOutputStream -> dataOutputStream.writeString(message))
                .integration()
                .build()
        );

    }

    @Override
    public ModuleName name() {
        return ModuleName.COMMAND_ME;
    }

    @Override
    public Command.Me config() {
        return fileFacade.command().me();
    }

    @Override
    public Permission.Command.Me permission() {
        return fileFacade.permission().command().me();
    }

    @Override
    public Localization.Command.Me localization(FEntity sender) {
        return fileFacade.localization(sender).command().me();
    }
}
