package net.flectone.pulse.module.command.mail;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.mail.listener.MailPulseListener;
import net.flectone.pulse.module.command.mail.model.Mail;
import net.flectone.pulse.module.command.mail.model.MailMetadata;
import net.flectone.pulse.module.command.tell.TellModule;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.DisableSource;
import net.flectone.pulse.util.constant.MessageType;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;

@Singleton
public class MailModule extends AbstractModuleCommand<Localization.Command.Mail> implements PulseListener {

    private final Command.Mail command;
    private final Permission.Command.Mail permission;
    private final TellModule tellModule;
    private final IntegrationModule integrationModule;
    private final FPlayerService fPlayerService;
    private final CommandParserProvider commandParserProvider;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public MailModule(FileResolver fileResolver,
                      TellModule tellModule,
                      IntegrationModule integrationModule,
                      FPlayerService fPlayerService,
                      CommandParserProvider commandParserProvider,
                      ListenerRegistry listenerRegistry) {
        super(localization -> localization.getCommand().getMail(), Command::getMail, MessageType.COMMAND_MAIL);

        this.command = fileResolver.getCommand().getMail();
        this.permission = fileResolver.getPermission().getCommand().getMail();
        this.tellModule = tellModule;
        this.integrationModule = integrationModule;
        this.fPlayerService = fPlayerService;
        this.commandParserProvider = commandParserProvider;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        String promptPlayer = addPrompt(0, Localization.Command.Prompt::getPlayer);
        String promptMessage = addPrompt(1, Localization.Command.Prompt::getMessage);
        registerCommand(manager -> manager
                .permission(permission.getName())
                .required(promptPlayer, commandParserProvider.playerParser(true))
                .required(promptMessage, commandParserProvider.nativeMessageParser())
        );

        listenerRegistry.register(MailPulseListener.class);
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        String playerName = getArgument(commandContext, 0);
        FPlayer fReceiver = fPlayerService.getFPlayer(playerName);
        if (fReceiver.isUnknown()) {
            sendErrorMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Mail::getNullPlayer)
                    .build()
            );

            return;
        }

        if (fReceiver.isOnline() && integrationModule.canSeeVanished(fReceiver, fPlayer)) {
            if (!tellModule.isEnable()) {
                sendErrorMessage(metadataBuilder()
                        .sender(fPlayer)
                        .format(Localization.Command.Mail::getOnlinePlayer)
                        .build()
                );

                return;
            }

            tellModule.execute(fPlayer, commandContext);
            return;
        }

        fPlayerService.loadIgnores(fReceiver);

        if (checkIgnore(fPlayer, fReceiver)) return;
        if (checkDisable(fPlayer, fReceiver, DisableSource.HE)) return;

        String message = getArgument(commandContext, 1);

        Mail mail = fPlayerService.saveAndGetMail(fPlayer, fReceiver, message);
        if (mail == null) return;

        sendMessage(MailMetadata.<Localization.Command.Mail>builder()
                .sender(fReceiver)
                .filterPlayer(fPlayer)
                .format(s -> Strings.CS.replaceOnce(s.getSender(), "<id>", String.valueOf(mail.id())))
                .mail(mail)
                .message(message)
                .destination(command.getDestination())
                .sound(getModuleSound())
                .build()
        );
    }
}
