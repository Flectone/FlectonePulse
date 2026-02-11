package net.flectone.pulse.data.database.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.data.database.Database;
import net.flectone.pulse.data.database.sql.MailSQL;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.command.mail.model.Mail;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Data Access Object for mail data in FlectonePulse.
 * Handles mail message creation, retrieval, and deletion.
 *
 * @author TheFaser
 * @since 0.9.0
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MailDAO implements BaseDAO<MailSQL> {

    private final Database database;

    @Override
    public Database database() {
        return database;
    }

    @Override
    public Class<MailSQL> sqlClass() {
        return MailSQL.class;
    }

    /**
     * Inserts a new mail message.
     *
     * @param sender the player who sent the mail
     * @param receiver the player who received the mail
     * @param message the mail message content
     * @return the created mail record, or null if players are unknown
     */
    public @Nullable Mail insert(@NonNull FPlayer sender, @NonNull FPlayer receiver, @NonNull String message) {
        if (sender.isUnknown() || receiver.isUnknown()) return null;

        return inTransaction(mailSQL -> {
            long date = System.currentTimeMillis();
            int id = mailSQL.insert(date, sender.id(), receiver.id(), message);
            return new Mail(id, date, sender.id(), receiver.id(), message);
        });
    }

    /**
     * Deletes a mail message.
     *
     * @param mail the mail record to delete
     */
    public void delete(@NonNull Mail mail) {
        useHandle(sql -> sql.invalidate(mail.id()));
    }

    /**
     * Gets mail messages received by a player.
     *
     * @param fPlayer the player who received the mail
     * @return list of received mail messages, empty list if player is unknown
     */
    public List<Mail> getReceiver(FPlayer fPlayer) {
        if (fPlayer.isUnknown()) return Collections.emptyList();

        return withHandle(sql -> sql.findByReceiver(fPlayer.id()));
    }

    /**
     * Gets mail messages sent by a player.
     *
     * @param fPlayer the player who sent the mail
     * @return list of sent mail messages, empty list if player is unknown
     */
    public List<Mail> getSender(FPlayer fPlayer) {
        if (fPlayer.isUnknown()) return Collections.emptyList();

        return withHandle(sql -> sql.findBySender(fPlayer.id()));
    }

}