package net.flectone.pulse;

import com.google.inject.AbstractModule;
import net.flectone.pulse.platform.adapter.FabricVersionAdapter;
import net.flectone.pulse.platform.adapter.FabricVersionAdapterV_1_20_2;

public class FabricInjectorV_1_20_2 extends AbstractModule {

    @Override
    protected void configure() {
        bind(FabricVersionAdapter.class).to(FabricVersionAdapterV_1_20_2.class);
    }

}
