package net.flectone.pulse.platform.formatter;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.util.file.FileFacade;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.text.Format;
import java.text.SimpleDateFormat;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TimeFormatter {

    // 1s = 20ticks -> 20ticks * 50 = 1000ms -> 1s = 1000ms
    public static final long MULTIPLIER = 50L;

    private static final Format SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final FileFacade fileFacade;

    public String format(FPlayer fPlayer, long time) {

        Localization.Time message = fileFacade.localization(fPlayer).time();
        if (message.format().isEmpty()) return "";

        String formattedTime = DurationFormatUtils.formatDuration(time, message.format(), false);

        StringBuilder result = new StringBuilder();
        for (String part : formattedTime.split(" ")) {
            if (isZeroComponent(part)) continue;

            result.append(simplify(part)).append(" ");
        }

        String finalResult = result.toString().trim();
        return finalResult.isEmpty() ? message.zero() : finalResult;
    }

    private boolean isZeroComponent(String part) {
        int unitIndex = 0;
        while (unitIndex < part.length() && (Character.isDigit(part.charAt(unitIndex))
                || part.charAt(unitIndex) == '.')
                || part.charAt(unitIndex) == ',') {
            unitIndex++;
        }

        try {
            double value = Double.parseDouble(part.substring(0, unitIndex));
            return value == 0.0;
        } catch (Exception e) {
            return false;
        }
    }

    private String simplify(String part) {
        int unitIndex = 0;
        while (unitIndex < part.length() && (Character.isDigit(part.charAt(unitIndex))
                || part.charAt(unitIndex) == '.')
                || part.charAt(unitIndex) == ',') {
            unitIndex++;
        }

        String numberPart = part.substring(0, unitIndex);
        if (numberPart.contains(".") || numberPart.contains(",")) {
            numberPart = numberPart
                    .replaceAll("0+$", "")
                    .replaceAll("[.,]$", "");
        }

        String unit = part.substring(unitIndex);
        return numberPart + unit;
    }

    public String format(FPlayer fPlayer, long time, String message) {
        if (time < 0) time = 0;
        return Strings.CS.replace(message, "<time>", format(fPlayer, time));
    }

    public String formatDate(long date) {
        return SIMPLE_DATE_FORMAT.format(date);
    }
}
