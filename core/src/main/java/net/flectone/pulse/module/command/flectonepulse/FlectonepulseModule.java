package net.flectone.pulse.module.command.flectonepulse;

import com.alessiodp.libby.Library;
import com.alessiodp.libby.relocation.Relocation;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.FlectonePulse;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.flectonepulse.web.SparkServer;
import net.flectone.pulse.module.command.flectonepulse.web.service.UrlService;
import net.flectone.pulse.platform.formatter.TimeFormatter;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.processing.resolver.LibraryResolver;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.incendo.cloud.suggestion.Suggestion;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FlectonepulseModule extends AbstractModuleCommand<Localization.Command.Flectonepulse> {

    private static final String SPARK_CLASS = "net.flectone.pulse.library.spark.Service";

    private final FileFacade fileFacade;
    private final FlectonePulse flectonePulse;
    private final CommandParserProvider commandParserProvider;
    private final TimeFormatter timeFormatter;
    private final FLogger fLogger;
    private final ReflectionResolver reflectionResolver;
    private final Injector injector;
    private final TaskScheduler taskScheduler;
    private final SimpleDateFormat simpleDateFormat;
    private final @Named("projectPath") Path projectPath;

    @Override
    public void onEnable() {
        super.onEnable();

        String promptType = addPrompt(0, Localization.Command.Prompt::type);
        String file = addPrompt(1, Localization.Command.Prompt::value);
        registerCommand(commandBuilder -> commandBuilder
                .permission(permission().name())
                .required(promptType, commandParserProvider.singleMessageParser(), typeSuggestion())
                .optional(file, commandParserProvider.singleMessageParser())
        );

        if (reflectionResolver.hasClass(SPARK_CLASS)) {
            enableSpark();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        if (reflectionResolver.hasClass(SPARK_CLASS)) {
            injector.getInstance(SparkServer.class).onDisable();
        }
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        Operation operation = getOperation(commandContext);
        boolean needReload = switch (operation) {
            case EDITOR -> {
                commandEditor(fPlayer, operation);
                yield false;
            }
            case EXPORT, EXPORT_ALL -> {
                commandExport(fPlayer, operation, commandContext);
                yield false;
            }
            case IMPORT -> commandImport(fPlayer, operation, commandContext);
            case RELOAD -> {
                sendMessageStarting(fPlayer, operation);
                yield true;
            }
        };

        if (!needReload) return;
        if (config().executeInMainThread()) {
            taskScheduler.runSync(() -> reload(fPlayer));
        } else {
            reload(fPlayer);
        }
    }

    public void reload(FPlayer fPlayer) {
        try {
            Instant start = Instant.now();

            flectonePulse.reload();

            Instant end = Instant.now();

            String formattedTime = timeFormatter.format(fPlayer, Duration.between(start, end).toMillis());

            sendMessage(EventMetadata.<Localization.Command.Flectonepulse>builder()
                    .sender(fPlayer)
                    .format(flectonepulse -> Strings.CS.replace(flectonepulse.formatTrue(), "<time>", formattedTime))
                    .destination(config().destination())
                    .sound(soundOrThrow())
                    .build()
            );

        } catch (Exception e) {
            fLogger.warning(e.getMessage());

            sendMessage(EventMetadata.<Localization.Command.Flectonepulse>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Flectonepulse::formatFalse)
                    .message(e.getLocalizedMessage())
                    .destination(config().destination())
                    .build()
            );

        }
    }

    @Override
    public MessageType messageType() {
        return MessageType.COMMAND_FLECTONEPULSE;
    }

    @Override
    public Command.Flectonepulse config() {
        return fileFacade.command().flectonepulse();
    }

    @Override
    public Permission.Command.Flectonepulse permission() {
        return fileFacade.permission().command().flectonepulse();
    }

    @Override
    public Localization.Command.Flectonepulse localization(FEntity sender) {
        return fileFacade.localization(sender).command().flectonepulse();
    }

    private boolean commandEditor(FPlayer fPlayer, Operation operation) {
        if (fileFacade.config().editor().host().isEmpty()) {
            sendErrorMessage(EventMetadata.<Localization.Command.Flectonepulse>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Flectonepulse::nullHostEditor)
                    .build()
            );

            return false;
        }

        sendMessageStarting(fPlayer, operation);

        UrlService urlService = injector.getInstance(UrlService.class);
        String url = urlService.generateUrl();

        reflectionResolver.hasClassOrElse(SPARK_CLASS, this::loadSparkLibrary);

        int port = fileFacade.config().editor().port();
        if (!isPortAvailable(port)) {
            sendErrorMessage(EventMetadata.<Localization.Command.Flectonepulse>builder()
                    .sender(fPlayer)
                    .format(localization -> Strings.CS.replace(localization.nullPortEditor(), "<port>", String.valueOf(port)))
                    .build()
            );

            return false;
        }

        enableSpark();

        sendMessage(EventMetadata.<Localization.Command.Flectonepulse>builder()
                .sender(fPlayer)
                .format(flectonepulse -> Strings.CS.replace(flectonepulse.formatEditor(), "<url>", url))
                .destination(config().destination())
                .sound(soundOrThrow())
                .build()
        );

        return true;
    }

    private boolean isPortAvailable(int port) {
        if (injector.getInstance(SparkServer.class).isEnable()) return true;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean commandExport(FPlayer fPlayer, Operation operation, CommandContext<FPlayer> commandContext) {
        sendMessageStarting(fPlayer, operation);

        Path zipFile = projectPath.resolve(getFilenameExported(commandContext));
        if (zipFile.toFile().exists()) {
            sendErrorMessage(EventMetadata.<Localization.Command.Flectonepulse>builder()
                    .sender(fPlayer)
                    .format(localization -> Strings.CS.replace(localization.fileExist(), "<file>", zipFile.getFileName().toString()))
                    .build()
            );

            return false;
        }

        try (FileSystem zipFileSystem = FileSystems.newFileSystem(zipFile, Map.of("create", "true"));
             Stream<Path> filesStream = Files.walk(projectPath)) {

            filesStream
                    .filter(path -> !path.equals(zipFile))
                    .filter(path -> {
                        String fileName = path.getFileName().toString();
                        if (fileName.endsWith(".zip")
                                || fileName.endsWith(".rar")
                                || fileName.endsWith(".7z")
                                || fileName.endsWith(".tar")) {
                            return false;
                        }

                        if (operation == Operation.EXPORT_ALL) return true;

                        String pathString = path.toString();
                        return !pathString.contains(projectPath.getFileName() + File.separator + "libraries")
                                && !pathString.contains(projectPath.getFileName() + File.separator + "backups")
                                && !pathString.contains(projectPath.getFileName() + File.separator + "minecraft")
                                && !path.getFileName().toString().endsWith(".db");
                    })
                    .forEach(path -> {
                        try {
                            Path relative = projectPath.relativize(path);
                            Path target = zipFileSystem.getPath("/" + relative);

                            if (Files.isDirectory(path)) {
                                Files.createDirectories(target);
                            } else {
                                if (target.getParent() != null) {
                                    Files.createDirectories(target.getParent());
                                }

                                Files.copy(path, target, StandardCopyOption.REPLACE_EXISTING);
                            }
                        } catch (Exception e) {
                            fLogger.warning(e);
                        }
                    });

            sendMessage(EventMetadata.<Localization.Command.Flectonepulse>builder()
                    .sender(fPlayer)
                    .format(localization -> Strings.CS.replace(localization.formatExport(), "<file>", zipFile.getFileName().toString()))
                    .destination(config().destination())
                    .sound(soundOrThrow())
                    .build()
            );

            return true;
        } catch (IOException e) {
            fLogger.warning(e);
        }

        return false;
    }

    private boolean commandImport(FPlayer fPlayer, Operation operation, CommandContext<FPlayer> commandContext) {
        Path zipFile = projectPath.resolve(getFilenameExported(commandContext));

        if (!Files.exists(zipFile)) {
            sendErrorMessage(EventMetadata.<Localization.Command.Flectonepulse>builder()
                    .sender(fPlayer)
                    .format(localization -> Strings.CS.replace(localization.nullFile(), "<file>", zipFile.getFileName().toString()))
                    .build()
            );

            return false;
        }

        sendMessageStarting(fPlayer, operation);

        try (FileSystem zipFileSystem = FileSystems.newFileSystem(zipFile);
             Stream<Path> filesStream = Files.walk(zipFileSystem.getPath("/"))) {
            Path zipRoot = zipFileSystem.getPath("/");

            filesStream
                    .forEach(zipPath -> {
                        try {
                            Path relative = zipRoot.relativize(zipPath);
                            Path target = projectPath.resolve(relative.toString());

                            if (Files.isDirectory(zipPath)) {
                                Files.createDirectories(target);
                            } else {
                                if (target.getParent() != null) {
                                    Files.createDirectories(target.getParent());
                                }
                                
                                Files.copy(zipPath, target, StandardCopyOption.REPLACE_EXISTING);
                            }
                        } catch (Exception e) {
                            fLogger.warning(e);
                        }
                    });

            sendMessage(EventMetadata.<Localization.Command.Flectonepulse>builder()
                    .sender(fPlayer)
                    .format(localization -> Strings.CS.replace(localization.formatImport(), "<file>", zipFile.getFileName().toString()))
                    .destination(config().destination())
                    .sound(soundOrThrow())
                    .build()
            );

            return true;
        } catch (IOException e) {
            fLogger.warning(e);
        }

        return false;
    }

    private String getFilenameExported(CommandContext<FPlayer> commandContext) {
        Optional<String> optionalFileName = commandContext.optional(getPrompt(1));
        return optionalFileName.orElse("export_" + simpleDateFormat.format(new Date())) + ".zip";
    }

    private void sendMessageStarting(FPlayer fPlayer, Operation operation) {
        sendMessage(EventMetadata.<Localization.Command.Flectonepulse>builder()
                .sender(fPlayer)
                .format(localization -> Strings.CS.replace(localization.formatStarting(), "<type>", operation.name().toLowerCase()))
                .destination(config().destination())
                .build()
        );
    }

    private Operation getOperation(CommandContext<FPlayer> commandContext) {
        String type = getArgument(commandContext, 0);
        return Arrays.stream(Operation.values())
                .filter(operation -> operation.name().equalsIgnoreCase(type))
                .findAny()
                .orElse(Operation.RELOAD);
    }

    private void enableSpark() {
        SparkServer sparkServer = injector.getInstance(SparkServer.class);
        if (!sparkServer.isEnable()) {
            sparkServer.onEnable();
        }
    }

    private @NonNull BlockingSuggestionProvider<FPlayer> typeSuggestion() {
        return (context, input) -> Arrays.stream(Operation.values())
                .map(operation -> Suggestion.suggestion(operation.name().toLowerCase()))
                .toList();
    }

    private void loadSparkLibrary(LibraryResolver libraryResolver) {
        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}sparkjava")
                .artifactId("spark-core")
                .version(BuildConfig.SPARK_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .relocate(Relocation.builder()
                        .pattern("spark")
                        .relocatedPattern("net.flectone.pulse.library.spark")
                        .build()
                )
                .build()
        );
    }

    public enum Operation {

        RELOAD,
        EDITOR,
        EXPORT,
        EXPORT_ALL,
        IMPORT

    }
}
