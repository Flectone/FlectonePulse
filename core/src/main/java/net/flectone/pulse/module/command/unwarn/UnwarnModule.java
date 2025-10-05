package net.flectone.pulse.module.command.unwarn;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
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
public class UnwarnModule extends AbstractModuleCommand<Localization.Command.Unwarn> {

    private final FileResolver fileResolver;
    private final FPlayerService fPlayerService;
    private final ModerationService moderationService;
    private final CommandParserProvider commandParserProvider;
    private final ProxySender proxySender;

    @Inject
    public UnwarnModule(FileResolver fileResolver,
                        FPlayerService fPlayerService,
                        ModerationService moderationService,
                        CommandParserProvider commandParserProvider,
                        ProxySender proxySender) {
        super(MessageType.COMMAND_UNWARN);

        this.fileResolver = fileResolver;
        this.fPlayerService = fPlayerService;
        this.moderationService = moderationService;
        this.commandParserProvider = commandParserProvider;
        this.proxySender = proxySender;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        String promptPlayer = addPrompt(0, Localization.Command.Prompt::getPlayer);
        String promptId = addPrompt(1, Localization.Command.Prompt::getId);
        registerCommand(manager -> manager
                .permission(permission().getName())
                .required(promptPlayer, commandParserProvider.warnedParser())
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

        unwarn(fPlayer, target, id);
    }

    @Override
    public Command.Unwarn config() {
        return fileResolver.getCommand().getUnwarn();
    }

    @Override
    public Permission.Command.Unwarn permission() {
        return fileResolver.getPermission().getCommand().getUnwarn();
    }

    @Override
    public Localization.Command.Unwarn localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getCommand().getUnwarn();
    }

    public void unwarn(FPlayer fPlayer, String target, int id) {
        if (isModuleDisabledFor(fPlayer)) return;

        FPlayer fTarget = fPlayerService.getFPlayer(target);
        if (fTarget.isUnknown()) {
            sendErrorMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Unwarn::getNullPlayer)
                    .build()
            );

            return;
        }

        List<Moderation> warns = new ArrayList<>();

        if (id == -1) {
            warns.addAll(moderationService.getValidWarns(fTarget));
        } else {
            moderationService.getValidWarns(fTarget).stream()
                    .filter(warn -> warn.getId() == id)
                    .findAny()
                    .ifPresent(warns::add);
        }

        if (warns.isEmpty()) {
            sendErrorMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Unwarn::getNotWarned)
                    .build()
            );

            return;
        }

        moderationService.remove(fTarget, warns);

        proxySender.send(fTarget, MessageType.SYSTEM_WARN);

        sendMessage(UnModerationMetadata.<Localization.Command.Unwarn>builder()
                .sender(fTarget)
                .format(unwarn -> Strings.CS.replace(unwarn.getFormat(), "<moderator>", fPlayer.getName()))
                .moderator(fPlayer)
                .moderations(warns)
                .destination(config().getDestination())
                .range(config().getRange())
                .sound(getModuleSound())
                .proxy(dataOutputStream -> {
                    dataOutputStream.writeAsJson(fPlayer);
                    dataOutputStream.writeAsJson(warns);
                })
                .integration(string -> Strings.CS.replace(string, "<moderator>", fPlayer.getName()))
                .build()
        );
    }
}
