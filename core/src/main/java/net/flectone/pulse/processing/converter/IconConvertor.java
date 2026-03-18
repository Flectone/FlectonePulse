package net.flectone.pulse.processing.converter;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.util.logging.FLogger;
import org.jspecify.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Base64;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class IconConvertor {

    private final FLogger fLogger;

    @Nullable
    public String convert(File icon) {
        if (!icon.exists()) return null;

        try {

            BufferedImage bufferedImage = ImageIO.read(icon);
            if (bufferedImage.getHeight() != 64 || bufferedImage.getWidth() != 64) {
                fLogger.warning("Image %s size must be 64x64", icon.getName());
                return null;
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "PNG", out);
            byte[] bytes = out.toByteArray();

            return new String(Base64.getEncoder().encode(bytes));

        } catch (Exception e) {
            fLogger.warning(e, "Failed to load %s", icon.getName());
        }

        return null;
    }
}
