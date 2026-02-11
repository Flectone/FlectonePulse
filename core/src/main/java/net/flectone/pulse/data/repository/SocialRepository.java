package net.flectone.pulse.data.repository;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.data.database.dao.IgnoreDAO;
import net.flectone.pulse.data.database.dao.MailDAO;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.command.ignore.model.Ignore;
import net.flectone.pulse.module.command.mail.model.Mail;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Repository for managing social interactions in FlectonePulse.
 * Handles ignore relationships and mail messages between players.
 *
 * @author TheFaser
 * @since 0.8.1
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SocialRepository {

    private final IgnoreDAO ignoreDAO;
    private final MailDAO mailDAO;

    /**
     * Loads ignore relationships for a player.
     *
     * @param fPlayer the player to load ignores for
     * @return new FPlayer with ignores
     */
    public FPlayer loadIgnores(FPlayer fPlayer) {
        return ignoreDAO.load(fPlayer);
    }

    /**
     * Gets mail messages received by a player.
     *
     * @param fPlayer the player who received the mail
     * @return list of received mail messages
     */
    public List<Mail> getReceiverMails(FPlayer fPlayer) {
        return mailDAO.getReceiver(fPlayer);
    }

    /**
     * Gets mail messages sent by a player.
     *
     * @param fPlayer the player who sent the mail
     * @return list of sent mail messages
     */
    public List<Mail> getSenderMails(FPlayer fPlayer) {
        return mailDAO.getSender(fPlayer);
    }

    /**
     * Saves an ignore relationship and returns the created record.
     *
     * @param fPlayer the player who is ignoring
     * @param fTarget the player being ignored
     * @return the created ignore record, or null if players are unknown
     */
    public @Nullable Ignore saveAndGetIgnore(FPlayer fPlayer, FPlayer fTarget) {
        return ignoreDAO.insert(fPlayer, fTarget);
    }

    /**
     * Saves a mail message and returns the created record.
     *
     * @param fPlayer the player who sent the mail
     * @param fTarget the player who received the mail
     * @param message the mail message content
     * @return the created mail record, or null if players are unknown
     */
    public @Nullable Mail saveAndGetMail(FPlayer fPlayer, FPlayer fTarget, String message) {
        return mailDAO.insert(fPlayer, fTarget, message);
    }

    /**
     * Deletes an ignore relationship.
     *
     * @param ignore the ignore record to delete
     */
    public void deleteIgnore(Ignore ignore) {
        ignoreDAO.invalidate(ignore);
    }

    /**
     * Deletes a mail message.
     *
     * @param mail the mail record to delete
     */
    public void deleteMail(Mail mail) {
        mailDAO.delete(mail);
    }

}