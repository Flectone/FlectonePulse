package net.flectone.pulse.data.repository;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.data.database.dao.IgnoreDAO;
import net.flectone.pulse.data.database.dao.MailDAO;
import net.flectone.pulse.data.database.dao.TimeDAO;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.PlayTime;
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
    private final TimeDAO timeDAO;

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

    /**
     * Saves a player's time session when they join the server.
     *
     * @param fPlayer the player whose session is being saved
     */
    public void saveJoinSession(FPlayer fPlayer) {
        timeDAO.saveJoin(fPlayer);
    }

    /**
     * Saves a player's time session when they join the server.
     *
     * @param playTime session to save
     */
    public void saveJoinSession(PlayTime playTime) {
        timeDAO.saveSession(playTime);
    }

    /**
     * Saves a player's AFK time session.
     *
     * @param fPlayer the player whose AFK status is being updated
     * @param afk true if the player is going AFK, false if returning
     */
    public void saveAfkSession(FPlayer fPlayer, boolean afk) {
        timeDAO.saveAfk(fPlayer, afk);
    }

    /**
     * Saves a player's last seen timestamp when they quit the server.
     *
     * @param fPlayer the player whose last seen time is being saved
     */
    public void saveLastSeen(FPlayer fPlayer) {
        timeDAO.saveQuit(fPlayer);
    }

    /**
     * Gets the play time statistics for a specific player.
     *
     * @param fPlayer the player to get play time for
     * @return the player's play time statistics, or null if not found
     */
    public @Nullable PlayTime getPlayTime(FPlayer fPlayer) {
        return timeDAO.getByPlayer(fPlayer).orElse(null);
    }

    /**
     * Gets the total count of all play time records in the database.
     *
     * @return the total number of play time records
     */
    public int getPlayTimesCount() {
        return timeDAO.getTotalCount();
    }

    /**
     * Gets a paginated list of all play time records.
     *
     * @param limit the maximum number of records to retrieve
     * @param offset the number of records to skip before starting to return results
     * @return list of play time records within the specified range
     */
    public List<PlayTime> getAllPlayTimes(int limit, int offset) {
        return timeDAO.getAllPlayTimes(limit, offset);
    }

}