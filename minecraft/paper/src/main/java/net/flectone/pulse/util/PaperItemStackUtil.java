package net.flectone.pulse.util;

import com.github.retrooper.packetevents.util.adventure.AdventureSerializer;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PaperItemStackUtil {

    public static final Pattern FLECTONEPULSE_ITEM_MARK_PATTERN = Pattern.compile("<flectonepulse_item_mark:([a-fA-F0-9\\-]{36})>");

    private final Cache<UUID, ItemStack> uuidItemStackCache = CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.SECONDS)
            .maximumSize(100)
            .build();

    public String saveItem(UUID key, ItemStack itemStack) {
        ItemStack cachedItemStack = getItem(key);
        if (cachedItemStack == null) {
            uuidItemStackCache.put(key, itemStack);
        }

        return "<flectonepulse_item_mark:" + key + ">";
    }

    public ItemStack getItem(UUID key) {
        return uuidItemStackCache.getIfPresent(key);
    }

    public void sendMessage(FPlayer fPlayer, String serialized) {
        Player player = Bukkit.getPlayer(fPlayer.uuid());
        if (player == null) return;

        Component component = AdventureSerializer.serializer().gson().deserialize(serialized);
        player.sendMessage(replaceItemMark(component));
    }

    private Component replaceItemMark(Component component) {
        return component.replaceText(TextReplacementConfig.builder()
                .match(FLECTONEPULSE_ITEM_MARK_PATTERN)
                .replacement((matchResult, builder) -> {
                    String foundUuid = matchResult.group(1);
                    ItemStack itemStack = uuidItemStackCache.getIfPresent(UUID.fromString(foundUuid));
                    if (itemStack == null) return Component.empty();

                    return Component.text("").color(NamedTextColor.GRAY).hoverEvent(itemStack);
                })
                .build()
        );
    }

}
