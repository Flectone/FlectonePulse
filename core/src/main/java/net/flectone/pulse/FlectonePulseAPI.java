package net.flectone.pulse;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;

@Singleton
public class FlectonePulseAPI  {

    @Getter
    private static FlectonePulse instance;

    @Inject
    public FlectonePulseAPI(FlectonePulse instance) {
        FlectonePulseAPI.instance = instance;
    }
}