package net.flectone.pulse.module.message.format.moderation.delete;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.format.moderation.delete.listener.DeletePulseListener;
import net.flectone.pulse.module.message.format.moderation.delete.model.HistoryMessage;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.sender.MessageSender;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.apache.commons.lang3.Strings;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DeleteModule extends AbstractModuleLocalization<Localization.Message.Format.Moderation.Delete> {

    private final Map<UUID, List<HistoryMessage>> playersHistory = new ConcurrentHashMap<>();

    // only for skipping FlectonePulse messages
    private final List<Component> cachedComponents = new CopyOnWriteArrayList<>();

    private final FileFacade fileFacade;
    private final ListenerRegistry listenerRegistry;
    private final MessagePipeline messagePipeline;
    private final FPlayerService fPlayerService;
    private final MessageSender messageSender;

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(DeletePulseListener.class);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        playersHistory.clear();
        cachedComponents.clear();
    }

    @Override
    public MessageType messageType() {
        return MessageType.DELETE;
    }

    @Override
    public Message.Format.Moderation.Delete config() {
        return fileFacade.message().format().moderation().delete();
    }

    @Override
    public Permission.Message.Format.Moderation.Delete permission() {
        return fileFacade.permission().message().format().moderation().delete();
    }

    @Override
    public Localization.Message.Format.Moderation.Delete localization(FEntity sender) {
        return fileFacade.localization(sender).message().format().moderation().delete();
    }

    public void clearHistory(FPlayer fPlayer) {
        playersHistory.remove(fPlayer.uuid());
    }

    public MessageContext addTag(MessageContext messageContext) {
        FEntity sender = messageContext.sender();
        FPlayer receiver = messageContext.receiver();
        if (isModuleDisabledFor(receiver)) return messageContext;

        UUID messageUUID = messageContext.messageUUID();

        return messageContext.addTagResolver(MessagePipeline.ReplacementTag.DELETE, (argumentQueue, context) -> {
            String placeholder = Strings.CS.replace(
                    localization(receiver).placeholder(),
                    "<uuid>",
                    messageUUID.toString()
            );

            MessageContext newContext = messagePipeline.createContext(sender, receiver, placeholder)
                    .withFlags(messageContext.flags())
                    .addFlags(
                            new MessageFlag[]{MessageFlag.MENTION, MessageFlag.INTERACTIVE_CHAT, MessageFlag.QUESTION, MessageFlag.DELETE, MessageFlag.USER_MESSAGE},
                            new boolean[]{false, false, false, false, false}
                    );

            Component componentPlaceholder = messagePipeline.build(newContext);

            return Tag.selfClosingInserting(componentPlaceholder);
        });
    }

    public void save(FPlayer receiver, UUID messageUUID, Component component, boolean needToCache) {
        // skip console
        if (receiver.isUnknown()) return;
        // skip offline history
        if (!receiver.isOnline()) return;

        UUID playerUUID = receiver.uuid();
        HistoryMessage historyMessage = new HistoryMessage(messageUUID, component);

        List<HistoryMessage> history = playersHistory.computeIfAbsent(playerUUID, k -> new ObjectArrayList<>());

        if (history.size() >= config().historyLength()) {
            history.removeFirst();
        }

        history.add(historyMessage);

        if (needToCache && !isCached(component)) {
            cachedComponents.add(component);
        }
    }

    public boolean isCached(Component component) {
        // idk why, but this doesn't work
        // return playersHistory.values().stream().anyMatch(historyMessages -> historyMessages.stream().anyMatch(historyMessage -> historyMessage.component().equals(component)));
        return cachedComponents.contains(component);
    }

    public void removeCache(Component component) {
        cachedComponents.remove(component);
    }

    public boolean remove(FEntity sender, UUID messageUUID) {
        if (isModuleDisabledFor(sender)) return false;
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
            String format = localization(fReceiver).format();
            if (format.isBlank()) {
                history.removeIf(historyMessage -> historyMessage.uuid().equals(messageUUID));
            } else {
                for (int i = 0; i < history.size(); i++) {
                    HistoryMessage historyMessage = history.get(i);
                    if (messageUUID.equals(historyMessage.uuid())) {
                        MessageContext messageContext = messagePipeline.createContext(sender, fReceiver, format);
                        Component removedComponent = messagePipeline.build(messageContext);
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
        for (int i = 0; i < config().historyLength(); i++) {
            if (i >= history.size()) {
                messageSender.sendMessage(fPlayer, Component.newline(), true);
            }
        }

        history.forEach(historyMessage ->
                messageSender.sendMessage(fPlayer, historyMessage.component(), true)
        );
    }
}
