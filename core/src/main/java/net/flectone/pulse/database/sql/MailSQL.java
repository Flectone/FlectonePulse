package net.flectone.pulse.database.sql;

import net.flectone.pulse.model.Mail;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

public interface MailSQL extends SQL {

    @SqlUpdate("INSERT INTO `mail` (`date`, `sender`, `receiver`, `message`) VALUES (:date, :sender, :receiver, :message)")
    @GetGeneratedKeys("id")
    int insert(@Bind("date") long date, @Bind("sender") int senderId, @Bind("receiver") int receiverId, @Bind("message") String message);

    @SqlUpdate("DELETE FROM `mail` WHERE `id` = :id")
    void delete(@Bind("id") int id);

    @SqlQuery("SELECT * FROM `mail` WHERE `receiver` = :receiver")
    List<Mail> findByReceiver(@Bind("receiver") int receiverId);

    @SqlQuery("SELECT * FROM `mail` WHERE `sender` = :sender")
    List<Mail> findBySender(@Bind("sender") int senderId);

}
