package net.flectone.pulse.model.util;

import lombok.Getter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.HttpURLConnection;
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
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");

        BufferedImage bufferedImage = ImageIO.read(connection.getInputStream());
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
                text.append("<").append(hexColor).append(">").append(pixel);
                x += stepSize;
            }

            pixels.add(text.toString());
            y += stepSize;
            x = 0;
        }

        return pixels;
    }

    private int clamp(int value, int max) {
        return Math.clamp(value, 0, max);
    }
}
