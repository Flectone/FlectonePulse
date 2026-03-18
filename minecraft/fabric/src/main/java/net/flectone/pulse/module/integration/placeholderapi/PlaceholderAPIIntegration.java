package net.flectone.pulse.module.integration.placeholderapi;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.FColor;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.command.mute.MuteModule;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.module.message.afk.AfkModule;
import net.flectone.pulse.module.message.format.condition.ConditionModule;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.processing.mapper.FPlayerMapper;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.StringUtils;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PlaceholderAPIIntegration implements FIntegration, PulseListener {

    private final FileFacade fileFacade;
    private final FPlayerMapper fPlayerMapper;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final PlatformServerAdapter platformServerAdapter;
    private final PermissionChecker permissionChecker;
    private final Provider<MuteModule> muteModuleProvider;
    private final Provider<ConditionModule> conditionModuleProvider;
    private final Provider<AfkModule> afkModuleProvider;
    private final TaskScheduler taskScheduler;
    private final ModuleController moduleController;
    @Getter private final FLogger fLogger;

    @Override
    public String getIntegrationName() {
        return "TextPlaceholderAPI";
    }

    @Override
    public void hook() {
        taskScheduler.runAsyncLater(this::register);
    }

    @Override
    public void unhook() {
        Placeholders.remove(Identifier.of(BuildConfig.PROJECT_MOD_ID, "mute_suffix"));
        Placeholders.remove(Identifier.of(BuildConfig.PROJECT_MOD_ID, "afk_duration"));
        Placeholders.remove(Identifier.of(BuildConfig.PROJECT_MOD_ID, "afk_duration_formatted"));
        Placeholders.remove(Identifier.of(BuildConfig.PROJECT_MOD_ID, "condition"));
        Placeholders.remove(Identifier.of(BuildConfig.PROJECT_MOD_ID, "fcolor"));
        Placeholders.remove(Identifier.of(BuildConfig.PROJECT_MOD_ID, "setting"));
        Placeholders.remove(Identifier.of(BuildConfig.PROJECT_MOD_ID, "player"));
        Placeholders.remove(Identifier.of(BuildConfig.PROJECT_MOD_ID, "ip"));
        Placeholders.remove(Identifier.of(BuildConfig.PROJECT_MOD_ID, "ping"));
        Placeholders.remove(Identifier.of(BuildConfig.PROJECT_MOD_ID, "online"));
        Placeholders.remove(Identifier.of(BuildConfig.PROJECT_MOD_ID, "tps"));

        logUnhook();
    }

    @Pulse(priority = Event.Priority.LOW)
    public Event onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.context();
        FEntity sender = messageContext.sender();
        if (moduleController.isDisabledFor(PlaceholderAPIModule.class, sender)) return event;

        boolean isUserMessage = messageContext.isFlag(MessageFlag.PLAYER_MESSAGE);
        if (!permissionChecker.check(sender, fileFacade.permission().integration().placeholderapi().use()) && isUserMessage) return event;
        if (!(sender instanceof FPlayer fPlayer)) return event;

        Object player = platformPlayerAdapter.convertToPlatformPlayer(messageContext.isFlag(MessageFlag.PLACEHOLDER_CONTEXT_SENDER)
                ? fPlayer
                : messageContext.receiver()
        );
        if (!(player instanceof ServerPlayerEntity playerEntity)) return event;

        String message = messageContext.message();

        Text text = Placeholders.parseText(Text.literal(message), PlaceholderContext.of(playerEntity.getCommandSource()));
        return event.withContext(messageContext.withMessage(text.getString()));
    }

    private void register() {
        Placeholders.register(Identifier.of(BuildConfig.PROJECT_MOD_ID, "mute_suffix"), (context, argument) -> {
            FPlayer fPlayer = fPlayerMapper.map(context.source());

            return PlaceholderResult.value(muteModuleProvider.get().getMuteSuffix(fPlayer, fPlayer));
        });

        Placeholders.register(Identifier.of(BuildConfig.PROJECT_MOD_ID, "afk_duration"), (context, argument) -> {
            FPlayer fPlayer = fPlayerMapper.map(context.source());

            return PlaceholderResult.value(String.valueOf(afkModuleProvider.get().getAfkDuration(fPlayer)));
        });

        Placeholders.register(Identifier.of(BuildConfig.PROJECT_MOD_ID, "afk_duration_formatted"), (context, argument) -> {
            FPlayer fPlayer = fPlayerMapper.map(context.source());

            return PlaceholderResult.value(afkModuleProvider.get().getAfkDurationFormatted(fPlayer, fPlayer));
        });

        Placeholders.register(Identifier.of(BuildConfig.PROJECT_MOD_ID, "condition"), (context, argument) ->
                PlaceholderResult.value(conditionModuleProvider.get().getConditionValue(argument, fPlayerMapper.map(context.source())))
        );

        Placeholders.register(Identifier.of(BuildConfig.PROJECT_MOD_ID, "fcolor"), (context, argument) ->
                fColorPlaceholder(context, argument, FColor.Type.SEE, FColor.Type.OUT)
        );

        Placeholders.register(Identifier.of(BuildConfig.PROJECT_MOD_ID, "fcolor_out"), (context, argument) ->
                fColorPlaceholder(context, argument, FColor.Type.OUT)
        );

        Placeholders.register(Identifier.of(BuildConfig.PROJECT_MOD_ID, "fcolor_see"), (context, argument) ->
                fColorPlaceholder(context, argument, FColor.Type.SEE)
        );

        Placeholders.register(Identifier.of(BuildConfig.PROJECT_MOD_ID, "setting"), (context, argument) -> {
            if (argument == null) return PlaceholderResult.value("");

            FPlayer fPlayer = fPlayerMapper.map(context.source());

            SettingText settingText = SettingText.fromString(argument);
            if (settingText != null) {
                String value = fPlayer.getSetting(settingText);
                if (settingText == SettingText.CHAT_NAME && value == null) return PlaceholderResult.value("default");

                return PlaceholderResult.value(StringUtils.defaultString(value));
            }

            return PlaceholderResult.value(fPlayer.isSetting(argument.toUpperCase()) ? "yes" : "no");
        });

        Placeholders.register(Identifier.of(BuildConfig.PROJECT_MOD_ID, "player"), (context, argument) -> {
            FPlayer fPlayer = fPlayerMapper.map(context.source());
            return PlaceholderResult.value(fPlayer.name());
        });

        Placeholders.register(Identifier.of(BuildConfig.PROJECT_MOD_ID, "ip"), (context, argument) -> {
            FPlayer fPlayer = fPlayerMapper.map(context.source());
            return PlaceholderResult.value(fPlayer.ip());
        });

        Placeholders.register(Identifier.of(BuildConfig.PROJECT_MOD_ID, "ping"), (context, argument) -> {
            FPlayer fPlayer = fPlayerMapper.map(context.source());
            return PlaceholderResult.value(String.valueOf(platformPlayerAdapter.getPing(fPlayer)));
        });

        Placeholders.register(Identifier.of(BuildConfig.PROJECT_MOD_ID, "online"), (context, argument) ->
                PlaceholderResult.value(String.valueOf(platformServerAdapter.getOnlinePlayerCount()))
        );

        Placeholders.register(Identifier.of(BuildConfig.PROJECT_MOD_ID, "tps"), (context, argument) ->
                PlaceholderResult.value(platformServerAdapter.getTPS())
        );

        logHook();
    }

    private PlaceholderResult fColorPlaceholder(PlaceholderContext context, String argument, FColor.Type... types) {
        if (argument == null) return PlaceholderResult.invalid();
        if (!StringUtils.isNumeric(argument)) return PlaceholderResult.invalid();

        FPlayer fPlayer = fPlayerMapper.map(context.source());

        Int2ObjectArrayMap<String> colorsMap = new Int2ObjectArrayMap<>(fileFacade.message().format().fcolor().defaultColors());
        for (FColor.Type type : types) {
            colorsMap.putAll(fPlayer.getFColors(type));
        }

        int colorNumber = Integer.parseInt(argument);
        return PlaceholderResult.value(colorsMap.get(colorNumber));
    }
}
