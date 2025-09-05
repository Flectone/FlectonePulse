package net.flectone.pulse.module.command.unban;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.UnModerationMetadata;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.util.constant.MessageType;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Singleton
public class UnbanModule extends AbstractModuleCommand<Localization.Command.Unban> {

    private final Command.Unban command;
    private final Permission.Command.Unban permission;
    private final FPlayerService fPlayerService;
    private final ModerationService moderationService;
    private final CommandParserProvider commandParserProvider;
    private final ProxySender proxySender;

    @Inject
    public UnbanModule(FileResolver fileResolver,
                       FPlayerService fPlayerService,
                       ModerationService moderationService,
                       CommandParserProvider commandParserProvider,
                       ProxySender proxySender) {
        super(localization -> localization.getCommand().getUnban(), Command::getUnban, MessageType.COMMAND_UNBAN);

        this.command = fileResolver.getCommand().getUnban();
        this.permission = fileResolver.getPermission().getCommand().getUnban();
        this.fPlayerService = fPlayerService;
        this.moderationService = moderationService;
        this.commandParserProvider = commandParserProvider;
        this.proxySender = proxySender;
    }

    @Override
    protected boolean isConfigEnable() {
        return command.isEnable();
    }

    @Override
    public void onEnable() {
        // if FPlayer.UNKNOWN (all-permissions) fails check (method will return true),
        // a moderation plugin is intercepting this command
        if (isModuleDisabledFor(FPlayer.UNKNOWN)) return;

        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        String promptPlayer = addPrompt(0, Localization.Command.Prompt::getPlayer);
        String promptId = addPrompt(1, Localization.Command.Prompt::getId);
        registerCommand(manager -> manager
                .permission(permission.getName())
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

    public void unban(FPlayer fPlayer, String target, int id) {
        if (isModuleDisabledFor(fPlayer)) return;

        FPlayer fTarget = fPlayerService.getFPlayer(target);
        if (fTarget.isUnknown()) {
            sendErrorMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Unban::getNullPlayer)
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
                    .format(Localization.Command.Unban::getNotBanned)
                    .build()
            );

            return;
        }

        moderationService.remove(fTarget, bans);

        proxySender.send(fTarget, MessageType.SYSTEM_BAN);

        sendMessage(UnModerationMetadata.<Localization.Command.Unban>builder()
                .sender(fTarget)
                .format(unwarn -> Strings.CS.replace(unwarn.getFormat(), "<moderator>", fPlayer.getName()))
                .moderator(fPlayer)
                .moderations(bans)
                .destination(command.getDestination())
                .range(command.getRange())
                .sound(getModuleSound())
                .proxy(dataOutputStream -> {
                    dataOutputStream.writeAsJson(fPlayer);
                    dataOutputStream.writeAsJson(bans);
                })
                .integration(string -> Strings.CS.replace(string, "<moderator>", fPlayer.getName()))
                .build()
        );
    }
}
