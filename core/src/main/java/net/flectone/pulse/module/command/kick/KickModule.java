package net.flectone.pulse.module.command.kick;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.util.constant.DisableSource;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.platform.formatter.ModerationMessageFormatter;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import org.incendo.cloud.context.CommandContext;

import java.util.Optional;
import java.util.function.BiFunction;

@Singleton
public class KickModule extends AbstractModuleCommand<Localization.Command.Kick> {

    private final Command.Kick command;
    private final Permission.Command.Kick permission;
    private final FPlayerService fPlayerService;
    private final ModerationService moderationService;
    private final ModerationMessageFormatter moderationMessageFormatter;
    private final CommandParserProvider commandParserProvider;
    private final MessagePipeline messagePipeline;
    private final Gson gson;

    @Inject
    public KickModule(FileResolver fileResolver,
                      FPlayerService fPlayerService,
                      ModerationService moderationService,
                      ModerationMessageFormatter moderationMessageFormatter,
                      CommandParserProvider commandParserProvider,
                      MessagePipeline messagePipeline,
                      Gson gson) {
        super(localization -> localization.getCommand().getKick(), Command::getKick, fPlayer -> fPlayer.isSetting(FPlayer.Setting.KICK));

        this.command = fileResolver.getCommand().getKick();
        this.permission = fileResolver.getPermission().getCommand().getKick();
        this.fPlayerService = fPlayerService;
        this.moderationService = moderationService;
        this.moderationMessageFormatter = moderationMessageFormatter;
        this.commandParserProvider = commandParserProvider;
        this.messagePipeline = messagePipeline;
        this.gson = gson;
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
        String promptMessage = addPrompt(1, Localization.Command.Prompt::getMessage);
        registerCommand(commandBuilder -> commandBuilder
                .permission(permission.getName())
                .required(promptPlayer, commandParserProvider.playerParser())
                .optional(promptMessage, commandParserProvider.nativeMessageParser())
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkCooldown(fPlayer)) return;
        if (checkDisable(fPlayer, fPlayer, DisableSource.YOU)) return;
        if (checkMute(fPlayer)) return;
        if (isModuleDisabledFor(fPlayer)) return;

        String playerName = getArgument(commandContext, 0);
        FPlayer fTarget = fPlayerService.getFPlayer(playerName);
        if (!fTarget.isOnline()) {
            builder(fPlayer)
                    .format(Localization.Command.Kick::getNullPlayer)
                    .sendBuilt();
            return;
        }

        String promptMessage = getPrompt(1);
        Optional<String> optionalReason = commandContext.optional(promptMessage);
        String reason = optionalReason.orElse(null);

        Moderation kick = moderationService.kick(fTarget, reason, fPlayer.getId());
        if (kick == null) return;

        kick(fPlayer, fTarget, kick);

        builder(fTarget)
                .destination(command.getDestination())
                .range(command.getRange())
                .tag(MessageType.COMMAND_KICK)
                .format(buildFormat(kick))
                .proxy(output -> {
                    output.writeUTF(gson.toJson(fTarget));
                    output.writeUTF(gson.toJson(kick));
                })
                .integration(s -> moderationMessageFormatter.replacePlaceholders(s, FPlayer.UNKNOWN, kick))
                .sound(getSound())
                .sendBuilt();
    }

    public BiFunction<FPlayer, Localization.Command.Kick, String> buildFormat(Moderation kick) {
        return (fReceiver, message) -> moderationMessageFormatter.replacePlaceholders(message.getServer(), fReceiver, kick);
    }

    public void kick(FEntity fModerator, FPlayer fReceiver, Moderation kick) {
        if (isModuleDisabledFor(fModerator)) return;

        String format = moderationMessageFormatter.replacePlaceholders(resolveLocalization(fReceiver).getPerson(), fReceiver, kick);

        fPlayerService.kick(fReceiver, messagePipeline.builder(fReceiver, format).build());
    }
}
