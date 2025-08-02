package net.flectone.pulse.data.database.dao;

import net.flectone.pulse.data.database.Database;
import net.flectone.pulse.data.database.sql.SQL;
import org.jdbi.v3.core.Handle;

import java.util.function.Consumer;
import java.util.function.Function;

public abstract class BaseDAO<S extends SQL> {

    private final Database database;
    private final Class<S> sqlClass;

    protected BaseDAO(Database database, Class<S> sqlClass) {
        this.database = database;
        this.sqlClass = sqlClass;
    }

    public S getSQL(Handle handle) {
        return handle.attach(sqlClass);
    }

    protected void useTransaction(Consumer<S> action) {
        database.getJdbi().useTransaction(handle ->
                action.accept(getSQL(handle))
        );
    }

    protected <R> R inTransaction(Function<S, R> action) {
        return database.getJdbi().inTransaction(handle ->
                action.apply(getSQL(handle))
        );
    }

    protected void useHandle(Consumer<S> action) {
        database.getJdbi().useHandle(handle ->
                action.accept(getSQL(handle))
        );
    }

    protected <R> R withHandle(Function<S, R> action) {
        return database.getJdbi().withHandle(handle ->
                action.apply(getSQL(handle))
        );
    }
}
