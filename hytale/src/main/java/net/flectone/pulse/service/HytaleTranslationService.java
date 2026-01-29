package net.flectone.pulse.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hypixel.hytale.server.core.modules.i18n.I18nModule;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.Translator;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.text.MessageFormat;
import java.util.Locale;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class HytaleTranslationService implements TranslationService {

    private final TaskScheduler taskScheduler;
    private final FlectoneTranslator translator = new FlectoneTranslator();

    @Override
    public void reload() {
        taskScheduler.runAsync(this::initGlobalTranslator, true);
    }

    @Override
    public void initGlobalTranslator() {
        GlobalTranslator.translator().removeSource(translator);
        GlobalTranslator.translator().addSource(translator);
    }

    private static class FlectoneTranslator implements Translator {

        @Override
        public @NonNull Key name() {
            return Key.key("flectonepulse:translation");
        }

        @Override
        public @Nullable MessageFormat translate(final @NonNull String key, final @NonNull Locale locale) {
            String translated = tryTranslate(key, locale.getDisplayName());
            if (translated == null) return null;

            return new MessageFormat(translated, locale);
        }

        @Override
        public @Nullable Component translate(@NonNull TranslatableComponent component, @NonNull Locale locale) {
            String translated = tryTranslate(component.key(), locale.getDisplayName());
            if (translated == null) return null;

            return Component.text(translated).mergeStyle(component);
        }

        public String tryTranslate(String key, String locale) {
            I18nModule i18nModule = I18nModule.get();
            String translated = i18nModule.getMessage(locale, key);
            return translated != null ? translated : i18nModule.getMessage(I18nModule.DEFAULT_LANGUAGE, key);
        }

    }

}
