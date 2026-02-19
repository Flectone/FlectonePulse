package net.flectone.pulse.module.command.sprite;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.MessageSendEvent;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.sender.SoundPlayer;
import net.flectone.pulse.util.WebUtil;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.incendo.cloud.suggestion.Suggestion;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SpriteModule extends AbstractModuleCommand<Localization.Command.Sprite> {

    private static final String FLECTONEPULSE_ATLAS_API = "https://flectone.net/files/r/minecraft/<version>/atlases/minecraft_textures_atlas_<atlas>.png.txt";
    private static final String ATLAS_FILE_NAME = "minecraft_textures_atlas_<atlas>.png.txt";
    private static final Pattern SPRITE_PATTERN = Pattern.compile("^minecraft:|\\s+x=.*");

    private final Map<String, List<String>> atlasSpritesMap = new ConcurrentHashMap<>();

    private final FileFacade fileFacade;
    private final CommandParserProvider commandParserProvider;
    private final MessagePipeline messagePipeline;
    private final EventDispatcher eventDispatcher;
    private final PlatformServerAdapter platformServerAdapter;
    private final TaskScheduler taskScheduler;
    private final SoundPlayer soundPlayer;
    private final WebUtil webUtil;
    private final FLogger fLogger;
    private final @Named("minecraftPath") Path minecraftPath;

    @Override
    public void onEnable() {
        super.onEnable();

        atlasSpritesMap.clear();
        lazyLoadLocalAtlases();

        String promptCategory = addPrompt(0, Localization.Command.Prompt::category);
        String promptNumber = addPrompt(1, Localization.Command.Prompt::number);
        registerCommand(manager -> manager
                .required(promptCategory, commandParserProvider.singleMessageParser(), categorySuggestion())
                .optional(promptNumber, commandParserProvider.integerParser())
                .permission(permission().name())
        );
    }

    private @NonNull BlockingSuggestionProvider<FPlayer> categorySuggestion() {
        return (context, input) -> config().categories()
                .stream()
                .map(Suggestion::suggestion)
                .toList();
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        String atlas = getArgument(commandContext, 0);
        if (!config().categories().contains(atlas)) {
            sendErrorMessage(EventMetadata.<Localization.Command.Sprite>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Sprite::nullAtlas)
                    .build()
            );

            return;
        }

        if (!atlasSpritesMap.containsKey(atlas)) {
            sendMessage(EventMetadata.<Localization.Command.Sprite>builder()
                    .sender(fPlayer)
                    .format(localization -> Strings.CS.replace(localization.atlasDownloading(), "<atlas>", atlas))
                    .build()
            );

            if (!downloadAtlasFile(atlas)) {
                sendErrorMessage(EventMetadata.<Localization.Command.Sprite>builder()
                        .sender(fPlayer)
                        .format(Localization.Command.Sprite::downloadError)
                        .build()
                );
                return;
            }

            lazyLoadLocalAtlases();
        }

        List<String> sprites = atlasSpritesMap.get(atlas);
        if (sprites == null || sprites.isEmpty()) {
            sendErrorMessage(EventMetadata.<Localization.Command.Sprite>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Sprite::nullAtlas)
                    .build()
            );

            return;
        }

        int size = sprites.size();

        String promptNumber = getPrompt(1);
        Optional<Integer> optionalNumber = commandContext.optional(promptNumber);
        int page = optionalNumber.orElse(1);

        int perPage = config().perPage();

        int countPage = (int) Math.ceil((double) size / perPage);
        if (page > countPage || page < 1) {
            sendErrorMessage(EventMetadata.<Localization.Command.Sprite>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Sprite::nullPage)
                    .build()
            );

            return;
        }

        List<String> finalSprites = sprites.stream()
                .skip((long) (page - 1) * perPage)
                .limit(perPage)
                .toList();

        String header = StringUtils.replaceEach(
                localization(fPlayer).header(),
                new String[]{"<atlas>", "<count>"},
                new String[]{atlas, String.valueOf(size)}
        );

        MessageContext headerContext = messagePipeline.createContext(fPlayer, header);
        Component component = messagePipeline.build(headerContext).append(Component.newline());

        StringBuilder spriteLine = new StringBuilder();
        for (String sprite : finalSprites) {
            String line = StringUtils.replaceEach(
                    localization(fPlayer).lineElement(),
                    new String[]{"<atlas>", "<sprite>"},
                    new String[]{atlas, sprite}
            );

            spriteLine.append(line);
        }

        MessageContext lineContext = messagePipeline.createContext(fPlayer, spriteLine.toString());
        component = component.append(messagePipeline.build(lineContext)).append(Component.newline());

        String commandLine = "/" + getCommandName() + " " + atlas;
        String footer = StringUtils.replaceEach(
                localization(fPlayer).footer(),
                new String[]{"<command>", "<prev_page>", "<next_page>", "<current_page>", "<last_page>"},
                new String[]{
                        commandLine,
                        String.valueOf(page - 1),
                        String.valueOf(page + 1),
                        String.valueOf(page),
                        String.valueOf(countPage)
                }
        );

        MessageContext footerContext = messagePipeline.createContext(fPlayer, footer);
        component = component.append(messagePipeline.build(footerContext));

        eventDispatcher.dispatch(new MessageSendEvent(MessageType.COMMAND_SPRITE, fPlayer, component));

        soundPlayer.play(soundOrThrow(), fPlayer);
    }

    @Override
    public MessageType messageType() {
        return MessageType.COMMAND_SPRITE;
    }

    @Override
    public Command.Sprite config() {
        return fileFacade.command().sprite();
    }

    @Override
    public Permission.Command.Sprite permission() {
        return fileFacade.permission().command().sprite();
    }

    @Override
    public Localization.Command.Sprite localization(FEntity sender) {
        return fileFacade.localization(sender).command().sprite();
    }

    private void lazyLoadLocalAtlases() {
        taskScheduler.runAsync(() -> {
            File atlasesFolder = resolveAtlasesFolder().toFile();
            if (!atlasesFolder.exists()) return;

            File[] atlases = atlasesFolder.listFiles();
            if (atlases == null) return;

            for (File atlas : atlases) {
                String atlasName = StringUtils.replaceEach(
                        atlas.getName(),
                        new String[]{"minecraft_textures_atlas_", ".png.txt"},
                        new String[]{"", ""}
                );

                if (atlasSpritesMap.containsKey(atlasName)) continue;

                try (Stream<String> lines = Files.lines(atlas.toPath(), StandardCharsets.UTF_8)) {
                    atlasSpritesMap.put(atlasName, lines.map(string -> RegExUtils.replaceAll((CharSequence) string, SPRITE_PATTERN, "")).toList());
                } catch (IOException e) {
                    fLogger.warning(e);
                }
            }
        });
    }

    private boolean downloadAtlasFile(String atlasName) {
        Path outputPath = resolveAtlasesFolder().resolve(Strings.CS.replace(ATLAS_FILE_NAME, "<atlas>", atlasName));
        if (Files.exists(outputPath)) return true;

        String url = StringUtils.replaceEach(
                FLECTONEPULSE_ATLAS_API,
                new String[]{"<version>", "<atlas>"},
                new String[]{platformServerAdapter.getServerVersionName(), atlasName}
        );

        return webUtil.downloadFile(url, outputPath);
    }

    private Path resolveAtlasesFolder() {
        return minecraftPath
                .resolve(platformServerAdapter.getServerVersionName())
                .resolve("atlases");
    }

}