package net.flectone.pulse.module.integration.placeholderapi;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.adapter.PlatformServerAdapter;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.context.MessageContext;
import net.flectone.pulse.mapper.FPlayerMapper;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.processor.MessageProcessor;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.logging.FLogger;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Arrays;

@Singleton
public class PlaceholderAPIIntegration implements FIntegration, MessageProcessor {

    private final Message.Format.Color color;
    private final FPlayerService fPlayerService;
    private final FPlayerMapper fPlayerMapper;
    private final PlatformServerAdapter platformServerAdapter;
    private final FLogger fLogger;

    @Inject
    public PlaceholderAPIIntegration(FileResolver fileResolver,
                                     FPlayerService fPlayerService,
                                     FPlayerMapper fPlayerMapper,
                                     PlatformServerAdapter platformServerAdapter,
                                     FLogger fLogger) {
        this.color = fileResolver.getMessage().getFormat().getColor();
        this.fPlayerService = fPlayerService;
        this.fPlayerMapper = fPlayerMapper;
        this.platformServerAdapter = platformServerAdapter;
        this.fLogger = fLogger;
    }

    @Override
    public void hook() {
        register();
    }

    @Override
    public void unhook() {
        Placeholders.remove(Identifier.of(BuildConfig.PROJECT_MOD_ID, "fcolor"));

        Arrays.stream(FPlayer.Setting.values()).forEach(setting ->
                Placeholders.remove(Identifier.of(BuildConfig.PROJECT_MOD_ID, setting.name().toLowerCase()))
        );

        Placeholders.remove(Identifier.of(BuildConfig.PROJECT_MOD_ID, "player"));
        Placeholders.remove(Identifier.of(BuildConfig.PROJECT_MOD_ID, "ip"));
        Placeholders.remove(Identifier.of(BuildConfig.PROJECT_MOD_ID, "ping"));
        Placeholders.remove(Identifier.of(BuildConfig.PROJECT_MOD_ID, "online"));
        Placeholders.remove(Identifier.of(BuildConfig.PROJECT_MOD_ID, "tps"));
        fLogger.info("✖ Text Placeholder API unhooked");
    }

    @Async(delay = 20)
    public void register() {

        Placeholders.register(Identifier.of(BuildConfig.PROJECT_MOD_ID, "fcolor"), (context, argument) -> {
            if (argument == null) return PlaceholderResult.invalid();

            FPlayer fPlayer = fPlayerMapper.map(context.source());
            String value = fPlayer.getColors().getOrDefault(argument, color.getValues().get(argument));
            return PlaceholderResult.value(value == null ? "" : value);
        });

        Arrays.stream(FPlayer.Setting.values()).forEach(setting ->
            Placeholders.register(Identifier.of(BuildConfig.PROJECT_MOD_ID, setting.name().toLowerCase()), (context, argument) -> {
                FPlayer fPlayer = fPlayerMapper.map(context.source());

                String value = fPlayer.getSettingValue(setting);
                if (setting == FPlayer.Setting.CHAT && value == null) return PlaceholderResult.value("default");
                if (setting == FPlayer.Setting.STREAM_PREFIX && value != null && value.isBlank()) return PlaceholderResult.value("");
                if (value == null) return PlaceholderResult.value("");

                return value.isEmpty() ? PlaceholderResult.value("true") : PlaceholderResult.value(value);
            })
        );

        Placeholders.register(Identifier.of(BuildConfig.PROJECT_MOD_ID, "player"), (context, argument) -> {
            FPlayer fPlayer = fPlayerMapper.map(context.source());
            return PlaceholderResult.value(fPlayer.getName());
        });

        Placeholders.register(Identifier.of(BuildConfig.PROJECT_MOD_ID, "ip"), (context, argument) -> {
            FPlayer fPlayer = fPlayerMapper.map(context.source());
            return PlaceholderResult.value(fPlayer.getIp());
        });

        Placeholders.register(Identifier.of(BuildConfig.PROJECT_MOD_ID, "ping"), (context, argument) -> {
            FPlayer fPlayer = fPlayerMapper.map(context.source());
            return PlaceholderResult.value(String.valueOf(fPlayerService.getPing(fPlayer)));
        });

        Placeholders.register(Identifier.of(BuildConfig.PROJECT_MOD_ID, "online"), (context, argument) ->
                PlaceholderResult.value(String.valueOf(platformServerAdapter.getOnlinePlayerCount()))
        );

        Placeholders.register(Identifier.of(BuildConfig.PROJECT_MOD_ID, "tps"), (context, argument) ->
                PlaceholderResult.value(platformServerAdapter.getTPS())
        );

        fLogger.info("✔ Text Placeholder API hooked");
    }

    @Override
    public void process(MessageContext messageContext) {
        FEntity sender = messageContext.getSender();
        if (!(sender instanceof FPlayer fPlayer)) return;

        Object player = fPlayerService.toPlatformFPlayer(fPlayer);
        if (!(player instanceof ServerPlayerEntity playerEntity)) return;

        String message = messageContext.getMessage();

        Text text = Placeholders.parseText(Text.literal(message), PlaceholderContext.of(playerEntity.getCommandSource()));
        messageContext.setMessage(text.getString());
    }
}
