package net.flectone.pulse.module.command.unmute;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.UnModerationMetadata;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.platform.controller.ModuleCommandController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.incendo.cloud.context.CommandContext;

import java.util.List;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class UnmuteModule implements ModuleCommand<Localization.Command.Unmute> {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final ModerationService moderationService;
    private final CommandParserProvider commandParserProvider;
    private final ProxySender proxySender;
    private final MessagePipeline messagePipeline;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;
    private final ModuleCommandController commandModuleController;

    @Override
    public void onEnable() {
        String promptPlayer = commandModuleController.addPrompt(this, 0, Localization.Command.Prompt::player);
        String promptId = commandModuleController.addPrompt(this, 1, Localization.Command.Prompt::id);
        commandModuleController.registerCommand(this, manager -> manager
                .permission(permission().name())
                .required(promptPlayer, commandParserProvider.mutedParser())
                .optional(promptId, commandParserProvider.integerParser())
        );
    }

    @Override
    public void onDisable() {
        commandModuleController.clearPrompts(this);
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (moduleController.isDisabledFor(this, fPlayer, true)) return;

        String target = commandModuleController.getArgument(this, commandContext, 0);

        String promptId = commandModuleController.getPrompt(this, 1);
        Optional<Integer> optionalId = commandContext.optional(promptId);
        int id = optionalId.orElse(-1);

        unmute(fPlayer, target, id);
    }

    @Override
    public ModuleName name() {
        return ModuleName.COMMAND_UNMUTE;
    }

    @Override
    public Command.Unmute config() {
        return fileFacade.command().unmute();
    }

    @Override
    public Permission.Command.Unmute permission() {
        return fileFacade.permission().command().unmute();
    }

    @Override
    public Localization.Command.Unmute localization(FEntity sender) {
        return fileFacade.localization(sender).command().unmute();
    }

    public void unmute(FPlayer fPlayer, String target, int id) {
        if (moduleController.isDisabledFor(this, fPlayer)) return;

        FPlayer fTarget = fPlayerService.getFPlayer(target);
        if (fTarget.isUnknown()) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Unmute>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Unmute::nullPlayer)
                    .build()
            );

            return;
        }

        if (config().checkGroupWeight() && !fPlayerService.hasHigherGroupThan(fPlayer, fTarget)) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Unmute>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Unmute::lowerWeightGroup)
                    .build()
            );
            return;
        }

        List<Moderation> mutes = new ObjectArrayList<>();

        if (id == -1) {
            mutes.addAll(moderationService.getValidMutes(fTarget));
        } else {
            moderationService.getValidMutes(fTarget).stream()
                    .filter(moderation -> moderation.id() == id)
                    .findAny()
                    .ifPresent(mutes::add);
        }

        if (mutes.isEmpty()) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Unmute>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Unmute::notMuted)
                    .build()
            );

            return;
        }

        moderationService.remove(fTarget, mutes);

        proxySender.send(fTarget, ModuleName.SYSTEM_MUTE);

        messageDispatcher.dispatch(this, UnModerationMetadata.<Localization.Command.Unmute>builder()
                .base(EventMetadata.<Localization.Command.Unmute>builder()
                        .sender(fTarget)
                        .format(Localization.Command.Unmute::format)
                        .destination(config().destination())
                        .range(config().range())
                        .sound(soundOrThrow())
                        .proxy(dataOutputStream -> {
                            dataOutputStream.writeAsJson(fPlayer);
                            dataOutputStream.writeAsJson(mutes);
                        })
                        .integration()
                        .tagResolvers(fResolver -> new TagResolver[]{
                                messagePipeline.targetTag("moderator", fResolver, fPlayer)
                        })
                        .build()
                )
                .moderator(fPlayer)
                .moderations(mutes)
                .build()
        );
    }
}
