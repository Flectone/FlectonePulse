package net.flectone.pulse.platform.render;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.serializer.HytaleComponentSerializer;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class HytaleHudService {

    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final HytaleComponentSerializer componentSerializer;

    private final Map<UUID, HytaleHud> playerHuds = new ConcurrentHashMap<>();
    private final AtomicLong actionBarIdGenerator = new AtomicLong();

    public void updateSidebar(FPlayer fPlayer, List<Component> lines, Message.Sidebar config) {
        execute(fPlayer, hud -> hud.setSidebar(lines, config));
    }

    public void removeSidebar(FPlayer fPlayer) {
        execute(fPlayer, HytaleHud::clearSidebar);
    }

    public long showActionBar(FPlayer fPlayer, Component component) {
        long actionBarId = actionBarIdGenerator.incrementAndGet();

        execute(fPlayer, hud -> hud.setActionBar(component, actionBarId));

        return actionBarId;
    }

    public void hideActionBar(FPlayer fPlayer, long actionBarId) {
        execute(fPlayer, hud -> hud.clearActionBar(actionBarId));
    }

    private void execute(FPlayer fPlayer, Consumer<HytaleHud> action) {
        if (!(platformPlayerAdapter.convertToPlatformPlayer(fPlayer) instanceof PlayerRef playerRef)) return;
        if (!playerRef.isValid()) {
            playerHuds.remove(fPlayer.uuid());
            return;
        }

        Ref<EntityStore> refStore = playerRef.getReference();
        if (refStore == null) return;

        World world = refStore.getStore().getExternalData().getWorld();
        if (world == null) return;

        world.execute(() -> {
            Store<EntityStore> store = refStore.getStore();
            Player player = store.getComponent(refStore, Player.getComponentType());
            if (player == null) {
                playerHuds.remove(fPlayer.uuid());
                return;
            }

            UUID uuid = fPlayer.uuid();
            HytaleHud hud = playerHuds.get(uuid);
            if (hud == null || hud.getPlayerRef() != playerRef) {
                hud = new HytaleHud(playerRef, componentSerializer);
                playerHuds.put(uuid, hud);
            }

            action.accept(hud);

            if (hud.isEmpty()) {
                if (player.getHudManager().getCustomHud(HytaleHud.KEY) == hud) {
                    player.getHudManager().removeCustomHud(playerRef, HytaleHud.KEY);
                }

                playerHuds.remove(uuid);
                return;
            }

            if (player.getHudManager().getCustomHud(HytaleHud.KEY) == hud) {
                hud.flush();
                return;
            }

            player.getHudManager().addCustomHud(playerRef, hud);
        });
    }

}
