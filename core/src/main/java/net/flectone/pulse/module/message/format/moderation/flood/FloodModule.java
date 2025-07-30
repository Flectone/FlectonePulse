package net.flectone.pulse.module.message.format.moderation.flood;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.constant.MessageFlag;
import net.flectone.pulse.context.MessageContext;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.processor.MessageProcessor;
import net.flectone.pulse.registry.MessageProcessRegistry;

@Singleton
public class FloodModule extends AbstractModule implements MessageProcessor {

    private final Message.Format.Moderation.Flood message;
    private final Permission.Message.Format.Moderation.Flood permission;
    private final PermissionChecker permissionChecker;
    private final MessageProcessRegistry messageProcessRegistry;

    @Inject
    public FloodModule(FileResolver fileResolver,
                       PermissionChecker permissionChecker,
                       MessageProcessRegistry messageProcessRegistry) {
        this.message = fileResolver.getMessage().getFormat().getModeration().getFlood();
        this.permission = fileResolver.getPermission().getMessage().getFormat().getModeration().getFlood();
        this.permissionChecker = permissionChecker;
        this.messageProcessRegistry = messageProcessRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        messageProcessRegistry.register(100, this);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Override
    public void process(MessageContext messageContext) {
        if (!messageContext.isFlag(MessageFlag.FLOOD)) return;
        if (!messageContext.isFlag(MessageFlag.USER_MESSAGE)) return;

        String processedMessage = replace(messageContext.getSender(), messageContext.getMessage());
        messageContext.setMessage(processedMessage);
    }

    private String replace(FEntity sender, String message) {
        if (checkModulePredicates(sender)) return message;
        if (permissionChecker.check(sender, permission.getBypass())) return message;
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
