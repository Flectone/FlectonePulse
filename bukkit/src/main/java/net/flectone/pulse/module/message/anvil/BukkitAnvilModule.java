package net.flectone.pulse.module.message.anvil;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.manager.BukkitListenerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.message.anvil.listener.AnvilListener;
import net.flectone.pulse.util.ComponentUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.meta.ItemMeta;

@Singleton
public class BukkitAnvilModule extends AnvilModule {

    private final BukkitListenerManager bukkitListenerManager;
    private final ComponentUtil componentUtil;

    @Inject
    public BukkitAnvilModule(FileManager fileManager,
                             BukkitListenerManager bukkitListenerManager,
                             ComponentUtil componentUtil) {
        super(fileManager);
        this.bukkitListenerManager = bukkitListenerManager;
        this.componentUtil = componentUtil;
    }

    @Override
    public void reload() {
        super.reload();

        bukkitListenerManager.register(AnvilListener.class, EventPriority.NORMAL);
    }

    @Override
    public void format(FPlayer fPlayer, Object itemMeta) {
        if (checkModulePredicates(fPlayer)) return;
        if (!(itemMeta instanceof ItemMeta bukkitItemMeta)) return;

        String displayName = bukkitItemMeta.getDisplayName();
        if (displayName.isEmpty()) return;

        try {
            Component component = componentUtil.builder(fPlayer, displayName)
                    .userMessage(true)
                    .colors(false)
                    .build()
                    .applyFallbackStyle(LegacyComponentSerializer.legacySection().deserialize(displayName).style())
                    .mergeStyle(LegacyComponentSerializer.legacySection().deserialize(displayName));

            bukkitItemMeta.setDisplayName(LegacyComponentSerializer.legacySection().serialize(component));

        } catch (ParsingException ignored) {}
    }
}
