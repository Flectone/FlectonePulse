package net.flectone.pulse.module.message.format.moderation.delete;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.listener.MessagePulseListener;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.format.moderation.delete.listener.DeletePulseListener;
import net.flectone.pulse.module.message.format.moderation.delete.model.HistoryMessage;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class DeleteModule extends AbstractModuleLocalization<Localization.Message.Format.Moderation.Delete> {

    private final Map<UUID, List<HistoryMessage>> playersHistory = new ConcurrentHashMap<>();

    private final Message.Format.Moderation.Delete message;
    private final Permission.Message.Format.Moderation.Delete permission;
    private final ListenerRegistry listenerRegistry;
    private final MessagePipeline messagePipeline;
    private final FPlayerService fPlayerService;
    private final MessagePulseListener messagePulseListener;
    private final PlatformPlayerAdapter platformPlayerAdapter;

    @Inject
    public DeleteModule(FileResolver fileResolver,
                        ListenerRegistry listenerRegistry,
                        MessagePipeline messagePipeline,
                        FPlayerService fPlayerService,
                        MessagePulseListener messagePulseListener,
                        PlatformPlayerAdapter platformPlayerAdapter) {
        super(localization -> localization.getMessage().getFormat().getModeration().getDelete());

        this.message = fileResolver.getMessage().getFormat().getModeration().getDelete();
        this.permission = fileResolver.getPermission().getMessage().getFormat().getModeration().getDelete();
        this.listenerRegistry = listenerRegistry;
        this.messagePipeline = messagePipeline;
        this.fPlayerService = fPlayerService;
        this.messagePulseListener = messagePulseListener;
        this.platformPlayerAdapter = platformPlayerAdapter;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        listenerRegistry.register(DeletePulseListener.class);
    }

    @Override
    public void onDisable() {
        playersHistory.clear();
    }

    public void clearHistory(FPlayer fPlayer) {
        playersHistory.remove(fPlayer.getUuid());
    }

    public void save(FPlayer receiver, UUID messageUUID, Component component) {
        // skip console
        if (receiver.isUnknown()) return;
        // skip offline history
        if (!receiver.isOnline()) return;

        UUID playerUUID = receiver.getUuid();
        HistoryMessage historyMessage = new HistoryMessage(messageUUID, component);

        List<HistoryMessage> history = playersHistory.computeIfAbsent(playerUUID, k -> new ArrayList<>());

        if (history.size() >= message.getHistoryLength()) {
            history.removeFirst();
        }

        history.add(historyMessage);
    }

    public boolean remove(FEntity sender, UUID messageUUID) {
        if (checkModulePredicates(sender)) return false;
        if (messageUUID == null) return false;

        List<Map.Entry<UUID, List<HistoryMessage>>> entryToDelete = playersHistory.entrySet().stream()
                .filter(entry -> entry.getValue()
                        .stream()
                        .anyMatch(historyMessage -> historyMessage.uuid().equals(messageUUID))
                )
                .toList();

        if (entryToDelete.isEmpty()) return false;

        entryToDelete.forEach(entry -> {
            UUID receiver = entry.getKey();
            List<HistoryMessage> history = entry.getValue();

            FPlayer fReceiver = fPlayerService.getFPlayer(receiver);
            String format = resolveLocalization(fReceiver).getFormat();
            if (format.isBlank()) {
                history.removeIf(historyMessage -> historyMessage.uuid().equals(messageUUID));
            } else {
                for (int i = 0; i < history.size(); i++) {
                    HistoryMessage historyMessage = history.get(i);
                    if (messageUUID.equals(historyMessage.uuid())) {
                        Component removedComponent = messagePipeline.builder(sender, fReceiver, format).build();
                        history.set(i, new HistoryMessage(messageUUID, removedComponent));
                    }
                }
            }

            sendUpdate(receiver);
        });

        return true;
    }

    public void sendUpdate(UUID receiver) {
        List<HistoryMessage> history = playersHistory.get(receiver);
        if (history == null) return;

        FPlayer fPlayer = fPlayerService.getFPlayer(receiver);

        // empty messages
        for (int i = 0; i < message.getHistoryLength(); i++) {
            if (i >= history.size()) {
                messagePulseListener.sendMessage(fPlayer, Component.newline());
            }
        }

        history.forEach(historyMessage ->
                messagePulseListener.sendMessage(fPlayer, historyMessage.component())
        );
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

}
