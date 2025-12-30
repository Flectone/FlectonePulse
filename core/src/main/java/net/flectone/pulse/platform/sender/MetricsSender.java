package net.flectone.pulse.platform.sender;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.dto.MetricsDTO;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

/**
 * Sends anonymous metrics data to FlectonePulse servers.
 *
 * <p><b>Usage example:</b>
 * <pre>{@code
 * MetricsSender metricsSender = flectonePulse.get(MetricsSender.class);
 *
 * MetricsDTO metrics = ...;
 *
 * metricsSender.sendMetrics(metrics);
 * }</pre>
 *
 * @since 0.8.1
 * @author TheFaser
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MetricsSender {

    private static final String FLECTONEPULSE_API_URL = "https://flectone.net/api/pulse/metrics";

    private final Gson gson;

    /**
     * Sends metrics data to FlectonePulse API.
     * Failures are silently ignored to prevent affecting server performance.
     *
     * @param metrics the metrics data to send
     */
    public void sendMetrics(MetricsDTO metrics) {
        try {
            String jsonData = gson.toJson(metrics);

            URL url = new URI(FLECTONEPULSE_API_URL).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Length", String.valueOf(jsonData.getBytes(StandardCharsets.UTF_8).length));
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Content-Encoding", "gzip");
            connection.setDoOutput(true);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            try (OutputStream os = connection.getOutputStream();
                 GZIPOutputStream gzipOS = new GZIPOutputStream(os)) {
                gzipOS.write(jsonData.getBytes(StandardCharsets.UTF_8));
            }

            connection.disconnect();
            connection.getResponseCode();

        } catch (Exception ignored) {
            // ignore errors
        }
    }

}