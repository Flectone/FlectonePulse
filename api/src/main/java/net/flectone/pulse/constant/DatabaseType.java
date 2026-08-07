package net.flectone.pulse.constant;

/**
 * Storage backend FlectonePulse persists its data to.
 *
 * <p>Declared here rather than nested in the database layer, which lives in the common module
 * because JDBI and the JDBC drivers are downloaded at runtime.</p>
 */
public enum DatabaseType {

    POSTGRESQL,
    H2,
    SQLITE,
    MYSQL,
    MARIADB

}
