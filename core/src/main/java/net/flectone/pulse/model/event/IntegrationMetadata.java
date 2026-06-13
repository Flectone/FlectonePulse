package net.flectone.pulse.model.event;

import lombok.Builder;
import lombok.With;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

@With
@Builder(toBuilder = true)
public record IntegrationMetadata(
        @NonNull List<String> messageNames,
        @NonNull UnaryOperator<String> format
) {

    public static final IntegrationMetadata EMPTY = IntegrationMetadata.builder().build();

    @SuppressWarnings("DataFlowIssue")
    public IntegrationMetadata {
        messageNames = Objects.requireNonNullElse(messageNames, List.of());
        format = Objects.requireNonNullElse(format, string -> string);
    }

}
