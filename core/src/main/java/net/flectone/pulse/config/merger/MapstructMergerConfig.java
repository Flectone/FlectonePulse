package net.flectone.pulse.config.merger;

import org.mapstruct.MapperConfig;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Global MapStruct configuration for merger components.
 * <p>
 * This configuration applies to all MapStruct mappers in the package,
 * providing consistent behavior for null value handling and component model.
 * </p>
 *
 * @author TheFaser
 * @since 1.7.1
 */
@MapperConfig(
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.JAKARTA
)
public interface MapstructMergerConfig {
}
