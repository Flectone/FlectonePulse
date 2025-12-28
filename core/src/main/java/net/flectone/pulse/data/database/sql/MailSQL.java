package net.flectone.pulse.data.database.sql;

import net.flectone.pulse.module.command.mail.model.Mail;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

/**
 * SQL interface for mail data operations in FlectonePulse.
 * Defines database queries for managing player mail messages.
 *
 * @author TheFaser
 * @since 0.9.0
 */
public interface MailSQL extends SQL {

    /**
     * Inserts a new mail message.
     *
     * @param date the timestamp when the mail was sent
     * @param senderId the ID of the player who sent the mail
     * @param receiverId the ID of the player who received the mail
     * @param message the mail message content
     * @return the generated mail ID
     */
    @GetGeneratedKeys("id")
    @SqlUpdate("INSERT INTO `fp_mail` (`date`, `sender`, `receiver`, `message`) VALUES (:date, :sender, :receiver, :message)")
    int insert(@Bind("date") long date, @Bind("sender") int senderId, @Bind("receiver") int receiverId, @Bind("message") String message);

    /**
     * Invalidates a mail message.
     *
     * @param id the mail ID
     */
    @SqlUpdate("UPDATE `fp_mail` SET `valid` = false WHERE `id` = :id")
    void invalidate(@Bind("id") int id);

    /**
     * Finds all valid mail messages for a receiver.
     *
     * @param receiverId the ID of the player who received the mail
     * @return list of mail messages
     */
    @SqlQuery("SELECT * FROM `fp_mail` WHERE `receiver` = :receiver AND `valid` = true")
    List<Mail> findByReceiver(@Bind("receiver") int receiverId);

    /**
     * Finds all valid mail messages from a sender.
     *
     * @param senderId the ID of the player who sent the mail
     * @return list of mail messages
     */
    @SqlQuery("SELECT * FROM `fp_mail` WHERE `sender` = :sender AND `valid` = true")
    List<Mail> findBySender(@Bind("sender") int senderId);

}