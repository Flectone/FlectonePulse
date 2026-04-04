package net.flectone.pulse.util;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.util.file.FileWriter;
import net.flectone.pulse.util.logging.FLogger;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class WebUtil {

    public static final String USER_AGENT = "FlectonePulse/1.0";

    private final FLogger fLogger;

    public boolean downloadFile(String fileUrl, Path outputPath) {
        try {
            HttpURLConnection connection = createConnection(fileUrl);
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                fLogger.info("Downloading %s", fileUrl);

                try (InputStream inputStream = connection.getInputStream()) {
                    Files.createDirectories(outputPath.getParent());
                    Files.copy(inputStream, outputPath);
                    outputPath.toFile().setLastModified(FileWriter.LAST_MODIFIED_TIME);
                    return true;
                }
            }

            fLogger.warning("Failed to download %s. HTTP response: %s - %s", outputPath.getFileName(), connection.getResponseCode(), fileUrl);
        } catch (IOException _) {
            fLogger.warning("Failed to download %s file", outputPath.getFileName());
        }

        return false;
    }

    public HttpURLConnection createConnection(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
        connection.setRequestProperty("User-Agent", USER_AGENT);
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(15000);
        return connection;
    }

}
