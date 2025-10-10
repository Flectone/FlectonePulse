package net.flectone.pulse.util;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.util.logging.FLogger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Base64;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class IconUtil {

    private final FLogger fLogger;

    public String convertIcon(File icon) {
        try {

            BufferedImage bufferedImage = ImageIO.read(icon);
            if (bufferedImage.getHeight() != 64 || bufferedImage.getWidth() != 64) {
                fLogger.warning("Image " + icon.getName() + " size must be 64x64");
                return null;
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "PNG", out);
            byte[] bytes = out.toByteArray();

            return new String(Base64.getEncoder().encode(bytes));

        } catch (Exception e) {
            fLogger.warning("Failed to load " + icon.getName(), e);
        }

        return null;
    }
}
