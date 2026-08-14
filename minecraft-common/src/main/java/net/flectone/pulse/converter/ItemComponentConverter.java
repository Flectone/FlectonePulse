package net.flectone.pulse.converter;

import com.github.retrooper.packetevents.protocol.color.Color;
import com.github.retrooper.packetevents.protocol.color.DyeColor;
import com.github.retrooper.packetevents.protocol.component.ComponentType;
import com.github.retrooper.packetevents.protocol.component.builtin.TypedBlockEntityData;
import com.github.retrooper.packetevents.protocol.component.builtin.TypedEntityData;
import com.github.retrooper.packetevents.protocol.component.builtin.item.*;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.enchantment.type.EnchantmentType;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.mapper.MappedEntity;
import com.github.retrooper.packetevents.protocol.mapper.MappedEntitySet;
import com.github.retrooper.packetevents.protocol.nbt.*;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.potion.Potion;
import com.github.retrooper.packetevents.protocol.sound.Sound;
import com.github.retrooper.packetevents.protocol.world.WorldBlockPosition;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.Dummy;
import com.github.retrooper.packetevents.util.Filterable;
import com.github.retrooper.packetevents.util.UniqueIdUtil;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.util.adventure.AdventureSerializer;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ItemComponentConverter {

    public @Nullable NBT encode(Object value, ClientVersion version) {
        try {
            return encodeValue(value, version);
        } catch (RuntimeException _) {
            return null;
        }
    }

    private @Nullable NBT encodeValue(Object value, ClientVersion version) {
        return switch (value) {
            case Component component -> encodeText(component, version);
            case Integer integer -> new NBTInt(integer);
            case Boolean bool -> new NBTByte(bool);
            case ResourceLocation location -> new NBTString(location.toString());
            case NBTCompound compound -> compound;
            case Dummy _ -> new NBTCompound();
            case DyeColor dyeColor -> new NBTString(dyeColor.name().toLowerCase(Locale.ROOT));
            case ItemRarity rarity -> new NBTString(rarity.name().toLowerCase(Locale.ROOT));
            case ItemModel model -> new NBTString(model.getModelLocation().toString());
            case ItemTooltipStyle style -> new NBTString(style.getTooltipLoc().toString());
            case ItemUnbreakable unbreakable -> encodeUnbreakable(unbreakable, version);
            case ItemLore lore -> encodeLore(lore, version);
            case ItemEnchantments enchantments -> encodeEnchantments(enchantments, version);
            case ItemAttributeModifiers modifiers -> encodeAttributeModifiers(modifiers, version);
            case ArmorTrim trim -> encodeArmorTrim(trim, version);
            case ItemDyeColor dyeColor -> encodeDyedColor(dyeColor, version);
            case ItemTooltipDisplay tooltipDisplay -> encodeTooltipDisplay(tooltipDisplay);
            case ItemProfile profile -> ItemProfile.encode(PacketWrapper.createDummyWrapper(version), profile);
            case BannerLayers bannerLayers -> encodeBannerLayers(bannerLayers);
            case BundleContents bundleContents -> encodeItems(bundleContents.getItems(), version);
            case ChargedProjectiles projectiles -> encodeItems(projectiles.getItems(), version);
            case ItemContainerContents contents -> encodeContainerContents(contents, version);
            case ItemUseRemainder remainder -> encodeItemStack(remainder.getTarget(), version);
            case ItemCustomModelData modelData -> encodeCustomModelData(modelData, version);
            case ItemFireworks fireworks -> encodeFireworks(fireworks, version);
            case FireworkExplosion explosion -> encodeFireworkExplosion(explosion, version);
            case ItemPotionContents potionContents -> encodePotionContents(potionContents);
            case SuspiciousStewEffects stewEffects -> encodeStewEffects(stewEffects);
            case ItemInstrument instrument -> new NBTString(instrument.getInstrument().getName().toString());
            case PotDecorations potDecorations -> encodePotDecorations(potDecorations);
            case ItemBlockStateProperties properties -> encodeBlockStateProperties(properties);
            case DebugStickState stickState -> encodeDebugStickState(stickState);
            case ItemContainerLoot containerLoot -> encodeContainerLoot(containerLoot);
            case ItemRecipes recipes -> encodeRecipes(recipes);
            case ItemLock lock -> encodeLock(lock, version);
            case ItemEnchantable enchantable -> singleTag("value", new NBTInt(enchantable.getValue()));
            case ItemWeapon weapon -> encodeWeapon(weapon);
            case ItemUseCooldown cooldown -> encodeUseCooldown(cooldown);
            case ItemDamageResistant resistant -> singleTag("types", tagKey(resistant.getTypesTagKey()));
            case ItemPotionDurationScale scale -> new NBTFloat(scale.getScale());
            case ItemProvidesBannerPatterns patterns -> tagKey(patterns.getTagKey());
            case ItemProvidesTrimMaterial material -> new NBTString(material.getMaterial().getName().toString());
            case ItemRepairable repairable -> singleTag("items", encodeEntitySet(repairable.getItems()));
            case ItemJukeboxPlayable playable -> encodeJukeboxPlayable(playable, version);
            case ItemMapDecorations decorations -> encodeMapDecorations(decorations);
            case LodestoneTracker tracker -> encodeLodestoneTracker(tracker);
            case ItemBreakSound breakSound -> Sound.encode(breakSound.getSound(), version);
            case WrittenBookContent book -> encodeWrittenBook(book, version);
            case WritableBookContent book -> encodeWritableBook(book);
            case TypedEntityData data -> withEntityId(data.getCompound(), data.getType().getName(), version);
            case TypedBlockEntityData data -> withEntityId(data.getCompound(), data.getType().getName(), version);
            default -> null;
        };
    }

    private NBTCompound encodeItemStack(ItemStack itemStack, ClientVersion version) {
        NBTCompound compound = new NBTCompound();
        compound.setTag("id", new NBTString(itemStack.getType().getName().toString()));
        compound.setTag("count", new NBTInt(itemStack.getAmount()));

        NBTCompound components = encodeComponents(itemStack, version);
        if (!components.getTags().isEmpty()) {
            compound.setTag("components", components);
        }

        return compound;
    }

    private NBTCompound encodeComponents(ItemStack itemStack, ClientVersion version) {
        NBTCompound components = new NBTCompound();
        if (!itemStack.hasComponentPatches()) return components;

        for (Map.Entry<ComponentType<?>, Optional<?>> entry : itemStack.getComponents().getPatches().entrySet()) {
            String name = entry.getKey().getName().toString();
            Object value = entry.getValue().orElse(null);

            // an empty patch means the item removed a component it has by default
            if (value == null) {
                components.setTag("!" + name, new NBTCompound());
                continue;
            }

            NBT nbt = encode(value, version);
            if (nbt == null) continue;

            components.setTag(name, nbt);
        }

        return components;
    }

    private NBT encodeLore(ItemLore lore, ClientVersion version) {
        List<NBT> lines = new ArrayList<>(lore.getLines().size());

        for (Component line : lore.getLines()) {
            lines.add(encodeText(line, version));
        }

        return createList(lines);
    }

    // text inside item components is a json string until 1.21.5, only then it became nbt
    private NBT encodeText(Component component, ClientVersion version) {
        AdventureSerializer serializer = AdventureSerializer.serializer(version);

        return version.isOlderThan(ClientVersion.V_1_21_5)
                ? new NBTString(serializer.asJson(component))
                : serializer.asNbtTag(component);
    }

    private NBT encodeUnbreakable(ItemUnbreakable unbreakable, ClientVersion version) {
        NBTCompound compound = new NBTCompound();

        if (version.isOlderThan(ClientVersion.V_1_21_5)) {
            compound.setTag("show_in_tooltip", new NBTByte(unbreakable.isShowInTooltip()));
        }

        return compound;
    }

    private NBT encodeEnchantments(ItemEnchantments enchantments, ClientVersion version) {
        NBTCompound levels = new NBTCompound();

        for (Map.Entry<EnchantmentType, Integer> entry : enchantments.getEnchantments().entrySet()) {
            levels.setTag(entry.getKey().getName().toString(), new NBTInt(entry.getValue()));
        }

        if (version.isNewerThanOrEquals(ClientVersion.V_1_21_5)) {
            return levels;
        }

        NBTCompound compound = new NBTCompound();
        compound.setTag("levels", levels);
        compound.setTag("show_in_tooltip", new NBTByte(enchantments.isShowInTooltip()));
        return compound;
    }

    private NBT encodeAttributeModifiers(ItemAttributeModifiers modifiers, ClientVersion version) {
        NBTList<NBTCompound> modifierList = NBTList.createCompoundList();

        for (ItemAttributeModifiers.ModifierEntry entry : modifiers.getModifiers()) {
            modifierList.addTag(encodeAttributeModifier(entry, version));
        }

        if (version.isNewerThanOrEquals(ClientVersion.V_1_21_5)) {
            return modifierList;
        }

        NBTCompound compound = new NBTCompound();
        compound.setTag("modifiers", modifierList);
        compound.setTag("show_in_tooltip", new NBTByte(modifiers.isShowInTooltip()));
        return compound;
    }

    private NBTCompound encodeAttributeModifier(ItemAttributeModifiers.ModifierEntry entry, ClientVersion version) {
        ItemAttributeModifiers.Modifier modifier = entry.getModifier();

        NBTCompound compound = new NBTCompound();
        compound.setTag("type", new NBTString(encodeAttributeName(entry.getAttribute().getName(), version)));
        compound.setTag("slot", new NBTString(entry.getSlotGroup().getId()));

        // 1.21 replaced the uuid and name pair with a single id
        if (version.isOlderThan(ClientVersion.V_1_21)) {
            compound.setTag("uuid", new NBTIntArray(UniqueIdUtil.toIntArray(modifier.getId())));
            compound.setTag("name", new NBTString(modifier.getName()));
        } else {
            compound.setTag("id", new NBTString(modifier.getName()));
        }

        compound.setTag("amount", new NBTDouble(modifier.getValue()));
        compound.setTag("operation", new NBTString(modifier.getOperation().getCodecName()));
        return compound;
    }

    // packetevents stores only the modern attribute names, 1.20.5 and 1.20.6 clients
    // still expect the prefixed ones and reject the whole item otherwise
    private String encodeAttributeName(ResourceLocation name, ClientVersion version) {
        if (version.isNewerThanOrEquals(ClientVersion.V_1_21)) {
            return name.toString();
        }

        String prefix = switch (name.getKey()) {
            case "block_break_speed", "block_interaction_range", "entity_interaction_range" -> "player.";
            case "spawn_reinforcements" -> "zombie.";
            default -> "generic.";
        };

        return name.getNamespace() + ":" + prefix + name.getKey();
    }

    private NBT encodeArmorTrim(ArmorTrim trim, ClientVersion version) {
        NBTCompound compound = new NBTCompound();
        compound.setTag("material", new NBTString(trim.getMaterial().getName().toString()));
        compound.setTag("pattern", new NBTString(trim.getPattern().getName().toString()));

        if (version.isOlderThan(ClientVersion.V_1_21_5)) {
            compound.setTag("show_in_tooltip", new NBTByte(trim.isShowInTooltip()));
        }

        return compound;
    }

    private NBT encodeDyedColor(ItemDyeColor dyeColor, ClientVersion version) {
        if (version.isNewerThanOrEquals(ClientVersion.V_1_21_5)) {
            return new NBTInt(dyeColor.getRgb());
        }

        NBTCompound compound = new NBTCompound();
        compound.setTag("rgb", new NBTInt(dyeColor.getRgb()));
        compound.setTag("show_in_tooltip", new NBTByte(dyeColor.isShowInTooltip()));
        return compound;
    }

    private NBT encodeTooltipDisplay(ItemTooltipDisplay tooltipDisplay) {
        NBTCompound compound = new NBTCompound();
        compound.setTag("hide_tooltip", new NBTByte(tooltipDisplay.isHideTooltip()));

        if (!tooltipDisplay.getHiddenComponents().isEmpty()) {
            NBTList<NBTString> hiddenComponents = NBTList.createStringList();

            for (ComponentType<?> componentType : tooltipDisplay.getHiddenComponents()) {
                hiddenComponents.addTag(new NBTString(componentType.getName().toString()));
            }

            compound.setTag("hidden_components", hiddenComponents);
        }

        return compound;
    }

    // unlike the packet, nbt only takes registered patterns, an inline one has no valid form
    private @Nullable NBT encodeBannerLayers(BannerLayers bannerLayers) {
        NBTList<NBTCompound> list = NBTList.createCompoundList();

        for (BannerLayers.Layer layer : bannerLayers.getLayers()) {
            if (!layer.getPattern().isRegistered()) return null;

            NBTCompound compound = new NBTCompound();
            compound.setTag("pattern", new NBTString(layer.getPattern().getName().toString()));
            compound.setTag("color", new NBTString(layer.getColor().name().toLowerCase(Locale.ROOT)));
            list.addTag(compound);
        }

        return list;
    }

    private NBT encodeItems(List<ItemStack> items, ClientVersion version) {
        NBTList<NBTCompound> list = NBTList.createCompoundList();

        for (ItemStack itemStack : items) {
            if (itemStack == null || itemStack.isEmpty()) continue;

            list.addTag(encodeItemStack(itemStack, version));
        }

        return list;
    }

    private NBT encodeContainerContents(ItemContainerContents contents, ClientVersion version) {
        NBTList<NBTCompound> list = NBTList.createCompoundList();
        List<ItemStack> items = contents.getItems();

        for (int slot = 0; slot < items.size(); slot++) {
            ItemStack itemStack = items.get(slot);
            if (itemStack == null || itemStack.isEmpty()) continue;

            NBTCompound compound = new NBTCompound();
            compound.setTag("slot", new NBTInt(slot));
            compound.setTag("item", encodeItemStack(itemStack, version));
            list.addTag(compound);
        }

        return list;
    }

    private NBT encodeCustomModelData(ItemCustomModelData modelData, ClientVersion version) {
        if (version.isOlderThan(ClientVersion.V_1_21_4)) {
            return new NBTInt(modelData.getLegacyId());
        }

        NBTCompound compound = new NBTCompound();

        if (!modelData.getFloats().isEmpty()) {
            NBTList<NBTFloat> floats = new NBTList<>(NBTType.FLOAT);
            for (Float value : modelData.getFloats()) {
                floats.addTag(new NBTFloat(value));
            }

            compound.setTag("floats", floats);
        }

        if (!modelData.getFlags().isEmpty()) {
            NBTList<NBTByte> flags = new NBTList<>(NBTType.BYTE);
            for (Boolean value : modelData.getFlags()) {
                flags.addTag(new NBTByte(value));
            }

            compound.setTag("flags", flags);
        }

        if (!modelData.getStrings().isEmpty()) {
            NBTList<NBTString> strings = NBTList.createStringList();
            for (String value : modelData.getStrings()) {
                strings.addTag(new NBTString(value));
            }

            compound.setTag("strings", strings);
        }

        if (!modelData.getColors().isEmpty()) {
            PacketWrapper<?> wrapper = PacketWrapper.createDummyWrapper(version);
            List<NBT> colors = new ArrayList<>(modelData.getColors().size());
            for (Color color : modelData.getColors()) {
                colors.add(Color.CODEC.encode(wrapper, color));
            }

            compound.setTag("colors", createList(colors));
        }

        return compound;
    }

    private NBT encodeFireworks(ItemFireworks fireworks, ClientVersion version) {
        NBTList<NBTCompound> explosions = NBTList.createCompoundList();

        for (FireworkExplosion explosion : fireworks.getExplosions()) {
            explosions.addTag(encodeFireworkExplosion(explosion, version));
        }

        NBTCompound compound = new NBTCompound();
        compound.setTag("explosions", explosions);
        compound.setTag("flight_duration", new NBTByte((byte) fireworks.getFlightDuration()));
        return compound;
    }

    private NBTCompound encodeFireworkExplosion(FireworkExplosion explosion, ClientVersion version) {
        NBTCompound compound = new NBTCompound();
        compound.setTag("shape", new NBTString(explosion.getShape().name().toLowerCase(Locale.ROOT)));

        if (!explosion.getColors().isEmpty()) {
            compound.setTag("colors", encodeColors(explosion.getColors(), version));
        }

        if (!explosion.getFadeColors().isEmpty()) {
            compound.setTag("fade_colors", encodeColors(explosion.getFadeColors(), version));
        }

        compound.setTag("has_trail", new NBTByte(explosion.isHasTrail()));
        compound.setTag("has_twinkle", new NBTByte(explosion.isHasTwinkle()));
        return compound;
    }

    // an int array until 1.21.5, a list of ints since
    private NBT encodeColors(List<Integer> colors, ClientVersion version) {
        if (version.isNewerThanOrEquals(ClientVersion.V_1_21_5)) {
            NBTList<NBTInt> list = new NBTList<>(NBTType.INT);
            for (Integer color : colors) {
                list.addTag(new NBTInt(color));
            }

            return list;
        }

        int[] array = new int[colors.size()];
        for (int index = 0; index < array.length; index++) {
            array[index] = colors.get(index);
        }

        return new NBTIntArray(array);
    }

    private NBT encodePotionContents(ItemPotionContents potionContents) {
        NBTCompound compound = new NBTCompound();

        Potion potion = potionContents.getPotion();
        if (potion != null) {
            compound.setTag("potion", new NBTString(potion.getName().toString()));
        }

        Integer customColor = potionContents.getCustomColor();
        if (customColor != null) {
            compound.setTag("custom_color", new NBTInt(customColor));
        }

        String customName = potionContents.getCustomName();
        if (customName != null) {
            compound.setTag("custom_name", new NBTString(customName));
        }

        return compound;
    }

    private NBT encodeStewEffects(SuspiciousStewEffects stewEffects) {
        NBTList<NBTCompound> list = NBTList.createCompoundList();

        for (SuspiciousStewEffects.EffectEntry entry : stewEffects.getEffects()) {
            NBTCompound compound = new NBTCompound();
            compound.setTag("id", new NBTString(entry.getType().getName().toString()));
            compound.setTag("duration", new NBTInt(entry.getDuration()));
            list.addTag(compound);
        }

        return list;
    }

    private NBT encodePotDecorations(PotDecorations potDecorations) {
        ItemType[] decorations = {
                potDecorations.getBack(), potDecorations.getLeft(),
                potDecorations.getRight(), potDecorations.getFront()
        };

        NBTList<NBTString> list = NBTList.createStringList();
        for (ItemType decoration : decorations) {
            // an absent sherd is a brick, positions in the list may not shift
            list.addTag(new NBTString(decoration == null
                    ? "minecraft:brick"
                    : decoration.getName().toString()));
        }

        return list;
    }

    private NBT encodeBlockStateProperties(ItemBlockStateProperties properties) {
        NBTCompound compound = new NBTCompound();

        for (Map.Entry<String, String> entry : properties.getProperties().entrySet()) {
            if (entry.getValue() == null) continue;

            compound.setTag(entry.getKey(), new NBTString(entry.getValue()));
        }

        return compound;
    }

    private NBT encodeDebugStickState(DebugStickState stickState) {
        NBTCompound compound = new NBTCompound();

        stickState.getProperties().forEach((stateType, property) ->
                compound.setTag(stateType.getName(), new NBTString(property)));

        return compound;
    }

    private NBT encodeContainerLoot(ItemContainerLoot containerLoot) {
        NBTCompound compound = new NBTCompound();
        compound.setTag("loot_table", new NBTString(containerLoot.getLootTable().toString()));

        if (containerLoot.getSeed() != 0L) {
            compound.setTag("seed", new NBTLong(containerLoot.getSeed()));
        }

        return compound;
    }

    private NBT encodeRecipes(ItemRecipes recipes) {
        NBTList<NBTString> list = NBTList.createStringList();

        for (ResourceLocation recipe : recipes.getRecipes()) {
            list.addTag(new NBTString(recipe.toString()));
        }

        return list;
    }

    // a key until 1.21.2, an item predicate since
    private NBT encodeLock(ItemLock lock, ClientVersion version) {
        return version.isOlderThan(ClientVersion.V_1_21_2)
                ? new NBTString(lock.getCode())
                : lock.getPredicate();
    }

    private NBT encodeWeapon(ItemWeapon weapon) {
        NBTCompound compound = new NBTCompound();
        compound.setTag("item_damage_per_attack", new NBTInt(weapon.getItemDamagePerAttack()));
        compound.setTag("disable_blocking_for_seconds", new NBTFloat(weapon.getDisableBlockingForSeconds()));
        return compound;
    }

    private NBT encodeUseCooldown(ItemUseCooldown cooldown) {
        NBTCompound compound = new NBTCompound();
        compound.setTag("seconds", new NBTFloat(cooldown.getSeconds()));
        cooldown.getCooldownGroup().ifPresent(group ->
                compound.setTag("cooldown_group", new NBTString(group.toString())));

        return compound;
    }

    private @Nullable NBT encodeJukeboxPlayable(ItemJukeboxPlayable playable, ClientVersion version) {
        ResourceLocation song = playable.getSongKey();
        if (song == null) return null;

        if (version.isNewerThanOrEquals(ClientVersion.V_1_21_5)) {
            return new NBTString(song.toString());
        }

        NBTCompound compound = new NBTCompound();
        compound.setTag("song", new NBTString(song.toString()));
        compound.setTag("show_in_tooltip", new NBTByte(playable.isShowInTooltip()));
        return compound;
    }

    private NBT encodeMapDecorations(ItemMapDecorations decorations) {
        NBTCompound compound = new NBTCompound();

        decorations.getDecorations().forEach((id, decoration) -> {
            NBTCompound decorationCompound = new NBTCompound();
            ItemMapDecorations.Decoration.writeCompound(decorationCompound, decoration);
            compound.setTag(id, decorationCompound);
        });

        return compound;
    }

    private NBT encodeLodestoneTracker(LodestoneTracker tracker) {
        NBTCompound compound = new NBTCompound();
        WorldBlockPosition target = tracker.getTarget();

        if (target != null) {
            Vector3i position = target.getBlockPosition();

            NBTCompound targetCompound = new NBTCompound();
            targetCompound.setTag("pos", new NBTIntArray(new int[]{
                    position.getX(), position.getY(), position.getZ()
            }));
            targetCompound.setTag("dimension", new NBTString(target.getWorld().toString()));
            compound.setTag("target", targetCompound);
        }

        compound.setTag("tracked", new NBTByte(tracker.isTracked()));
        return compound;
    }

    private NBT encodeWrittenBook(WrittenBookContent book, ClientVersion version) {
        NBTCompound compound = new NBTCompound();
        compound.setTag("title", encodeFilterable(book.getTitle(), NBTString::new));
        compound.setTag("author", new NBTString(book.getAuthor()));

        if (!book.getPages().isEmpty()) {
            List<NBT> pages = new ArrayList<>(book.getPages().size());
            for (Filterable<Component> page : book.getPages()) {
                pages.add(encodeFilterable(page, line -> encodeText(line, version)));
            }

            compound.setTag("pages", createList(pages));
        }

        compound.setTag("generation", new NBTInt(book.getGeneration()));
        compound.setTag("resolved", new NBTByte(book.isResolved()));
        return compound;
    }

    private NBT encodeWritableBook(WritableBookContent book) {
        List<NBT> pages = new ArrayList<>(book.getPages().size());

        for (Filterable<String> page : book.getPages()) {
            pages.add(encodeFilterable(page, NBTString::new));
        }

        NBTCompound compound = new NBTCompound();
        compound.setTag("pages", createList(pages));
        return compound;
    }

    // a filtered value is a pair, a value without one is written as is
    private <T> NBT encodeFilterable(Filterable<T> filterable, Function<T, NBT> encoder) {
        NBT raw = encoder.apply(filterable.getRaw());
        T filtered = filterable.getFiltered();

        if (filtered == null) return raw;

        NBTCompound compound = new NBTCompound();
        compound.setTag("raw", raw);
        compound.setTag("filtered", encoder.apply(filtered));
        return compound;
    }

    // 1.21.9 moved the type out of the entity nbt, it is needed again to read the entity back
    private NBT withEntityId(NBTCompound compound, ResourceLocation type, ClientVersion version) {
        if (version.isOlderThan(ClientVersion.V_1_21_9) || compound.getTagOrNull("id") != null) {
            return compound;
        }

        NBTCompound copy = compound.copy();
        copy.setTag("id", new NBTString(type.toString()));
        return copy;
    }

    private NBT encodeEntitySet(MappedEntitySet<?> set) {
        ResourceLocation tagKey = set.getTagKey();
        if (tagKey != null) return tagKey(tagKey);

        List<? extends MappedEntity> entities = set.getEntities();
        NBTList<NBTString> list = NBTList.createStringList();

        if (entities != null) {
            for (MappedEntity entity : entities) {
                list.addTag(new NBTString(entity.getName().toString()));
            }
        }

        return list;
    }

    private NBTString tagKey(ResourceLocation tagKey) {
        return new NBTString("#" + tagKey);
    }

    private NBTCompound singleTag(String key, NBT value) {
        NBTCompound compound = new NBTCompound();
        compound.setTag(key, value);
        return compound;
    }

    // a text component is serialized as a string or as a compound depending on its content,
    // so the list type is picked from the tags, like the client does when it reads them
    @SuppressWarnings("unchecked")
    private NBTList<NBT> createList(List<NBT> tags) {
        NBTList<NBT> list = new NBTList<>((NBTType<NBT>) NBTList.getCommonTagType(tags));

        for (NBT tag : tags) {
            list.addTagOrWrap(tag);
        }

        return list;
    }

}