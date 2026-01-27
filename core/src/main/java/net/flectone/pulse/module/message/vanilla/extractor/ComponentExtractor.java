package net.flectone.pulse.module.message.vanilla.extractor;

import net.flectone.pulse.config.Message;
import net.flectone.pulse.module.message.vanilla.model.ParsedComponent;
import net.flectone.pulse.util.file.FileFacade;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class ComponentExtractor<T> {

    private final Map<String, Message.Vanilla.VanillaMessage> translationVanillaMessages = new HashMap<>();

    private final FileFacade fileFacade;

    protected ComponentExtractor(FileFacade fileFacade) {
        this.fileFacade = fileFacade;
    }

    public void reload() {
        translationVanillaMessages.clear();

        List<Message.Vanilla.VanillaMessage> vanillaMessages = fileFacade.message().vanilla().types();

        vanillaMessages.forEach(vanillaMessage -> vanillaMessage.translationKeys()
                .forEach(translationKey -> translationVanillaMessages.put(translationKey, vanillaMessage))
        );
    }

    public Optional<ParsedComponent> extract(T message) {
        String translationKey = extractTranslationKey(message);
        if (StringUtils.isEmpty(translationKey)) return Optional.empty();

        Map<String, String> localization = fileFacade.localization().message().vanilla().types();
        if (!localization.containsKey(translationKey)) return Optional.empty();

        Message.Vanilla.VanillaMessage vanillaMessage = getVanillaMessage(translationKey);

        Map<Integer, Object> arguments = extractArguments(message);

        return Optional.of(new ParsedComponent(translationKey, vanillaMessage, arguments));
    }

    public Message.Vanilla.VanillaMessage getVanillaMessage(String translationKey) {
        return translationVanillaMessages.getOrDefault(translationKey, Message.Vanilla.VanillaMessage.builder().build());
    }

    public abstract String extractTranslationKey(T message);

    public abstract Map<Integer, Object> extractArguments(T message);

}
