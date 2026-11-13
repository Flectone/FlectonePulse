package net.flectone.pulse.module.message.format.uniform;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.retrooper.packetevents.protocol.advancements.Advancement;
import com.github.retrooper.packetevents.protocol.advancements.AdvancementDisplay;
import com.github.retrooper.packetevents.protocol.advancements.AdvancementHolder;
import com.github.retrooper.packetevents.protocol.chat.ChatType;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_19;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_19_1;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_19_3;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.score.FixedScoreFormat;
import com.github.retrooper.packetevents.protocol.score.ScoreFormat;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.message.format.converter.LegacyMessageConverter;
import net.flectone.pulse.module.message.format.uniform.listener.MinecraftUniformPacketListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.serializer.ComponentSerializer;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Singleton
public class MinecraftUniformModule extends UniformModuleImpl {

    private final ListenerRegistry listenerRegistry;
    private final LegacyMessageConverter componentFormatter;
    private final ComponentSerializer componentSerializer;

    @Inject
    public MinecraftUniformModule(FileFacade fileFacade,
                                  ListenerRegistry listenerRegistry,
                                  @Named("uniformMessage") Cache<Component, Boolean> formattedMessages,
                                  LegacyMessageConverter componentFormatter,
                                  ComponentSerializer componentSerializer) {
        super(fileFacade, listenerRegistry, formattedMessages);

        this.listenerRegistry = listenerRegistry;
        this.componentFormatter = componentFormatter;
        this.componentSerializer = componentSerializer;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(MinecraftUniformPacketListener.class);
    }

    public @Nullable Component formatComponent(@Nullable Component component, FPlayer receiver) {
        if (isFormatted(component)) return component;

        Component jsonComponent = parseJsonComponent(component);
        if (jsonComponent != null) {
            component = jsonComponent;
        }

        return componentFormatter.convert(component, receiver);
    }

    public boolean formatChatMessage(ChatMessage chatMessage, FPlayer receiver) {
        Component content = chatMessage.getChatContent();
        if (content == null) return false;

        ChatType type = chatMessage.getType();
        if (isActionBar(type)) {
            if (!config().types().actionBar()) return false;
        } else if (isPlayerChat(type)) {
            if (!config().types().playerChat()) return false;
        } else if (!config().types().systemChat()) {
            return false;
        }

        Component converted = formatComponent(content, receiver);
        if (converted == content) return false;

        switch (chatMessage) {
            case ChatMessage_v1_19_3 signed -> {
                signed.setPlainContent(componentSerializer.toPlain(content));
                signed.setUnsignedChatContent(content);
                return true;
            }
            case ChatMessage_v1_19_1 signed -> {
                signed.setPlainContent(componentSerializer.toPlain(content));
                signed.setUnsignedChatContent(content);
                return true;
            }
            case ChatMessage_v1_19 signed -> {
                signed.setUnsignedChatContent(content);
                return true;
            }
            default -> {
                try {
                    chatMessage.setChatContent(content);
                    return true;
                } catch (UnsupportedOperationException ignored) {
                    return false;
                }
            }
        }
    }

    public boolean formatAdvancements(List<AdvancementHolder> holders, FPlayer receiver) {
        boolean changed = false;

        for (AdvancementHolder holder : holders) {
            Advancement advancement = holder.getAdvancement();
            if (advancement == null) continue;

            AdvancementDisplay display = advancement.getDisplay();
            if (display == null) continue;

            Component title = display.getTitle();
            Component convertedTitle = formatComponent(title, receiver);
            if (convertedTitle != title) {
                display.setTitle(convertedTitle);
                changed = true;
            }

            Component description = display.getDescription();
            Component convertedDescription = formatComponent(description, receiver);
            if (convertedDescription != description) {
                display.setDescription(convertedDescription);
                changed = true;
            }
        }

        return changed;
    }

    public List<EntityData<?>> formatEntityMetadata(List<EntityData<?>> metadata, FPlayer receiver) {
        if (!config().types().entityName()) return metadata;

        List<EntityData<?>> convertedMetadata = new ArrayList<>(metadata.size());
        boolean changed = false;

        for (EntityData<?> entityData : metadata) {
            EntityData<?> converted = formatEntityData(entityData, receiver);
            convertedMetadata.add(converted);
            if (converted != entityData) {
                changed = true;
            }
        }

        return changed ? convertedMetadata : metadata;
    }

