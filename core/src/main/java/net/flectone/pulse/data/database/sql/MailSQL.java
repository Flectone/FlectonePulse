package net.flectone.pulse.data.database.sql;

import net.flectone.pulse.module.command.mail.model.Mail;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

public interface MailSQL extends SQL {

    @GetGeneratedKeys("id")
    @SqlUpdate("INSERT INTO `mail` (`date`, `sender`, `receiver`, `message`) VALUES (:date, :sender, :receiver, :message)")
    int insert(@Bind("date") long date, @Bind("sender") int senderId, @Bind("receiver") int receiverId, @Bind("message") String message);

    @SqlUpdate("UPDATE `mail` SET `valid` = false WHERE `id` = :id")
    void invalidate(@Bind("id") int id);

    @SqlQuery("SELECT * FROM `mail` WHERE `receiver` = :receiver AND `valid` = true")
    List<Mail> findByReceiver(@Bind("receiver") int receiverId);

    @SqlQuery("SELECT * FROM `mail` WHERE `sender` = :sender AND `valid` = true")
    List<Mail> findBySender(@Bind("sender") int senderId);

}
