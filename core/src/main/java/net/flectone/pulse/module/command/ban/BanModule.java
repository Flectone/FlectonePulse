package net.flectone.pulse.module.command.ban;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.configuration.Command;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.constant.MessageType;
import net.flectone.pulse.formatter.ModerationMessageFormatter;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.ban.listener.BanPulseListener;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.registry.CommandRegistry;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.sender.ProxySender;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.util.Pair;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.meta.CommandMeta;

import java.util.Optional;
import java.util.function.BiFunction;

@Singleton
public class BanModule extends AbstractModuleCommand<Localization.Command.Ban> {

    @Getter private final Command.Ban command;
    private final Permission.Command.Ban permission;
    private final FPlayerService fPlayerService;
    private final ModerationService moderationService;
    private final CommandRegistry commandRegistry;
    private final ModerationMessageFormatter moderationMessageFormatter;
    private final MessagePipeline messagePipeline;
    private final ProxySender proxySender;
    private final Gson gson;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public BanModule(FileResolver fileResolver,
                     FPlayerService fPlayerService,
                     ModerationService moderationService,
                     CommandRegistry commandRegistry,
                     ModerationMessageFormatter moderationMessageFormatter,
                     MessagePipeline messagePipeline,
                     ProxySender proxySender,
                     Gson gson,
                     ListenerRegistry listenerRegistry) {
        super(localization -> localization.getCommand().getBan(), fPlayer -> fPlayer.isSetting(FPlayer.Setting.BAN));

        this.command = fileResolver.getCommand().getBan();
        this.permission = fileResolver.getPermission().getCommand().getBan();
        this.fPlayerService = fPlayerService;
        this.moderationService = moderationService;
        this.commandRegistry = commandRegistry;
        this.moderationMessageFormatter = moderationMessageFormatter;
        this.messagePipeline = messagePipeline;
        this.proxySender = proxySender;
        this.gson = gson;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    protected boolean isConfigEnable() {
        return command.isEnable();
    }

    @Override
    public void onEnable() {
        // if FPlayer.UNKNOWN (all-permissions) fails check (method will return true),
        // a moderation plugin is intercepting this command
        if (checkModulePredicates(FPlayer.UNKNOWN)) return;

        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        String commandName = getName(command);
        String promptPlayer = getPrompt().getPlayer();
        String promptReason = getPrompt().getReason();
        String promptTime = getPrompt().getTime();

        commandRegistry.registerCommand(manager ->
                manager.commandBuilder(commandName, command.getAliases(), CommandMeta.empty())
                        .permission(permission.getName())
                        .required(promptPlayer, commandRegistry.playerParser(command.isSuggestOfflinePlayers()))
                        .optional(promptTime + " " + promptReason, commandRegistry.durationReasonParser())
                        .handler(this)
        );

        listenerRegistry.register(BanPulseListener.class);
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkCooldown(fPlayer)) return;
        if (checkMute(fPlayer)) return;
        if (checkModulePredicates(fPlayer)) return;

        String promptReason = getPrompt().getReason();
        String promptTime = getPrompt().getTime();

        Optional<Pair<Long, String>> optionalTime = commandContext.optional(promptTime + " " + promptReason);
        Pair<Long, String> timeReasonPair = optionalTime.orElse(new Pair<>(-1L, null));

        long time = timeReasonPair.left();
        String reason = timeReasonPair.right();

        if (time != -1 && time < 1) {
            builder(fPlayer)
                    .format(Localization.Command.Ban::getNullTime)
                    .sendBuilt();
            return;
        }

        String promptPlayer = getPrompt().getPlayer();
        String target = commandContext.get(promptPlayer);

        ban(fPlayer, target, time, reason);
    }

    public void ban(FPlayer fPlayer, String target, long time, String reason) {
        if (checkModulePredicates(fPlayer)) return;

        FPlayer fTarget = fPlayerService.getFPlayer(target);
        if (fTarget.isUnknown()) {
            builder(fPlayer)
                    .format(Localization.Command.Ban::getNullPlayer)
                    .sendBuilt();
            return;
        }

        long databaseTime = time != -1 ? time + System.currentTimeMillis() : -1;

        Moderation ban = moderationService.ban(fTarget, databaseTime, reason, fPlayer.getId());
        if (ban == null) return;

        proxySender.send(fTarget, MessageType.SYSTEM_BAN, dataOutputStream -> {});

        kick(fPlayer, fTarget, ban);

        builder(fTarget)
                .range(command.getRange())
                .destination(command.getDestination())
                .tag(MessageType.COMMAND_BAN)
                .format(buildFormat(ban))
                .proxy(output -> {
                    output.writeUTF(gson.toJson(fPlayer));
                    output.writeUTF(gson.toJson(ban));
                })
                .integration(s -> moderationMessageFormatter.replacePlaceholders(s, FPlayer.UNKNOWN, ban))
                .sound(getSound())
                .sendBuilt();
    }

    public BiFunction<FPlayer, Localization.Command.Ban, String> buildFormat(Moderation ban) {
        return (fReceiver, message) -> {
            String format = message.getServer();

            return moderationMessageFormatter.replacePlaceholders(format, fReceiver, ban);
        };
    }

    public void kick(FEntity fModerator, FPlayer fTarget, Moderation ban) {
        if (checkModulePredicates(fModerator)) return;
        if (fModerator == null) return;

        Localization.Command.Ban localization = resolveLocalization(fTarget);

        String formatPlayer = localization.getPerson();
        formatPlayer = moderationMessageFormatter.replacePlaceholders(formatPlayer, fTarget, ban);

        fPlayerService.kick(fTarget, messagePipeline.builder(fModerator, fTarget, formatPlayer).build());
    }
}
