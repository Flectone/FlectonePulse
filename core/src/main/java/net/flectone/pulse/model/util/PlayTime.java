package net.flectone.pulse.model.util;

import org.jdbi.v3.core.mapper.reflect.ColumnName;

public record PlayTime(
        @ColumnName("id") int id,
        @ColumnName("player") int playerId,
        @ColumnName("first") long first,
        @ColumnName("last") long last,
        @ColumnName("total") long total,
        @ColumnName("sessions") int sessions
) {
}
