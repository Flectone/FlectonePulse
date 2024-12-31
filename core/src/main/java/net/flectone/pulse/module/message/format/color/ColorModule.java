package net.flectone.pulse.module.message.format.color;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.file.Message;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.HashMap;
import java.util.Map;


@Singleton
public class ColorModule extends AbstractModule {

    @Getter
    private final Message.Format.Color message;
    private final Permission.Message.Format.Color permission;

    @Inject
    public ColorModule(FileManager fileManager) {
        message = fileManager.getMessage().getFormat().getColor();
        permission = fileManager.getPermission().getMessage().getFormat().getColor();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);
    }

    public TagResolver colorTag(FEntity sender) {
        if (checkModulePredicates(sender)) return TagResolver.empty();

        Map<String, String> playerColors = sender instanceof FPlayer fPlayer ? fPlayer.getColors() : new HashMap<>();

        return TagResolver.resolver("fcolor", (argumentQueue, context) -> {
            Tag.Argument colorArg = argumentQueue.peek();
            if (colorArg == null) return Tag.selfClosingInserting(Component.empty());

            String number = colorArg.value();
            if (!playerColors.containsKey(number) && !message.getValues().containsKey(number)) {
                return Tag.inserting(Component.empty());
            }

            String color = playerColors.getOrDefault(number, message.getValues().get(number));

            return Tag.inserting(Component.empty().color(TextColor.fromHexString(color)));
        });
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }
}
