package net.flectone.pulse.module.message.vanilla.listener;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.ProxyMessageEvent;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.message.vanilla.VanillaModule;
import net.flectone.pulse.module.message.vanilla.extractor.ComponentExtractor;
import net.flectone.pulse.module.message.vanilla.model.ParsedComponent;
import net.flectone.pulse.module.message.vanilla.model.VanillaMetadata;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.io.ProxyPayload;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class VanillaProxyMessageListener implements PulseListener {

    private final VanillaModule vanillaModule;
    private final ModuleController moduleController;
    private final MessageDispatcher messageDispatcher;
    private final Gson gson;
    private final ComponentExtractor componentExtractor;
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

            messageDispatcher.dispatch(vanillaModule, VanillaMetadata.<Localization.Message.Vanilla>builder()
                    .base(EventMetadata.<Localization.Message.Vanilla>builder()
                            .uuid(event.uuid())
                            .sender(event.sender())
                            .format(localization -> StringUtils.defaultString(localization.types().get(parsedComponent.translationKey())))
                            .tagResolvers(fResolver -> new TagResolver[]{vanillaModule.argumentTag(fResolver, parsedComponent)})
                            .range(Range.get(Range.Type.SERVER))
                            .filter(fResolver -> vanillaMessageName.isEmpty() || socialService.isSetting(fResolver, vanillaMessageName))
                            .filter(fResolver -> socialService.canSeeVanished(event.sender(), fResolver, vanished))
                            .destination(parsedComponent.vanillaMessage().destination())
                            .build()
                    )
                    .parsedComponent(parsedComponent)
                    .fakeMessage(false)
                    .vanished(vanished)
                    .build()
            );
        }

        return event.withProcessed(true);
    }

    private Map<Integer, Object> parseVanillaArguments(ProxyPayload proxyPayload) throws IOException {
        JsonObject jsonObject = gson.fromJson(proxyPayload.readString(), JsonObject.class);

        Int2ObjectOpenHashMap<Object> result = new Int2ObjectOpenHashMap<>();

        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            int key = Integer.parseInt(entry.getKey());
            JsonObject argumentJson = entry.getValue().getAsJsonObject();

            Optional<FEntity> entity = proxyPayload.parseFEntity(gson, argumentJson);
            result.put(key, entity.isPresent() ? entity.get() : gson.fromJson(argumentJson, Component.class));
        }

        return result;
    }

}
