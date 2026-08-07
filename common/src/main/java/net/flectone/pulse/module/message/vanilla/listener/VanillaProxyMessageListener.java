package net.flectone.pulse.module.message.vanilla.listener;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.dispatcher.MessageDispatcher;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FEntityImpl;
import net.flectone.pulse.model.entity.FPlayerImpl;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.ProxyMessageEvent;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.value.Range;
import net.flectone.pulse.module.message.vanilla.VanillaModule;
import net.flectone.pulse.module.message.vanilla.extractor.ComponentExtractor;
import net.flectone.pulse.module.message.vanilla.model.ParsedComponent;
import net.flectone.pulse.module.message.vanilla.model.VanillaMessageContext;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.payload.ProxyPayload;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class VanillaProxyMessageListener implements PulseListener {

    private final VanillaModule vanillaModule;
    private final ModuleController moduleController;
    private final MessageDispatcher messageDispatcher;
    private final Gson gson;
    private final ComponentExtractor<?> componentExtractor;
    private final SocialService socialService;

    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.processed()) return event;
        if (event.name() != ModuleName.MESSAGE_VANILLA) return event;
        if (moduleController.isDisabledFor(vanillaModule, event.sender())) return event.withProcessed(true);

        try (ProxyPayload proxyPayload = new ProxyPayload(event.payload())) {
            String translationKey = proxyPayload.readString();
            Map<Integer, Object> arguments = parseVanillaArguments(proxyPayload);

            Message.Vanilla.VanillaMessage vanillaMessage = componentExtractor.getVanillaMessage(translationKey);
            if (!vanillaMessage.range().is(Range.Type.PROXY)) return event.withProcessed(true);

            ParsedComponent parsedComponent = new ParsedComponent(translationKey, vanillaMessage, arguments);

            String vanillaMessageName = vanillaMessage.name();
            boolean vanished = proxyPayload.readBoolean();

            messageDispatcher.dispatch(vanillaModule, EventMetadata.builder()
                    .range(Range.get(Range.Type.SERVER))
                    .filter(fResolver -> vanillaMessageName.isEmpty() || socialService.isSetting(fResolver, vanillaMessageName))
                    .filter(fResolver -> socialService.canSeeVanished(event.sender(), fResolver, vanished))
                    .destination(parsedComponent.vanillaMessage().destination())
                    .messageContext(fResolver -> VanillaMessageContext.builder()
                            .base(MessageContext.builder()
                                    .uuid(event.uuid())
                                    .sender(event.sender())
                                    .receiver(fResolver)
                                    .message(StringUtils.defaultString(vanillaModule.localization(fResolver).types().get(parsedComponent.translationKey())))
                                    .tagResolver(vanillaModule.argumentTag(fResolver, parsedComponent))
                                    .build()
                            )
                            .parsedComponent(parsedComponent)
                            .fakeMessage(false)
                            .vanished(vanished)
                            .build()
                    )
                    .build()
            );
        }

        return event.withProcessed(true);
    }

    private Map<Integer, Object> parseVanillaArguments(ProxyPayload proxyPayload) throws IOException {
        JsonObject jsonObject = gson.fromJson(proxyPayload.readString(), JsonObject.class);

        HashMap<Integer, Object> result = new HashMap<>();

        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            int key = Integer.parseInt(entry.getKey());
            JsonObject argumentJson = entry.getValue().getAsJsonObject();

            Optional<FEntity> entity = parseFEntity(argumentJson);
            result.put(key, entity.isPresent() ? entity.get() : gson.fromJson(argumentJson, Component.class));
        }

        return result;
    }

    public Optional<FEntity> parseFEntity(JsonObject jsonObject) {
        if (jsonObject.has("name") && jsonObject.has("uuid") && jsonObject.has("type")) {
            boolean isPlayer = jsonObject.has("id");
            return Optional.of(gson.fromJson(jsonObject, isPlayer ? FPlayerImpl.class : FEntityImpl.class));
        }

        return Optional.empty();
    }

}
