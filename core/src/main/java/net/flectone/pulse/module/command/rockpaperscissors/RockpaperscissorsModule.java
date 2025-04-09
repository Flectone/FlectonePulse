package net.flectone.pulse.module.command.rockpaperscissors;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Command;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.sender.ProxySender;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.rockpaperscissors.model.RockPaperScissors;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.registry.CommandRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.DisableAction;
import net.flectone.pulse.util.MessageTag;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.meta.CommandMeta;
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
    private final CommandRegistry commandRegistry;
    private final IntegrationModule integrationModule;

    @Inject
    public RockpaperscissorsModule(FileManager fileManager,
                                   ProxySender proxySender,
                                   FPlayerService fPlayerService,
                                   CommandRegistry commandRegistry,
                                   IntegrationModule integrationModule) {
        super(localization -> localization.getCommand().getRockpaperscissors(), fPlayer -> fPlayer.isSetting(FPlayer.Setting.ROCKPAPERSCISSORS));

        this.proxySender = proxySender;
        this.fPlayerService = fPlayerService;
        this.commandRegistry = commandRegistry;
        this.integrationModule = integrationModule;

        command = fileManager.getCommand().getRockpaperscissors();
        permission = fileManager.getPermission().getCommand().getRockpaperscissors();
    }

    @Override
    protected boolean isConfigEnable() {
        return command.isEnable();
    }

    @Override
    public void reload() {
        gameMap.clear();

        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        String commandName = getName(command);
        String promptPlayer = getPrompt().getPlayer();
        String promptMove = getPrompt().getMove();
        String promptUUID = getPrompt().getId();
        commandRegistry.registerCommand(manager ->
                manager.commandBuilder(commandName, command.getAliases(), CommandMeta.empty())
                        .permission(permission.getName())
                        .required(promptPlayer, commandRegistry.playerParser())
                        .optional(promptMove, commandRegistry.nativeSingleMessageParser())
                        .optional(promptUUID, UUIDParser.uuidParser())
                        .handler(this)
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkModulePredicates(fPlayer)) return;
        if (checkCooldown(fPlayer)) return;
        if (checkDisable(fPlayer, fPlayer, DisableAction.YOU)) return;
        if (checkMute(fPlayer)) return;

        String promptPlayer = getPrompt().getPlayer();
        String player = commandContext.get(promptPlayer);
        FPlayer fReceiver = fPlayerService.getFPlayer(player);
        if (!fReceiver.isOnline() || integrationModule.isVanished(fReceiver)) {
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
        if (checkDisable(fPlayer, fReceiver, DisableAction.HE)) return;

        String promptMove = getPrompt().getMove();
        Optional<String> optionalMove = commandContext.optional(promptMove);
        String move = optionalMove.orElse(null);

        String promptUUID = getPrompt().getId();
        Optional<UUID> optionalUUID = commandContext.optional(promptUUID);
        UUID uuid = optionalUUID.orElse(null);

        if (move != null && uuid != null) {
            finalMove(fPlayer, fReceiver, move, uuid);
            return;
        }

        RockPaperScissors rockPaperScissors = new RockPaperScissors(fPlayer.getUuid(), fReceiver.getUuid());

        proxySender.sendMessage(fPlayer, MessageTag.COMMAND_ROCKPAPERSCISSORS_CREATE, byteArrayDataOutput -> {
            byteArrayDataOutput.writeUTF(rockPaperScissors.getId().toString());
            byteArrayDataOutput.writeUTF(rockPaperScissors.getReceiver().toString());
        });

        create(rockPaperScissors.getId(), fPlayer, fReceiver.getUuid());

        builder(fPlayer)
                .format((fResolver, s) -> s.getFormatMove()
                        .replace("<target>", fReceiver.getName())
                        .replace("<uuid>", rockPaperScissors.getId().toString())
                )
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

            boolean isSent = proxySender.sendMessage(fPlayer, MessageTag.COMMAND_ROCKPAPERSCISSORS_FINAL, byteArrayDataOutput -> {
                byteArrayDataOutput.writeUTF(rockPaperScissors.getId().toString());
                byteArrayDataOutput.writeUTF(move);
            });

            if (isSent) return;

            sendFinalMessage(rockPaperScissors.getId(), fPlayer, move);

            return;
        }

        builder(fReceiver)
                .receiver(fPlayer)
                .format((fResolver, s) -> s.getSender())
                .sendBuilt();

        boolean isSent = proxySender.sendMessage(fPlayer, MessageTag.COMMAND_ROCKPAPERSCISSORS_MOVE, byteArrayDataOutput -> {
            byteArrayDataOutput.writeUTF(rockPaperScissors.getId().toString());
            byteArrayDataOutput.writeUTF(move);
        });

        if (isSent) return;

        move(rockPaperScissors.getId(), fPlayer, move);
    }

    public void sendFinalMessage(UUID id, FPlayer fPlayer, String move) {
        if (checkModulePredicates(fPlayer)) return;

        RockPaperScissors rockPaperScissors = gameMap.get(id);
        if (rockPaperScissors == null) return;

        FPlayer fReceiver = fPlayerService.getFPlayer(rockPaperScissors.getSender());

        gameMap.remove(id);

        String senderMove = rockPaperScissors.getSenderMove();

        boolean isDraw = senderMove.equalsIgnoreCase(move);

        if (isDraw) {
            BiFunction<FPlayer, Localization.Command.Rockpaperscissors, String> message =
                    (p, m) -> m.getFormatDraw().replace("<move>", resolveLocalization(p).getStrategies().get(move));

            builder(fPlayer)
                    .format(message)
                    .sendBuilt();

            builder(fReceiver)
                    .format(message)
                    .sendBuilt();

            return;
        }

        BiFunction<FPlayer, Localization.Command.Rockpaperscissors, String> message = (p, m) -> m.getFormatWin()
                        .replace("<sender_move>", resolveLocalization(p).getStrategies().get(senderMove))
                        .replace("<receiver_move>", resolveLocalization(p).getStrategies().get(move));

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
        if (checkModulePredicates(fPlayer)) return;

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
                .format((fResolver, s) -> s.getFormatMove()
                        .replace("<target>", fPlayer.getName())
                        .replace("<uuid>", rockPaperScissors.getId().toString()))
                .sendBuilt();
    }

    public void create(UUID id, FEntity fPlayer, UUID receiver) {
        if (checkModulePredicates(fPlayer)) return;

        RockPaperScissors rockPaperScissors = new RockPaperScissors(id, fPlayer.getUuid(), receiver);
        gameMap.put(id, rockPaperScissors);
    }
}
