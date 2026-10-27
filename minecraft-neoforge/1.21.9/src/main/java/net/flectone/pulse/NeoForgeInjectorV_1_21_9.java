package net.flectone.pulse;

import com.google.inject.AbstractModule;
import net.flectone.pulse.platform.adapter.NeoForgeVersionAdapter;
import net.flectone.pulse.platform.adapter.NeoForgeVersionAdapterV_1_21_9;

public class NeoForgeInjectorV_1_21_9 extends AbstractModule {

    @Override
    protected void configure() {
        bind(NeoForgeVersionAdapter.class).to(NeoForgeVersionAdapterV_1_21_9.class);
    }

}
