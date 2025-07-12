package net.flectone.pulse.module.message.chat;

import com.google.inject.Inject;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.model.Cooldown;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Sound;
import net.flectone.pulse.module.AbstractModuleMessage;

import java.util.HashMap;
import java.util.Map;

public abstract class ChatModule extends AbstractModuleMessage<Localization.Message.Chat> {

    protected final Map<String, Cooldown> cooldownMap = new HashMap<>();
    protected final Map<String, Sound> soundMap = new HashMap<>();

    protected final Message.Chat message;
    protected final Permission.Message.Chat permission;

    @Inject private PermissionChecker permissionChecker;

    protected ChatModule(FileResolver fileResolver) {
        super(localization -> localization.getMessage().getChat());

        this.message = fileResolver.getMessage().getChat();
        this.permission = fileResolver.getPermission().getMessage().getChat();
    }

    @Override
    public void onEnable() {
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
    public void onDisable() {
        cooldownMap.clear();
        soundMap.clear();
    }

    @Override
    protected boolean isConfigEnable() {
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
            if (!permissionChecker.check(fPlayer, permission.getTypes().get(chatName))) continue;

            playerChat = chat;
            priority = chat.getPriority();
        }

        return playerChat;
    }
}
