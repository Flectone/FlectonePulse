package net.flectone.pulse.module.command.rockpaperscissors;

import lombok.Getter;
import net.flectone.pulse.database.dao.FPlayerDAO;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.connector.ProxyConnector;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.rockpaperscissors.model.RockPaperScissors;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.util.DisableAction;
import net.flectone.pulse.util.MessageTag;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

public abstract class RockpaperscissorsModule extends AbstractModuleCommand<Localization.Command.Rockpaperscissors> {

    private final Map<UUID, RockPaperScissors> gameMap = new HashMap<>();

    @Getter private final Command.Rockpaperscissors command;
    @Getter private final Permission.Command.Rockpaperscissors permission;

    private final ProxyConnector proxyConnector;
    private final FPlayerDAO fPlayerDAO;
    private final CommandUtil commandUtil;
    private final IntegrationModule integrationModule;

    public RockpaperscissorsModule(FileManager fileManager,
                                   ProxyConnector proxyConnector,
                                   FPlayerDAO fPlayerDAO,
                                   CommandUtil commandUtil,
                                   IntegrationModule integrationModule) {
        super(localization -> localization.getCommand().getRockpaperscissors(), fPlayer -> fPlayer.is(FPlayer.Setting.ROCKPAPERSCISSORS));

        this.proxyConnector = proxyConnector;
        this.fPlayerDAO = fPlayerDAO;
        this.commandUtil = commandUtil;
        this.integrationModule = integrationModule;

        command = fileManager.getCommand().getRockpaperscissors();
        permission = fileManager.getPermission().getCommand().getRockpaperscissors();
    }

    @Override
    public void onCommand(FPlayer fPlayer, Object arguments) {
        if (checkModulePredicates(fPlayer)) return;
        if (checkCooldown(fPlayer)) return;
        if (checkDisable(fPlayer, fPlayer, DisableAction.YOU)) return;
        if (checkMute(fPlayer)) return;

        String player = commandUtil.getString(0, arguments);
        FPlayer fReceiver = fPlayerDAO.getFPlayer(player);
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

        if (checkDisable(fPlayer, fReceiver, DisableAction.HE)) {
            return;
        }

        String move = commandUtil.getString(1, arguments);
        UUID uuid = commandUtil.getByClassOrDefault(2, UUID.class, UUID.randomUUID(), arguments);

        if (move != null && uuid != null) {
            var strategy = command.getStrategies().get(move);

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

                boolean isSent = proxyConnector.sendMessage(fPlayer, MessageTag.COMMAND_ROCKPAPERSCISSORS_FINAL, byteArrayDataOutput -> {
                    byteArrayDataOutput.writeUTF(rockPaperScissors.getId().toString());
                    byteArrayDataOutput.writeUTF(move);
                });

                if (isSent) return;

                finalMove(rockPaperScissors.getId(), fPlayer, move);

                return;
            }

            builder(fReceiver)
                    .format((fResolver, s) -> s.getSender())
                    .sendBuilt();

            boolean isSent = proxyConnector.sendMessage(fPlayer, MessageTag.COMMAND_ROCKPAPERSCISSORS_MOVE, byteArrayDataOutput -> {
                byteArrayDataOutput.writeUTF(rockPaperScissors.getId().toString());
                byteArrayDataOutput.writeUTF(move);
            });

            if (isSent) return;

            move(rockPaperScissors.getId(), fPlayer, move);

            return;
        }

        RockPaperScissors rockPaperScissors = new RockPaperScissors(fPlayer.getUuid(), fReceiver.getUuid());

        proxyConnector.sendMessage(fPlayer, MessageTag.COMMAND_ROCKPAPERSCISSORS_CREATE, byteArrayDataOutput -> {
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

    public void finalMove(UUID id, FPlayer fPlayer, String move) {
        if (checkModulePredicates(fPlayer)) return;

        RockPaperScissors rockPaperScissors = gameMap.get(id);
        if (rockPaperScissors == null) return;

        FPlayer fReceiver = fPlayerDAO.getFPlayer(rockPaperScissors.getSender());

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

        FPlayer fReceiver = fPlayerDAO.getFPlayer(rockPaperScissors.getReceiver());

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

    @Override
    public void reload() {
        gameMap.clear();

        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        getCommand().getAliases().forEach(commandUtil::unregister);

        createCommand();
    }

    @Override
    public boolean isConfigEnable() {
        return command.isEnable();
    }
}
