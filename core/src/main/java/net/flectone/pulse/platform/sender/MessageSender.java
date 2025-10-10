package net.flectone.pulse.platform.sender;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.advancements.*;
import com.github.retrooper.packetevents.protocol.chat.ChatType;
import com.github.retrooper.packetevents.protocol.chat.ChatTypes;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessageLegacy;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_16;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.BossBar;
import net.flectone.pulse.model.util.Times;
import net.flectone.pulse.model.util.Toast;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.processing.serializer.PacketSerializer;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;

import java.util.*;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MessageSender {

    private final PacketSerializer packetSerializer;
    private final TaskScheduler taskScheduler;
    private final PacketSender packetSender;
    private final PacketProvider packetProvider;
    private final IntegrationModule integrationModule;
    private final FLogger fLogger;

    public void sendToConsole(Component component) {
        String consoleString = ANSIComponentSerializer.ansi().serialize(GlobalTranslator.render(component, Locale.ROOT));
        fLogger.info(consoleString);
    }

    public void sendMessage(FPlayer fPlayer, Component component, boolean silent) {
        if (fPlayer.isConsole()) {
            sendToConsole(component);
            return;
        }

        // integration with InteractiveChat
        if (integrationModule.sendMessageWithInteractiveChat(fPlayer, component)) return;

        User user = packetProvider.getUser(fPlayer);
        if (user == null) return;

        // PacketEvents realization
        ClientVersion version = user.getPacketVersion();
        PacketWrapper<?> chatPacket;
        if (version.isNewerThanOrEquals(ClientVersion.V_1_19)) {
            chatPacket = new WrapperPlayServerSystemChatMessage(false, component);
        } else {
            ChatType type = ChatTypes.CHAT;
            ChatMessage message = version.isNewerThanOrEquals(ClientVersion.V_1_16)
                    ? new ChatMessage_v1_16(component, type, new UUID(0L, 0L))
                    : new ChatMessageLegacy(component, type);

            chatPacket = new WrapperPlayServerChatMessage(message);
        }

        packetSender.send(fPlayer.getUuid(), chatPacket, silent);
    }

    public void sendTitle(FPlayer fPlayer, Component title, Component subTitle, Times times) {
        User user = packetProvider.getUser(fPlayer);
        if (user == null) return;

        user.sendTitle(title, subTitle, times.fadeInTicks(), times.stayTicks(), times.fadeOutTicks());
    }

    public void sendActionBar(FPlayer fPlayer, Component component) {
        sendActionBar(fPlayer, component, 0);
    }

    public void sendActionBar(FPlayer fPlayer, Component component, int stayTicks) {
        if (packetProvider.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_19)) {
            packetSender.send(fPlayer, new WrapperPlayServerSystemChatMessage(true, component));
        } else if (packetProvider.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_17)) {
            packetSender.send(fPlayer, new WrapperPlayServerActionBar(component));
        } else if (packetProvider.getServerVersion().isNewerThan(ServerVersion.V_1_16)) {
            packetSender.send(fPlayer, new WrapperPlayServerChatMessage(new ChatMessage_v1_16(component, ChatTypes.GAME_INFO, fPlayer.getUuid())));
        } else if (packetProvider.getServerVersion().isNewerThan(ServerVersion.V_1_8_8)) {
            packetSender.send(fPlayer, new WrapperPlayServerChatMessage(new ChatMessageLegacy(component, ChatTypes.GAME_INFO)));
        } else { // PacketEvents issue https://github.com/retrooper/packetevents/issues/1241
            packetSender.send(fPlayer, new WrapperPlayServerChatMessage(new ChatMessageLegacy(Component.text(LegacyComponentSerializer.legacySection().serialize(component)), ChatTypes.GAME_INFO)));
        }

        // cannot set stay ticks for action bar, so
        if (stayTicks <= 30) return;

        int remainingTicks = stayTicks - 30;
        int delay = Math.min(30, remainingTicks);

        taskScheduler.runAsyncLater(() -> sendActionBar(fPlayer, component, remainingTicks), delay);
    }

    public void sendPlayerListHeaderAndFooter(FPlayer fPlayer, Component header, Component footer) {
        packetSender.send(fPlayer, new WrapperPlayServerPlayerListHeaderAndFooter(header, footer));
    }

    public void sendBoosBar(FPlayer fPlayer, Component component, BossBar bossBar) {
        UUID bossBarUUID = UUID.randomUUID();

        WrapperPlayServerBossBar addWrapper = new WrapperPlayServerBossBar(bossBarUUID, WrapperPlayServerBossBar.Action.ADD);

        addWrapper.setTitle(component);
        addWrapper.setHealth(bossBar.getHealth());
        addWrapper.setOverlay(bossBar.getOverlay());
        addWrapper.setColor(bossBar.getColor());
        addWrapper.setFlags(bossBar.getFlags());

        packetSender.send(fPlayer, addWrapper);

        taskScheduler.runAsyncLater(() -> {
            WrapperPlayServerBossBar removeWrapper = new WrapperPlayServerBossBar(bossBarUUID, WrapperPlayServerBossBar.Action.REMOVE);
            packetSender.send(fPlayer, removeWrapper);

        }, bossBar.getDuration());
    }

    public void sendBrand(FPlayer fPlayer, Component component) {
        String message = LegacyComponentSerializer.legacySection().serialize(component) + "§r";

        packetSender.send(fPlayer,
                new WrapperPlayServerPluginMessage(PacketSerializer.MINECRAFT_BRAND, packetSerializer.serialize(message))
        );
    }

    public void sendToast(FPlayer fPlayer, Component title, Toast toast) {
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

        Map<String, AdvancementProgress.CriterionProgress> progressMap = new HashMap<>();
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
