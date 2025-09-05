package net.flectone.pulse.module.command.rockpaperscissors;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.rockpaperscissors.model.RockPaperScissors;
import net.flectone.pulse.module.command.rockpaperscissors.model.RockPaperScissorsMetadata;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageType;
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
        super(localization -> localization.getCommand().getRockpaperscissors(), Command::getRockpaperscissors, MessageType.COMMAND_ROCKPAPERSCISSORS);

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
        if (isModuleDisabledFor(fPlayer, true)) return;

        String player = getArgument(commandContext, 0);
        FPlayer fReceiver = fPlayerService.getFPlayer(player);
        if (!fReceiver.isOnline() || !integrationModule.canSeeVanished(fReceiver, fPlayer)) {
            sendErrorMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Rockpaperscissors::getNullPlayer)
                    .build()
            );

            return;
        }

        if (fReceiver.equals(fPlayer)) {
            sendErrorMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Rockpaperscissors::getMyself)
                    .build()
            );

            return;
        }

        fPlayerService.loadIgnores(fReceiver);

        if (checkIgnore(fPlayer, fReceiver)) return;
        if (checkDisable(fPlayer, fReceiver)) return;

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

        proxySender.send(fPlayer, MessageType.COMMAND_ROCKPAPERSCISSORS, dataOutputStream -> {
            dataOutputStream.writeUTF(GamePhase.CREATE.name());
            dataOutputStream.writeUTF(rockPaperScissors.getId().toString());
            dataOutputStream.writeUTF(rockPaperScissors.getReceiver().toString());
        }, UUID.randomUUID());

        create(rockPaperScissors.getId(), fPlayer, fReceiver.getUuid());

        sendMessage(RockPaperScissorsMetadata.<Localization.Command.Rockpaperscissors>builder()
                .sender(fPlayer)
                .format(s -> StringUtils.replaceEach(s.getFormatMove(),
                        new String[]{"<target>", "<uuid>"},
                        new String[]{fReceiver.getName(), rockPaperScissors.getId().toString()}
                ))
                .rockPaperScissors(rockPaperScissors)
                .gamePhase(GamePhase.CREATE)
                .sound(getModuleSound())
                .build()
        );
    }

    public void finalMove(FPlayer fPlayer, FPlayer fReceiver, String move, UUID uuid) {
        List<String> strategy = command.getStrategies().get(move);

        if (strategy == null) {
            sendErrorMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Rockpaperscissors::getWrongMove)
                    .build()
            );

            return;
        }

        RockPaperScissors rockPaperScissors = gameMap.get(uuid);

        if (rockPaperScissors == null) {
            sendErrorMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Rockpaperscissors::getNullGame)
                    .build()
            );

            return;
        }

        if (rockPaperScissors.getSenderMove() != null) {
            if (rockPaperScissors.getSender().equals(fPlayer.getUuid())) {
                sendErrorMessage(metadataBuilder()
                        .sender(fPlayer)
                        .format(Localization.Command.Rockpaperscissors::getAlready)
                        .build()
                );

                return;
            }

            boolean isSent = proxySender.send(fPlayer, MessageType.COMMAND_ROCKPAPERSCISSORS, dataOutputStream -> {
                dataOutputStream.writeUTF(GamePhase.END.name());
                dataOutputStream.writeUTF(rockPaperScissors.getId().toString());
                dataOutputStream.writeUTF(move);
            }, UUID.randomUUID());

            if (isSent) return;

            end(rockPaperScissors.getId(), fPlayer, move, UUID.randomUUID());

            return;
        }

        sendMessage(metadataBuilder()
                .sender(fReceiver)
                .filterPlayer(fPlayer)
                .format(Localization.Command.Rockpaperscissors::getSender)
                .build()
        );

        boolean isSent = proxySender.send(fPlayer, MessageType.COMMAND_ROCKPAPERSCISSORS, dataOutputStream -> {
            dataOutputStream.writeUTF(GamePhase.MOVE.name());
            dataOutputStream.writeUTF(rockPaperScissors.getId().toString());
            dataOutputStream.writeUTF(move);
        }, UUID.randomUUID());

        if (isSent) return;

        move(rockPaperScissors.getId(), fPlayer, move, UUID.randomUUID());
    }

    public void end(UUID id, FPlayer fPlayer, String move, UUID metadataUUID) {
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

            sendMessage(metadataBuilder()
                    .uuid(metadataUUID)
                    .sender(fPlayer)
                    .format(message)
                    .build()
            );

            sendMessage(metadataBuilder()
                    .uuid(metadataUUID)
                    .sender(fReceiver)
                    .format(message)
                    .build()
            );

            return;
        }

        BiFunction<FPlayer, Localization.Command.Rockpaperscissors, String> message = (p, m) -> StringUtils.replaceEach(
                m.getFormatWin(),
                new String[]{"<sender_move>", "<receiver_move>"},
                new String[]{resolveLocalization(p).getStrategies().get(senderMove), resolveLocalization(p).getStrategies().get(move)}
        );

        FEntity winFPlayer = command.getStrategies().get(move).contains(senderMove) ? fPlayer : fReceiver;

        sendMessage(RockPaperScissorsMetadata.<Localization.Command.Rockpaperscissors>builder()
                .uuid(metadataUUID)
                .sender(winFPlayer)
                .filterPlayer(fPlayer)
                .format(message)
                .rockPaperScissors(rockPaperScissors)
                .gamePhase(GamePhase.END)
                .build()
        );

        sendMessage(RockPaperScissorsMetadata.<Localization.Command.Rockpaperscissors>builder()
                .uuid(metadataUUID)
                .sender(winFPlayer)
                .filterPlayer(fReceiver)
                .format(message)
                .rockPaperScissors(rockPaperScissors)
                .gamePhase(GamePhase.END)
                .build()
        );
    }

    public void move(UUID id, FEntity fPlayer, String move, UUID metadataUUID) {
        if (isModuleDisabledFor(fPlayer)) return;

        RockPaperScissors rockPaperScissors = gameMap.get(id);
        if (rockPaperScissors == null) return;

        FPlayer fReceiver = fPlayerService.getFPlayer(rockPaperScissors.getReceiver());

        rockPaperScissors.setSenderMove(move);

        sendMessage(RockPaperScissorsMetadata.<Localization.Command.Rockpaperscissors>builder()
                .sender(fPlayer)
                .filterPlayer(fReceiver, true)
                .format(Localization.Command.Rockpaperscissors::getReceiver)
                .rockPaperScissors(rockPaperScissors)
                .gamePhase(GamePhase.MOVE)
                .build()
        );

        sendMessage(RockPaperScissorsMetadata.<Localization.Command.Rockpaperscissors>builder()
                .uuid(metadataUUID)
                .sender(fPlayer)
                .filterPlayer(fReceiver, true)
                .format(s -> StringUtils.replaceEach(
                        s.getFormatMove(),
                        new String[]{"<target>", "<uuid>"},
                        new String[]{fPlayer.getName(), rockPaperScissors.getId().toString()}
                ))
                .rockPaperScissors(rockPaperScissors)
                .gamePhase(GamePhase.MOVE)
                .build()
        );
    }

    public void create(UUID id, FEntity fPlayer, UUID receiver) {
        if (isModuleDisabledFor(fPlayer)) return;

        RockPaperScissors rockPaperScissors = new RockPaperScissors(id, fPlayer.getUuid(), receiver);
        gameMap.put(id, rockPaperScissors);
    }

    public enum GamePhase {
        CREATE,
        MOVE,
        END
    }
}
