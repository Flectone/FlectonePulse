package net.flectone.pulse.execution.pipeline;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.processing.serializer.ComponentSerializer;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.translation.GlobalTranslator;
import org.jspecify.annotations.NonNull;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class HytaleMessagePipeline extends MessagePipeline {

    private static final Map<String, Locale> LOCALE_CACHE = new ConcurrentHashMap<>();

    private final Provider<SocialService> socialServiceProvider;

    @Inject
    public HytaleMessagePipeline(FLogger fLogger,
                                 MiniMessage miniMessage,
                                 EventDispatcher eventDispatcher,
                                 ComponentSerializer componentSerializer,
                                 Provider<SocialService> socialServiceProvider) {
        super(fLogger, miniMessage, eventDispatcher, componentSerializer);

        this.socialServiceProvider = socialServiceProvider;
    }

    @Override
    public @NonNull Component build(MessageContext messageContext) {
        Component component = super.build(messageContext);
        if (Component.IS_NOT_EMPTY.test(component)) {
            return GlobalTranslator.render(component, getLocale(messageContext.receiver()));
        }

        return component;
    }

    public Locale getLocale(FPlayer fPlayer) {
        String locale = socialServiceProvider.get().getSetting(fPlayer, SettingText.LOCALE);
        if (locale == null) return Locale.ENGLISH;

        return LOCALE_CACHE.computeIfAbsent(locale, string -> Locale.forLanguageTag(string.replace('_', '-')));
    }

}
