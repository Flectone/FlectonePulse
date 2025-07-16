package net.flectone.pulse.module.command.flectonepulse;

import com.alessiodp.libby.Library;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import lombok.NonNull;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.FlectonePulse;
import net.flectone.pulse.configuration.Command;
import net.flectone.pulse.configuration.Config;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.formatter.TimeFormatter;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.flectonepulse.web.JavalinServer;
import net.flectone.pulse.module.command.flectonepulse.web.service.UrlService;
import net.flectone.pulse.registry.CommandRegistry;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.resolver.LibraryResolver;
import net.flectone.pulse.util.logging.FLogger;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.meta.CommandMeta;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.incendo.cloud.suggestion.Suggestion;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Singleton
public class FlectonepulseModule extends AbstractModuleCommand<Localization.Command.Flectonepulse> {

    private final Config.Editor config;
    private final Command.Flectonepulse command;
    private final Permission.Command.Flectonepulse permission;
    private final FlectonePulse flectonePulse;
    private final CommandRegistry commandRegistry;
    private final TimeFormatter timeFormatter;
    private final FLogger fLogger;
    private final LibraryResolver libraryResolver;
    private final Injector injector;

    @Inject
    public FlectonepulseModule(FileResolver fileResolver,
                               CommandRegistry commandRegistry,
                               TimeFormatter timeFormatter,
                               FlectonePulse flectonePulse,
                               FLogger fLogger,
                               LibraryResolver libraryResolver,
                               Injector injector) {
        super(localization -> localization.getCommand().getFlectonepulse(), null);

        this.config = fileResolver.getConfig().getEditor();
        this.command = fileResolver.getCommand().getFlectonepulse();
        this.permission = fileResolver.getPermission().getCommand().getFlectonepulse();
        this.flectonePulse = flectonePulse;
        this.commandRegistry = commandRegistry;
        this.timeFormatter = timeFormatter;
        this.fLogger = fLogger;
        this.libraryResolver = libraryResolver;
        this.injector = injector;
    }

    @Override
    protected boolean isConfigEnable() {
        return command.isEnable();
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        String commandName = getName(command);
        String promptType = getPrompt().getType();
        commandRegistry.registerCommand(manager ->
                manager.commandBuilder(commandName, command.getAliases(), CommandMeta.empty())
                        .permission(permission.getName())
                        .required(promptType, commandRegistry.singleMessageParser(), typeSuggestion())
                        .handler(this)
        );

        addPredicate(this::checkCooldown);

        if (hasJavalin()) {
            enableJavalin();
        }
    }

    @Override
    public void onDisable() {
        if (hasJavalin()) {
            injector.getInstance(JavalinServer.class).onDisable();
        }
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkModulePredicates(fPlayer)) return;

        String promptType = getPrompt().getType();
        String type = commandContext.get(promptType);
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

            if (!hasJavalin()) {
                loadJavalinLibrary();
            }

            enableJavalin();

            builder(fPlayer)
                    .destination(command.getDestination())
                    .format(flectonepulse -> flectonepulse.getFormatEditor().replace("<url>", url))
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
                    .format(flectonepulse -> flectonepulse.getFormatTrue().replace("<time>", formattedTime))
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

    private void enableJavalin() {
        JavalinServer javalinServer = injector.getInstance(JavalinServer.class);
        if (!javalinServer.isEnable()) {
            javalinServer.onEnable();
        }
    }

    private @NonNull BlockingSuggestionProvider<FPlayer> typeSuggestion() {
        return (context, input) -> List.of(
                Suggestion.suggestion("reload"),
                Suggestion.suggestion("editor")
        );
    }

    private void loadJavalinLibrary() {
        libraryResolver.loadLibrary(Library.builder()
                .groupId("io{}javalin")
                .artifactId("javalin")
                .version(BuildConfig.JAVALIN_VERSION)
                .resolveTransitiveDependencies(true)
                .build()
        );
    }

    private boolean hasJavalin() {
        try {
            Class.forName("io.javalin.Javalin");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}
