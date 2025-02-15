package net.flectone.pulse.module.message.format.moderation.flood;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.util.PermissionUtil;

@Singleton
public class FloodModule extends AbstractModule {

    private final Message.Format.Moderation.Flood message;
    private final Permission.Message.Format.Moderation.Flood permission;

    private final PermissionUtil permissionUtil;

    @Inject
    public FloodModule(FileManager fileManager,
                       PermissionUtil permissionUtil) {
        this.permissionUtil = permissionUtil;

        message = fileManager.getMessage().getFormat().getModeration().getFlood();
        permission = fileManager.getPermission().getMessage().getFormat().getModeration().getFlood();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    public String replace(FEntity sender, String message) {
        if (checkModulePredicates(sender)) return message;
        if (permissionUtil.has(sender, permission.getBypass())) return message;
        if (message == null || message.isEmpty()) return message;

        message = replaceRepeatedSymbols(message);

        message = replaceRepeatedWords(message);

        return message;
    }

    private String replaceRepeatedSymbols(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        StringBuilder stringBuilder = new StringBuilder();
        char prevChar = text.charAt(0);
        int count = 1;

        for (int i = 1; i < text.length(); i++) {
            char currentChar = text.charAt(i);
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

        StringBuilder stringBuilder = new StringBuilder();
        String[] words = text.split(" ");
        String prevWord = words[0];
        int count = 1;

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
