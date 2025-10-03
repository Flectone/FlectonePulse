package net.flectone.pulse.module.message.weather;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.weather.listener.WeatherPulseListener;
import net.flectone.pulse.module.message.weather.model.WeatherMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

@Singleton
public class WeatherModule extends AbstractModuleLocalization<Localization.Message.Weather> {

    private final FileResolver fileResolver;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public WeatherModule(FileResolver fileResolver,
                         ListenerRegistry listenerRegistry) {
        super(MessageType.WEATHER);

        this.fileResolver = fileResolver;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission());

        createSound(config().getSound(), permission().getSound());

        listenerRegistry.register(WeatherPulseListener.class);
    }

    @Override
    public Message.Weather config() {
        return fileResolver.getMessage().getWeather();
    }

    @Override
    public Permission.Message.Weather permission() {
        return fileResolver.getPermission().getMessage().getWeather();
    }

    @Override
    public Localization.Message.Weather localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getWeather();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(WeatherMetadata.<Localization.Message.Weather>builder()
                .sender(fPlayer)
                .range(config().getRange())
                .format(localization -> switch (translationKey) {
                    case COMMANDS_WEATHER_SET_CLEAR, COMMANDS_WEATHER_CLEAR -> localization.getClear();
                    case COMMANDS_WEATHER_SET_RAIN, COMMANDS_WEATHER_RAIN -> localization.getRain();
                    case COMMANDS_WEATHER_SET_THUNDER, COMMANDS_WEATHER_THUNDER -> localization.getThunder();
                    default -> "";
                })
                .translationKey(translationKey)
                .destination(config().getDestination())
                .sound(getModuleSound())
                .build()
        );
    }
}