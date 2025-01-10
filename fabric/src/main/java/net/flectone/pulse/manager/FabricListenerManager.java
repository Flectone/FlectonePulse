package net.flectone.pulse.manager;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

@Singleton
public class FabricListenerManager extends ListenerManager {

    @Inject
    public FabricListenerManager(Injector injector) {
        super(injector);
    }

}
