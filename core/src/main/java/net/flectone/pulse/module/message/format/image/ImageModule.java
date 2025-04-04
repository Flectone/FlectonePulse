package net.flectone.pulse.module.message.format.image;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.format.image.model.FImage;
import net.flectone.pulse.formatter.MessageFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.StyleBuilderApplicable;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.flectone.pulse.util.TagResolverUtil.emptyTagResolver;

@Singleton
public class ImageModule extends AbstractModule {

    private final Map<String, Component> imageMap = new HashMap<>();

    private final Message.Format.Image message;
    private final Permission.Message.Format.Image permission;

    private final TaskScheduler taskScheduler;

    @Inject private MessageFormatter messageFormatter;

    @Inject
    public ImageModule(FileManager fileManager,
                       TaskScheduler taskScheduler) {

        this.taskScheduler = taskScheduler;

        message = fileManager.getMessage().getFormat().getImage();
        permission = fileManager.getPermission().getMessage().getFormat().getImage();
    }

    @Override
    public void reload() {
        imageMap.clear();
        registerModulePermission(permission);

        // 10 min timer
        taskScheduler.runAsyncTimer(imageMap::clear, 12000L, 12000L);
    }

    public TagResolver imageTag(FEntity sender, FEntity receiver) {
        String tag = "image";
        if (checkModulePredicates(sender)) return emptyTagResolver(tag);

        return TagResolver.resolver(tag, (argumentQueue, context) -> {
            final Tag.Argument argument = argumentQueue.peek();
            if (argument == null) return Tag.selfClosingInserting(Component.empty());

            final String link = argument.value();

            Component component = imageMap.get(link);
            if (component == null) {
                component = createComponent(link);
            }

            List<StyleBuilderApplicable> styleBuilderApplicables = new ArrayList<>();
            styleBuilderApplicables.add(HoverEvent.showText(component));
            styleBuilderApplicables.add(ClickEvent.openUrl(link));
            styleBuilderApplicables.add(messageFormatter.builder(sender, receiver, message.getColor())
                    .build()
                    .color()
            );

            return Tag.styling(styleBuilderApplicables.toArray(new StyleBuilderApplicable[]{}));
        });
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
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

            imageMap.put(link, component);

        } catch (IOException ignored) {}

        return component;
    }
}
