package net.flectone.pulse.model.event;

import lombok.Builder;
import lombok.With;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

@With
@Builder(toBuilder = true)
public record IntegrationMessageFormat(
        @NonNull List<String> messageNames,
        @NonNull UnaryOperator<String> format
) {

    @SuppressWarnings("DataFlowIssue")
    public IntegrationMessageFormat {
        messageNames = Objects.requireNonNullElse(messageNames, List.of());
        format = Objects.requireNonNullElse(format, string -> string);
    }

}
