package net.flectone.pulse.module.command.unwarn;

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
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.platform.controller.CommandModuleController;
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
public class UnwarnModule implements AbstractModuleCommand<Localization.Command.Unwarn> {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final ModerationService moderationService;
    private final CommandParserProvider commandParserProvider;
    private final ProxySender proxySender;
    private final MessagePipeline messagePipeline;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;
    private final CommandModuleController commandModuleController;

    @Override
    public void onEnable() {
        String promptPlayer = commandModuleController.addPrompt(this, 0, Localization.Command.Prompt::player);
        String promptId = commandModuleController.addPrompt(this, 1, Localization.Command.Prompt::id);
        commandModuleController.registerCommand(this, manager -> manager
                .permission(permission().name())
                .required(promptPlayer, commandParserProvider.warnedParser())
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

        unwarn(fPlayer, target, id);
    }

    @Override
    public ModuleName name() {
        return ModuleName.COMMAND_UNWARN;
    }

    @Override
    public Command.Unwarn config() {
        return fileFacade.command().unwarn();
    }

    @Override
    public Permission.Command.Unwarn permission() {
        return fileFacade.permission().command().unwarn();
    }

    @Override
    public Localization.Command.Unwarn localization(FEntity sender) {
        return fileFacade.localization(sender).command().unwarn();
    }

    public void unwarn(FPlayer fPlayer, String target, int id) {
        if (moduleController.isDisabledFor(this, fPlayer)) return;

        FPlayer fTarget = fPlayerService.getFPlayer(target);
        if (fTarget.isUnknown()) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Unwarn>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Unwarn::nullPlayer)
                    .build()
            );

            return;
        }

        if (config().checkGroupWeight() && !fPlayerService.hasHigherGroupThan(fPlayer, fTarget)) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Unwarn>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Unwarn::lowerWeightGroup)
                    .build()
            );

            return;
        }

        List<Moderation> warns = new ObjectArrayList<>();

        if (id == -1) {
            warns.addAll(moderationService.getValidWarns(fTarget));
        } else {
            moderationService.getValidWarns(fTarget).stream()
                    .filter(warn -> warn.id() == id)
                    .findAny()
                    .ifPresent(warns::add);
        }

        if (warns.isEmpty()) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Unwarn>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Unwarn::notWarned)
                    .build()
            );

            return;
        }

        moderationService.remove(fTarget, warns);

        proxySender.send(fTarget, ModuleName.SYSTEM_WARN);

        messageDispatcher.dispatch(this, UnModerationMetadata.<Localization.Command.Unwarn>builder()
                .base(EventMetadata.<Localization.Command.Unwarn>builder()
                        .sender(fTarget)
                        .format(Localization.Command.Unwarn::format)
                        .destination(config().destination())
                        .range(config().range())
                        .sound(soundOrThrow())
                        .proxy(dataOutputStream -> {
                            dataOutputStream.writeAsJson(fPlayer);
                            dataOutputStream.writeAsJson(warns);
                        })
                        .integration()
                        .tagResolvers(fResolver -> new TagResolver[]{
                                messagePipeline.targetTag("moderator", fResolver, fPlayer)
                        })
                        .build()
                )
                .moderator(fPlayer)
                .moderations(warns)
                .build()
        );
    }
}
