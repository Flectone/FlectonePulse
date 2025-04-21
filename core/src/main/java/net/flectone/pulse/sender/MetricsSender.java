package net.flectone.pulse.sender;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.database.dto.MetricsDTO;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

@Singleton
public class MetricsSender {

    private final String API_URL = "https://flectone.net/api/pulse/metrics";

    private final Gson gson;

    @Inject
    public MetricsSender(Gson gson) {
        this.gson = gson;
    }

    public void sendMetrics(MetricsDTO metrics) {
        try {
            String jsonData = gson.toJson(metrics);

            URL url = new URL(API_URL);
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

        } catch (Exception ignored) {}
    }

}