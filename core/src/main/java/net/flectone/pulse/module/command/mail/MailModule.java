package net.flectone.pulse.module.command.mail;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.configuration.Command;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Mail;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.tell.TellModule;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.registry.CommandRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.DisableAction;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.meta.CommandMeta;

import java.util.List;

@Singleton
public class MailModule extends AbstractModuleCommand<Localization.Command.Mail> {

    private final Command.Mail command;
    private final Permission.Command.Mail permission;

    private final TellModule tellModule;
    private final IntegrationModule integrationModule;
    private final FPlayerService fPlayerService;
    private final CommandRegistry commandRegistry;

    @Inject
    public MailModule(FileManager fileManager,
                      TellModule tellModule,
                      IntegrationModule integrationModule,
                      FPlayerService fPlayerService,
                      CommandRegistry commandRegistry) {
        super(localization -> localization.getCommand().getMail(), fPlayer -> fPlayer.isSetting(FPlayer.Setting.MAIL));

        this.tellModule = tellModule;
        this.integrationModule = integrationModule;
        this.fPlayerService = fPlayerService;
        this.commandRegistry = commandRegistry;

        command = fileManager.getCommand().getMail();
        permission = fileManager.getPermission().getCommand().getMail();
    }

    @Override
    protected boolean isConfigEnable() {
        return command.isEnable();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        String commandName = getName(command);
        String promptPlayer = getPrompt().getPlayer();
        String promptMessage = getPrompt().getMessage();
        commandRegistry.registerCommand(manager ->
                manager.commandBuilder(commandName, command.getAliases(), CommandMeta.empty())
                        .permission(permission.getName())
                        .required(promptPlayer, commandRegistry.playerParser(true))
                        .required(promptMessage, commandRegistry.nativeMessageParser())
                        .handler(this)
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkCooldown(fPlayer)) return;
        if (checkDisable(fPlayer, fPlayer, DisableAction.YOU)) return;
        if (checkMute(fPlayer)) return;
        if (checkModulePredicates(fPlayer)) return;

        String promptPlayer = getPrompt().getPlayer();
        String playerName = commandContext.get(promptPlayer);

        FPlayer fReceiver = fPlayerService.getFPlayer(playerName);
        if (fReceiver.isUnknown()) {
            builder(fPlayer)
                    .format(Localization.Command.Mail::getNullPlayer)
                    .sendBuilt();
            return;
        }

        if (fReceiver.isOnline() && !integrationModule.isVanished(fReceiver)) {
            if (!tellModule.isEnable()) return;
            tellModule.execute(fPlayer, commandContext);
            return;
        }

        fPlayerService.loadIgnores(fReceiver);

        if (checkIgnore(fPlayer, fReceiver)) return;
        if (checkDisable(fPlayer, fReceiver, DisableAction.HE)) return;

        String promptMessage = getPrompt().getMessage();
        String message = commandContext.get(promptMessage);

        Mail mail = fPlayerService.saveAndGetMail(fPlayer, fReceiver, message);
        if (mail == null) return;

        builder(fReceiver)
                .destination(command.getDestination())
                .receiver(fPlayer)
                .format(s -> s.getSender().replaceFirst("<id>", String.valueOf(mail.id())))
                .message(message)
                .sendBuilt();
    }

    @Async
    public void send(FPlayer fReceiver) {
        if (checkModulePredicates(fReceiver)) return;

        List<Mail> mails = fPlayerService.getReceiverMails(fReceiver);
        if (mails.isEmpty()) return;

        for (Mail mail : mails) {
            FPlayer fPlayer = fPlayerService.getFPlayer(mail.sender());

            builder(fPlayer)
                    .receiver(fReceiver)
                    .format((fResolver, s) -> s.getReceiver())
                    .message((fResolver, s) -> mail.message())
                    .sendBuilt();

            fPlayerService.deleteMail(mail);
        }
    }


}
