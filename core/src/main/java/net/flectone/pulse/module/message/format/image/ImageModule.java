package net.flectone.pulse.module.message.format.image;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.context.MessageContext;
import net.flectone.pulse.processor.MessageProcessor;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.format.image.model.FImage;
import net.flectone.pulse.registry.MessageProcessRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.StyleBuilderApplicable;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static net.flectone.pulse.util.TagResolverUtil.emptyTagResolver;

@Singleton
public class ImageModule extends AbstractModule implements MessageProcessor {

    private final Cache<String, Component> imageCache = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .maximumSize(100)
            .build();

    private final Message.Format.Image message;
    private final Permission.Message.Format.Image permission;

    private final MessagePipeline messagePipeline;

    @Inject
    public ImageModule(FileManager fileManager,
                       MessagePipeline messagePipeline,
                       MessageProcessRegistry messageProcessRegistry) {

        this.messagePipeline = messagePipeline;

        message = fileManager.getMessage().getFormat().getImage();
        permission = fileManager.getPermission().getMessage().getFormat().getImage();

        messageProcessRegistry.register(100, this);
    }

    @Override
    public void reload() {
        imageCache.invalidateAll();
        registerModulePermission(permission);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Override
    public void process(MessageContext messageContext) {
        if (!messageContext.isImage()) return;

        messageContext.addTagResolvers(imageTag(messageContext.getSender(), messageContext.getReceiver()));
    }

    private TagResolver imageTag(FEntity sender, FEntity receiver) {
        String tag = "image";
        if (checkModulePredicates(sender)) return emptyTagResolver(tag);

        return TagResolver.resolver(tag, (argumentQueue, context) -> {
            final Tag.Argument argument = argumentQueue.peek();
            if (argument == null) return Tag.selfClosingInserting(Component.empty());

            final String link = argument.value();

            Component component;
            try {
                component = imageCache.get(link, () -> createComponent(link));
            } catch (ExecutionException e) {
                return Tag.selfClosingInserting(Component.empty());
            }

            List<StyleBuilderApplicable> styleBuilderApplicables = new ArrayList<>();
            styleBuilderApplicables.add(HoverEvent.showText(component));
            styleBuilderApplicables.add(ClickEvent.openUrl(link));
            styleBuilderApplicables.add(messagePipeline.builder(sender, receiver, message.getColor())
                    .build()
                    .color()
            );

            return Tag.styling(styleBuilderApplicables.toArray(new StyleBuilderApplicable[]{}));
        });
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

        } catch (IOException ignored) {}

        return component;
    }
}
