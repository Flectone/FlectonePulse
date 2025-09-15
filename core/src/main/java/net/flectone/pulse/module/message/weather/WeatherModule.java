package net.flectone.pulse.module.message.weather;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
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

    private final Message.Weather message;
    private final Permission.Message.Weather permission;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public WeatherModule(FileResolver fileResolver,
                         ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getWeather(), MessageType.WEATHER);

        this.message = fileResolver.getMessage().getWeather();
        this.permission = fileResolver.getPermission().getMessage().getWeather();
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(WeatherPulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(WeatherMetadata.<Localization.Message.Weather>builder()
                .sender(fPlayer)
                .range(message.getRange())
                .format(localization -> switch (translationKey) {
                    case COMMANDS_WEATHER_SET_CLEAR -> localization.getFormatClear();
                    case COMMANDS_WEATHER_SET_RAIN -> localization.getFormatRain();
                    case COMMANDS_WEATHER_SET_THUNDER -> localization.getFormatThunder();
                    default -> "";
                })
                .translationKey(translationKey)
                .destination(message.getDestination())
                .sound(getModuleSound())
                .build()
        );
    }

}