package net.flectone.pulse.module.command.rockpaperscissors;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.util.constant.DisableSource;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.rockpaperscissors.model.RockPaperScissors;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.service.FPlayerService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.UUIDParser;

import java.util.*;
import java.util.function.BiFunction;

@Singleton
public class RockpaperscissorsModule extends AbstractModuleCommand<Localization.Command.Rockpaperscissors> {

    private final Map<UUID, RockPaperScissors> gameMap = new HashMap<>();

    private final Command.Rockpaperscissors command;
    private final Permission.Command.Rockpaperscissors permission;
    private final ProxySender proxySender;
    private final FPlayerService fPlayerService;
    private final CommandParserProvider commandParserProvider;
    private final IntegrationModule integrationModule;

    @Inject
    public RockpaperscissorsModule(FileResolver fileResolver,
                                   ProxySender proxySender,
                                   FPlayerService fPlayerService,
                                   CommandParserProvider commandParserProvider,
                                   IntegrationModule integrationModule) {
        super(localization -> localization.getCommand().getRockpaperscissors(), Command::getRockpaperscissors, fPlayer -> fPlayer.isSetting(FPlayer.Setting.ROCKPAPERSCISSORS));

        this.command = fileResolver.getCommand().getRockpaperscissors();
        this.permission = fileResolver.getPermission().getCommand().getRockpaperscissors();
        this.proxySender = proxySender;
        this.fPlayerService = fPlayerService;
        this.commandParserProvider = commandParserProvider;
        this.integrationModule = integrationModule;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        String promptPlayer = addPrompt(0, Localization.Command.Prompt::getPlayer);
        String promptMove = addPrompt(1, Localization.Command.Prompt::getMove);
        String promptUUID = addPrompt(2, Localization.Command.Prompt::getId);
        registerCommand(manager -> manager
                .permission(permission.getName())
                .required(promptPlayer, commandParserProvider.playerParser())
                .optional(promptMove, commandParserProvider.nativeSingleMessageParser())
                .optional(promptUUID, UUIDParser.uuidParser())
        );
    }

