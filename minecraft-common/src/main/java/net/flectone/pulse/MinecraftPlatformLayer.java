package net.flectone.pulse;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.manager.server.VersionComparison;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import lombok.SneakyThrows;

import java.util.List;

public record MinecraftPlatformLayer(
        ServerVersion version,
        VersionComparison comparison,
        String injector
) {

    public MinecraftPlatformLayer(ServerVersion version, String injector) {
        this(version, VersionComparison.NEWER_THAN_OR_EQUALS, injector);
    }

    public static Module apply(Module module, ServerVersion serverVersion, List<MinecraftPlatformLayer> layers) {
        Module result = module;

        for (MinecraftPlatformLayer layer : layers) {
            if (!serverVersion.is(layer.comparison(), layer.version())) continue;

            result = Modules.override(result).with(layer.createInjector());
        }

        return result;
    }

    @SneakyThrows
    private Module createInjector() {
        return (Module) Class.forName(injector).getDeclaredConstructor().newInstance();
    }

}
