package net.flectone.pulse.module.message.vanilla.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hypixel.hytale.protocol.*;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.file.FileFacade;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class HytaleComponentExtractor extends ComponentExtractor<FormattedMessage> {

    private final FPlayerService fPlayerService;

    @Inject
    public HytaleComponentExtractor(FileFacade fileFacade,
                                    FPlayerService fPlayerService) {
        super(fileFacade);

        this.fPlayerService = fPlayerService;
    }

    @Override
    public String extractTranslationKey(FormattedMessage message) {
        if (StringUtils.isNotEmpty(message.messageId)) {
            return message.messageId;
        }

        if (StringUtils.isNotEmpty(message.rawText)) {
            return message.rawText;
        }

        if (message.children != null) {
            for (FormattedMessage child : message.children) {
                String childKey = extractTranslationKey(child);
                if (childKey != null && !childKey.isEmpty()) {
                    return childKey;
                }
            }
        }

        return null;
    }

    // arguments are not in order in which they are written in final message, this must be understood
    @Override
    public Map<Integer, Object> extractArguments(FormattedMessage message) {
        Map<Integer, Object> arguments = new LinkedHashMap<>();

        int index = 0;

        if (message.children != null) {
            for (FormattedMessage child : message.children) {
                Object value = extractFormattedMessageValue(child);
                if (value != null) {
                    arguments.put(index++, value);
                }
            }
        }

        if (message.messageParams != null) {
            List<String> sortedKeys = new ArrayList<>(message.messageParams.keySet());

            for (String key : sortedKeys) {
                Object value = extractFormattedMessageValue(message.messageParams.get(key));
                if (value != null) {
                    arguments.put(index++, value);
                }
            }
        }

        if (message.params != null) {
            List<String> sortedKeys = new ArrayList<>(message.params.keySet());

            for (String key : sortedKeys) {
                Object value = extractParamValue(message.params.get(key));
                if (value != null) {
                    arguments.put(index++, value);
                }
            }
        }

        return arguments;
    }

    private Object extractFormattedMessageValue(FormattedMessage message) {
        if (StringUtils.isNotEmpty(message.rawText)) {
            return message.rawText;
        }

        if (StringUtils.isNotEmpty(message.messageId)) {
            return message.messageId;
        }

        if (message.params != null && !message.params.isEmpty()) {
            ParamValue firstParam = message.params.values().iterator().next();
            return extractParamValue(firstParam);
        }

        if (message.messageParams != null && !message.messageParams.isEmpty()) {
            FormattedMessage firstParam = message.messageParams.values().iterator().next();
            return extractFormattedMessageValue(firstParam);
        }

        if (message.children != null && message.children.length != 0) {
            return extractFormattedMessageValue(message.children[0]);
        }

        return null;
    }

    private Object extractParamValue(ParamValue param) {
        return switch (param) {
            case StringParamValue stringParamValue -> {
                FPlayer fPlayer = fPlayerService.getFPlayer(stringParamValue.value);
                yield fPlayer.isUnknown() ? stringParamValue.value : fPlayer;
            }
            case BoolParamValue boolParamValue -> boolParamValue.value;
            case IntParamValue intParamValue -> intParamValue.value;
            case LongParamValue longParamValue -> longParamValue.value;
            case DoubleParamValue doubleParamValue -> doubleParamValue.value;
            default -> null;
        };
    }
}