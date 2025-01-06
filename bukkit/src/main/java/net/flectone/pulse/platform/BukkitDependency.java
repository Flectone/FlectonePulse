package net.flectone.pulse.platform;

import com.alessiodp.libby.BukkitLibraryManager;
import com.alessiodp.libby.Library;
import com.alessiodp.libby.logging.adapters.JDKLogAdapter;
import com.google.inject.Singleton;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.logger.FLogger;
import net.flectone.pulse.util.BukkitServerUtil;
import org.bukkit.plugin.Plugin;

@Singleton
public class BukkitDependency extends PlatformDependency {

    public BukkitDependency(Plugin plugin, FLogger fLogger) {
        super(new BukkitLibraryManager(plugin, "libraries", new JDKLogAdapter(fLogger)));
    }

    @Override
    public void loadLibraries() {
        super.loadLibraries();

        addDependency(Library.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-api")
                .version(BuildConfig.ADVENTURE_API)
                .resolveTransitiveDependencies(true)
                .build()
        );

        addDependency(Library.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-text-minimessage")
                .version(BuildConfig.ADVENTURE_API)
                .resolveTransitiveDependencies(true)
                .build()
        );

        addDependency(Library.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-text-serializer-plain")
                .version(BuildConfig.ADVENTURE_API)
                .resolveTransitiveDependencies(true)
                .build()
        );

        addDependency(Library.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-text-serializer-legacy")
                .version(BuildConfig.ADVENTURE_API)
                .resolveTransitiveDependencies(true)
                .build()
        );

        addDependency(Library.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-nbt")
                .version(BuildConfig.ADVENTURE_API)
                .resolveTransitiveDependencies(true)
                .build()
        );

        addDependency(Library.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-platform-bukkit")
                .version(BuildConfig.ADVENTURE_PLATFORM_BUKKIT_VERSION)
                .resolveTransitiveDependencies(true)
                .build()
        );

        addDependency(Library.builder()
                .groupId("com{}github{}retrooper")
                .artifactId("packetevents-spigot")
                .version(BuildConfig.PACKETEVENTS_SPIGOT_VERSION)
                .resolveTransitiveDependencies(true)
                .build()
        );

        addDependency(Library.builder()
                .groupId("net{}megavex")
                .artifactId("scoreboard-library-api")
                .version(BuildConfig.SCOREBOARD_LIBRARY_API_VERSION)
                .resolveTransitiveDependencies(true)
                .build()
        );

        addDependency(Library.builder()
                .groupId("net{}megavex")
                .artifactId("scoreboard-library-implementation")
                .version(BuildConfig.SCOREBOARD_LIBRARY_API_VERSION)
                .resolveTransitiveDependencies(true)
                .build()
        );

        addDependency(Library.builder()
                .groupId("net{}megavex")
                .artifactId("scoreboard-library-packetevents")
                .version(BuildConfig.SCOREBOARD_LIBRARY_API_VERSION)
                .resolveTransitiveDependencies(true)
                .build()
        );

        addDependency(Library.builder()
                .groupId("com{}github{}Anon8281")
                .artifactId("UniversalScheduler")
                .version(BuildConfig.UNIVERSALSCHEDULER_VERSION)
                .resolveTransitiveDependencies(true)
                .build()
        );

        addDependency(Library.builder()
                .groupId("com{}zaxxer")
                .artifactId("HikariCP")
                .version(BuildConfig.HIKARICP_VERSION)
                .resolveTransitiveDependencies(true)
                .build()
        );


        String commandApiArtifact = "commandapi-bukkit-shade";

        if (BukkitServerUtil.IS_PAPER && BukkitServerUtil.IS_1_20_6_OR_NEWER && !BukkitServerUtil.IS_FOLIA) {
            commandApiArtifact += "-mojang-mapped";
        }

        addDependency(Library.builder()
                .groupId("dev{}jorel")
                .artifactId(commandApiArtifact)
                .version(BuildConfig.COMMANDAPI_BUKKIT_VERSION)
                .resolveTransitiveDependencies(true)
                .build()
        );
    }
}
