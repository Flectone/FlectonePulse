package net.flectone.pulse.module.integration.placeholderapi;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.ServerPlaceholderContext;
import eu.pb4.placeholders.api.parsers.NodeParser;
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
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.StringUtils;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PlaceholderAPIIntegration implements FIntegration, PulseListener {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final PlatformServerAdapter platformServerAdapter;
    private final PermissionChecker permissionChecker;
    private final Provider<MuteModule> muteModuleProvider;
    private final Provider<ConditionModule> conditionModuleProvider;
    private final Provider<AfkModule> afkModuleProvider;
    private final TaskScheduler taskScheduler;
    private final ModuleController moduleController;
    @Getter private final FLogger fLogger;

    private boolean hooked;

    @Override
    public String getIntegrationName() {
        return "TextPlaceholderAPI";
    }

    @Override
    public void hook() {
        if (!hooked) {
            taskScheduler.runAsyncLater(() -> {
                register();
                hooked = true;
            });
        }
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
        if (!(player instanceof ServerPlayer playerEntity)) return event;

        String message = messageContext.message();

        String text = NodeParser.builder()
                .serverPlaceholders()
                .commonPlaceholders()
                .simplifiedTextFormat()
                .build()
                .parseComponent(message, ServerPlaceholderContext.of(playerEntity).asParserContext())
                .getString();

        return event.withContext(messageContext.withMessage(text));
    }

    private void register() {
        Placeholders.registerCommon(Identifier.parse(BuildConfig.PROJECT_MOD_ID + ":mute_suffix"), (context, argument) -> {
            if (!context.hasPlayer()) return PlaceholderResult.invalid();

            FPlayer fPlayer = fPlayerService.getFPlayer(context.player().getUUID());

            return PlaceholderResult.value(muteModuleProvider.get().getMuteSuffix(fPlayer, fPlayer));
        });

        Placeholders.registerCommon(Identifier.parse(BuildConfig.PROJECT_MOD_ID + ":afk_duration"), (context, argument) -> {
            if (!context.hasPlayer()) return PlaceholderResult.invalid();

            FPlayer fPlayer = fPlayerService.getFPlayer(context.player().getUUID());

            return PlaceholderResult.value(String.valueOf(afkModuleProvider.get().getAfkDuration(fPlayer)));
        });

        Placeholders.registerCommon(Identifier.parse(BuildConfig.PROJECT_MOD_ID + ":afk_duration_formatted"), (context, argument) -> {
            if (!context.hasPlayer()) return PlaceholderResult.invalid();

            FPlayer fPlayer = fPlayerService.getFPlayer(context.player().getUUID());

            return PlaceholderResult.value(afkModuleProvider.get().getAfkDurationFormatted(fPlayer, fPlayer));
        });

        Placeholders.registerCommon(Identifier.parse(BuildConfig.PROJECT_MOD_ID + ":condition"), (context, argument) -> {
            if (!context.hasPlayer()) return PlaceholderResult.invalid();

            return PlaceholderResult.value(conditionModuleProvider.get().getConditionValue(argument, fPlayerService.getFPlayer(context.player().getUUID())));
        });

        Placeholders.registerCommon(Identifier.parse(BuildConfig.PROJECT_MOD_ID + ":fcolor"), (context, argument) ->
                fColorPlaceholder(context, argument, FColor.Type.SEE, FColor.Type.OUT)
        );

        Placeholders.registerCommon(Identifier.parse(BuildConfig.PROJECT_MOD_ID + ":fcolor_out"), (context, argument) ->
                fColorPlaceholder(context, argument, FColor.Type.OUT)
        );

        Placeholders.registerCommon(Identifier.parse(BuildConfig.PROJECT_MOD_ID + ":fcolor_see"), (context, argument) ->
                fColorPlaceholder(context, argument, FColor.Type.SEE)
        );

        Placeholders.registerCommon(Identifier.parse(BuildConfig.PROJECT_MOD_ID + ":setting"), (context, argument) -> {
            if (!context.hasPlayer()) return PlaceholderResult.invalid();
            if (argument == null) return PlaceholderResult.value("");

            FPlayer fPlayer = fPlayerService.getFPlayer(context.player().getUUID());

            SettingText settingText = SettingText.fromString(argument);
            if (settingText != null) {
                String value = fPlayer.getSetting(settingText);
                if (settingText == SettingText.CHAT_NAME && value == null) return PlaceholderResult.value("default");

                return PlaceholderResult.value(StringUtils.defaultString(value));
            }

            return PlaceholderResult.value(fPlayer.isSetting(argument.toUpperCase()) ? "yes" : "no");
        });

        Placeholders.registerCommon(Identifier.parse(BuildConfig.PROJECT_MOD_ID + ":player"), (context, argument) -> {
            if (!context.hasPlayer()) return PlaceholderResult.invalid();

            FPlayer fPlayer = fPlayerService.getFPlayer(context.player().getUUID());
            return PlaceholderResult.value(fPlayer.name());
        });

        Placeholders.registerCommon(Identifier.parse(BuildConfig.PROJECT_MOD_ID + ":ip"), (context, argument) -> {
            if (!context.hasPlayer()) return PlaceholderResult.invalid();

            FPlayer fPlayer = fPlayerService.getFPlayer(context.player().getUUID());
            return PlaceholderResult.value(fPlayer.ip());
        });

        Placeholders.registerCommon(Identifier.parse(BuildConfig.PROJECT_MOD_ID + ":ping"), (context, argument) -> {
            if (!context.hasPlayer()) return PlaceholderResult.invalid();

            FPlayer fPlayer = fPlayerService.getFPlayer(context.player().getUUID());
            return PlaceholderResult.value(String.valueOf(platformPlayerAdapter.getPing(fPlayer)));
        });

        Placeholders.registerServer(Identifier.parse(BuildConfig.PROJECT_MOD_ID + ":online"), (context, argument) ->
                PlaceholderResult.value(String.valueOf(platformServerAdapter.getOnlinePlayerCount()))
        );

        Placeholders.registerServer(Identifier.parse(BuildConfig.PROJECT_MOD_ID + ":tps"), (context, argument) ->
                PlaceholderResult.value(platformServerAdapter.getTPS())
        );

        logHook();
    }

    private PlaceholderResult fColorPlaceholder(PlaceholderContext context, String argument, FColor.Type... types) {
        if (argument == null) return PlaceholderResult.invalid();
        if (!StringUtils.isNumeric(argument)) return PlaceholderResult.invalid();
        if (!context.hasPlayer()) return PlaceholderResult.invalid();

        FPlayer fPlayer = fPlayerService.getFPlayer(context.player().getUUID());

        Int2ObjectArrayMap<String> colorsMap = new Int2ObjectArrayMap<>(fileFacade.message().format().fcolor().defaultColors());
        for (FColor.Type type : types) {
            colorsMap.putAll(fPlayer.getFColors(type));
        }

        int colorNumber = Integer.parseInt(argument);
        return PlaceholderResult.value(colorsMap.get(colorNumber));
    }
}
