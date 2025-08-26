package net.flectone.pulse.module.command.ignore.model;

import org.jdbi.v3.core.mapper.reflect.ColumnName;

public record Ignore (
        @ColumnName("id") int id,
        @ColumnName("date") long date,
        @ColumnName("target") int target
) {}
