package net.flectone.pulse.module.command.mail.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Mail;
import net.flectone.pulse.model.event.player.PlayerLoadEvent;
import net.flectone.pulse.module.command.mail.MailModule;
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
    public void onPlayerLoadEvent(PlayerLoadEvent event) {
        FPlayer fReceiver = event.getPlayer();
        if (mailModule.checkModulePredicates(fReceiver)) return;

        List<Mail> mails = fPlayerService.getReceiverMails(fReceiver);
        if (mails.isEmpty()) return;

        for (Mail mail : mails) {
            FPlayer fPlayer = fPlayerService.getFPlayer(mail.sender());

            mailModule.builder(fPlayer)
                    .receiver(fReceiver)
                    .format((fResolver, s) -> s.getReceiver())
                    .message((fResolver, s) -> mail.message())
                    .sendBuilt();

            fPlayerService.deleteMail(mail);
        }
    }

}
