package net.flectone.pulse.module.command.flectonepulse;

import com.alessiodp.libby.Library;
import com.alessiodp.libby.relocation.Relocation;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import lombok.NonNull;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.FlectonePulse;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.platform.formatter.TimeFormatter;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.flectonepulse.web.SparkServer;
import net.flectone.pulse.module.command.flectonepulse.web.service.UrlService;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.processing.resolver.LibraryResolver;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.util.logging.FLogger;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.incendo.cloud.suggestion.Suggestion;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Singleton
public class FlectonepulseModule extends AbstractModuleCommand<Localization.Command.Flectonepulse> {

    private final String sparkClass = "net.flectone.pulse.library.spark.Service";

    private final Config.Editor config;
    private final Command.Flectonepulse command;
    private final Permission.Command.Flectonepulse permission;
    private final FlectonePulse flectonePulse;
    private final CommandParserProvider commandParserProvider;
    private final TimeFormatter timeFormatter;
    private final FLogger fLogger;
    private final ReflectionResolver reflectionResolver;
    private final Injector injector;

    @Inject
    public FlectonepulseModule(FileResolver fileResolver,
                               CommandParserProvider commandParserProvider,
                               TimeFormatter timeFormatter,
                               FlectonePulse flectonePulse,
                               FLogger fLogger,
                               ReflectionResolver reflectionResolver,
                               Injector injector) {
        super(localization -> localization.getCommand().getFlectonepulse(), Command::getFlectonepulse);

        this.config = fileResolver.getConfig().getEditor();
        this.command = fileResolver.getCommand().getFlectonepulse();
        this.permission = fileResolver.getPermission().getCommand().getFlectonepulse();
        this.flectonePulse = flectonePulse;
        this.commandParserProvider = commandParserProvider;
        this.timeFormatter = timeFormatter;
        this.fLogger = fLogger;
        this.reflectionResolver = reflectionResolver;
        this.injector = injector;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        String promptType = addPrompt(0, Localization.Command.Prompt::getType);
        registerCommand(commandBuilder -> commandBuilder
                .permission(permission.getName())
                .required(promptType, commandParserProvider.singleMessageParser(), typeSuggestion())
        );

        addPredicate(this::checkCooldown);

        if (reflectionResolver.hasClass(sparkClass)) {
            enableSpark();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        if (reflectionResolver.hasClass(sparkClass)) {
            injector.getInstance(SparkServer.class).onDisable();
        }
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer)) return;

        String type = getArgument(commandContext, 0);
        if (type.equalsIgnoreCase("editor")) {
            if (config.getHost().isEmpty()) {
                builder(fPlayer)
                        .format(Localization.Command.Flectonepulse::getNullHostEditor)
                        .sendBuilt();
                return;
            }

            builder(fPlayer)
                    .destination(command.getDestination())
                    .format(Localization.Command.Flectonepulse::getFormatWebStarting)
                    .sendBuilt();

            UrlService urlService = injector.getInstance(UrlService.class);
            String url = urlService.generateUrl();

            reflectionResolver.hasClassOrElse(sparkClass, this::loadSparkLibrary);

            enableSpark();

            builder(fPlayer)
                    .destination(command.getDestination())
                    .format(flectonepulse -> Strings.CS.replace(flectonepulse.getFormatEditor(), "<url>", url))
                    .sound(getSound())
                    .sendBuilt();
            return;
        }

        try {
            Instant start = Instant.now();

            flectonePulse.reload();

            Instant end = Instant.now();

            String formattedTime = timeFormatter.format(fPlayer, Duration.between(start, end).toMillis());

            builder(fPlayer)
                    .destination(command.getDestination())
                    .format(flectonepulse -> Strings.CS.replace(flectonepulse.getFormatTrue(), "<time>", formattedTime))
                    .sound(getSound())
                    .sendBuilt();

        } catch (Exception e) {
            fLogger.warning(e);

            builder(fPlayer)
                    .destination(command.getDestination())
                    .format(Localization.Command.Flectonepulse::getFormatFalse)
                    .message(e.getLocalizedMessage())
                    .sendBuilt();
        }
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
