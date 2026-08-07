package net.flectone.pulse.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Path;

/**
 * Small helpers for the plugin's outgoing http requests.
 * @author TheFaser
 */
public interface WebUtil {

    /**
     * The user agent sent with every outgoing request.
     */
    String USER_AGENT = "FlectonePulse/1.0";

    /**
     * Downloads a file to disk.
     *
     * @param fileUrl the url
     * @param outputPath where to write it
     * @param warn404 whether a missing file is worth a log line
     * @return the http status code, or -1 if the request never completed
     */
    int downloadFile(String fileUrl, Path outputPath, boolean warn404);

    /**
     * Opens a connection carrying the plugin's user agent.
     *
     * @param url the url
     * @return the open connection
     * @throws IOException if the connection cannot be opened
     */
    HttpURLConnection createConnection(String url) throws IOException;

}
