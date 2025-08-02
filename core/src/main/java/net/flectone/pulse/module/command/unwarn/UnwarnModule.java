package net.flectone.pulse.module.command.unwarn;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import org.incendo.cloud.context.CommandContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Singleton
public class UnwarnModule extends AbstractModuleCommand<Localization.Command.Unwarn> {

    private final Command.Unwarn command;
    private final Permission.Command.Unwarn permission;
    private final FPlayerService fPlayerService;
    private final ModerationService moderationService;
    private final CommandParserProvider commandParserProvider;
    private final ProxySender proxySender;
    private final Gson gson;

    @Inject
    public UnwarnModule(FileResolver fileResolver,
                        FPlayerService fPlayerService,
                        ModerationService moderationService,
                        CommandParserProvider commandParserProvider,
                        ProxySender proxySender,
                        Gson gson) {
        super(localization -> localization.getCommand().getUnwarn(), Command::getUnwarn);

        this.command = fileResolver.getCommand().getUnwarn();
        this.permission = fileResolver.getPermission().getCommand().getUnwarn();
        this.fPlayerService = fPlayerService;
        this.moderationService = moderationService;
        this.commandParserProvider = commandParserProvider;
        this.proxySender = proxySender;
        this.gson = gson;
    }

    @Override
    public void onEnable() {
        // if FPlayer.UNKNOWN (all-permissions) fails check (method will return true),
        // a moderation plugin is intercepting this command
        if (checkModulePredicates(FPlayer.UNKNOWN)) return;

        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        String promptPlayer = addPrompt(0, Localization.Command.Prompt::getPlayer);
        String promptId = addPrompt(1, Localization.Command.Prompt::getId);
        registerCommand(manager -> manager
                .permission(permission.getName())
                .required(promptPlayer, commandParserProvider.warnedParser())
                .optional(promptId, commandParserProvider.integerParser())
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkModulePredicates(fPlayer)) return;
        if (checkCooldown(fPlayer)) return;

        String target = getArgument(commandContext, 0);

        String promptId = getPrompt(1);
        Optional<Integer> optionalId = commandContext.optional(promptId);
        int id = optionalId.orElse(-1);

        unwarn(fPlayer, target, id);
    }

    public void unwarn(FPlayer fPlayer, String target, int id) {
        if (checkModulePredicates(fPlayer)) return;

        FPlayer fTarget = fPlayerService.getFPlayer(target);
        if (fTarget.isUnknown()) {
            builder(fPlayer)
                    .format(Localization.Command.Unwarn::getNullPlayer)
                    .sendBuilt();
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
            builder(fPlayer)
                    .format(Localization.Command.Unwarn::getNotWarned)
                    .sendBuilt();
            return;
        }

        moderationService.remove(fTarget, warns);

        proxySender.send(fTarget, MessageType.SYSTEM_WARN, dataOutputStream -> {});

        builder(fTarget)
                .tag(MessageType.COMMAND_UNWARN)
                .destination(command.getDestination())
                .range(command.getRange())
                .filter(filter -> filter.isSetting(FPlayer.Setting.WARN))
                .format(unwarn -> unwarn.getFormat().replace("<moderator>", fPlayer.getName()))
                .proxy(output -> output.writeUTF(gson.toJson(fPlayer)))
                .integration(s -> s.replace("<moderator>", fPlayer.getName()))
                .sound(getSound())
                .sendBuilt();
    }
}
