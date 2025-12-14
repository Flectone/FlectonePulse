package net.flectone.pulse.module.integration.placeholderapi;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.FColor;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.command.mute.MuteModule;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.processing.mapper.FPlayerMapper;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.logging.FLogger;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PlaceholderAPIIntegration implements FIntegration, PulseListener {

    private final FileResolver fileResolver;
    private final FPlayerService fPlayerService;
    private final FPlayerMapper fPlayerMapper;
    private final PlatformServerAdapter platformServerAdapter;
    private final Provider<PlaceholderAPIModule> placeholderAPIModuleProvider;
    private final FLogger fLogger;
    private final PermissionChecker permissionChecker;
    private final MuteModule muteModule;

    @Override
    public void hook() {
        register();
    }

    @Override
    public void unhook() {
        Placeholders.remove(Identifier.parse(BuildConfig.PROJECT_MOD_ID + ":mute_suffix"));
        Placeholders.remove(Identifier.parse(BuildConfig.PROJECT_MOD_ID + ":fcolor"));

        Arrays.stream(MessageType.values()).forEach(setting ->
                Placeholders.remove(Identifier.parse(BuildConfig.PROJECT_MOD_ID + ":" + setting.name().toLowerCase()))
        );

        Arrays.stream(SettingText.values()).forEach(setting ->
                Placeholders.remove(Identifier.parse(BuildConfig.PROJECT_MOD_ID + ":" + setting.name().toLowerCase()))
        );

        Placeholders.remove(Identifier.parse(BuildConfig.PROJECT_MOD_ID + ":player"));
        Placeholders.remove(Identifier.parse(BuildConfig.PROJECT_MOD_ID + ":ip"));
        Placeholders.remove(Identifier.parse(BuildConfig.PROJECT_MOD_ID + ":ping"));
        Placeholders.remove(Identifier.parse(BuildConfig.PROJECT_MOD_ID + ":online"));
        Placeholders.remove(Identifier.parse(BuildConfig.PROJECT_MOD_ID + ":tps"));
        fLogger.info("✖ Text Placeholder API unhooked");
    }

    @Async(delay = 20)
    public void register() {
        Placeholders.register(Identifier.parse(BuildConfig.PROJECT_MOD_ID + ":mute_suffix"), (context, argument) -> {
            FPlayer fPlayer = fPlayerMapper.map(context.source());

            return PlaceholderResult.value(muteModule.getMuteSuffix(fPlayer, fPlayer));
        });

        Placeholders.register(Identifier.parse(BuildConfig.PROJECT_MOD_ID + ":fcolor"), (context, argument) ->
                fColorPlaceholder(context, argument, FColor.Type.SEE, FColor.Type.OUT)
        );

        Placeholders.register(Identifier.parse(BuildConfig.PROJECT_MOD_ID + ":fcolor_out"), (context, argument) ->
                fColorPlaceholder(context, argument, FColor.Type.OUT)
        );

        Placeholders.register(Identifier.parse(BuildConfig.PROJECT_MOD_ID + ":fcolor_see"), (context, argument) ->
                fColorPlaceholder(context, argument, FColor.Type.SEE)
        );

        Arrays.stream(MessageType.values()).forEach(messageType ->
                Placeholders.register(Identifier.parse(BuildConfig.PROJECT_MOD_ID + ":" + messageType.name().toLowerCase()), (context, argument) -> {
                    FPlayer fPlayer = fPlayerMapper.map(context.source());

                    return PlaceholderResult.value(String.valueOf(fPlayer.isSetting(messageType)));
                })
        );

        Arrays.stream(SettingText.values()).forEach(settingText ->
                Placeholders.register(Identifier.parse(BuildConfig.PROJECT_MOD_ID + ":" + settingText.name().toLowerCase()), (context, argument) -> {
                    FPlayer fPlayer = fPlayerMapper.map(context.source());

                    String value = fPlayer.getSetting(settingText);
                    if (settingText == SettingText.CHAT_NAME && value == null) return PlaceholderResult.value("default");

                    return value == null ? PlaceholderResult.value("") : PlaceholderResult.value(value);
                })
        );

        Placeholders.register(Identifier.parse(BuildConfig.PROJECT_MOD_ID + ":player"), (context, argument) -> {
            FPlayer fPlayer = fPlayerMapper.map(context.source());
            return PlaceholderResult.value(fPlayer.getName());
        });

        Placeholders.register(Identifier.parse(BuildConfig.PROJECT_MOD_ID + ":ip"), (context, argument) -> {
            FPlayer fPlayer = fPlayerMapper.map(context.source());
            return PlaceholderResult.value(fPlayer.getIp());
        });

        Placeholders.register(Identifier.parse(BuildConfig.PROJECT_MOD_ID + ":ping"), (context, argument) -> {
            FPlayer fPlayer = fPlayerMapper.map(context.source());
            return PlaceholderResult.value(String.valueOf(fPlayerService.getPing(fPlayer)));
        });

        Placeholders.register(Identifier.parse(BuildConfig.PROJECT_MOD_ID + ":online"), (context, argument) ->
                PlaceholderResult.value(String.valueOf(platformServerAdapter.getOnlinePlayerCount()))
        );

        Placeholders.register(Identifier.parse(BuildConfig.PROJECT_MOD_ID + ":tps"), (context, argument) ->
                PlaceholderResult.value(platformServerAdapter.getTPS())
        );

        fLogger.info("✔ Text Placeholder API hooked");
    }

    @Pulse(priority = Event.Priority.LOW)
    public void onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.getContext();
        FEntity sender = messageContext.getSender();
        if (placeholderAPIModuleProvider.get().isModuleDisabledFor(sender)) return;

        boolean isUserMessage = messageContext.isFlag(MessageFlag.USER_MESSAGE);
        if (!permissionChecker.check(sender, fileResolver.getPermission().getIntegration().getPlaceholderapi().getUse()) && isUserMessage) return;
        if (!(sender instanceof FPlayer fPlayer)) return;

        Object player = fPlayerService.toPlatformFPlayer(fPlayer);
        if (!(player instanceof ServerPlayer playerEntity)) return;

        String message = messageContext.getMessage();

        Component text = Placeholders.parseText(Component.literal(message), PlaceholderContext.of(playerEntity.createCommandSourceStack()));
        messageContext.setMessage(text.getString());
    }

    private PlaceholderResult fColorPlaceholder(PlaceholderContext context, String argument, FColor.Type... types) {
        if (argument == null) return PlaceholderResult.invalid();
        if (!StringUtils.isNumeric(argument)) return PlaceholderResult.invalid();

        FPlayer fPlayer = fPlayerMapper.map(context.source());

        Map<Integer, String> colorsMap = new HashMap<>(fileResolver.getMessage().getFormat().getFcolor().getDefaultColors());
        for (FColor.Type type : types) {
            colorsMap.putAll(fPlayer.getFColors(type));
        }

        int colorNumber = Integer.parseInt(argument);
        return PlaceholderResult.value(colorsMap.get(colorNumber));
    }
}