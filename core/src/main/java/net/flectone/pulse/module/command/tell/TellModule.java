package net.flectone.pulse.module.command.tell;

import lombok.Getter;
import net.flectone.pulse.database.dao.FPlayerDAO;
import net.flectone.pulse.database.dao.IgnoreDAO;
import net.flectone.pulse.file.Command;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.manager.ProxyManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.util.DisableAction;
import net.flectone.pulse.util.MessageTag;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;

@Getter
public abstract class TellModule extends AbstractModuleCommand<Localization.Command.Tell> {

    private final HashMap<UUID, String> senderReceiverMap = new HashMap<>();

    @Getter private final Command.Tell command;
    @Getter private final Permission.Command.Tell permission;

    private final FPlayerDAO fPlayerDAO;
    private final IgnoreDAO ignoreDAO;
    private final FPlayerManager fPlayerManager;
    private final ProxyManager proxyManager;
    private final IntegrationModule integrationModule;
    private final CommandUtil commandUtil;

    public TellModule(FileManager fileManager,
                      FPlayerDAO fPlayerDAO,
                      IgnoreDAO ignoreDAO,
                      FPlayerManager fPlayerManager,
                      ProxyManager proxyManager,
                      IntegrationModule integrationModule,
                      CommandUtil commandUtil) {
        super(localization -> localization.getCommand().getTell(), fPlayer -> fPlayer.is(FPlayer.Setting.TELL));

        this.fPlayerDAO = fPlayerDAO;
        this.ignoreDAO = ignoreDAO;
        this.fPlayerManager = fPlayerManager;
        this.proxyManager = proxyManager;
        this.integrationModule = integrationModule;
        this.commandUtil = commandUtil;

        command = fileManager.getCommand().getTell();
        permission = fileManager.getPermission().getCommand().getTell();
    }

    @Override
    public void onCommand(FPlayer fPlayer, Object arguments) {
        if (checkModulePredicates(fPlayer)) return;
        if (checkCooldown(fPlayer)) return;
        if (checkDisable(fPlayer, fPlayer, DisableAction.YOU)) return;
        if (checkMute(fPlayer)) return;

        String playerName = commandUtil.getString(0, arguments);
        String message = commandUtil.getString(1, arguments);

        send(fPlayer, playerName, message);
    }

    public void send(FPlayer fPlayer, String playerName, String message) {
        if (checkModulePredicates(fPlayer)) return;

        if (fPlayer.getName().equalsIgnoreCase(playerName)) {
            builder(fPlayer)
                    .format(Localization.Command.Tell::getMyself)
                    .message(message)
                    .sendBuilt();
            return;
        }

        Optional<FPlayer> optionalFReceiver = fPlayerDAO.getOnlineFPlayers().stream()
                .filter(filterPlayer -> filterPlayer.getName().equalsIgnoreCase(playerName))
                .findAny();

        if (optionalFReceiver.isEmpty()) {
            builder(fPlayer)
                    .format(Localization.Command.Tell::getNullPlayer)
                    .sendBuilt();
            return;
        }

        FPlayer fReceiver = fPlayerDAO.getFPlayer(optionalFReceiver.get().getId());

        ignoreDAO.setIgnores(fReceiver);

        if (checkIgnore(fPlayer, fReceiver)) return;
        if (checkDisable(fPlayer, fReceiver, DisableAction.HE)) return;

        String receiverUUID = fReceiver.getUuid().toString();

        boolean isSent = proxyManager.sendMessage(fPlayer, MessageTag.COMMAND_TELL, byteArrayDataOutput -> {
            byteArrayDataOutput.writeUTF(receiverUUID);
            byteArrayDataOutput.writeUTF(message);

            send(fReceiver, fPlayer, (fResolver, s) -> s.getSender(), message);
        });

        if (isSent) return;

        FPlayer fNewReceiver = fPlayerManager.get(fReceiver.getUuid());
        if (integrationModule.isVanished(fNewReceiver)) {
            builder(fPlayer)
                    .format(Localization.Command.Tell::getNullPlayer)
                    .sendBuilt();
            return;
        }

        send(fPlayer, fNewReceiver, (fResolver, s) -> s.getReceiver(), message);
        send(fNewReceiver, fPlayer, (fResolver, s) -> s.getSender(), message);
    }

    public void send(FEntity fPlayer, FPlayer fReceiver, BiFunction<FPlayer, Localization.Command.Tell, String> format, String string) {
        builder(fPlayer)
                .destination(command.getDestination())
                .receiver(fReceiver)
                .format(format)
                .message(string)
                .sound(getSound())
                .sendBuilt();

        senderReceiverMap.put(fReceiver.getUuid(), fPlayer.getName());
    }

    @Override
    public void reload() {
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