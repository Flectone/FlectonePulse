package net.flectone.pulse.module.command.mail;

import lombok.Getter;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.file.Command;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.manager.ThreadManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.mail.model.Mail;
import net.flectone.pulse.module.command.tell.TellModule;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.util.DisableAction;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public abstract class MailModule extends AbstractModuleCommand<Localization.Command.Mail> {

    @Getter
    private final Command.Mail command;
    @Getter
    private final Permission.Command.Mail permission;

    private final TellModule tellModule;
    private final ThreadManager threadManager;
    private final IntegrationModule integrationModule;
    private final CommandUtil commandUtil;

    public MailModule(FileManager fileManager,
                      TellModule tellModule,
                      ThreadManager threadManager,
                      IntegrationModule integrationModule,
                      CommandUtil commandUtil) {
        super(localization -> localization.getCommand().getMail(), fPlayer -> fPlayer.is(FPlayer.Setting.MAIL));

        this.tellModule = tellModule;
        this.threadManager = threadManager;
        this.integrationModule = integrationModule;
        this.commandUtil = commandUtil;

        command = fileManager.getCommand().getMail();
        permission = fileManager.getPermission().getCommand().getMail();
    }

    @Override
    public void onCommand(Database database, FPlayer fPlayer, Object arguments) throws SQLException {
        if (checkCooldown(fPlayer)) return;
        if (checkDisable(fPlayer, fPlayer, DisableAction.YOU)) return;
        if (checkMute(fPlayer)) return;
        if (checkModulePredicates(fPlayer)) return;

        String playerName = commandUtil.getString(0, arguments);
        if (playerName == null) return;

        Optional<FPlayer> optionalPlayer = database.getFPlayers().stream()
                .filter(offlinePlayer -> playerName.equalsIgnoreCase(offlinePlayer.getName()))
                .findAny();

        if (optionalPlayer.isEmpty()) {
            builder(fPlayer)
                    .format(Localization.Command.Mail::getNullPlayer)
                    .sendBuilt();
            return;
        }

        FPlayer fReceiver = database.getFPlayer(optionalPlayer.get().getUuid());

        if (fReceiver.isOnline() && !integrationModule.isVanished(fReceiver)) {
            if (!tellModule.isEnable()) return;
            tellModule.onCommand(fPlayer, arguments);
            return;
        }

        if (checkDisable(fPlayer, fReceiver, DisableAction.HE)) {
            return;
        }

        String string = commandUtil.getString(1, arguments);

        Mail mail = database.insertMail(fPlayer, fReceiver, string);
        if (mail == null) return;

        builder(fReceiver)
                .receiver(fPlayer)
                .format(s -> s.getSender().replaceFirst("<id>", String.valueOf(mail.id())))
                .message(string)
                .sendBuilt();
    }

    @Async
    public void send(FPlayer fReceiver) {
        if (checkModulePredicates(fReceiver)) return;

        threadManager.runDatabase(database -> {
            List<Mail> mails = database.getMails(fReceiver);
            if (mails.isEmpty()) return;

            for (Mail mail : mails) {
                FPlayer fPlayer = database.getFPlayer(mail.sender());

                builder(fPlayer)
                        .receiver(fReceiver)
                        .format((fResolver, s) -> s.getReceiver())
                        .message((fResolver, s) -> mail.message())
                        .sendBuilt();

                database.removeMail(mail);
            }
        });
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
