package net.flectone.pulse.module.message.anvil;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.message.anvil.listener.AnvilListener;
import net.flectone.pulse.registry.BukkitListenerRegistry;
import net.flectone.pulse.formatter.MessageFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.meta.ItemMeta;

@Singleton
public class BukkitAnvilModule extends AnvilModule {

    private final BukkitListenerRegistry bukkitListenerManager;
    private final MessageFormatter messageFormatter;

    @Inject
    public BukkitAnvilModule(FileManager fileManager,
                             BukkitListenerRegistry bukkitListenerManager,
                             MessageFormatter messageFormatter) {
        super(fileManager);
        this.bukkitListenerManager = bukkitListenerManager;
        this.messageFormatter = messageFormatter;
    }

    @Override
    public void reload() {
        super.reload();

        bukkitListenerManager.register(AnvilListener.class, EventPriority.NORMAL);
    }

    @Override
    public boolean format(FPlayer fPlayer, Object itemMeta) {
        if (checkModulePredicates(fPlayer)) return false;
        if (!(itemMeta instanceof ItemMeta bukkitItemMeta)) return false;

        String displayName = bukkitItemMeta.getDisplayName();
        if (displayName.isEmpty()) return false;

        try {
            Component deserialized = LegacyComponentSerializer.legacySection().deserialize(displayName);

            Component component = messageFormatter.builder(fPlayer, displayName.replace("§", "&"))
                    .userMessage(true)
                    .colors(false)
                    .build()
                    .applyFallbackStyle(deserialized.style())
                    .mergeStyle(deserialized);

            bukkitItemMeta.setDisplayName(LegacyComponentSerializer.legacySection().serialize(component));

            return true;

        } catch (ParsingException ignored) {}

        return false;
    }
}
