package net.flectone.pulse.converter;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.component.ComponentType;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.nbt.NBT;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.util.adventure.NbtTagHolder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.platform.provider.MinecraftPacketProvider;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.event.DataComponentValue;
import net.kyori.adventure.text.event.HoverEvent;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AdventureHoverConverter {

    private final MinecraftPacketProvider packetProvider;
    private final ItemComponentConverter itemComponentConverter;

    public HoverEvent<HoverEvent.ShowItem> convert(ItemStack itemStack) {
        Key itemKey = Key.key(itemStack.getType().getName().toString().toLowerCase(Locale.ROOT));
        int amount = itemStack.getAmount();

        if (packetProvider.getServerVersion().isOlderThan(ServerVersion.V_1_20_5)) {
            return convertLegacy(itemStack, itemKey, amount);
        }

        return convertComponents(itemStack, itemKey, amount);
    }

    private HoverEvent<HoverEvent.ShowItem> convertLegacy(ItemStack itemStack, Key itemKey, int amount) {
        NBTCompound nbt = itemStack.getNBT();
        if (nbt == null) return HoverEvent.showItem(itemKey, amount);

        return HoverEvent.showItem(itemKey, amount, new NbtTagHolder(nbt));
    }

    private HoverEvent<HoverEvent.ShowItem> convertComponents(ItemStack itemStack, Key itemKey, int amount) {
        if (!itemStack.hasComponentPatches()) {
            return HoverEvent.showItem(itemKey, amount);
        }

        ClientVersion version = packetProvider.getServerVersion().toClientVersion();
        Map<Key, DataComponentValue> components = new HashMap<>();

        for (Map.Entry<ComponentType<?>, Optional<?>> entry : itemStack.getComponents().getPatches().entrySet()) {
            Key componentKey = Key.key(entry.getKey().getName().toString().toLowerCase(Locale.ROOT));
            Object value = entry.getValue().orElse(null);

            // an empty patch means the item removed a component it has by default
            if (value == null) {
                components.put(componentKey, DataComponentValue.removed());
                continue;
            }

            NBT nbt = itemComponentConverter.encode(value, version);
            if (nbt == null) continue;

            components.put(componentKey, new NbtTagHolder(nbt));
        }

        return HoverEvent.showItem(itemKey, amount, components);
    }
}