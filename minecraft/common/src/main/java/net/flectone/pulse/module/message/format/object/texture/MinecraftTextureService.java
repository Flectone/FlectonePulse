package net.flectone.pulse.module.message.format.object.texture;

import com.alessiodp.libby.Library;
import com.alessiodp.libby.relocation.Relocation;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.module.message.format.object.texture.mineskin.MineskinIntegration;
import net.flectone.pulse.module.message.format.object.texture.model.Frame;
import net.flectone.pulse.module.message.format.object.texture.model.Texture;
import net.flectone.pulse.processing.resolver.LibraryResolver;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.file.FileWriter;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftTextureService {

    private static final int HEAD_SIZE = 8;
    private static final BufferedImage BLACK_SQUARE = new BufferedImage(HEAD_SIZE, HEAD_SIZE, BufferedImage.TYPE_BYTE_GRAY);

    private final Map<String, Component> textureMap = new ConcurrentHashMap<>();

    private final FileFacade fileFacade;
    private final @Named("imagePath") Path imagePath;
    private final Gson gson;
    private final FLogger fLogger;
    private final Injector injector;
    private final LibraryResolver libraryResolver;

    private MineskinIntegration mineskinIntegration;

    public void reload() {
        if (StringUtils.isNotEmpty(config().mineskinApiKey())) {
            if (mineskinIntegration == null) {
                libraryResolver.loadLibrary(Library.builder()
                        .groupId("org{}mineskin")
                        .artifactId("java-client-jsoup")
                        .version(BuildConfig.MINESKIN_API_VERSION)
                        .repository("https://repo.inventivetalent.org/repository/public/")
                        .resolveTransitiveDependencies(true)
                        .relocate(Relocation.builder()
                                .pattern("com{}google{}gson")
                                .relocatedPattern(BuildConfig.RELOCATED_PATTERN + ".gson")
                                .build()
                        )
                        .build()
                );
            }

            mineskinIntegration = injector.getInstance(MineskinIntegration.class);
            mineskinIntegration.hook();
        }

        // lazy clear
        textureMap.keySet().stream()
                .filter(key -> {
                    // remove empty image
                    String textureImage = config().values().get(key);
                    if (StringUtils.isEmpty(textureImage)) return true;

                    // json should always must be relevant
                    String textureFileName = getFileNameWithoutExtension(textureImage);
                    if (getJsonTexturePath(textureFileName).toFile().lastModified() != FileWriter.LAST_MODIFIED_TIME) return true;

                    // image may not exist, but if it does, it must be relevant
                    File textureFileImage = imagePath.resolve(textureImage).toFile();
                    return textureFileImage.exists() && textureFileImage.lastModified() != FileWriter.LAST_MODIFIED_TIME;
                })
                .forEach(textureMap::remove);

        config().values().forEach((key, value) -> {
            // skip empty image
            if (StringUtils.isEmpty(value)) return;

            // skip cached image
            if (textureMap.containsKey(key)) return;

            try {
                // load texture
                List<Frame> frames = loadTexture(value);

                // build component
                Component textureComponent = Component.empty();

                int lastFrameY = 0;
                for (Frame frame : frames) {
                    // next y = next line, example:
                    //
                    // |x1 y0| |x2 y0| |x3 y0|
                    // |x4 y1| |x5 y1| |x6 y1|
                    //
                    if (frame.y() != lastFrameY) {
                        textureComponent = textureComponent.append(Component.newline());
                        lastFrameY = frame.y();
                    }

                    textureComponent = textureComponent.append(Component.object().contents(
                            ObjectContents.playerHead()
                                    .profileProperty(PlayerHeadObjectContents.property(
                                            "textures",
                                            frame.value()
                                    ))
                                    .build()
                    ));

                }

                // save texture to cache
                textureMap.put(key, textureComponent);
            } catch (IllegalArgumentException e) {
                fLogger.warning(e.getMessage());
            } catch (IOException e) {
                fLogger.warning(e);
            }

        });
    }

    public void terminateMineskin() {
        if (mineskinIntegration != null && mineskinIntegration.isHooked()) {
            mineskinIntegration.unhook();
        }
    }

    @Nullable
    public Component getTexture(String key) {
        return textureMap.get(key);
    }

    public boolean isMineSkinHooked() {
        return mineskinIntegration != null && mineskinIntegration.isHooked();
    }

    private List<Frame> loadTexture(String textureFile) throws IOException {
        File textureFileImage = imagePath.resolve(textureFile).toFile();
        String textureFileName = getFileNameWithoutExtension(textureFile);

        // try load texture from .json
        Optional<Texture> optionalTexture = loadJsonTexture(textureFileName);
        if (optionalTexture.isPresent()) {
            Texture texture = optionalTexture.get();

            if (!texture.frames().isEmpty()
                    // if image has been changed, then our json is out of date
                    && (!textureFileImage.exists() || texture.lastModified() == textureFileImage.lastModified()) || !isMineSkinHooked()) {
                return texture.frames();
            }
        }

        // try load texture from image
        List<Frame> frames = loadImageTexture(textureFileImage);
        if (!frames.isEmpty()) {
            saveJsonTexture(textureFileImage.lastModified(), frames, textureFileName);
        }

        return frames;
    }

    private List<Frame> loadImageTexture(File textureFileImage) throws IOException {
        if (!isMineSkinHooked()) return Collections.emptyList();
        if (!textureFileImage.exists()) return Collections.emptyList();

        // update last modified time image
        setLastModified(textureFileImage);

        // read image
        BufferedImage original = ImageIO.read(textureFileImage);

        // check dimensions
        if (original.getWidth() % HEAD_SIZE != 0 || original.getHeight() % HEAD_SIZE != 0) {
            throw new IllegalArgumentException(textureFileImage.getName() + " image dimensions must be multiples of 8");
        }

        int framesX = original.getWidth() / HEAD_SIZE;
        int framesY = original.getHeight() / HEAD_SIZE;

        List<CompletableFuture<Frame>> futures = new ObjectArrayList<>(framesX * framesY);

        for (int y = 0; y < framesY; y++) {
            for (int x = 0; x < framesX; x++) {
                BufferedImage headPart = createHead(original, x, y);
                BufferedImage skinImage = createSkin(headPart);

                futures.add(mineskinIntegration.loadTexture(x, y, skinImage));
            }
        }

        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }


    private Optional<Texture> loadJsonTexture(String textureFileName) throws IOException {
        // get json file path
        Path texturePathJson = getJsonTexturePath(textureFileName);

        // check file exists
        File textureFileJson = texturePathJson.toFile();
        if (!textureFileJson.exists()) return Optional.empty();

        // update last modified time
        setLastModified(textureFileJson);

        // read json file to texture
        String json = Files.readString(texturePathJson, StandardCharsets.UTF_8);
        return Optional.of(gson.fromJson(json, Texture.class));
    }

    private void saveJsonTexture(long lastModified, List<Frame> frames, String textureFileName) throws IOException {
        // create texture json
        Texture texture = new Texture(lastModified, frames);
        String json = gson.toJson(texture);

        // get json file path
        Path jsonTexturePath = getJsonTexturePath(textureFileName);

        // write json
        Files.writeString(jsonTexturePath, json, StandardCharsets.UTF_8);

        // update last modified time
        setLastModified(jsonTexturePath.toFile());
    }

    private Path getJsonTexturePath(String textureFileName) {
        return imagePath.resolve(textureFileName + ".json");
    }

    private void setLastModified(File file) {
        file.setLastModified(FileWriter.LAST_MODIFIED_TIME);
    }

    private String getFileNameWithoutExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex != -1 ? fileName.substring(0, lastDotIndex) : fileName;
    }

    private BufferedImage createHead(BufferedImage original, int x, int y) {
        BufferedImage head = original.getSubimage(
                x * HEAD_SIZE,
                y * HEAD_SIZE,
                HEAD_SIZE,
                HEAD_SIZE
        );

        // check for a full empty square, because image must have at least 1 colored pixel
        for (int headY = 0; headY < head.getHeight(); headY++) {
            for (int headX = 0; headX < head.getWidth(); headX++) {
                int pixel = head.getRGB(headX, headY);
                if ((pixel >> 24) != 0x00) {
                    return head;
                }
            }
        }

        return BLACK_SQUARE;
    }

    private BufferedImage createSkin(BufferedImage headPart) {
        BufferedImage skin = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = skin.createGraphics();

        graphics.setComposite(AlphaComposite.Clear);
        graphics.fillRect(0, 0, 64, 64);
        graphics.setComposite(AlphaComposite.SrcOver);

        graphics.drawImage(headPart, 8, 8, null);

        graphics.dispose();

        return skin;
    }

    private Message.Format.Object.TextureTag config() {
        return fileFacade.message().format().object().textureTag();
    }

}