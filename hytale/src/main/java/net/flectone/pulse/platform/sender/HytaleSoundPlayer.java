package net.flectone.pulse.platform.sender;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Sound;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.util.checker.PermissionChecker;
import org.incendo.cloud.type.tuple.Pair;

import java.util.Arrays;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class HytaleSoundPlayer implements SoundPlayer {

    private final PermissionChecker permissionChecker;
    private final PlatformPlayerAdapter platformPlayerAdapter;

    @Override
    public void play(Pair<Sound, PermissionSetting> soundPermission, FEntity sender, FPlayer receiver) {
        if (soundPermission == null) return;

        Sound sound = soundPermission.first();
        if (!sound.enable()) return;
        if (!permissionChecker.check(sender, soundPermission.second())) return;

        Object player = platformPlayerAdapter.convertToPlatformPlayer(receiver);
        if (!(player instanceof PlayerRef playerRef)) return;

        UUID worldUUID = playerRef.getWorldUuid();
        if (worldUUID == null) return;

        Universe universe = Universe.get();
        if (universe == null) return;

        World world = universe.getWorld(worldUUID);
        if (world == null) return;

        Ref<EntityStore> playerStoreRef = playerRef.getReference();
        if (playerStoreRef == null) return;

        EntityStore store = world.getEntityStore();

        int index = SoundEvent.getAssetMap().getIndex(sound.name());

        SoundCategory category = Arrays.stream(SoundCategory.VALUES)
                .filter(soundCategory -> soundCategory.name().equalsIgnoreCase(sound.category()))
                .findAny()
                .orElse(SoundCategory.UI);

        world.execute(() -> {
            TransformComponent transform = store.getStore().getComponent(playerStoreRef, EntityModule.get().getTransformComponentType());
            if (transform == null) return;

            Vector3d position = transform.getPosition();
            SoundUtil.playSoundEvent3dToPlayer(playerStoreRef, index, category, position.x, position.y, position.z, sound.volume(), sound.pitch(), store.getStore());
        });
    }

}
