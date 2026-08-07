package net.flectone.pulse.module.integration.icu;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ibm.icu.lang.UScript;
import com.ibm.icu.text.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ICUIntegration implements FIntegration {

    private final Bidi bidi = new Bidi();
    private final Locale localeThai = new Locale("th");
    private final Locale localeKhmer = new Locale("km");
    private final Locale localeLao = new Locale("lo");
    private final Locale localeTibetan = new Locale("bo");

    @Getter private final FLogger fLogger;
    private final FileFacade fileFacade;

    @Override
    public String getIntegrationName() {
        return "ICU";
    }

    public String process(@Nullable String text) {
        if (StringUtils.isEmpty(text)) return text;

        Integration.Icu config = config();
        if (config.normalization().enable()) {
            text = applyNormalization(text);
        }

        if (config.arabicShaping().enable() && containsScript(text, UScript.ARABIC)) {
            text = applyArabicShaping(text);
        }

        if (config.bidi().enable()) {
            text = applyBidi(text);
        }

        if (config.wordBreaking().enable()) {
            text = applyWordBreaking(text);
        }

        return text;
    }

    private String applyBidi(String text) {
        bidi.setPara(text, Bidi.LEVEL_DEFAULT_LTR, null);
        return bidi.writeReordered(Bidi.DO_MIRRORING);
    }

    private String applyArabicShaping(String text) {
        int options = ArabicShaping.LETTERS_SHAPE
                | (config().bidi().enable() ? ArabicShaping.TEXT_DIRECTION_VISUAL_RTL : ArabicShaping.TEXT_DIRECTION_VISUAL_LTR)
                | (config().arabicShaping().numerals() ? ArabicShaping.DIGITS_EN2AN : ArabicShaping.DIGITS_NOOP);

        try {
            return new ArabicShaping(options).shape(text);
        } catch (ArabicShapingException e) {
            fLogger.warning("ICU ArabicShaping failed: ", e.getMessage());
            return text;
        }
    }

    private String applyWordBreaking(String text) {
        Integration.Icu.WordBreaking config = config().wordBreaking();
        String breakChar = StringUtils.isNotEmpty(config.breakCharacter()) ? config.breakCharacter() : " ";

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); ) {
            int codePointAt = text.codePointAt(i);
            int script = UScript.getScript(codePointAt);

            boolean needsBreak = switch (script) {
                case UScript.THAI -> config().wordBreaking().thai();
                case UScript.KHMER -> config.khmer();
                case UScript.LAO -> config.lao();
                case UScript.TIBETAN -> config.tibetan();
                default -> false;
            };

            if (needsBreak) {
                int start = i;
                while (i < text.length()) {
                    int segCp = text.codePointAt(i);
                    if (UScript.getScript(segCp) != script) {
                        break;
                    }

                    i += Character.charCount(segCp);
                }

                String segment = text.substring(start, i);
                result.append(breakSegment(segment, script, breakChar));
            } else {
                result.appendCodePoint(codePointAt);
                i += Character.charCount(codePointAt);
            }
        }

        return result.toString();
    }

    private String breakSegment(String segment, int script, String breakChar) {
        Locale locale = scriptToLocale(script);
        BreakIterator wordInstance = BreakIterator.getWordInstance(locale);
        wordInstance.setText(segment);

        StringBuilder stringBuilder = new StringBuilder();
        int start = wordInstance.first();
        for (int end = wordInstance.next(); end != BreakIterator.DONE; start = end, end = wordInstance.next()) {
            if (!stringBuilder.isEmpty()) {
                stringBuilder.append(breakChar);
            }

            stringBuilder.append(segment, start, end);
        }

        return stringBuilder.toString();
    }

    private Locale scriptToLocale(int script) {
        return switch (script) {
            case UScript.THAI -> localeThai;
            case UScript.KHMER -> localeKhmer;
            case UScript.LAO -> localeLao;
            case UScript.TIBETAN -> localeTibetan;
            default -> Locale.ROOT;
        };
    }

    private boolean containsScript(String text, int targetScript) {
        for (int i = 0; i < text.length(); ) {
            int codePointAt = text.codePointAt(i);
            if (UScript.getScript(codePointAt) == targetScript) {
                return true;
            }

            i += Character.charCount(codePointAt);
        }

        return false;
    }

    private String applyNormalization(String text) {
        Normalizer2 normalizer = switch (config().normalization().form()) {
            case NFC -> Normalizer2.getNFCInstance();
            case NFD -> Normalizer2.getNFDInstance();
            case NFKC -> Normalizer2.getNFKCInstance();
            case NFKD -> Normalizer2.getNFKDInstance();
        };

        return normalizer.normalize(text);
    }

    public Integration.Icu config() {
        return fileFacade.integration().icu();
    }

}