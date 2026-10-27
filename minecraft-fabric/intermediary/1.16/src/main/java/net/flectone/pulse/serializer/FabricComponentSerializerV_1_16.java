package net.flectone.pulse.serializer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.resolver.ReflectionResolver;
import net.minecraft.network.chat.Component;

@Singleton
public class FabricComponentSerializerV_1_16 extends FabricComponentSerializerV_1_20_5 {

    @Inject
    public FabricComponentSerializerV_1_16(ReflectionResolver reflectionResolver) {
        super(reflectionResolver);
    }

    @Override
    public Component toFabric(String json) {
        return Component.Serializer.fromJson(json);
    }

}
