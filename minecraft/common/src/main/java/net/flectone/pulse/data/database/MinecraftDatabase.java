package net.flectone.pulse.data.database;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import net.flectone.pulse.data.database.dao.VersionDAO;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.processing.resolver.SystemVariableResolver;
import net.flectone.pulse.util.comparator.VersionComparator;
import net.flectone.pulse.util.creator.BackupCreator;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;

import java.io.IOException;
import java.nio.file.Path;

@Singleton
public class MinecraftDatabase extends Database {

    private final FileFacade fileFacade;
    private final PacketProvider packetProvider;
    private final FLogger fLogger;

    @Inject
    public MinecraftDatabase(FileFacade fileFacade,
                             VersionComparator versionComparator,
                             @Named("projectPath") Path projectPath,
                             SystemVariableResolver systemVariableResolver,
                             PlatformServerAdapter platformServerAdapter,
                             FLogger fLogger,
                             PacketProvider packetProvider,
                             ReflectionResolver reflectionResolver,
                             Provider<VersionDAO> versionDAOProvider,
                             BackupCreator backupCreator) {
        super(fileFacade, versionComparator, projectPath, systemVariableResolver, platformServerAdapter, fLogger, reflectionResolver, versionDAOProvider, backupCreator);

        this.fileFacade = fileFacade;
        this.packetProvider = packetProvider;
        this.fLogger = fLogger;
    }

    @Override
    public void connect() throws IOException {
        if (packetProvider.getServerVersion().isOlderThanOrEquals(ServerVersion.V_1_12_2) && config().type() == Type.SQLITE) {
            fLogger.warning("SQLite database is not supported on this version of Minecraft, H2 Database will be used");

            fileFacade.updateFilePack(filePack -> filePack.withConfig(filePack.config().withDatabase(config().withType(Type.H2))));
        }

        super.connect();
    }

}
