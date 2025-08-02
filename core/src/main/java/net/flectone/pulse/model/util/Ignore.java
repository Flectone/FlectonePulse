package net.flectone.pulse.model.util;

import org.jdbi.v3.core.mapper.reflect.ColumnName;

public record Ignore(
        @ColumnName("id") int id,
        @ColumnName("date") long date,
        @ColumnName("target") int target
) {}
