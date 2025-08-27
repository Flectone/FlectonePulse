package net.flectone.pulse.module.command.unmute;

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
public class UnmuteModule extends AbstractModuleCommand<Localization.Command.Unmute> {

    private final Command.Unmute command;
    private final Permission.Command.Unmute permission;
    private final FPlayerService fPlayerService;
    private final ModerationService moderationService;
    private final CommandParserProvider commandParserProvider;
    private final ProxySender proxySender;

    @Inject
    public UnmuteModule(FileResolver fileResolver,
                        FPlayerService fPlayerService,
                        ModerationService moderationService,
                        CommandParserProvider commandParserProvider,
                        ProxySender proxySender) {
        super(localization -> localization.getCommand().getUnmute(), Command::getUnmute, MessageType.COMMAND_UNMUTE);

        this.command = fileResolver.getCommand().getUnmute();
        this.permission = fileResolver.getPermission().getCommand().getUnmute();
        this.fPlayerService = fPlayerService;
        this.moderationService = moderationService;
        this.commandParserProvider = commandParserProvider;
        this.proxySender = proxySender;
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
                .required(promptPlayer, commandParserProvider.mutedParser())
                .optional(promptId, commandParserProvider.integerParser())
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer)) return;
        if (checkCooldown(fPlayer)) return;

        String target = getArgument(commandContext, 0);

        String promptId = getPrompt(1);
        Optional<Integer> optionalId = commandContext.optional(promptId);
        int id = optionalId.orElse(-1);

        unmute(fPlayer, target, id);
    }

    public void unmute(FPlayer fPlayer, String target, int id) {
        if (isModuleDisabledFor(fPlayer)) return;

        FPlayer fTarget = fPlayerService.getFPlayer(target);
        if (fTarget.isUnknown()) {
            sendMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Unmute::getNullPlayer)
                    .build()
            );

            return;
        }

        List<Moderation> mutes = new ArrayList<>();

        if (id == -1) {
            mutes.addAll(moderationService.getValidMutes(fTarget));
        } else {
            moderationService.getValidMutes(fTarget).stream()
                    .filter(moderation -> moderation.getId() == id)
                    .findAny()
                    .ifPresent(mutes::add);
        }

        if (mutes.isEmpty()) {
            sendMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Unmute::getNotMuted)
                    .build()
            );

            return;
        }

        moderationService.remove(fTarget, mutes);

        proxySender.send(fTarget, MessageType.SYSTEM_MUTE, dataOutputStream -> {});

        sendMessage(UnModerationMetadata.<Localization.Command.Unmute>builder()
                .sender(fTarget)
                .format(unmute -> Strings.CS.replace(unmute.getFormat(), "<moderator>", fPlayer.getName()))
                .moderator(fPlayer)
                .moderations(mutes)
                .destination(command.getDestination())
                .range(command.getRange())
                .sound(getModuleSound())
                .filter(filter -> filter.isSetting(FPlayer.Setting.MUTE))
                .proxy(dataOutputStream -> {
                    dataOutputStream.writeAsJson(fPlayer);
                    dataOutputStream.writeAsJson(mutes);
                })
                .integration(string -> Strings.CS.replace(
                        string,
                        "<moderator>",
                        fPlayer.getName()
                ))
                .build()
        );
    }
}
