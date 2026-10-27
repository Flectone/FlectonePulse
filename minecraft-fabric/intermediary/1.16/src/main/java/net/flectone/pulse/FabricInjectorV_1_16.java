package net.flectone.pulse;

import com.google.inject.AbstractModule;
import net.flectone.pulse.platform.adapter.FabricVersionAdapter;
import net.flectone.pulse.platform.adapter.FabricVersionAdapterV_1_16;
import net.flectone.pulse.serializer.FabricComponentSerializer;
import net.flectone.pulse.serializer.FabricComponentSerializerV_1_16;

public class FabricInjectorV_1_16 extends AbstractModule {

    @Override
    protected void configure() {
        bind(FabricVersionAdapter.class).to(FabricVersionAdapterV_1_16.class);
        bind(FabricComponentSerializer.class).to(FabricComponentSerializerV_1_16.class);
    }

}
