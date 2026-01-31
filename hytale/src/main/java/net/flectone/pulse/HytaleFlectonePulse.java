package net.flectone.pulse;

import com.alessiodp.libby.LibraryManager;
import com.alessiodp.libby.StandaloneLibraryManager;
import com.alessiodp.libby.logging.LogLevel;
import com.alessiodp.libby.logging.adapters.LogAdapter;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.Stage;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import lombok.Getter;
import net.flectone.pulse.exception.ReloadException;
import net.flectone.pulse.execution.scheduler.HytaleTaskScheduler;
import net.flectone.pulse.processing.resolver.HytaleLibraryResolver;
import net.flectone.pulse.processing.resolver.LibraryResolver;
import net.flectone.pulse.util.logging.FLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

@Getter
@Singleton
public class HytaleFlectonePulse extends JavaPlugin implements FlectonePulse {

    private final Path projectPath;

    private FLogger fLogger;
    private Injector injector;

    public HytaleFlectonePulse(@NonNull JavaPluginInit init) {
        super(init);

        projectPath = init.getFile().getParent().resolve("FlectonePulse");
    }

    @Override
    protected void setup() {
        // initialize custom logger
        HytaleLogger hytaleLogger = this.getLogger();
        fLogger = new FLogger(logRecord -> hytaleLogger.at(logRecord.getLevel()).log(logRecord.getMessage()));
        fLogger.logEnabling();

        // set up library resolver for dependency loading
        LibraryManager libraryManager = getLibraryManager(hytaleLogger);
        LibraryResolver libraryResolver = new HytaleLibraryResolver(libraryManager);
        libraryResolver.addLibraries();
        libraryResolver.resolveRepositories();
        libraryResolver.loadLibraries();

        // create guice injector for dependency injection
        injector = Guice.createInjector(Stage.PRODUCTION, new HytaleInjector(this, projectPath, libraryResolver, fLogger));

        HytaleTaskScheduler hytaleTaskScheduler = injector.getInstance(HytaleTaskScheduler.class);
        HytaleServer.SCHEDULED_EXECUTOR.scheduleWithFixedDelay(hytaleTaskScheduler::onTick, 50L, 50L, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void start() {
        onEnable();
    }

    @Override
    protected void shutdown() {
        onDisable();
    }

    @Override
    public void onEnable() {
        if (!isReady()) return;

        get(FlectonePulseAPI.class).onEnable();
    }

    @Override
    public void onDisable() {
        if (!isReady()) {
            terminateFailedPacketAdapter();
            return;
        }

        get(FlectonePulseAPI.class).onDisable();
    }

    @Override
    public void reload() throws ReloadException {
        if (!isReady()) return;

        get(FlectonePulseAPI.class).reload();
    }

    @NotNull
    private LibraryManager getLibraryManager(HytaleLogger hytaleLogger) {
        LogAdapter logAdapter = new LogAdapter() {
            @Override
            public void log(@NotNull LogLevel logLevel, @Nullable String s) {
                hytaleLogger.at(Level.parse(logLevel.name())).log(s);
            }

            @Override
            public void log(@NotNull LogLevel logLevel, @Nullable String s, @Nullable Throwable throwable) {
                hytaleLogger.at(Level.parse(logLevel.name())).log(s, throwable);
            }
        };

        return new StandaloneLibraryManager(logAdapter, projectPath, "libraries");
    }

    @Override
    public void initPacketAdapter() {
        // nothing
    }

    @Override
    public void terminateFailedPacketAdapter() {
        // nothing
    }

    @Override
    public void terminatePacketAdapter() {
        // nothing
    }

    @Override
    public void closeUIs() {
        // nothing
    }

}
