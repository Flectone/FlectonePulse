package net.flectone.pulse.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.message.format.moderation.delete.model.HistoryMessage;
import net.flectone.pulse.platform.sender.MessageSender;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.UnaryOperator;

/**
 * Shared chat history per receiver + brute-force chat replay mechanism.
 *
 * <p>Used by {@code DeleteModule} (moderation: hide deleted messages, redraw chat)
 * and {@code TranslateModule} (auto-translate: redraw chat when async translation lands).
 * Both consumers read/write the same per-receiver list but apply their own state flags on
 * {@link HistoryMessage}.
 *
 * <p>Replay is a brute redraw: spam empty newlines to push old chat off the visible area,
 * then reprint the stored history. This is the only mechanism Minecraft offers for
 * "modifying" already-sent system chat lines.
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ChatHistoryService {

    private final Map<UUID, List<HistoryMessage>> playersHistory = new ConcurrentHashMap<>();

    // Components originated by FlectonePulse itself — used to dedupe vanilla MessageReceiveEvent
    // against MessageSendEvent (same component arrives twice; only one should land in history).
    private final List<Component> selfOriginatedComponents = new CopyOnWriteArrayList<>();

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final MessageSender messageSender;

    private int historyLength() {
        return fileFacade.message().format().moderation().delete().historyLength();
    }

    public void save(FPlayer receiver, UUID messageUUID, Component component, boolean needToCache) {
        save(receiver, messageUUID, component, null, needToCache);
    }

    public void save(FPlayer receiver,
                     UUID messageUUID,
                     Component component,
                     net.flectone.pulse.module.message.format.translate.model.TranslatedMessage translatedMessage,
                     boolean needToCache) {
        // skip console
        if (receiver.isUnknown()) return;
        // skip offline history
        if (!receiver.isOnline()) return;

        UUID playerUUID = receiver.uuid();
        HistoryMessage historyMessage = new HistoryMessage(messageUUID, component, translatedMessage);

        List<HistoryMessage> history = playersHistory.computeIfAbsent(playerUUID, _ -> new ObjectArrayList<>());

        if (history.stream().anyMatch(h -> h.uuid().equals(messageUUID))) return;

        if (history.size() >= historyLength()) {
            history.removeFirst();
        }

        history.add(historyMessage);

        if (needToCache && !isCached(component)) {
            selfOriginatedComponents.add(component);
        }
    }

    public boolean isCached(Component component) {
        return selfOriginatedComponents.contains(component);
    }

    public void removeCache(Component component) {
        selfOriginatedComponents.remove(component);
    }

    public void clearHistory(FPlayer fPlayer) {
        playersHistory.remove(fPlayer.uuid());
    }

    public List<HistoryMessage> getHistory(UUID receiverUUID) {
        return playersHistory.get(receiverUUID);
    }

    public Map<UUID, List<HistoryMessage>> snapshotHistories() {
        return Map.copyOf(playersHistory);
    }

    /**
     * Replace a history entry matching {@code messageId} for the given receiver.
     * Returns true if the entry was found and updated.
     */
    public boolean updateEntry(UUID receiverUUID, UUID messageId, UnaryOperator<HistoryMessage> updater) {
        List<HistoryMessage> history = playersHistory.get(receiverUUID);
        if (history == null) return false;

        for (int i = 0; i < history.size(); i++) {
            HistoryMessage entry = history.get(i);
            if (messageId.equals(entry.uuid())) {
                history.set(i, updater.apply(entry));
                return true;
            }
        }
        return false;
    }

    /**
     * Brute-force chat redraw for the receiver:
     * 1. Push empty newlines to scroll old chat off-screen.
     * 2. Reprint history rendering each entry via {@link HistoryMessage#getDisplayComponent(String)}.
     */
    public void sendUpdate(UUID receiverUUID) {
        List<HistoryMessage> history = playersHistory.get(receiverUUID);
        if (history == null) return;

        FPlayer fPlayer = fPlayerService.getFPlayer(receiverUUID);
        String playerLocale = fPlayer.getSetting(SettingText.LOCALE);

        // push old messages off-screen
        for (int i = 0; i < historyLength(); i++) {
            if (i >= history.size()) {
                messageSender.sendMessage(fPlayer, Component.newline(), true);
            }
        }

        history.forEach(historyMessage ->
                messageSender.sendMessage(fPlayer, historyMessage.getDisplayComponent(playerLocale), true)
        );
    }

    public void shutdown() {
        playersHistory.clear();
        selfOriginatedComponents.clear();
    }
}
