package net.flectone.pulse.module.command.unban;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
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
public class UnbanModule extends AbstractModuleCommand<Localization.Command.Unban> {

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
                .required(promptPlayer, commandParserProvider.bannedParser())
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

        unban(fPlayer, target, id);
    }

    @Override
    public MessageType messageType() {
        return MessageType.COMMAND_UNBAN;
    }

    @Override
    public Command.Unban config() {
        return fileFacade.command().unban();
    }

    @Override
    public Permission.Command.Unban permission() {
        return fileFacade.permission().command().unban();
    }

    @Override
    public Localization.Command.Unban localization(FEntity sender) {
        return fileFacade.localization(sender).command().unban();
    }

    public void unban(FPlayer fPlayer, String target, int id) {
        if (isModuleDisabledFor(fPlayer)) return;

        FPlayer fTarget = fPlayerService.getFPlayer(target);
        if (fTarget.isUnknown()) {
            sendErrorMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Unban::nullPlayer)
                    .build()
            );

            return;
        }

        if (config().checkGroupWeight() && !fPlayerService.hasHigherGroupThan(fPlayer, fTarget)) {
            sendErrorMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Unban::lowerWeightGroup)
                    .build()
            );
            return;
        }

        List<Moderation> bans = new ArrayList<>();

        if (id == -1) {
            bans.addAll(moderationService.getValidBans(fTarget));
        } else {
            moderationService.getValidBans(fTarget)
                    .stream()
                    .filter(moderation -> moderation.getId() == id)
                    .findAny()
                    .ifPresent(bans::add);
        }

        if (bans.isEmpty()) {
            sendErrorMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Unban::notBanned)
                    .build()
            );

            return;
        }

        moderationService.remove(fTarget, bans);

        proxySender.send(fTarget, MessageType.SYSTEM_BAN);

        sendMessage(UnModerationMetadata.<Localization.Command.Unban>builder()
                .sender(fTarget)
                .format(unwarn -> Strings.CS.replace(unwarn.format(), "<moderator>", fPlayer.getName()))
                .moderator(fPlayer)
                .moderations(bans)
                .destination(config().destination())
                .range(config().range())
                .sound(soundOrThrow())
                .proxy(dataOutputStream -> {
                    dataOutputStream.writeAsJson(fPlayer);
                    dataOutputStream.writeAsJson(bans);
                })
                .integration(string -> Strings.CS.replace(string, "<moderator>", fPlayer.getName()))
                .build()
        );
    }
}
