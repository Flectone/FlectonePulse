package net.flectone.pulse.processing.convertor;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.component.ComponentType;
import com.github.retrooper.packetevents.protocol.component.builtin.item.*;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.banner.BannerPattern;
import com.github.retrooper.packetevents.protocol.item.enchantment.type.EnchantmentType;
import com.github.retrooper.packetevents.protocol.nbt.*;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.util.UniqueIdUtil;
import com.github.retrooper.packetevents.util.adventure.AdventureSerializer;
import com.github.retrooper.packetevents.util.adventure.NbtTagHolder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.platform.provider.MinecraftPacketProvider;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.DataComponentValue;
import net.kyori.adventure.text.event.HoverEvent;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AdventureHoverConvertor {

    private final MinecraftPacketProvider packetProvider;

    public HoverEvent<HoverEvent.ShowItem> convert(ItemStack itemStack) {
        Key itemKey = Key.key(itemStack.getType().getName().toString().toLowerCase());
        int amount = itemStack.getAmount();

        if (packetProvider.getServerVersion().isOlderThan(ServerVersion.V_1_20_5)) {
            NBTCompound nbt = itemStack.getNBT();
            if (nbt == null) return HoverEvent.showItem(itemKey, amount);
            return HoverEvent.showItem(itemKey, amount, new NbtTagHolder(nbt));
        }

        if (!itemStack.hasComponentPatches()) {
            return HoverEvent.showItem(itemKey, amount);
        }

        Map<Key, DataComponentValue> components = new HashMap<>();

        ClientVersion clientVersion = packetProvider.getServerVersion().toClientVersion();

        for (Map.Entry<ComponentType<?>, Optional<?>> entry : itemStack.getComponents().getPatches().entrySet()) {
            Object value = entry.getValue().orElse(null);
            if (value == null) continue;

            NBT nbt = encodeComponentValue(value, clientVersion);
            if (nbt == null) continue;

            components.put(Key.key(entry.getKey().getName().toString().toLowerCase()), new NbtTagHolder(nbt));
        }

        return HoverEvent.showItem(itemKey, amount, components);
    }

    // https://github.com/retrooper/packetevents/pull/1277/changes
    private NBT encodeComponentValue(Object value, ClientVersion version) {
        return switch (value) {
            case Component component ->
                    AdventureSerializer.serializer(version).asNbtTag(component);
            case Integer integer ->
                    new NBTInt(integer);
            case ItemEnchantments ench -> {
                NBTCompound levels = new NBTCompound();
                for (Map.Entry<EnchantmentType, Integer> e : ench.getEnchantments().entrySet()) {
                    levels.setTag(e.getKey().getName().toString(), new NBTInt(e.getValue()));
                }
                if (version.isOlderThan(ClientVersion.V_1_21_5)) {
                    NBTCompound compound = new NBTCompound();
                    compound.setTag("levels", levels);
                    compound.setTag("show_in_tooltip", new NBTByte(ench.isShowInTooltip() ? (byte) 1 : (byte) 0));
                    yield compound;
                }
                yield levels;
            }
            case ItemLore lore -> {
                NBTList<NBTCompound> list = NBTList.createCompoundList();
                for (Component line : lore.getLines()) {
                    list.addTagUnsafe(AdventureSerializer.serializer(version).asNbtTag(line));
                }
                yield list;
            }
            case ItemAttributeModifiers modifiers -> {
                NBTList<NBTCompound> modifierList = new NBTList<>(NBTType.COMPOUND);
                for (ItemAttributeModifiers.ModifierEntry entry : modifiers.getModifiers()) {
                    NBTCompound compound = new NBTCompound();
                    compound.setTag("type", new NBTString(entry.getAttribute().getName().toString()));
                    compound.setTag("slot", new NBTString(entry.getSlotGroup().getId()));
                    NBTCompound modCompound = new NBTCompound();
                    if (version.isNewerThanOrEquals(ClientVersion.V_1_21)) {
                        modCompound.setTag("id", new NBTString(entry.getModifier().getName()));
                    } else {
                        modCompound.setTag("uuid", new NBTIntArray(UniqueIdUtil.toIntArray(entry.getModifier().getId())));
                        modCompound.setTag("name", new NBTString(entry.getModifier().getName()));
                    }
                    modCompound.setTag("amount", new NBTDouble(entry.getModifier().getValue()));
                    modCompound.setTag("operation", new NBTString(entry.getModifier().getOperation().name().toLowerCase(Locale.ROOT)));
                    compound.setTag("modifier", modCompound);
                    modifierList.addTag(compound);
                }
                if (version.isOlderThan(ClientVersion.V_1_21_5)) {
                    NBTCompound compound = new NBTCompound();
                    compound.setTag("modifiers", modifierList);
                    compound.setTag("show_in_tooltip", new NBTByte(modifiers.isShowInTooltip() ? (byte) 1 : (byte) 0));
                    yield compound;
                }
                yield modifierList;
            }
            case ArmorTrim trim -> {
                NBTCompound compound = new NBTCompound();
                compound.setTag("material", new NBTString(trim.getMaterial().getName().toString()));
                compound.setTag("pattern", new NBTString(trim.getPattern().getName().toString()));
                if (version.isOlderThan(ClientVersion.V_1_21_5)) {
                    compound.setTag("show_in_tooltip", new NBTByte(trim.isShowInTooltip() ? (byte) 1 : (byte) 0));
                }
                yield compound;
            }
            case ItemDyeColor color -> {
                if (version.isOlderThan(ClientVersion.V_1_21_5)) {
                    NBTCompound compound = new NBTCompound();
                    compound.setTag("rgb", new NBTInt(color.getRgb()));
                    compound.setTag("show_in_tooltip", new NBTByte(color.isShowInTooltip() ? (byte) 1 : (byte) 0));
                    yield compound;
                }
                yield new NBTInt(color.getRgb());
            }
            case ItemTooltipDisplay display -> {
                NBTCompound compound = new NBTCompound();
                compound.setTag("hide_tooltip", new NBTByte(display.isHideTooltip() ? (byte) 1 : (byte) 0));
                if (!display.getHiddenComponents().isEmpty()) {
                    NBTList<NBTString> list = new NBTList<>(NBTType.STRING);
                    for (ComponentType<?> ct : display.getHiddenComponents()) {
                        list.addTag(new NBTString(ct.getName().toString()));
                    }
                    compound.setTag("hidden_components", list);
                }
                yield compound;
            }
            case ItemProfile profile -> {
                NBTCompound compound = new NBTCompound();
                if (profile.getName() != null) compound.setTag("name", new NBTString(profile.getName()));
                if (profile.getId() != null) compound.setTag("id", new NBTIntArray(UniqueIdUtil.toIntArray(profile.getId())));
                if (!profile.getProperties().isEmpty()) {
                    NBTList<NBTCompound> list = new NBTList<>(NBTType.COMPOUND);
                    for (ItemProfile.Property prop : profile.getProperties()) {
                        NBTCompound propCompound = new NBTCompound();
                        propCompound.setTag("name", new NBTString(prop.getName()));
                        propCompound.setTag("value", new NBTString(prop.getValue()));
                        if (prop.getSignature() != null) propCompound.setTag("signature", new NBTString(prop.getSignature()));
                        list.addTag(propCompound);
                    }
                    compound.setTag("properties", list);
                }
                yield compound;
            }
            case BannerLayers layers -> {
                NBTList<NBTCompound> list = new NBTList<>(NBTType.COMPOUND);
                for (BannerLayers.Layer layer : layers.getLayers()) {
                    NBTCompound compound = new NBTCompound();
                    compound.setTag("color", new NBTString(layer.getColor().name().toLowerCase(Locale.ROOT)));
                    NBT patternNbt = layer.getPattern().isRegistered()
                            ? new NBTString(layer.getPattern().getName().toString())
                            : BannerPattern.encode(layer.getPattern(), version);
                    compound.setTag("pattern", patternNbt);
                    list.addTag(compound);
                }
                yield list;
            }
            case NBTCompound compound -> compound;
            default -> null;
        };
    }

}
