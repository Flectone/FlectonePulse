package net.flectone.pulse.platform.handler;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.message.tab.playerlist.PlayerlistnameModule;
import net.flectone.pulse.module.message.vanilla.MinecraftVanillaModule;
import net.flectone.pulse.module.message.vanilla.extractor.Extractor;
import net.flectone.pulse.module.message.vanilla.model.ParsedComponent;
import net.flectone.pulse.module.message.vanilla.model.VanillaMetadata;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Singleton
public class MinecraftProxyMessageHandler extends ProxyMessageHandler {

    private final Injector injector;
    private final Gson gson;

    @Inject
    public MinecraftProxyMessageHandler(Injector injector,
                                        FileFacade fileFacade,
                                        FPlayerService fPlayerService,
                                        FLogger fLogger,
                                        ModerationService moderationService,
                                        Gson gson,
                                        TaskScheduler taskScheduler) {
        super(injector, fileFacade, fPlayerService, fLogger, moderationService, gson, taskScheduler);

        this.injector = injector;
        this.gson = gson;
    }

    @Override
    public void handleSystemOnline(UUID uuid) throws IOException {
        super.handleSystemOnline(uuid);

        injector.getInstance(PlayerlistnameModule.class).add(uuid);
    }

    @Override
    public void handleSystemOffline(UUID uuid) throws IOException {
        super.handleSystemOffline(uuid);

        injector.getInstance(PlayerlistnameModule.class).remove(uuid);
    }

    public void handleModuleMessage(DataInputStream input, FEntity fEntity, UUID metadataUUID, MessageType tag) throws IOException {
        if (tag == MessageType.VANILLA) {
            handleVanilla(input, fEntity, metadataUUID);
            return;
        }

        super.handleModuleMessage(input, fEntity, metadataUUID, tag);
    }

    private void handleVanilla(DataInputStream input, FEntity fEntity, UUID metadataUUID) throws IOException {
        MinecraftVanillaModule module = injector.getInstance(MinecraftVanillaModule.class);
        if (module.isModuleDisabledFor(fEntity)) return;

        String translationKey = input.readUTF();
        Map<Integer, Object> arguments = parseVanillaArguments(readAsJsonObject(input));

        Message.Vanilla.VanillaMessage vanillaMessage = injector.getInstance(Extractor.class).getVanillaMessage(translationKey);

        ParsedComponent parsedComponent = new ParsedComponent(translationKey, vanillaMessage, arguments);

        String vanillaMessageName = vanillaMessage.name();

        module.sendMessage(VanillaMetadata.<Localization.Message.Vanilla>builder()
                .uuid(metadataUUID)
                .parsedComponent(parsedComponent)
                .sender(fEntity)
                .format(localization -> StringUtils.defaultString(localization.types().get(parsedComponent.translationKey())))
                .tagResolvers(fResolver -> new TagResolver[]{module.argumentTag(fResolver, parsedComponent)})
                .range(Range.get(Range.Type.SERVER))
                .filter(fResolver -> vanillaMessageName.isEmpty() || fResolver.isSetting(vanillaMessageName))
                .destination(parsedComponent.vanillaMessage().destination())
                .build()
        );
    }

    private Map<Integer, Object> parseVanillaArguments(JsonObject jsonObject) {
        Map<Integer, Object> result = new HashMap<>();

        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            Integer key = Integer.parseInt(entry.getKey());
            JsonObject argumentJson = entry.getValue().getAsJsonObject();

            Optional<FEntity> entity = parseFEntity(argumentJson);
            result.put(key, entity.isPresent() ? entity.get() : gson.fromJson(argumentJson, Component.class));
        }

        return result;
    }

}
