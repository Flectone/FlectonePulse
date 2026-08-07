package net.flectone.pulse.persistence.database;

import net.flectone.pulse.config.Config;

import java.io.IOException;

/**
 * FlectonePulse's connection to its storage backend.
 *
 */
public interface Database {

    /**
     * @return the database section of the configuration
     */
    Config.Database config();

    /**
     * Opens the connection pool and migrates the schema if needed.
     *
     * @throws IOException if the connection or migration fails
     */
    void connect() throws IOException;

    /**
     * Creates the tables if they are missing.
     */
    void init();

    /**
     * Closes the connection pool.
     */
    void disconnect();

    /**
     * @return true if the pool is closed or was never opened
     */
    boolean isClosed();

}
