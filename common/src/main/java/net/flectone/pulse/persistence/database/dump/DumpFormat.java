package net.flectone.pulse.persistence.database.dump;

final class DumpFormat {

    static final int VERSION = 1;

    static final String FORMAT = "format";
    static final String CREATED_AT = "createdAt";
    static final String SOURCE = "source";
    static final String PROJECT_VERSION = "projectVersion";
    static final String TABLES = "tables";
    static final String COLUMNS = "columns";
    static final String ROWS = "rows";

    private DumpFormat() {
    }

}
