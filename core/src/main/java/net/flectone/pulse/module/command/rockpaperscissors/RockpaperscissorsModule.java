package net.flectone.pulse.module.command.rockpaperscissors;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.rockpaperscissors.model.RockPaperScissors;
import net.flectone.pulse.module.command.rockpaperscissors.model.RockPaperScissorsMetadata;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.sender.DisableSender;
import net.flectone.pulse.platform.sender.IgnoreSender;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.UUIDParser;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RockpaperscissorsModule extends AbstractModuleCommand<Localization.Command.Rockpaperscissors> {

    private final Map<UUID, RockPaperScissors> gameMap = new Object2ObjectArrayMap<>();

    private final FileFacade fileFacade;
    private final ProxySender proxySender;
    private final FPlayerService fPlayerService;
    private final CommandParserProvider commandParserProvider;
    private final IntegrationModule integrationModule;
    private final IgnoreSender ignoreSender;
    private final DisableSender disableSender;

    @Override
    public void onEnable() {
        super.onEnable();

        String promptPlayer = addPrompt(0, Localization.Command.Prompt::player);
        String promptMove = addPrompt(1, Localization.Command.Prompt::move);
        String promptUUID = addPrompt(2, Localization.Command.Prompt::id);
        registerCommand(manager -> manager
                .permission(permission().name())
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
            sendErrorMessage(EventMetadata.<Localization.Command.Rockpaperscissors>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Rockpaperscissors::nullPlayer)
                    .build()
            );

            return;
        }

        if (fReceiver.equals(fPlayer)) {
            sendErrorMessage(EventMetadata.<Localization.Command.Rockpaperscissors>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Rockpaperscissors::myself)
                    .build()
            );

            return;
        }

        fReceiver = fPlayerService.loadIgnoresIfOffline(fReceiver);
        if (ignoreSender.sendIfIgnored(fPlayer, fReceiver)) return;

        FPlayer finalFReceiver = fPlayerService.loadSettingsIfOffline(fReceiver);
        if (disableSender.sendIfDisabled(fPlayer, fReceiver, messageType())) return;

        String promptMove = getPrompt(1);
        Optional<String> optionalMove = commandContext.optional(promptMove);
        String move = optionalMove.orElse(null);

        String promptUUID = getPrompt(2);
        Optional<UUID> optionalUUID = commandContext.optional(promptUUID);
        UUID uuid = optionalUUID.orElse(null);

        if (move != null && uuid != null) {
            finalMove(fPlayer, finalFReceiver, move, uuid);
            return;
        }

        RockPaperScissors rockPaperScissors = new RockPaperScissors(fPlayer.uuid(), fReceiver.uuid());

        proxySender.send(fPlayer, MessageType.COMMAND_ROCKPAPERSCISSORS, dataOutputStream -> {
            dataOutputStream.writeUTF(GamePhase.CREATE.name());
            dataOutputStream.writeUTF(rockPaperScissors.getId().toString());
            dataOutputStream.writeUTF(rockPaperScissors.getReceiver().toString());
        }, UUID.randomUUID());

        create(rockPaperScissors.getId(), fPlayer, fReceiver.uuid());

        sendMessage(RockPaperScissorsMetadata.<Localization.Command.Rockpaperscissors>builder()
                .base(EventMetadata.<Localization.Command.Rockpaperscissors>builder()
                        .sender(fPlayer)
                        .format(s -> StringUtils.replaceEach(s.formatMove(),
                                new String[]{"<target>", "<uuid>"},
                                new String[]{finalFReceiver.name(), rockPaperScissors.getId().toString()}
                        ))
                        .sound(soundOrThrow())
                        .build()
                )
                .rockPaperScissors(rockPaperScissors)
                .gamePhase(GamePhase.CREATE)
                .build()
        );
    }

    @Override
    public MessageType messageType() {
        return MessageType.COMMAND_ROCKPAPERSCISSORS;
    }

    @Override
    public Command.Rockpaperscissors config() {
        return fileFacade.command().rockpaperscissors();
    }

    @Override
    public Permission.Command.Rockpaperscissors permission() {
        return fileFacade.permission().command().rockpaperscissors();
    }

    @Override
    public Localization.Command.Rockpaperscissors localization(FEntity sender) {
        return fileFacade.localization(sender).command().rockpaperscissors();
    }

    public void finalMove(FPlayer fPlayer, FPlayer fReceiver, String move, UUID uuid) {
        List<String> strategy = config().strategies().get(move);

        if (strategy == null) {
            sendErrorMessage(EventMetadata.<Localization.Command.Rockpaperscissors>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Rockpaperscissors::wrongMove)
                    .build()
            );

            return;
        }

        RockPaperScissors rockPaperScissors = gameMap.get(uuid);

        if (rockPaperScissors == null) {
            sendErrorMessage(EventMetadata.<Localization.Command.Rockpaperscissors>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Rockpaperscissors::nullGame)
                    .build()
            );

            return;
        }

        if (rockPaperScissors.getSenderMove() != null) {
            if (rockPaperScissors.getSender().equals(fPlayer.uuid())) {
                sendErrorMessage(EventMetadata.<Localization.Command.Rockpaperscissors>builder()
                        .sender(fPlayer)
                        .format(Localization.Command.Rockpaperscissors::already)
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

        sendMessage(EventMetadata.<Localization.Command.Rockpaperscissors>builder()
                .sender(fPlayer)
                .format(Localization.Command.Rockpaperscissors::sender)
                .tagResolvers(fResolver -> new TagResolver[]{targetTag(fResolver, fReceiver)})
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
                            m.formatDraw(),
                            "<move>",
                            localization(p).strategies().get(move)
                    );

            sendMessage(EventMetadata.<Localization.Command.Rockpaperscissors>builder()
                    .uuid(metadataUUID)
                    .sender(fPlayer)
                    .format(message)
                    .build()
            );

            sendMessage(EventMetadata.<Localization.Command.Rockpaperscissors>builder()
                    .uuid(metadataUUID)
                    .sender(fReceiver)
                    .format(message)
                    .build()
            );

            return;
        }

        BiFunction<FPlayer, Localization.Command.Rockpaperscissors, String> message = (p, m) -> StringUtils.replaceEach(
                m.formatWin(),
                new String[]{"<sender_move>", "<receiver_move>"},
                new String[]{localization(p).strategies().get(senderMove), localization(p).strategies().get(move)}
        );

        FEntity winFPlayer = config().strategies().get(move).contains(senderMove) ? fPlayer : fReceiver;

        sendMessage(RockPaperScissorsMetadata.<Localization.Command.Rockpaperscissors>builder()
                .base(EventMetadata.<Localization.Command.Rockpaperscissors>builder()
                        .uuid(metadataUUID)
                        .sender(winFPlayer)
                        .filterPlayer(fPlayer)
                        .format(message)
                        .build()
                )
                .rockPaperScissors(rockPaperScissors)
                .gamePhase(GamePhase.END)
                .build()
        );

        sendMessage(RockPaperScissorsMetadata.<Localization.Command.Rockpaperscissors>builder()
                .base(EventMetadata.<Localization.Command.Rockpaperscissors>builder()
                        .uuid(metadataUUID)
                        .sender(winFPlayer)
                        .filterPlayer(fReceiver)
                        .format(message)
                        .build()
                )
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
                .base(EventMetadata.<Localization.Command.Rockpaperscissors>builder()
                        .sender(fPlayer)
                        .filterPlayer(fReceiver, false)
                        .format(Localization.Command.Rockpaperscissors::receiver)
                        .build()
                )
                .rockPaperScissors(rockPaperScissors)
                .gamePhase(GamePhase.MOVE)
                .build()
        );

        sendMessage(RockPaperScissorsMetadata.<Localization.Command.Rockpaperscissors>builder()
                .base(EventMetadata.<Localization.Command.Rockpaperscissors>builder()
                        .uuid(metadataUUID)
                        .sender(fPlayer)
                        .filterPlayer(fReceiver, false)
                        .format(s -> StringUtils.replaceEach(
                                s.formatMove(),
                                new String[]{"<target>", "<uuid>"},
                                new String[]{fPlayer.name(), rockPaperScissors.getId().toString()}
                        ))
                        .build()
                )
                .rockPaperScissors(rockPaperScissors)
                .gamePhase(GamePhase.MOVE)
                .build()
        );
    }

    public void create(UUID id, FEntity fPlayer, UUID receiver) {
        if (isModuleDisabledFor(fPlayer)) return;

        RockPaperScissors rockPaperScissors = new RockPaperScissors(id, fPlayer.uuid(), receiver);
        gameMap.put(id, rockPaperScissors);
    }

    public enum GamePhase {
        CREATE,
        MOVE,
        END
    }
}
