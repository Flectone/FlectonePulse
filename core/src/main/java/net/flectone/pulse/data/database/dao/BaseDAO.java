package net.flectone.pulse.data.database.dao;

import net.flectone.pulse.data.database.Database;
import net.flectone.pulse.data.database.sql.SQL;
import org.jdbi.v3.core.Handle;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Base Data Access Object for database operations in FlectonePulse.
 * Provides common methods for transaction and handle management.
 *
 * @author TheFaser
 * @since 0.9.0
 */
public interface BaseDAO<S extends SQL> {

    /**
     * Gets the database instance associated with this DAO.
     *
     * @return the database instance
     */
    Database database();

    /**
     * Gets the SQL interface class for this DAO.
     *
     * @return the SQL interface class
     */
    Class<S> sqlClass();

    /**
     * Attaches the SQL interface to the given handle.
     *
     * @param handle the database handle
     * @return the attached SQL interface
     */
    default S getSQL(Handle handle) {
        return handle.attach(sqlClass());
    }

    /**
     * Executes an action within a transaction.
     *
     * @param action the action to execute
     */
    default void useTransaction(Consumer<S> action) {
        database().getJdbi().useTransaction(handle ->
                action.accept(getSQL(handle))
        );
    }

    /**
     * Executes a function within a transaction and returns the result.
     *
     * @param action the function to execute
     * @param <R> the return type
     * @return the result of the function
     */
    default <R> R inTransaction(Function<S, R> action) {
        return database().getJdbi().inTransaction(handle ->
                action.apply(getSQL(handle))
        );
    }

    /**
     * Executes an action using a database handle.
     *
     * @param action the action to execute
     */
    default void useHandle(Consumer<S> action) {
        database().getJdbi().useHandle(handle ->
                action.accept(getSQL(handle))
        );
    }

    /**
     * Executes a function using a database handle and returns the result.
     *
     * @param action the function to execute
     * @param <R> the return type
     * @return the result of the function
     */
    default <R> R withHandle(Function<S, R> action) {
        return database().getJdbi().withHandle(handle ->
                action.apply(getSQL(handle))
        );
    }
}