package net.flectone.pulse.platform.formatter;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.constant.SettingText;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.service.SocialService;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TimeFormatterImpl implements TimeFormatter {

    private final Map<Localization.Time, DateTimeFormatter> dateFormatters = new ConcurrentHashMap<>();

    private final FileFacade fileFacade;
    private final SocialService socialService;

    @Override
    public String format(FPlayer fPlayer, long time) {
        if (time < 0) {
            time = 0;
        }

        Localization.Time localization = localization(fPlayer);
        if (localization.format().isEmpty()) return "";

        String formattedTime = DurationFormatUtils.formatDuration(time, localization.format(), false);

        StringBuilder result = new StringBuilder();
        for (String part : formattedTime.split(" ")) {
            if (isZeroComponent(part)) continue;

            result.append(simplify(part)).append(" ");
        }

        String finalResult = result.toString().trim();
        return finalResult.isEmpty() ? localization.zero() : finalResult;
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
        } catch (Exception _) {
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

    @Override
    public String format(FPlayer fPlayer, long time, String message) {
        return Strings.CS.replace(message, "<time>", format(fPlayer, time));
    }

    @Override
    public String formatDate(FPlayer fPlayer, long date) {
        return dateFormatters
                .computeIfAbsent(localization(fPlayer), localization -> DateTimeFormatter
                        .ofPattern(localization.date())
                        .withZone(ZoneId.systemDefault())
                )
                .format(Instant.ofEpochMilli(date));
    }

    @Override
    public void invalidate() {
        dateFormatters.clear();
    }

    private Localization.Time localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).time();
    }
}
