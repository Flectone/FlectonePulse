package net.flectone.pulse.module.command.flectonepulse;

import com.alessiodp.libby.Library;
import com.alessiodp.libby.relocation.Relocation;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.FlectonePulse;
import net.flectone.pulse.annotation.Sync;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.flectonepulse.web.SparkServer;
import net.flectone.pulse.module.command.flectonepulse.web.service.UrlService;
import net.flectone.pulse.platform.formatter.TimeFormatter;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.processing.resolver.LibraryResolver;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.logging.FLogger;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.incendo.cloud.suggestion.Suggestion;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

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

    @Override
    public void onEnable() {
        super.onEnable();

        String promptType = addPrompt(0, Localization.Command.Prompt::type);
        registerCommand(commandBuilder -> commandBuilder
                .permission(permission().name())
                .required(promptType, commandParserProvider.singleMessageParser(), typeSuggestion())
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

        String type = getArgument(commandContext, 0);
        if (type.equalsIgnoreCase("editor")) {
            if (fileFacade.config().editor().host().isEmpty()) {
                sendErrorMessage(metadataBuilder()
                        .sender(fPlayer)
                        .format(Localization.Command.Flectonepulse::nullHostEditor)
                        .build()
                );

                return;
            }

            sendMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Flectonepulse::formatWebStarting)
                    .destination(config().destination())
                    .build()
            );

            UrlService urlService = injector.getInstance(UrlService.class);
            String url = urlService.generateUrl();

            reflectionResolver.hasClassOrElse(SPARK_CLASS, this::loadSparkLibrary);

            enableSpark();

            sendMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(flectonepulse -> Strings.CS.replace(flectonepulse.formatEditor(), "<url>", url))
                    .destination(config().destination())
                    .sound(soundOrThrow())
                    .build()
            );

            return;
        }

        if (config().executeInMainThread()) {
            syncReload(fPlayer);
        } else {
            reload(fPlayer);
        }
    }

    @Sync
    public void syncReload(FPlayer fPlayer) {
        reload(fPlayer);
    }

    public void reload(FPlayer fPlayer) {
        try {
            Instant start = Instant.now();

            flectonePulse.reload();

            Instant end = Instant.now();

            String formattedTime = timeFormatter.format(fPlayer, Duration.between(start, end).toMillis());

            sendMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(flectonepulse -> Strings.CS.replace(flectonepulse.formatTrue(), "<time>", formattedTime))
                    .destination(config().destination())
                    .sound(soundOrThrow())
                    .build()
            );

        } catch (Exception e) {
            fLogger.warning(e.getMessage());

            sendMessage(metadataBuilder()
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

    private void enableSpark() {
        SparkServer sparkServer = injector.getInstance(SparkServer.class);
        if (!sparkServer.isEnable()) {
            sparkServer.onEnable();
        }
    }

    private @NonNull BlockingSuggestionProvider<FPlayer> typeSuggestion() {
        return (context, input) -> List.of(
                Suggestion.suggestion("reload"),
                Suggestion.suggestion("editor")
        );
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
}
