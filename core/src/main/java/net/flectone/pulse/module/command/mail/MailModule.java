package net.flectone.pulse.module.command.mail;

import lombok.Getter;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.database.dao.FPlayerDAO;
import net.flectone.pulse.database.dao.MailDAO;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.mail.model.Mail;
import net.flectone.pulse.module.command.tell.TellModule;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.util.DisableAction;

import java.util.List;
import java.util.Optional;

public abstract class MailModule extends AbstractModuleCommand<Localization.Command.Mail> {

    @Getter private final Command.Mail command;
    @Getter private final Permission.Command.Mail permission;

    private final TellModule tellModule;
    private final IntegrationModule integrationModule;
    private final FPlayerDAO fPlayerDAO;
    private final MailDAO mailDAO;
    private final CommandUtil commandUtil;

    public MailModule(FileManager fileManager,
                      TellModule tellModule,
                      IntegrationModule integrationModule,
                      FPlayerDAO fPlayerDAO,
                      MailDAO mailDAO,
                      CommandUtil commandUtil) {
        super(localization -> localization.getCommand().getMail(), fPlayer -> fPlayer.is(FPlayer.Setting.MAIL));

        this.tellModule = tellModule;
        this.integrationModule = integrationModule;
        this.fPlayerDAO = fPlayerDAO;
        this.mailDAO = mailDAO;
        this.commandUtil = commandUtil;

        command = fileManager.getCommand().getMail();
        permission = fileManager.getPermission().getCommand().getMail();
    }

    @Override
    public void onCommand(FPlayer fPlayer, Object arguments) {
        if (checkCooldown(fPlayer)) return;
        if (checkDisable(fPlayer, fPlayer, DisableAction.YOU)) return;
        if (checkMute(fPlayer)) return;
        if (checkModulePredicates(fPlayer)) return;

        String playerName = commandUtil.getString(0, arguments);
        if (playerName == null) return;

        Optional<FPlayer> optionalPlayer = fPlayerDAO.getFPlayers().stream()
                .filter(offlinePlayer -> playerName.equalsIgnoreCase(offlinePlayer.getName()))
                .findAny();

        if (optionalPlayer.isEmpty()) {
            builder(fPlayer)
                    .format(Localization.Command.Mail::getNullPlayer)
                    .sendBuilt();
            return;
        }

        FPlayer fReceiver = fPlayerDAO.getFPlayer(optionalPlayer.get().getUuid());

        if (fReceiver.isOnline() && !integrationModule.isVanished(fReceiver)) {
            if (!tellModule.isEnable()) return;
            tellModule.onCommand(fPlayer, arguments);
            return;
        }

        if (checkDisable(fPlayer, fReceiver, DisableAction.HE)) {
            return;
        }

        String string = commandUtil.getString(1, arguments);

        Mail mail = mailDAO.insertMail(fPlayer, fReceiver, string);
        if (mail == null) return;

        builder(fReceiver)
                .destination(command.getDestination())
                .receiver(fPlayer)
                .format(s -> s.getSender().replaceFirst("<id>", String.valueOf(mail.id())))
                .message(string)
                .sendBuilt();
    }

    @Async
    public void send(FPlayer fReceiver) {
        if (checkModulePredicates(fReceiver)) return;

        List<Mail> mails = mailDAO.getMails(fReceiver);
        if (mails.isEmpty()) return;

        for (Mail mail : mails) {
            FPlayer fPlayer = fPlayerDAO.getFPlayer(mail.sender());

            builder(fPlayer)
                    .receiver(fReceiver)
                    .format((fResolver, s) -> s.getReceiver())
                    .message((fResolver, s) -> mail.message())
                    .sendBuilt();

            mailDAO.removeMail(mail);
        }
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
