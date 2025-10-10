package net.flectone.pulse.module.command.mail;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.mail.listener.MailPulseListener;
import net.flectone.pulse.module.command.mail.model.Mail;
import net.flectone.pulse.module.command.mail.model.MailMetadata;
import net.flectone.pulse.module.command.tell.TellModule;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.sender.DisableSender;
import net.flectone.pulse.platform.sender.IgnoreSender;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageType;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MailModule extends AbstractModuleCommand<Localization.Command.Mail> implements PulseListener {

    private final FileResolver fileResolver;
    private final TellModule tellModule;
    private final IntegrationModule integrationModule;
    private final FPlayerService fPlayerService;
    private final CommandParserProvider commandParserProvider;
    private final ListenerRegistry listenerRegistry;
    private final IgnoreSender ignoreSender;
    private final DisableSender disableSender;

    @Override
    public void onEnable() {
        super.onEnable();

        String promptPlayer = addPrompt(0, Localization.Command.Prompt::getPlayer);
        String promptMessage = addPrompt(1, Localization.Command.Prompt::getMessage);
        registerCommand(manager -> manager
                .permission(permission().getName())
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

        fPlayerService.loadIgnoresIfOffline(fReceiver);
        if (ignoreSender.sendIfIgnored(fPlayer, fReceiver)) return;

        fPlayerService.loadSettingsIfOffline(fReceiver);
        if (disableSender.sendIfDisabled(fPlayer, fReceiver, messageType())) return;

        String message = getArgument(commandContext, 1);

        Mail mail = fPlayerService.saveAndGetMail(fPlayer, fReceiver, message);
        if (mail == null) return;

        sendMessage(MailMetadata.<Localization.Command.Mail>builder()
                .sender(fPlayer)
                .format(s -> Strings.CS.replaceOnce(s.getSender(), "<id>", String.valueOf(mail.id())))
                .mail(mail)
                .target(fReceiver)
                .message(message)
                .destination(config().getDestination())
                .sound(getModuleSound())
                .tagResolvers(fResolver -> new TagResolver[]{targetTag(fResolver, fReceiver)})
                .build()
        );
    }

    @Override
    public MessageType messageType() {
        return MessageType.COMMAND_MAIL;
    }

    @Override
    public Command.Mail config() {
        return fileResolver.getCommand().getMail();
    }

    @Override
    public Permission.Command.Mail permission() {
        return fileResolver.getPermission().getCommand().getMail();
    }

    @Override
    public Localization.Command.Mail localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getCommand().getMail();
    }
}
