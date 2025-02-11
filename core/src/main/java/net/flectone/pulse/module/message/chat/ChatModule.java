package net.flectone.pulse.module.message.chat;

import com.google.inject.Inject;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.Cooldown;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Sound;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.util.PermissionUtil;

import java.util.HashMap;
import java.util.Map;

public abstract class ChatModule extends AbstractModuleMessage<Localization.Message.Chat> {

    protected final Map<String, Cooldown> cooldownMap = new HashMap<>();
    protected final Map<String, Sound> soundMap = new HashMap<>();

    protected final Message.Chat message;
    protected final Permission.Message.Chat permission;

    @Inject private PermissionUtil permissionUtil;

    @Inject
    public ChatModule(FileManager fileManager) {
        super(localization -> localization.getMessage().getChat());

        message = fileManager.getMessage().getChat();
        permission = fileManager.getPermission().getMessage().getChat();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        message.getTypes().forEach((key, value) -> {
            Permission.Message.Chat.Type permissions = permission.getTypes().get(key);
            if (permissions == null) return;

            registerPermission(permissions);
            cooldownMap.put(key, createCooldown(value.getCooldown(), permissions.getCooldownBypass()));
            soundMap.put(key, createSound(value.getSound(), permissions.getSound()));
        });
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    public abstract void send(FPlayer fPlayer, Object event);
    public abstract void send(FEntity fPlayer, String chatName, String string);

    protected Message.Chat.Type getPlayerChat(FPlayer fPlayer, String message) {
        Message.Chat.Type playerChat = null;

        int priority = Integer.MIN_VALUE;

        for (var entry : this.message.getTypes().entrySet()) {

            Message.Chat.Type chat = entry.getValue();
            String chatName = entry.getKey();

            if (!chat.isEnable()) continue;
            if (chat.getTrigger() != null
                    && !chat.getTrigger().isEmpty()
                    && !message.startsWith(chat.getTrigger())) continue;
            if (message.equals(chat.getTrigger())) continue;

            if (chat.getPriority() <= priority) continue;
            if (!permissionUtil.has(fPlayer, permission.getTypes().get(chatName))) continue;

            playerChat = chat;
            priority = chat.getPriority();
        }

        return playerChat;
    }
}
