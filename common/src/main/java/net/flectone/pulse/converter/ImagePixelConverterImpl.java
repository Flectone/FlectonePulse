package net.flectone.pulse.converter;

import com.github.benmanes.caffeine.cache.Cache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.parser.string.URLParser;
import net.flectone.pulse.util.WebUtil;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ImagePixelConverterImpl implements ImagePixelConverter {

    private static final int MAX_DIMENSION = 8 * 1024 * 1024;

    private final URLParser urlParser;
    private final @Named("imagePixels") Cache<String, List<String>> imagePixelsCache;

    @Override
    public void invalidate(@Nullable String link) {
        if (link == null) return;

        imagePixelsCache.invalidate(link);
    }

    @Override
    public @NonNull List<String> convertOrGetCache(@Nullable String link) {
        if (link == null) return List.of();

        List<String> pixels = imagePixelsCache.getIfPresent(link);
        if (pixels != null) return pixels;

        pixels = convert(link);
        imagePixelsCache.put(link, pixels);

        return pixels;
    }

    // Idea taken from here
    // https://github.com/QuiltServerTools/BlockBot/blob/5d5fa854002de2c12200edbe22f12382350ca7eb/src/main/kotlin/io/github/quiltservertools/blockbotdiscord/extensions/BlockBotApiExtension.kt#L136
    @Override
    public @NonNull List<String> convert(@Nullable String link) {
        Optional<URL> url = urlParser.parse(link);
        if (url.isEmpty()) return List.of();

        Optional<BufferedImage> optionalBufferedImage = readImage(url.get());
        if (optionalBufferedImage.isEmpty()) return List.of();

        BufferedImage bufferedImage = optionalBufferedImage.get();

        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        int stepSize = Math.max((int) Math.ceil(width / 48.0), 1);
        int stepSquared = stepSize * stepSize;

        int x = 0;
        int y = 0;

        List<String> pixels = new ArrayList<>();

        while (y < height) {
            StringBuilder text = new StringBuilder();
            while (x < width) {
                int rgb;

                if (stepSize != 1) {
                    int r = 0;
                    int g = 0;
                    int b = 0;

                    for (int x2 = 0; x2 < stepSize; x2++) {
                        for (int y2 = 0; y2 < stepSize; y2++) {
                            int color = bufferedImage.getRGB(clamp(x + x2, width - 1), clamp(y + y2, height - 1));
                            r += (color >> 16) & 0xFF;
                            g += (color >> 8) & 0xFF;
                            b += color & 0xFF;
                        }
                    }

                    rgb = ((r / stepSquared) << 16) | ((g / stepSquared) << 8) | (b / stepSquared);
                } else {
                    rgb = bufferedImage.getRGB(x, y) & 0xFFFFFF;
                }

                String hexColor = String.format("#%06x", rgb);
                String pixel = "█";
                text.append("<").append(hexColor).append(">").append(pixel);
                x += stepSize;
            }

            pixels.add(text.toString());
            y += stepSize;
            x = 0;
        }

        return List.copyOf(pixels);
    }

    private Optional<BufferedImage> readImage(URL url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("User-Agent", WebUtil.USER_AGENT);

            try (InputStream inputStream = connection.getInputStream();
                 ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream)) {
                Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInputStream);
                if (!readers.hasNext()) return Optional.empty();

                ImageReader reader = readers.next();
                reader.setInput(imageInputStream);

                long pixels = (long) reader.getWidth(0) * reader.getHeight(0);
                if (pixels >= MAX_DIMENSION) {
                    reader.dispose();
                    return Optional.empty();
                }

                BufferedImage image = reader.read(0);
                reader.dispose();

                return Optional.of(image);
            }
        } catch (IOException _) {
            return Optional.empty();
        }
    }

    private int clamp(int value, int max) {
        return Math.clamp(value, 0, max);
    }

}
