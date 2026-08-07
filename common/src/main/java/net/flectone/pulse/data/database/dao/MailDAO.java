package net.flectone.pulse.data.database.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.data.database.DatabaseImpl;
import net.flectone.pulse.data.database.sql.mail.MailSQL;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.command.mail.model.Mail;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MailDAO implements BaseDAO<MailSQL> {

    private final DatabaseImpl database;

    @Override
    public DatabaseImpl database() {
        return database;
    }

    @Override
    public Class<? extends MailSQL> sqlClass() {
        return MailSQL.class;
    }

    @NonNull
    public Optional<Mail> insert(@NonNull FPlayer sender, @NonNull FPlayer receiver, @NonNull String message) {
        if (database.isClosed()) return Optional.empty();
        if (sender.isUnknown() || receiver.isUnknown()) return Optional.empty();

        long date = System.currentTimeMillis();
        int id = withHandle(sql -> sql.insert(date, sender.id(), receiver.id(), message));

        return Optional.of(new Mail(id, date, sender.id(), receiver.id(), message));
    }

    public void delete(@NonNull Mail mail) {
        if (database.isClosed()) return;

        useHandle(sql -> sql.invalidate(mail.id()));
    }

    public List<Mail> getReceiver(FPlayer fPlayer) {
        if (database.isClosed()) return List.of();
        if (fPlayer.isUnknown()) return List.of();

        return withHandle(sql -> sql.findByReceiver(fPlayer.id()));
    }

    public List<Mail> getSender(FPlayer fPlayer) {
        if (database.isClosed()) return List.of();
        if (fPlayer.isUnknown()) return List.of();

        return withHandle(sql -> sql.findBySender(fPlayer.id()));
    }

}