    public boolean formatTeamInfo(WrapperPlayServerTeams.ScoreBoardTeamInfo scoreBoardTeamInfo, FPlayer receiver) {
        boolean changed = false;

        Component displayName = scoreBoardTeamInfo.getDisplayName();
        Component convertedDisplayName = formatComponent(displayName, receiver);
        if (convertedDisplayName != displayName) {
            scoreBoardTeamInfo.setDisplayName(convertedDisplayName);
            changed = true;
        }

        Component prefix = scoreBoardTeamInfo.getPrefix();
        Component convertedPrefix = formatComponent(prefix, receiver);
        if (convertedPrefix != prefix) {
            scoreBoardTeamInfo.setPrefix(convertedPrefix);
            changed = true;
        }

        Component suffix = scoreBoardTeamInfo.getSuffix();
        Component convertedSuffix = formatComponent(suffix, receiver);
        if (convertedSuffix != suffix) {
            scoreBoardTeamInfo.setSuffix(convertedSuffix);
            changed = true;
        }

        return changed;
    }

    public @Nullable ScoreFormat formatScoreFormat(@Nullable ScoreFormat scoreFormat, FPlayer receiver) {
        if (!(scoreFormat instanceof FixedScoreFormat fixedScoreFormat)) return scoreFormat;

        Component value = fixedScoreFormat.getValue();
        Component converted = formatComponent(value, receiver);
        if (converted == value) return scoreFormat;

        return new FixedScoreFormat(converted);
    }

    private @Nullable Component parseJsonComponent(@Nullable Component component) {
        if (component == null) return null;

        String legacy = componentSerializer.toLegacy(component).trim();
        if (!legacy.startsWith("{") && !legacy.startsWith("[")) return null;

        try {
            return componentSerializer.fromJson(legacy);
        } catch (Exception ignored) {
            return null;
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private EntityData<?> formatEntityData(EntityData<?> entityData, FPlayer receiver) {
        Object value = entityData.getValue();
        if (value == null) return entityData;

        if (entityData.getType() == EntityDataTypes.ADV_COMPONENT && value instanceof Component component) {
            Component converted = formatComponent(component, receiver);
            if (converted == component) return entityData;

            return new EntityData(entityData.getIndex(), EntityDataTypes.ADV_COMPONENT, converted);
        }

        if (entityData.getType() == EntityDataTypes.OPTIONAL_ADV_COMPONENT
                && value instanceof Optional<?> optional
                && optional.orElse(null) instanceof Component component) {
            Component converted = formatComponent(component, receiver);
            if (converted == component) return entityData;

            return new EntityData(entityData.getIndex(), EntityDataTypes.OPTIONAL_ADV_COMPONENT, Optional.of(converted));
        }

        if (entityData.getType() == EntityDataTypes.STRING && value instanceof String string) {
            Component component = Component.text(string);
            if (!componentFormatter.needsParsing(component)) return entityData;

            Component converted = formatComponent(component, receiver);
            if (converted == component) return entityData;

            return new EntityData(entityData.getIndex(), EntityDataTypes.STRING, componentSerializer.toLegacy(converted));
        }

        if (entityData.getType() == EntityDataTypes.COMPONENT && value instanceof String json) {
            String converted = convertJson(json, receiver);
            if (converted == null) return entityData;

            return new EntityData(entityData.getIndex(), EntityDataTypes.COMPONENT, converted);
        }

        if (entityData.getType() == EntityDataTypes.OPTIONAL_COMPONENT
                && value instanceof Optional<?> optional
                && optional.orElse(null) instanceof String json) {
            String converted = convertJson(json, receiver);
            if (converted == null) return entityData;

            return new EntityData(entityData.getIndex(), EntityDataTypes.OPTIONAL_COMPONENT, Optional.of(converted));
        }

        return entityData;
    }

    private @Nullable String convertJson(String json, FPlayer receiver) {
        try {
            Component component = componentSerializer.fromJson(json);
            if (component == null || !componentFormatter.needsParsing(component)) return null;

            Component converted = formatComponent(component, receiver);
            if (converted == null || converted == component) return null;

            return componentSerializer.toJson(converted);
        } catch (Exception ignored) {
            return null;
        }
    }

    private boolean isActionBar(@Nullable ChatType type) {
        return type != null && "game_info".equals(type.getName().getKey());
    }

    private boolean isPlayerChat(@Nullable ChatType type) {
        if (type == null) return false;

        return switch (type.getName().getKey()) {
            case "chat", "say_command", "msg_command_incoming", "msg_command_outgoing", "team_msg_command_incoming",
                 "team_msg_command_outgoing", "emote_command" -> true;
            default -> false;
        };
    }

}
