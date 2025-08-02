package net.flectone.pulse.module.message.format.translate;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.format.translate.listener.TranslatePulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Singleton
public class TranslateModule extends AbstractModuleLocalization<Localization.Message.Format.Translate> {

    private final Cache<String, UUID> messageCache = CacheBuilder.newBuilder()
            .maximumSize(5000)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build();

    private final Message.Format.Translate message;
    private final Permission.Message.Format.Translate permission;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public TranslateModule(FileResolver fileResolver,
                           ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getFormat().getTranslate());

        this.message = fileResolver.getMessage().getFormat().getTranslate();
        this.permission = fileResolver.getPermission().getMessage().getFormat().getTranslate();
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        listenerRegistry.register(TranslatePulseListener.class);
    }

    @Override
    public void onDisable() {
        messageCache.invalidateAll();
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    public UUID saveMessage(String message) {
        UUID uuid = messageCache.getIfPresent(message);
        if (uuid == null) {
            uuid = UUID.randomUUID();
            messageCache.put(message, uuid);
        }

        return uuid;
    }

    public String getMessage(String stringUUID) {
        try {
            UUID uuid = UUID.fromString(stringUUID);

            return getMessage(uuid);
        } catch (IllegalArgumentException ignored) {
        }

        return null;
    }

    @Nullable
    public String getMessage(UUID uuid) {
        return messageCache.asMap().entrySet()
                .stream()
                .filter(entry -> entry.getValue().equals(uuid))
                .findFirst()
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}
