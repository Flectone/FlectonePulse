package net.flectone.pulse.module.message.format.moderation.flood.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.constant.MessageFlag;
import net.flectone.pulse.context.MessageContext;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.message.format.moderation.flood.FloodModule;
import net.flectone.pulse.resolver.FileResolver;

@Singleton
public class FloodPulseListener implements PulseListener {

    private final Message.Format.Moderation.Flood message;
    private final Permission.Message.Format.Moderation.Flood permission;
    private final FloodModule floodModule;
    private final PermissionChecker permissionChecker;

    @Inject
    public FloodPulseListener(FileResolver fileResolver,
                              FloodModule floodModule,
                              PermissionChecker permissionChecker) {
        this.message = fileResolver.getMessage().getFormat().getModeration().getFlood();
        this.permission = fileResolver.getPermission().getMessage().getFormat().getModeration().getFlood();
        this.floodModule = floodModule;
        this.permissionChecker = permissionChecker;
    }

    @Pulse
    public void onMessageProcessingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.getContext();
        if (!messageContext.isFlag(MessageFlag.FLOOD)) return;
        if (!messageContext.isFlag(MessageFlag.USER_MESSAGE)) return;

        String processedMessage = replace(messageContext.getSender(), messageContext.getMessage());
        messageContext.setMessage(processedMessage);
    }

    private String replace(FEntity sender, String string) {
        if (floodModule.checkModulePredicates(sender)) return string;
        if (permissionChecker.check(sender, permission.getBypass())) return string;
        if (string == null || string.isEmpty()) return string;

        string = replaceRepeatedSymbols(string);

        string = replaceRepeatedWords(string);

        return string;
    }

    private String replaceRepeatedSymbols(String string) {
        if (string == null || string.isEmpty()) {
            return string;
        }

        StringBuilder stringBuilder = new StringBuilder();
        char prevChar = string.charAt(0);
        int count = 1;

        for (int i = 1; i < string.length(); i++) {
            char currentChar = string.charAt(i);
            if (currentChar == prevChar) {
                count++;
                continue;
            }

            appendSymbol(stringBuilder, prevChar, count);
            prevChar = currentChar;
            count = 1;
        }

        appendSymbol(stringBuilder, prevChar, count);

        return stringBuilder.toString();
    }

    private void appendSymbol(StringBuilder stringBuilder, char symbol, int count) {
        int counts = count > message.getMaxRepeatedSymbols()
                ? message.isTrimToSingle() ? 1 : message.getMaxRepeatedSymbols()
                : count;

        stringBuilder.append(String.valueOf(symbol).repeat(counts));
    }

    private String replaceRepeatedWords(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String[] words = text.split(" ");
        if (words.length == 0) return text;

        String prevWord = words[0];
        int count = 1;

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 1; i < words.length; i++) {
            String currentWord = words[i];
            if (currentWord.equalsIgnoreCase(prevWord)) {
                count++;
            } else {
                appendWord(stringBuilder, prevWord, count);
                prevWord = currentWord;
                count = 1;
            }
        }

        appendWord(stringBuilder, prevWord, count);

        return stringBuilder.toString().trim();
    }

    private void appendWord(StringBuilder stringBuilder, String word, int count) {
        int counts = count > message.getMaxRepeatedWords()
                ? message.isTrimToSingle() ? 1 : message.getMaxRepeatedWords()
                : count;

        while (counts > 0) {
            counts--;
            stringBuilder
                    .append(word)
                    .append(" ");
        }
    }
}