    @Override
    public void onDisable() {
        super.onDisable();

        gameMap.clear();
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer)) return;
        if (checkCooldown(fPlayer)) return;
        if (checkDisable(fPlayer, fPlayer, DisableSource.YOU)) return;
        if (checkMute(fPlayer)) return;

        String player = getArgument(commandContext, 0);
        FPlayer fReceiver = fPlayerService.getFPlayer(player);
        if (!fReceiver.isOnline() || !integrationModule.canSeeVanished(fReceiver, fPlayer)) {
            builder(fPlayer)
                    .format(Localization.Command.Rockpaperscissors::getNullPlayer)
                    .sendBuilt();
            return;
        }

        if (fReceiver.equals(fPlayer)) {
            builder(fPlayer)
                    .format(Localization.Command.Rockpaperscissors::getMyself)
                    .sendBuilt();
            return;
        }

        fPlayerService.loadIgnores(fReceiver);

        if (checkIgnore(fPlayer, fReceiver)) return;
        if (checkDisable(fPlayer, fReceiver, DisableSource.HE)) return;

        String promptMove = getPrompt(1);
        Optional<String> optionalMove = commandContext.optional(promptMove);
        String move = optionalMove.orElse(null);

        String promptUUID = getPrompt(2);
        Optional<UUID> optionalUUID = commandContext.optional(promptUUID);
        UUID uuid = optionalUUID.orElse(null);

        if (move != null && uuid != null) {
            finalMove(fPlayer, fReceiver, move, uuid);
            return;
        }

        RockPaperScissors rockPaperScissors = new RockPaperScissors(fPlayer.getUuid(), fReceiver.getUuid());

        proxySender.send(fPlayer, MessageType.COMMAND_ROCKPAPERSCISSORS_CREATE, dataOutputStream -> {
            dataOutputStream.writeUTF(rockPaperScissors.getId().toString());
            dataOutputStream.writeUTF(rockPaperScissors.getReceiver().toString());
        });

        create(rockPaperScissors.getId(), fPlayer, fReceiver.getUuid());

        builder(fPlayer)
                .format(s -> StringUtils.replaceEach(s.getFormatMove(),
                        new String[]{"<target>", "<uuid>"},
                        new String[]{fReceiver.getName(), rockPaperScissors.getId().toString()}
                ))
                .sound(getSound())
                .sendBuilt();
    }

    public void finalMove(FPlayer fPlayer, FPlayer fReceiver, String move, UUID uuid) {
        List<String> strategy = command.getStrategies().get(move);

        if (strategy == null) {
            builder(fPlayer)
                    .format(Localization.Command.Rockpaperscissors::getWrongMove)
                    .sendBuilt();
            return;
        }

        RockPaperScissors rockPaperScissors = gameMap.get(uuid);

        if (rockPaperScissors == null) {
            builder(fPlayer)
                    .format(Localization.Command.Rockpaperscissors::getNullGame)
                    .sendBuilt();
            return;
        }

        if (rockPaperScissors.getSenderMove() != null) {
            if (rockPaperScissors.getSender().equals(fPlayer.getUuid())) {
                builder(fPlayer)
                        .format(Localization.Command.Rockpaperscissors::getAlready)
                        .sendBuilt();
                return;
            }

            boolean isSent = proxySender.send(fPlayer, MessageType.COMMAND_ROCKPAPERSCISSORS_FINAL, dataOutputStream -> {
                dataOutputStream.writeUTF(rockPaperScissors.getId().toString());
                dataOutputStream.writeUTF(move);
            });

            if (isSent) return;

            sendFinalMessage(rockPaperScissors.getId(), fPlayer, move);

            return;
        }

        builder(fReceiver)
                .receiver(fPlayer)
                .format((fResolver, s) -> s.getSender())
                .sendBuilt();

        boolean isSent = proxySender.send(fPlayer, MessageType.COMMAND_ROCKPAPERSCISSORS_MOVE, dataOutputStream -> {
            dataOutputStream.writeUTF(rockPaperScissors.getId().toString());
            dataOutputStream.writeUTF(move);
        });

        if (isSent) return;

        move(rockPaperScissors.getId(), fPlayer, move);
    }

    public void sendFinalMessage(UUID id, FPlayer fPlayer, String move) {
        if (isModuleDisabledFor(fPlayer)) return;

        RockPaperScissors rockPaperScissors = gameMap.get(id);
        if (rockPaperScissors == null) return;

        FPlayer fReceiver = fPlayerService.getFPlayer(rockPaperScissors.getSender());

        gameMap.remove(id);

        String senderMove = rockPaperScissors.getSenderMove();

        boolean isDraw = senderMove.equalsIgnoreCase(move);

        if (isDraw) {
            BiFunction<FPlayer, Localization.Command.Rockpaperscissors, String> message =
                    (p, m) -> Strings.CS.replace(
                            m.getFormatDraw(),
                            "<move>",
                            resolveLocalization(p).getStrategies().get(move)
                    );

            builder(fPlayer)
                    .format(message)
                    .sendBuilt();

            builder(fReceiver)
                    .format(message)
                    .sendBuilt();

            return;
        }

        BiFunction<FPlayer, Localization.Command.Rockpaperscissors, String> message = (p, m) -> StringUtils.replaceEach(
                m.getFormatWin(),
                new String[]{"<sender_move>", "<receiver_move>"},
                new String[]{resolveLocalization(p).getStrategies().get(senderMove), resolveLocalization(p).getStrategies().get(move)}
        );

        FEntity winFPlayer = command.getStrategies().get(move).contains(senderMove) ? fPlayer : fReceiver;

        builder(winFPlayer)
                .receiver(fPlayer)
                .format(message)
                .sendBuilt();

        builder(winFPlayer)
                .receiver(fReceiver)
                .format(message)
                .sendBuilt();
    }

    public void move(UUID id, FEntity fPlayer, String move) {
        if (isModuleDisabledFor(fPlayer)) return;

        RockPaperScissors rockPaperScissors = gameMap.get(id);
        if (rockPaperScissors == null) return;

        FPlayer fReceiver = fPlayerService.getFPlayer(rockPaperScissors.getReceiver());

        rockPaperScissors.setSenderMove(move);

        builder(fPlayer)
                .receiver(fReceiver)
                .format((fResolver, s) -> s.getReceiver())
                .sendBuilt();

        builder(fPlayer)
                .receiver(fReceiver)
                .format((fResolver, s) -> StringUtils.replaceEach(
                        s.getFormatMove(),
                        new String[]{"<target>", "<uuid>"},
                        new String[]{fPlayer.getName(), rockPaperScissors.getId().toString()}
                ))
                .sendBuilt();
    }

    public void create(UUID id, FEntity fPlayer, UUID receiver) {
        if (isModuleDisabledFor(fPlayer)) return;

        RockPaperScissors rockPaperScissors = new RockPaperScissors(id, fPlayer.getUuid(), receiver);
        gameMap.put(id, rockPaperScissors);
    }
}
