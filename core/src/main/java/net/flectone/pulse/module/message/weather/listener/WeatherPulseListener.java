package net.flectone.pulse.module.message.weather.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.weather.WeatherModule;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class WeatherPulseListener implements PulseListener {

    private final WeatherModule weatherModule;

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        MinecraftTranslationKey translationKey = event.getTranslationKey();
        switch (translationKey) {
            case COMMANDS_WEATHER_SET_CLEAR, COMMANDS_WEATHER_SET_RAIN, COMMANDS_WEATHER_SET_THUNDER,
                 COMMANDS_WEATHER_CLEAR, COMMANDS_WEATHER_RAIN, COMMANDS_WEATHER_THUNDER -> {
                event.setCancelled(true);
                weatherModule.send(event.getFPlayer(), translationKey);
            }
        }
    }

}