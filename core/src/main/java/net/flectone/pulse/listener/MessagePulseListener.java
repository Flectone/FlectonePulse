package net.flectone.pulse.listener;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.advancements.*;
import com.github.retrooper.packetevents.protocol.chat.ChatTypes;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessageLegacy;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_16;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.model.*;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.SenderToReceiverMessageEvent;
import net.flectone.pulse.provider.PacketProvider;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.sender.PacketSender;
import net.flectone.pulse.serializer.PacketSerializer;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.*;

@Singleton
public class MessagePulseListener implements PulseListener {

    private final PacketSerializer packetSerializer;
    private final TaskScheduler taskScheduler;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final PacketSender packetSender;
    private final PacketProvider packetProvider;
    private final FLogger fLogger;

    @Inject
    public MessagePulseListener(TaskScheduler taskScheduler,
                                PlatformPlayerAdapter platformPlayerAdapter,
                                PacketSerializer packetSerializer,
                                PacketSender packetSender,
                                PacketProvider packetProvider,
                                FLogger fLogger) {
        this.taskScheduler = taskScheduler;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.packetSerializer = packetSerializer;
        this.packetSender = packetSender;
        this.packetProvider = packetProvider;
        this.fLogger = fLogger;
    }

    @Pulse(priority = Event.Priority.HIGHEST)
    public void onSenderToReceiverMessageEvent(SenderToReceiverMessageEvent event) {
        Component message = event.getMessage();
        if (!Component.IS_NOT_EMPTY.test(message)) return;

        FPlayer fReceiver = event.getReceiver();
        Component submessage = event.getSubmessage();
        Destination destination = event.getDestination();
        switch (destination.getType()) {
            case TITLE -> sendTitle(fReceiver, message, submessage, destination.getTimes());
            case SUBTITLE -> sendTitle(fReceiver, submessage, message, destination.getTimes());
            case ACTION_BAR -> sendActionBar(fReceiver, message, destination.getTimes().stayTicks());
            case BOSS_BAR -> sendBoosBar(fReceiver, message, destination.getBossBar());
            case TAB_HEADER -> sendPlayerListHeaderAndFooter(fReceiver, message, platformPlayerAdapter.getPlayerListFooter(fReceiver));
            case TAB_FOOTER -> sendPlayerListHeaderAndFooter(fReceiver, platformPlayerAdapter.getPlayerListHeader(fReceiver), message);
            case TOAST -> sendToast(fReceiver, message, destination.getToast());
            case BRAND -> sendBrand(fReceiver, message);
            default -> sendMessage(fReceiver, message);
        }
    }

    public void sendMessage(FPlayer fPlayer, Component component) {

        if (fPlayer.isUnknown()) {
            fLogger.info(component);
            return;
        }

        User user = packetProvider.getUser(fPlayer);
        if (user == null) return;

        user.sendMessage(component);
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
        } else {
            packetSender.send(fPlayer, new WrapperPlayServerChatMessage(new ChatMessageLegacy(component, ChatTypes.GAME_INFO)));
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
