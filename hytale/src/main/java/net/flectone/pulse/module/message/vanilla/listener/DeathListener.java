package net.flectone.pulse.module.message.vanilla.listener;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.message.vanilla.HytaleVanillaModule;
import net.flectone.pulse.module.message.vanilla.extractor.HytaleComponentExtractor;
import net.flectone.pulse.module.message.vanilla.model.ParsedComponent;
import net.flectone.pulse.service.FPlayerService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DeathListener extends DeathSystems.OnDeathSystem {

    public static final String DEATH_TRANSLATION_KEY = "server.death.player";
    public static final String DEATH_KILLED_BY_TRANSLATION_KEY = "server.death.player.killedBy";

    private final Provider<HytaleVanillaModule> hytaleVanillaModuleProvider;
    private final HytaleComponentExtractor hytaleComponentExtractor;
    private final FPlayerService fPlayerService;

    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return Query.and(Player.getComponentType());
    }

    @Override
    public void onComponentAdded(@NotNull Ref<EntityStore> ref, @NotNull DeathComponent deathComponent, @NotNull Store<EntityStore> store, @NotNull CommandBuffer<EntityStore> commandBuffer) {
        HytaleVanillaModule hytaleVanillaModule = hytaleVanillaModuleProvider.get();
        if (!hytaleVanillaModule.isEnable()) return;

        PlayerRef player = store.getComponent(ref, PlayerRef.getComponentType());
        if (player == null) return;

        FPlayer fTarget = fPlayerService.getFPlayer(player.getUuid());

        Map<Integer, Object> arguments = HashMap.newHashMap(2);
        arguments.put(0, fTarget);

        if (deathComponent.getDeathMessage() != null) {
            arguments.put(1, hytaleComponentExtractor.extractArguments(deathComponent.getDeathMessage().getFormattedMessage()).get(0));

            hytaleVanillaModule.send(fTarget, new ParsedComponent(
                    DEATH_KILLED_BY_TRANSLATION_KEY,
                    hytaleComponentExtractor.getVanillaMessage(DEATH_KILLED_BY_TRANSLATION_KEY),
                    arguments
            ));
        } else {
            hytaleVanillaModule.send(fTarget, new ParsedComponent(
                    DEATH_TRANSLATION_KEY,
                    hytaleComponentExtractor.getVanillaMessage(DEATH_TRANSLATION_KEY),
                    arguments
            ));
        }

    }

}
