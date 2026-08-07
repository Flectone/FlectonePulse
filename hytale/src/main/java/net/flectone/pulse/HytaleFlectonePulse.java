package net.flectone.pulse;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.Stage;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.HytaleServer;
import lombok.Getter;
import net.flectone.pulse.constant.HookType;
import net.flectone.pulse.exception.InjectorNotInitializedException;
import net.flectone.pulse.exception.ReloadException;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.logging.FLogger;
import net.flectone.pulse.resolver.HytaleLibraryResolver;
import net.flectone.pulse.resolver.LibraryResolver;
import net.flectone.pulse.scheduler.TaskScheduler;
import org.jspecify.annotations.NonNull;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Getter
@Singleton
public class HytaleFlectonePulse implements FlectonePulse {

    private final Supplier<HytaleFlectonePulseLoader> loader;
    private final Path projectPath;

    private FLogger fLogger;
    private Injector injector;

    public HytaleFlectonePulse(Supplier<HytaleFlectonePulseLoader> loader) {
        this.loader = loader;
        this.projectPath = loader.get().getFile().getParent().resolve("FlectonePulse");
    }

    @Override
    public void onLoad() {
        // initialize custom logger
        HytaleLogger hytaleLogger = getLoader().getLogger();
        fLogger = new FLogger(
                logRecord -> hytaleLogger.at(logRecord.getLevel()).log(logRecord.getMessage()),
                () -> injector == null ? null : injector.getInstance(FileFacade.class)
        );
        fLogger.logEnabling();

        // set up library resolver for dependency loading
        LibraryResolver libraryResolver = new HytaleLibraryResolver(hytaleLogger, projectPath);
        libraryResolver.resolveRepositories();

        try {
            libraryResolver.loadLibraries();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Failed to download library")) {
                hytaleLogger.atSevere().log("\n\n====================\n A problem occurred while downloading the libraries, perhaps you do not have access to repository. \n Try downloading the libraries manually from https://flectone.net/files/r/FlectonePulse-libraries.zip and extract them into FlectonePulse folder \n====================\n");
            }

            throw e;
        }


        try {
            // create guice injector for dependency injection
            injector = Guice.createInjector(Stage.PRODUCTION, new HytaleInjector(this, projectPath, libraryResolver, fLogger));
        } catch (Exception e) {
            throwInitException(e);
        }
    }

    @Override
    public void onEnable() {
        if (!isReady()) return;

        // get scheduler
        TaskScheduler taskScheduler = injector.getInstance(TaskScheduler.class);

        // create executor
        taskScheduler.start();

        // update tick
        HytaleServer.SCHEDULED_EXECUTOR.scheduleWithFixedDelay(taskScheduler::onTick, 50L, 50L, TimeUnit.MILLISECONDS);

        get(FlectonePulseAPIImpl.class).onEnable();
    }

    @Override
    public void onDisable() {
        if (!isReady()) return;

        get(FlectonePulseAPIImpl.class).onDisable();
    }

    @Override
    public void hook(HookType type, Object... args) {
        // nothing
    }

    public @NonNull HytaleFlectonePulseLoader getLoader() {
        return loader.get();
    }

    @Override
    public <T> T get(Class<T> type) {
        if (!isReady()) {
            throw new InjectorNotInitializedException();
        }

        return injector.getInstance(type);
    }

    @Override
    public boolean isReady() {
        return injector != null;
    }

    @Override
    public void reload() throws ReloadException {
        if (!isReady()) return;

        get(FlectonePulseAPIImpl.class).reload();
    }

}
