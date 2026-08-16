package net.flectone.pulse.persistence.database.dump;

import com.google.inject.Singleton;
import net.flectone.pulse.exception.DatabaseDumpException;
import org.jspecify.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

@Singleton
public class DumpValueConverter {

    public @Nullable Object fromResultSet(ResultSet resultSet, int columnIndex, int sqlType) throws SQLException {
        Object value = switch (sqlType) {
            case Types.BOOLEAN, Types.BIT -> resultSet.getBoolean(columnIndex);
            case Types.TINYINT, Types.SMALLINT, Types.INTEGER, Types.BIGINT -> resultSet.getLong(columnIndex);
            case Types.REAL, Types.FLOAT, Types.DOUBLE, Types.DECIMAL, Types.NUMERIC -> resultSet.getDouble(columnIndex);
            default -> resultSet.getString(columnIndex);
        };

        return resultSet.wasNull() ? null : value;
    }

    public @Nullable Object toColumnValue(@Nullable Object value, int sqlType, String column) {
        if (value == null) return null;

        return switch (sqlType) {
            case Types.BOOLEAN, Types.BIT -> toBoolean(value, column);
            case Types.TINYINT, Types.SMALLINT, Types.INTEGER -> toNumber(value, column).intValue();
            case Types.BIGINT -> toNumber(value, column).longValue();
            case Types.REAL, Types.FLOAT, Types.DOUBLE, Types.DECIMAL, Types.NUMERIC -> toNumber(value, column).doubleValue();
            default -> toText(value);
        };
    }

    private boolean toBoolean(Object value, String column) {
        return switch (value) {
            case Boolean bool -> bool;
            case Number number -> number.longValue() != 0;
            case String string -> string.equalsIgnoreCase("true") || string.equals("1");
            default -> throw new DatabaseDumpException("Column " + column + " expects a boolean, the dump holds " + value);
        };
    }

    private Number toNumber(Object value, String column) {
        return switch (value) {
            case Number number -> number;
            case Boolean bool -> bool ? 1 : 0;
            case String string -> parseNumber(string, column);
            default -> throw new DatabaseDumpException("Column " + column + " expects a number, the dump holds " + value);
        };
    }

    private Number parseNumber(String value, String column) {
        try {
            return value.contains(".") ? Double.parseDouble(value) : Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new DatabaseDumpException("Column " + column + " expects a number, the dump holds '" + value + "'", e);
        }
    }

    private String toText(Object value) {
        return value instanceof Boolean bool ? (bool ? "1" : "0") : String.valueOf(value);
    }

}
