package net.flectone.pulse;

import com.google.inject.AbstractModule;
import net.flectone.pulse.platform.adapter.FabricVersionAdapter;
import net.flectone.pulse.platform.adapter.FabricVersionAdapterV_1_20_5;
import net.flectone.pulse.serializer.FabricComponentSerializer;
import net.flectone.pulse.serializer.FabricComponentSerializerV_1_20_5;

public class FabricInjectorV_1_20_5 extends AbstractModule {

    @Override
    protected void configure() {
        bind(FabricVersionAdapter.class).to(FabricVersionAdapterV_1_20_5.class);
        bind(FabricComponentSerializer.class).to(FabricComponentSerializerV_1_20_5.class);
    }

}
