package net.flectone.pulse.module.message.anvil;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.constant.MessageFlag;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.message.anvil.listener.AnvilListener;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.registry.BukkitListenerRegistry;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.resolver.FileResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.inventory.meta.ItemMeta;

@Singleton
public class BukkitAnvilModule extends AnvilModule {

    private final ListenerRegistry listenerRegistry;
    private final MessagePipeline messagePipeline;

    @Inject
    public BukkitAnvilModule(FileResolver fileResolver,
                             BukkitListenerRegistry listenerRegistry,
                             MessagePipeline messagePipeline) {
        super(fileResolver);

        this.listenerRegistry = listenerRegistry;
        this.messagePipeline = messagePipeline;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(AnvilListener.class);
    }

    @Override
    public boolean format(FPlayer fPlayer, Object itemMeta) {
        if (checkModulePredicates(fPlayer)) return false;
        if (!(itemMeta instanceof ItemMeta bukkitItemMeta)) return false;

        String displayName = bukkitItemMeta.getDisplayName();
        if (displayName.isEmpty()) return false;

        try {
            Component deserialized = LegacyComponentSerializer.legacySection().deserialize(displayName);

            Component component = messagePipeline.builder(fPlayer, displayName.replace("§", "&"))
                    .flag(MessageFlag.USER_MESSAGE, true)
                    .flag(MessageFlag.COLORS, false)
                    .build()
                    .applyFallbackStyle(deserialized.style())
                    .mergeStyle(deserialized);

            bukkitItemMeta.setDisplayName(LegacyComponentSerializer.legacySection().serialize(component));

            return true;

        } catch (ParsingException ignored) {
            // ignore problem string
        }

        return false;
    }
}
