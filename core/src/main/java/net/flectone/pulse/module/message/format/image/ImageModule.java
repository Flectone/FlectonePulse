package net.flectone.pulse.module.message.format.image;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.format.image.listener.ImagePulseListener;
import net.flectone.pulse.module.message.format.image.model.FImage;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Singleton
public class ImageModule extends AbstractModule {

    @Getter private final Cache<String, Component> imageCache = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .maximumSize(100)
            .build();

    private final Message.Format.Image message;
    private final Permission.Message.Format.Image permission;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public ImageModule(FileResolver fileResolver,
                       ListenerRegistry listenerRegistry) {
        this.message = fileResolver.getMessage().getFormat().getImage();
        this.permission = fileResolver.getPermission().getMessage().getFormat().getImage();
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        listenerRegistry.register(ImagePulseListener.class);
    }

    @Override
    public void onDisable() {
        imageCache.invalidateAll();
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    public Component getImage(String link) throws ExecutionException {
        return imageCache.get(link, () -> createComponent(link));
    }

    private Component createComponent(String link) {
        FImage fImage = new FImage(link);

        Component component = Component.empty();

        try {
            List<String> pixels = fImage.convertImageUrl();
            if (pixels == null) return component;

            for (int i = 0; i < pixels.size(); i++) {
                Component pixelComponent = MiniMessage.miniMessage().deserialize(pixels.get(i));
                component = component.append(Component.newline()).append(pixelComponent);

                if (i == pixels.size() - 1) {
                    component = component.append(Component.newline());
                }
            }

            imageCache.put(link, component);

        } catch (IOException ignored) {
            // return empty component
        }

        return component;
    }
}
