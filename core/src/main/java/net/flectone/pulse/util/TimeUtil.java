package net.flectone.pulse.util;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import org.apache.commons.lang.time.DurationFormatUtils;

import java.text.Format;
import java.text.SimpleDateFormat;

@Singleton
public class TimeUtil {

    private final Format DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final FileManager fileManager;

    @Inject
    public TimeUtil(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    public String format(FPlayer fPlayer, long time) {

        Localization.Time message = fileManager.getLocalization(fPlayer).getTime();
        if (message.getFormat().isEmpty()) return "";

        String formattedTime = DurationFormatUtils.formatDuration(time, message.getFormat(), false);
        formattedTime = formattedTime.replaceAll("(?<!\\d)0(\\.\\d+)?\\p{L}", "").trim();

        return formattedTime.isEmpty() ? message.getZero() : formattedTime;
    }

    public String format(FPlayer fPlayer, long time, String message) {
        if (time < 0) time = 0;
        return message.replace("<time>", format(fPlayer, time));
    }

    public String formatDate(long date) {
        return DATE_FORMATTER.format(date);
    }
}
