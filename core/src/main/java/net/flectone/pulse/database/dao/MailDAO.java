package net.flectone.pulse.database.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.database.sql.MailSQL;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Mail;
import org.jdbi.v3.core.Handle;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

@Singleton
public class MailDAO extends BaseDAO<MailSQL> {

    @Inject
    public MailDAO(Database database) {
        super(database, MailSQL.class);
    }

    @Override
    public MailSQL getSQL(Handle handle) {
        return handle.attach(MailSQL.class);
    }

    @Nullable
    public Mail insert(FPlayer sender, FPlayer receiver, String message) {
        if (sender.isUnknown() || receiver.isUnknown()) return null;

        return inTransaction(mailSQL -> {
            long date = System.currentTimeMillis();
            int id = mailSQL.insert(date, sender.getId(), receiver.getId(), message);
            return new Mail(id, date, sender.getId(), receiver.getId(), message);
        });
    }

    public void delete(Mail mail) {
        useHandle(sql -> sql.delete(mail.id()));
    }

    public List<Mail> getReceiver(FPlayer fPlayer) {
        if (fPlayer.isUnknown()) return Collections.emptyList();

        return withHandle(sql -> sql.findByReceiver(fPlayer.getId()));
    }

    public List<Mail> getSender(FPlayer fPlayer) {
        if (fPlayer.isUnknown()) return Collections.emptyList();

        return withHandle(sql -> sql.findBySender(fPlayer.getId()));
    }

}