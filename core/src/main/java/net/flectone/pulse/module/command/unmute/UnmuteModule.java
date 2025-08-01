package net.flectone.pulse.module.command.unmute;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Command;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.constant.MessageType;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.provider.CommandParserProvider;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.sender.ProxySender;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
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
    private final Gson gson;

    @Inject
    public UnmuteModule(FileResolver fileResolver,
                        FPlayerService fPlayerService,
                        ModerationService moderationService,
                        CommandParserProvider commandParserProvider,
                        ProxySender proxySender,
                        Gson gson) {
        super(localization -> localization.getCommand().getUnmute(), Command::getUnmute);

        this.command = fileResolver.getCommand().getUnmute();
        this.permission = fileResolver.getPermission().getCommand().getUnmute();
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

        unmute(fPlayer, target, id);
    }

    public void unmute(FPlayer fPlayer, String target, int id) {
        if (checkModulePredicates(fPlayer)) return;

        FPlayer fTarget = fPlayerService.getFPlayer(target);
        if (fTarget.isUnknown()) {
            builder(fPlayer)
                    .format(Localization.Command.Unmute::getNullPlayer)
                    .sendBuilt();
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
            builder(fPlayer)
                    .format(Localization.Command.Unmute::getNotMuted)
                    .sendBuilt();
            return;
        }

        moderationService.remove(fTarget, mutes);

        proxySender.send(fTarget, MessageType.SYSTEM_MUTE, dataOutputStream -> {});

        builder(fTarget)
                .tag(MessageType.COMMAND_UNMUTE)
                .destination(command.getDestination())
                .range(command.getRange())
                .filter(filter -> filter.isSetting(FPlayer.Setting.MUTE))
                .format(unmute -> unmute.getFormat().replace("<moderator>", fPlayer.getName()))
                .proxy(output -> output.writeUTF(gson.toJson(fPlayer)))
                .integration(s -> s.replace("<moderator>", fPlayer.getName()))
                .sound(getSound())
                .sendBuilt();
    }
}
