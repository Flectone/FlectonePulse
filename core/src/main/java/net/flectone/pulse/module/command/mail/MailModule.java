package net.flectone.pulse.module.command.mail;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Command;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.constant.DisableSource;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Mail;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.mail.listener.MailPulseListener;
import net.flectone.pulse.module.command.tell.TellModule;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.registry.CommandRegistry;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.meta.CommandMeta;

@Singleton
public class MailModule extends AbstractModuleCommand<Localization.Command.Mail> implements PulseListener {

    private final Command.Mail command;
    private final Permission.Command.Mail permission;
    private final TellModule tellModule;
    private final IntegrationModule integrationModule;
    private final FPlayerService fPlayerService;
    private final CommandRegistry commandRegistry;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public MailModule(FileResolver fileResolver,
                      TellModule tellModule,
                      IntegrationModule integrationModule,
                      FPlayerService fPlayerService,
                      CommandRegistry commandRegistry,
                      ListenerRegistry listenerRegistry) {
        super(localization -> localization.getCommand().getMail(), fPlayer -> fPlayer.isSetting(FPlayer.Setting.MAIL));

        this.command = fileResolver.getCommand().getMail();
        this.permission = fileResolver.getPermission().getCommand().getMail();
        this.tellModule = tellModule;
        this.integrationModule = integrationModule;
        this.fPlayerService = fPlayerService;
        this.commandRegistry = commandRegistry;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    protected boolean isConfigEnable() {
        return command.isEnable();
    }

    @Override
    public void onEnable() {
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

        listenerRegistry.register(MailPulseListener.class);
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkCooldown(fPlayer)) return;
        if (checkDisable(fPlayer, fPlayer, DisableSource.YOU)) return;
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

        if (fReceiver.isOnline() && integrationModule.isVanishedVisible(fReceiver, fPlayer)) {
            if (!tellModule.isEnable()) {
                builder(fPlayer)
                        .format(Localization.Command.Mail::getOnlinePlayer)
                        .sendBuilt();
                return;
            }

            tellModule.execute(fPlayer, commandContext);
            return;
        }

        fPlayerService.loadIgnores(fReceiver);

        if (checkIgnore(fPlayer, fReceiver)) return;
        if (checkDisable(fPlayer, fReceiver, DisableSource.HE)) return;

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
}
