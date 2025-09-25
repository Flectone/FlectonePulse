package net.flectone.pulse.module.command.mail.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.player.PlayerJoinEvent;
import net.flectone.pulse.module.command.mail.model.Mail;
import net.flectone.pulse.module.command.mail.MailModule;
import net.flectone.pulse.module.command.mail.model.MailMetadata;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;

import java.util.List;

@Singleton
public class MailPulseListener implements PulseListener {

    private final MailModule mailModule;
    private final FPlayerService fPlayerService;

    @Inject
    public MailPulseListener(MailModule mailModule,
                             FPlayerService fPlayerService) {
        this.mailModule = mailModule;
        this.fPlayerService = fPlayerService;
    }

    @Pulse
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        FPlayer fReceiver = event.getPlayer();
        if (mailModule.isModuleDisabledFor(fReceiver)) return;

        List<Mail> mails = fPlayerService.getReceiverMails(fReceiver);
        if (mails.isEmpty()) return;

        for (Mail mail : mails) {
            FPlayer fPlayer = fPlayerService.getFPlayer(mail.sender());

            mailModule.sendMessage(MailMetadata.<Localization.Command.Mail>builder()
                    .sender(fPlayer)
                    .filterPlayer(fReceiver, false)
                    .format(Localization.Command.Mail::getReceiver)
                    .destination(mailModule.config().getDestination())
                    .mail(mail)
                    .target(fReceiver)
                    .message(mail.message())
                    .build()
            );

            fPlayerService.deleteMail(mail);
        }
    }

}
