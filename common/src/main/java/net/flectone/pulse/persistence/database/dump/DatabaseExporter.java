package net.flectone.pulse.persistence.database.dump;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.exception.DatabaseDumpException;
import net.flectone.pulse.logging.FLogger;
import net.flectone.pulse.persistence.database.DatabaseImpl;
import org.jdbi.v3.core.statement.Query;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Locale;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DatabaseExporter {

    private final DatabaseImpl database;
    private final DumpValueConverter valueConverter;
    private final @Named("defaultMapper") ObjectMapper objectMapper;
    private final FLogger fLogger;

    public long export(Path dumpFile) {
        long[] written = {0};

        try (OutputStream outputStream = Files.newOutputStream(dumpFile);
             JsonGenerator generator = objectMapper.writerWithDefaultPrettyPrinter().createGenerator(outputStream)) {
            generator.writeStartObject();
            generator.writeNumberProperty(DumpFormat.FORMAT, DumpFormat.VERSION);
            generator.writeNumberProperty(DumpFormat.CREATED_AT, System.currentTimeMillis());
            generator.writeStringProperty(DumpFormat.SOURCE, database.config().type().name());
            generator.writeStringProperty(DumpFormat.PROJECT_VERSION, BuildConfig.PROJECT_VERSION);

            generator.writeName(DumpFormat.TABLES);
            generator.writeStartObject();

            database.getJdbi().useHandle(handle -> {
                for (DumpTable table : DumpTable.values()) {
                    generator.writeName(table.tableName());
                    generator.writeStartObject();

                    try (Query query = handle.createQuery("SELECT * FROM `" + table.tableName() + "`")) {
                        written[0] += query.scanResultSet((resultSetSupplier, _) ->
                                writeTable(generator, resultSetSupplier.get())
                        );
                    }

                    generator.writeEndObject();
                }
            });

            generator.writeEndObject();
            generator.writeEndObject();
        } catch (IOException e) {
            throw new DatabaseDumpException("Failed to write the dump to " + dumpFile, e);
        }

        fLogger.info("Database exported to %s, %s rows", dumpFile, written[0]);

        return written[0];
    }

    private long writeTable(JsonGenerator generator, ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        int[] columnTypes = new int[columnCount + 1];

        generator.writeName(DumpFormat.COLUMNS);
        generator.writeStartArray();
        for (int column = 1; column <= columnCount; column++) {
            columnTypes[column] = metaData.getColumnType(column);
            generator.writeString(metaData.getColumnLabel(column).toLowerCase(Locale.ROOT));
        }
        generator.writeEndArray();

        long rows = 0;

        generator.writeName(DumpFormat.ROWS);
        generator.writeStartArray();
        while (resultSet.next()) {
            generator.writeStartArray();
            for (int column = 1; column <= columnCount; column++) {
                writeValue(generator, valueConverter.fromResultSet(resultSet, column, columnTypes[column]));
            }
            generator.writeEndArray();

            rows++;
        }
        generator.writeEndArray();

        return rows;
    }

    private void writeValue(JsonGenerator generator, Object value) {
        switch (value) {
            case null -> generator.writeNull();
            case Boolean bool -> generator.writeBoolean(bool);
            case Long number -> generator.writeNumber(number);
            case Double number -> generator.writeNumber(number);
            default -> generator.writeString(String.valueOf(value));
        }
    }

}
