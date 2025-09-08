package net.flectone.pulse.module.message.weather.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.weather.WeatherModule;

@Singleton
public class WeatherPulseListener implements PulseListener {

    private final WeatherModule weatherModule;

    @Inject
    public WeatherPulseListener(WeatherModule weatherModule) {
        this.weatherModule = weatherModule;
    }

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        if (!event.getTranslationKey().startsWith("commands.weather.set")) return;

        event.setCancelled(true);
        weatherModule.send(event.getFPlayer(), event.getTranslationKey());
    }

}