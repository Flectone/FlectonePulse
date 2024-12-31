package net.flectone.pulse.platform;

import com.alessiodp.libby.LibraryManager;
import com.google.inject.Singleton;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.util.BukkitServerUtil;

@Singleton
public class BukkitBaseDependency extends PlatformDependency {

    public BukkitBaseDependency(LibraryManager libraryManager) {
        super(libraryManager);
    }

    @Override
    public void loadLibraries() {
        super.loadLibraries();

        addDependency("com.github.retrooper", "packetevents-spigot", BuildConfig.PACKETEVENTS_SPIGOT_VERSION);
        addDependency("net.megavex", "scoreboard-library-api", BuildConfig.SCOREBOARD_LIBRARY_API_VERSION);
        addDependency("net.megavex", "scoreboard-library-implementation", BuildConfig.SCOREBOARD_LIBRARY_API_VERSION);
        addDependency("net.megavex", "scoreboard-library-packetevents", BuildConfig.SCOREBOARD_LIBRARY_API_VERSION);
        addDependency("com.github.Anon8281", "UniversalScheduler", BuildConfig.UNIVERSALSCHEDULER_VERSION);
        addDependency("com.zaxxer", "HikariCP", BuildConfig.HIKARICP_VERSION);

        String commandApiArtifact = "commandapi-bukkit-shade";

        if (BukkitServerUtil.IS_PAPER && BukkitServerUtil.IS_1_20_6_OR_NEWER && !BukkitServerUtil.IS_FOLIA) {
            commandApiArtifact += "-mojang-mapped";
        }

        addDependency("dev.jorel", commandApiArtifact, BuildConfig.COMMANDAPI_BUKKIT_VERSION);
    }
}
