package net.flectone.pulse.module.command.unmute;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.UnModerationMetadata;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.util.constant.MessageType;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class UnmuteModule extends AbstractModuleCommand<Localization.Command.Unmute> {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final ModerationService moderationService;
    private final CommandParserProvider commandParserProvider;
    private final ProxySender proxySender;

    @Override
    public void onEnable() {
        super.onEnable();

        String promptPlayer = addPrompt(0, Localization.Command.Prompt::player);
        String promptId = addPrompt(1, Localization.Command.Prompt::id);
        registerCommand(manager -> manager
                .permission(permission().name())
                .required(promptPlayer, commandParserProvider.mutedParser())
                .optional(promptId, commandParserProvider.integerParser())
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        String target = getArgument(commandContext, 0);

        String promptId = getPrompt(1);
        Optional<Integer> optionalId = commandContext.optional(promptId);
        int id = optionalId.orElse(-1);

        unmute(fPlayer, target, id);
    }

    @Override
    public MessageType messageType() {
        return MessageType.COMMAND_UNMUTE;
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
        if (isModuleDisabledFor(fPlayer)) return;

        FPlayer fTarget = fPlayerService.getFPlayer(target);
        if (fTarget.isUnknown()) {
            sendErrorMessage(EventMetadata.<Localization.Command.Unmute>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Unmute::nullPlayer)
                    .build()
            );

            return;
        }

        if (config().checkGroupWeight() && !fPlayerService.hasHigherGroupThan(fPlayer, fTarget)) {
            sendErrorMessage(EventMetadata.<Localization.Command.Unmute>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Unmute::lowerWeightGroup)
                    .build()
            );
            return;
        }

        List<Moderation> mutes = new ArrayList<>();

        if (id == -1) {
            mutes.addAll(moderationService.getValidMutes(fTarget));
        } else {
            moderationService.getValidMutes(fTarget).stream()
                    .filter(moderation -> moderation.id() == id)
                    .findAny()
                    .ifPresent(mutes::add);
        }

        if (mutes.isEmpty()) {
            sendErrorMessage(EventMetadata.<Localization.Command.Unmute>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Unmute::notMuted)
                    .build()
            );

            return;
        }

        moderationService.remove(fTarget, mutes);

        proxySender.send(fTarget, MessageType.SYSTEM_MUTE);

        sendMessage(UnModerationMetadata.<Localization.Command.Unmute>builder()
                .base(EventMetadata.<Localization.Command.Unmute>builder()
                        .sender(fTarget)
                        .format(unmute -> Strings.CS.replace(unmute.format(), "<moderator>", fPlayer.getName()))
                        .destination(config().destination())
                        .range(config().range())
                        .sound(soundOrThrow())
                        .proxy(dataOutputStream -> {
                            dataOutputStream.writeAsJson(fPlayer);
                            dataOutputStream.writeAsJson(mutes);
                        })
                        .integration(string -> Strings.CS.replace(string, "<moderator>", fPlayer.getName()))
                        .build()
                )
                .moderator(fPlayer)
                .moderations(mutes)
                .build()
        );
    }
}
