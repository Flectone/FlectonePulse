package net.flectone.pulse.module.integration.placeholderapi;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.model.FColor;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.processing.mapper.FPlayerMapper;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.logging.FLogger;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

@Singleton
public class PlaceholderAPIIntegration implements FIntegration, PulseListener {

    private final Message.Format.FColor fColorMessage;
    private final Permission.Integration.Placeholderapi permission;
    private final FPlayerService fPlayerService;
    private final FPlayerMapper fPlayerMapper;
    private final PlatformServerAdapter platformServerAdapter;
    private final Provider<PlaceholderAPIModule> placeholderAPIModuleProvider;
    private final FLogger fLogger;
    private final PermissionChecker permissionChecker;

    @Inject
    public PlaceholderAPIIntegration(FileResolver fileResolver,
                                     FPlayerService fPlayerService,
                                     FPlayerMapper fPlayerMapper,
                                     PlatformServerAdapter platformServerAdapter,
                                     Provider<PlaceholderAPIModule> placeholderAPIModuleProvider,
                                     FLogger fLogger,
                                     PermissionChecker permissionChecker) {
        this.fColorMessage = fileResolver.getMessage().getFormat().getFcolor();
        this.permission = fileResolver.getPermission().getIntegration().getPlaceholderapi();
        this.fPlayerService = fPlayerService;
        this.fPlayerMapper = fPlayerMapper;
        this.platformServerAdapter = platformServerAdapter;
        this.placeholderAPIModuleProvider = placeholderAPIModuleProvider;
        this.fLogger = fLogger;
        this.permissionChecker = permissionChecker;
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

        Placeholders.register(Identifier.of(BuildConfig.PROJECT_MOD_ID, "fcolor"), (context, argument) ->
                fColorPlaceholder(context, argument, FColor.Type.SEE, FColor.Type.OUT)
        );

        Placeholders.register(Identifier.of(BuildConfig.PROJECT_MOD_ID, "fcolor_out"), (context, argument) ->
                fColorPlaceholder(context, argument, FColor.Type.OUT)
        );

        Placeholders.register(Identifier.of(BuildConfig.PROJECT_MOD_ID, "fcolor_see"), (context, argument) ->
                fColorPlaceholder(context, argument, FColor.Type.SEE)
        );

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

    @Pulse(priority = Event.Priority.LOW)
    public void onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.getContext();
        FEntity sender = messageContext.getSender();
        if (placeholderAPIModuleProvider.get().isModuleDisabledFor(sender)) return;

        FEntity receiver = messageContext.getReceiver();
        boolean isUserMessage = messageContext.isFlag(MessageFlag.USER_MESSAGE);
        if (!permissionChecker.check(sender, permission.getUse()) && isUserMessage) return;
        if (!permissionChecker.check(receiver, permission.getUse()) && isUserMessage) return;
        if (!(sender instanceof FPlayer fPlayer)) return;

        Object player = fPlayerService.toPlatformFPlayer(fPlayer);
        if (!(player instanceof ServerPlayerEntity playerEntity)) return;

        String message = messageContext.getMessage();

        Text text = Placeholders.parseText(Text.literal(message), PlaceholderContext.of(playerEntity.getCommandSource()));
        messageContext.setMessage(text.getString());
    }

    private PlaceholderResult fColorPlaceholder(PlaceholderContext context, String argument, FColor.Type... types) {
        if (argument == null) return PlaceholderResult.invalid();
        if (!StringUtils.isNumeric(argument)) return PlaceholderResult.invalid();

        FPlayer fPlayer = fPlayerMapper.map(context.source());

        Map<Integer, String> colorsMap = new HashMap<>(fColorMessage.getDefaultColors());
        for (FColor.Type type : types) {
            colorsMap.putAll(fPlayer.getFColors(type));
        }

        int colorNumber = Integer.parseInt(argument);
        return PlaceholderResult.value(colorsMap.get(colorNumber));
    }
}
