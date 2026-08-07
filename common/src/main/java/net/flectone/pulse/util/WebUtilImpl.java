package net.flectone.pulse.util;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.file.FileWriter;
import net.flectone.pulse.logging.FLogger;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class WebUtilImpl implements WebUtil {

    private final FLogger fLogger;

    @Override
    public int downloadFile(String fileUrl, Path outputPath, boolean warn404) {
        try {
            HttpURLConnection connection = createConnection(fileUrl);
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                fLogger.info("Downloading %s", fileUrl);

                try (InputStream inputStream = connection.getInputStream()) {
                    Files.createDirectories(outputPath.getParent());
                    Files.copy(inputStream, outputPath);
                    outputPath.toFile().setLastModified(FileWriter.LAST_MODIFIED_TIME);
                }

            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND && warn404) {
                fLogger.warning("Failed to download %s. HTTP response: %s - %s", outputPath.getFileName(), connection.getResponseCode(), fileUrl);
            }

            return responseCode;
        } catch (IOException _) {
            fLogger.warning("Failed to download %s file", outputPath.getFileName());
        }

        return -1;
    }

    @Override
    public HttpURLConnection createConnection(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
        connection.setRequestProperty("User-Agent", USER_AGENT);
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(15000);
        return connection;
    }

}
