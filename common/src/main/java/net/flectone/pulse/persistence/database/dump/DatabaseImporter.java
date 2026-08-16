package net.flectone.pulse.persistence.database.dump;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.exception.DatabaseDumpException;
import net.flectone.pulse.logging.FLogger;
import net.flectone.pulse.persistence.database.DatabaseImpl;
import net.flectone.pulse.util.backup.BackupCreator;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.jdbi.v3.core.statement.Query;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DatabaseImporter {

    private static final int BATCH_SIZE = 1000;
    private static final String ID_COLUMN = "id";

    private final DatabaseImpl database;
    private final DumpValueConverter valueConverter;
    private final BackupCreator backupCreator;
    private final @Named("defaultMapper") ObjectMapper objectMapper;
    private final FLogger fLogger;

    public long load(Path dumpFile, @Nullable String preInitVersion) {
        backupCreator.backup(preInitVersion == null ? BuildConfig.PROJECT_VERSION : preInitVersion, database.config());

        long[] inserted = {0};

        try (InputStream inputStream = Files.newInputStream(dumpFile);
             JsonParser parser = objectMapper.createParser(inputStream)) {
            database.getJdbi().useTransaction(handle -> {
                clear(handle);

                inserted[0] = read(parser, handle);
            });
        } catch (IOException e) {
            throw new DatabaseDumpException("Failed to read the dump " + dumpFile, e);
        }

        fLogger.info("Database imported from %s, %s rows", dumpFile, inserted[0]);

        return inserted[0];
    }

    private void clear(Handle handle) {
        DumpTable[] tables = DumpTable.values();

        // children first, so nothing is left pointing at a row that is already gone
        for (int index = tables.length - 1; index >= 0; index--) {
            handle.execute("DELETE FROM `" + tables[index].tableName() + "`");
        }
    }

    private long read(JsonParser parser, Handle handle) {
        if (parser.nextToken() != JsonToken.START_OBJECT) {
            throw new DatabaseDumpException("The dump does not start with a json object");
        }

        Map<DumpTable, Long> identities = new LinkedHashMap<>();
        long inserted = 0;

        while (parser.nextToken() == JsonToken.PROPERTY_NAME) {
            String property = parser.currentName();
            parser.nextToken();

            switch (property) {
                case DumpFormat.FORMAT -> checkFormat(parser.getIntValue());
                case DumpFormat.SOURCE -> fLogger.info("Database dump was written by %s", parser.getString());
                case DumpFormat.TABLES -> inserted = readTables(parser, handle, identities);
                default -> parser.skipChildren();
            }
        }

        identities.forEach((table, nextId) -> restartIdentity(handle, table, nextId));

        return inserted;
    }

    private void checkFormat(int format) {
        if (format > DumpFormat.VERSION) {
            throw new DatabaseDumpException("The dump is in format " + format + ", this version of FlectonePulse reads up to " + DumpFormat.VERSION);
        }
    }

    private long readTables(JsonParser parser, Handle handle, Map<DumpTable, Long> identities) {
        long inserted = 0;

        while (parser.nextToken() == JsonToken.PROPERTY_NAME) {
            String tableName = parser.currentName();
            parser.nextToken();

            Optional<DumpTable> table = Arrays.stream(DumpTable.values())
                    .filter(dumpTable -> dumpTable.tableName().equals(tableName))
                    .findAny();

            if (table.isEmpty()) {
                fLogger.warning("Skipping unknown table %s of the dump", tableName);
                parser.skipChildren();

                continue;
            }

            inserted += readTable(parser, handle, table.get(), identities);
        }

        return inserted;
    }

    private long readTable(JsonParser parser, Handle handle, DumpTable table, Map<DumpTable, Long> identities) {
        Map<String, Column> columns = columns(handle, table);

        List<String> dumpColumns = List.of();
        long inserted = 0;

        while (parser.nextToken() == JsonToken.PROPERTY_NAME) {
            String property = parser.currentName();
            parser.nextToken();

            switch (property) {
                case DumpFormat.COLUMNS -> dumpColumns = readColumns(parser, columns, table);
                case DumpFormat.ROWS -> inserted = readRows(parser, handle, table, dumpColumns, columns, identities);
                default -> parser.skipChildren();
            }
        }

        fLogger.info("Loaded %s: %s rows", table.tableName(), inserted);

        return inserted;
    }

    private Map<String, Column> columns(Handle handle, DumpTable table) {
        Map<String, Column> columns = new LinkedHashMap<>();

        try (Query query = handle.createQuery("SELECT * FROM `" + table.tableName() + "` WHERE 1 = 0")) {
            query.scanResultSet((resultSetSupplier, _) -> {
                ResultSetMetaData metaData = resultSetSupplier.get().getMetaData();

                for (int column = 1; column <= metaData.getColumnCount(); column++) {
                    columns.put(metaData.getColumnLabel(column).toLowerCase(Locale.ROOT), new Column(metaData.getColumnType(column), metaData.isAutoIncrement(column)));
                }

                return null;
            });
        }

        return columns;
    }

    private List<String> readColumns(JsonParser parser, Map<String, Column> columns, DumpTable table) {
        List<String> dumpColumns = new ArrayList<>();

        while (parser.nextToken() != JsonToken.END_ARRAY) {
            String column = parser.getString().toLowerCase(Locale.ROOT);
            if (!columns.containsKey(column)) {
                fLogger.warning("Skipping column %s of %s, the table has no such column", column, table.tableName());
            }

            dumpColumns.add(column);
        }

        return dumpColumns;
    }

    private long readRows(JsonParser parser,
                          Handle handle,
                          DumpTable table,
                          List<String> dumpColumns,
                          Map<String, Column> columns,
                          Map<DumpTable, Long> identities) {
        List<String> targetColumns = dumpColumns.stream().filter(columns::containsKey).toList();
        if (targetColumns.isEmpty()) {
            throw new DatabaseDumpException("The dump lists no column of " + table.tableName() + " this database has");
        }

        String sql = "INSERT INTO `" + table.tableName() + "` (`" + String.join("`, `", targetColumns) + "`) VALUES (" + String.join(", ", Collections.nCopies(targetColumns.size(), "?")) + ")";

        List<Object[]> pending = new ArrayList<>();
        long inserted = 0;
        long maxId = 0;

        while (parser.nextToken() == JsonToken.START_ARRAY) {
            Object[] row = new Object[targetColumns.size()];
            int position = 0;

            for (String dumpColumn : dumpColumns) {
                parser.nextToken();

                Object value = readValue(parser, table);

                Column column = columns.get(dumpColumn);
                if (column == null) continue;

                if (ID_COLUMN.equals(dumpColumn) && value instanceof Number number) {
                    maxId = Math.max(maxId, number.longValue());
                }

                row[position++] = valueConverter.toColumnValue(value, column.sqlType(), dumpColumn);
            }

            if (parser.nextToken() != JsonToken.END_ARRAY) {
                throw new DatabaseDumpException("A row of " + table.tableName() + " holds more values than the table has columns");
            }

            pending.add(row);
            inserted++;

            if (pending.size() == BATCH_SIZE) {
                insert(handle, sql, pending, targetColumns, columns);
                pending.clear();
            }
        }

        if (!pending.isEmpty()) {
            insert(handle, sql, pending, targetColumns, columns);
        }

        Column id = columns.get(ID_COLUMN);
        if (maxId > 0 && id != null && id.generated()) {
            identities.put(table, maxId + 1);
        }

        return inserted;
    }

    private void insert(Handle handle, String sql, List<Object[]> rows, List<String> targetColumns, Map<String, Column> columns) {
        try (PreparedBatch batch = handle.prepareBatch(sql)) {
            for (Object[] row : rows) {
                for (int position = 0; position < row.length; position++) {
                    Object value = row[position];
                    if (value == null) {
                        batch.bindNull(position, columns.get(targetColumns.get(position)).sqlType());
                    } else {
                        batch.bind(position, value);
                    }
                }

                batch.add();
            }

            batch.execute();
        }
    }

    private @Nullable Object readValue(JsonParser parser, DumpTable table) {
        return switch (parser.currentToken()) {
            case VALUE_NULL -> null;
            case VALUE_TRUE -> Boolean.TRUE;
            case VALUE_FALSE -> Boolean.FALSE;
            case VALUE_NUMBER_INT -> parser.getLongValue();
            case VALUE_NUMBER_FLOAT -> parser.getDoubleValue();
            case VALUE_STRING -> parser.getString();
            case null, default -> throw new DatabaseDumpException("A row of " + table.tableName() + " holds " + parser.currentToken() + " where a value was expected");
        };
    }

    private void restartIdentity(Handle handle, DumpTable table, long nextId) {
        String tableName = table.tableName();

        switch (database.config().type()) {
            case H2 -> handle.execute("ALTER TABLE `" + tableName + "` ALTER COLUMN `" + ID_COLUMN + "` RESTART WITH " + nextId);
            case POSTGRESQL -> restartSequence(handle, tableName, nextId);
            case MYSQL, MARIADB, SQLITE -> {
                // these move their counter past the largest id inserted on their own
            }
        }
    }

    private void restartSequence(Handle handle, String tableName, long nextId) {
        // setval is a function, so postgres hands back a row and will not take an update
        try (Query query = handle.createQuery("SELECT setval(pg_get_serial_sequence('" + tableName + "', '" + ID_COLUMN + "'), " + nextId + ", false)")) {
            query.mapTo(Long.class).findOne();
        }
    }

    private record Column(
            int sqlType,
            boolean generated
    ) {
    }

}
