package net.flectone.pulse.data.repository;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.data.database.dao.IgnoreDAO;
import net.flectone.pulse.data.database.dao.MailDAO;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.command.ignore.model.Ignore;
import net.flectone.pulse.module.command.mail.model.Mail;

import java.util.List;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SocialRepository {

    private final IgnoreDAO ignoreDAO;
    private final MailDAO mailDAO;

    public void loadIgnores(FPlayer fPlayer) {
        ignoreDAO.load(fPlayer);
    }

    public List<Mail> getReceiverMails(FPlayer fPlayer) {
        return mailDAO.getReceiver(fPlayer);
    }

    public List<Mail> getSenderMails(FPlayer fPlayer) {
        return mailDAO.getSender(fPlayer);
    }

    public Ignore saveAndGetIgnore(FPlayer fPlayer, FPlayer fTarget) {
        return ignoreDAO.insert(fPlayer, fTarget);
    }

    public Mail saveAndGetMail(FPlayer fPlayer, FPlayer fTarget, String message) {
        return mailDAO.insert(fPlayer, fTarget, message);
    }

    public void deleteIgnore(Ignore ignore) {
        ignoreDAO.invalidate(ignore);
    }

    public void deleteMail(Mail mail) {
        mailDAO.delete(mail);
    }
}
