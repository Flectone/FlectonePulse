package net.flectone.pulse.data.database.dao;

import net.flectone.pulse.data.database.DatabaseImpl;
import net.flectone.pulse.data.database.sql.SQL;
import org.jdbi.v3.core.Handle;

import java.util.function.Consumer;
import java.util.function.Function;

public interface BaseDAO<S extends SQL> {

    DatabaseImpl database();

    Class<? extends S> sqlClass();

    default S getSQL(Handle handle) {
        return handle.attach(sqlClass());
    }

    default void useTransaction(Consumer<S> action) {
        database().getJdbi().useTransaction(handle ->
                action.accept(getSQL(handle))
        );
    }

    default void useCustomTransaction(Consumer<Handle> action) {
        database().getJdbi().useTransaction(action::accept);
    }

    default <R> R inTransaction(Function<S, R> action) {
        return database().getJdbi().inTransaction(handle ->
                action.apply(getSQL(handle))
        );
    }

    default void useHandle(Consumer<S> action) {
        database().getJdbi().useHandle(handle ->
                action.accept(getSQL(handle))
        );
    }

    default <R> R withHandle(Function<S, R> action) {
        return database().getJdbi().withHandle(handle ->
                action.apply(getSQL(handle))
        );
    }
}