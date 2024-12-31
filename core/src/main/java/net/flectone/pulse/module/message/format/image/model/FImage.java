package net.flectone.pulse.module.message.format.image.model;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Getter
public class FImage {

    private final String urlString;
    private final String name;

    public FImage(String urlString) {
        this.urlString = urlString;
        this.name = urlString.substring(urlString.lastIndexOf('/')+1);
    }

    // Idea taken from here
    // https://github.com/QuiltServerTools/BlockBot/blob/5d5fa854002de2c12200edbe22f12382350ca7eb/src/main/kotlin/io/github/quiltservertools/blockbotdiscord/extensions/BlockBotApiExtension.kt#L136
    public List<String> convertImageUrl() throws IOException {
        URL url = new URL(urlString);

        BufferedImage bufferedImage = ImageIO.read(url);
        if (bufferedImage == null) return null;

        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        if (height * width >= 8 * 1024 * 1024) return null;

        int stepSize = Math.max((int) Math.ceil(bufferedImage.getWidth() / 48.0), 1);
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
                text.append("<color:").append(hexColor).append(">").append(pixel);
                x += stepSize;
            }

            pixels.add(text.toString());
            y += stepSize;
            x = 0;
        }

        return pixels;
    }

    public Component convertImageUrlT() throws IOException {
        URL url = new URL(urlString);

        BufferedImage bufferedImage = ImageIO.read(url);
        if (bufferedImage == null) return null;

        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        if (height * width >= 8 * 1024 * 1024) return null;

        int stepSize = Math.max((int) Math.ceil(bufferedImage.getWidth() / 48.0), 1);
        int stepSquared = stepSize * stepSize;

        int x = 0;
        int y = 0;

        Component component = Component.newline();

        while (y < height) {
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
                component = component.append(Component.text(pixel).color(TextColor.fromHexString(hexColor)));
                x += stepSize;
            }

            component = component.appendNewline();
            y += stepSize;
            x = 0;
        }

        return component;
    }

    private int clamp(int value, int max) {
        return Math.max(0, Math.min(value, max));
    }
}
