package net.flectone.pulse.platform.render;

import com.github.retrooper.packetevents.protocol.advancements.*;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAdvancements;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Toast;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.kyori.adventure.text.Component;

import java.util.*;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftToastRender implements ToastRender {

    private final PacketProvider packetProvider;

    @Override
    public void render(FPlayer fPlayer, Component title, Component description, Toast toast) {
        User user = packetProvider.getUser(fPlayer);
        if (user == null) return;

        ItemType itemType = ItemTypes.getByName(toast.icon());
        ItemStack itemStack = ItemStack.builder()
                .type(itemType == null ? ItemTypes.DIAMOND : itemType)
                .build();

        AdvancementDisplay advancementDisplay = new AdvancementDisplay(
                title,
                Component.empty(),
                itemStack,
                AdvancementType.valueOf(toast.style().name()),
                null,
                true,
                false,
                0.0f,
                0.0f
        );

        String criterionName = "trigger";
        List<String> criteria = Collections.singletonList(criterionName);
        List<List<String>> requirements = Collections.singletonList(criteria);

        ResourceLocation advancementId = ResourceLocation.minecraft(UUID.randomUUID().toString());
        Advancement advancement = new Advancement(
                null,
                advancementDisplay,
                criteria,
                requirements,
                false
        );

        List<AdvancementHolder> advancementHolders = Collections.singletonList(
                new AdvancementHolder(advancementId, advancement)
        );

        Map<String, AdvancementProgress.CriterionProgress> progressMap = new Object2ObjectArrayMap<>();
        progressMap.put(criterionName, new AdvancementProgress.CriterionProgress(System.currentTimeMillis()));

        AdvancementProgress progress = new AdvancementProgress(progressMap);

        WrapperPlayServerUpdateAdvancements showPacket = new WrapperPlayServerUpdateAdvancements(
                false,
                advancementHolders,
                Collections.emptySet(),
                Collections.singletonMap(advancementId, progress),
                true
        );

        user.sendPacket(showPacket);

        WrapperPlayServerUpdateAdvancements removePacket = new WrapperPlayServerUpdateAdvancements(
                false,
                Collections.emptyList(),
                Collections.singleton(advancementId),
                Collections.emptyMap(),
                false
        );

        user.sendPacket(removePacket);
    }

}
