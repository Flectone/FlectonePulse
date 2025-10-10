package net.flectone.pulse.module.message.format.moderation.flood;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.format.moderation.flood.listener.FloodPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageFlag;
import org.apache.commons.lang3.StringUtils;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FloodModule extends AbstractModule {

    private final FileResolver fileResolver;
    private final PermissionChecker permissionChecker;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(FloodPulseListener.class);
    }

    @Override
    public Message.Format.Moderation.Flood config() {
        return fileResolver.getMessage().getFormat().getModeration().getFlood();
    }

    @Override
    public Permission.Message.Format.Moderation.Flood permission() {
        return fileResolver.getPermission().getMessage().getFormat().getModeration().getFlood();
    }

    public void format(MessageContext messageContext) {
        if (!messageContext.isFlag(MessageFlag.USER_MESSAGE)) return;

        FEntity sender = messageContext.getSender();
        if (isModuleDisabledFor(sender)) return;
        if (permissionChecker.check(sender, permission().getBypass())) return;

        String contextMessage = messageContext.getMessage();
        if (StringUtils.isEmpty(contextMessage)) return;

        contextMessage = replaceRepeatedSymbols(contextMessage);
        contextMessage = replaceRepeatedWords(contextMessage);

        messageContext.setMessage(contextMessage);
    }

    private String replaceRepeatedSymbols(String string) {
        if (StringUtils.isEmpty(string)) return string;

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
        int counts = count > config().getMaxRepeatedSymbols()
                ? config().isTrimToSingle() ? 1 : config().getMaxRepeatedSymbols()
                : count;

        stringBuilder.append(String.valueOf(symbol).repeat(counts));
    }

    private String replaceRepeatedWords(String text) {
        if (StringUtils.isEmpty(text)) return text;

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
        int counts = count > config().getMaxRepeatedWords()
                ? config().isTrimToSingle() ? 1 : config().getMaxRepeatedWords()
                : count;

        while (counts > 0) {
            counts--;
            stringBuilder
                    .append(word)
                    .append(" ");
        }
    }
}
