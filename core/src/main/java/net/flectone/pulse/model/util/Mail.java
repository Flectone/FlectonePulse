package net.flectone.pulse.model.util;

import org.jdbi.v3.core.mapper.reflect.ColumnName;

public record Mail(
        @ColumnName("id") int id,
        @ColumnName("date") long date,
        @ColumnName("sender") int sender,
        @ColumnName("receiver") int receiver,
        @ColumnName("message") String message
) {}
