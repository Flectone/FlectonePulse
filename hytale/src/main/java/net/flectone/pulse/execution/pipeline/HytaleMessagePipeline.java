package net.flectone.pulse.execution.pipeline;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.translation.GlobalTranslator;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class HytaleMessagePipeline extends MessagePipeline {

    private static final Map<String, Locale> LOCALE_CACHE = new ConcurrentHashMap<>();

    @Inject
    public HytaleMessagePipeline(FLogger fLogger,
                                 MiniMessage miniMessage,
                                 EventDispatcher eventDispatcher) {
        super(fLogger, miniMessage, eventDispatcher);
    }

    @Override
    public Component build(MessageContext context) {
        Component component = super.build(context);
        if (Component.IS_NOT_EMPTY.test(component)) {
            return GlobalTranslator.render(component, getLocale(context.receiver()));
        }

        return component;
    }

    public Locale getLocale(FPlayer fPlayer) {
        String locale = fPlayer.getSetting(SettingText.LOCALE);
        if (locale == null) return Locale.ENGLISH;

        return LOCALE_CACHE.computeIfAbsent(locale, string -> Locale.forLanguageTag(string.replace('_', '-')));
    }

}
