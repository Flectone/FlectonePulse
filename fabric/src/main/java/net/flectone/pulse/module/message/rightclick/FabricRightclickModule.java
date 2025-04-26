package net.flectone.pulse.module.message.rightclick;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class FabricRightclickModule extends RightclickModule {

    @Inject
    public FabricRightclickModule() {
        super(localization -> localization.getMessage().getRightclick());
    }

    @Override
    public void reload() {

    }

    @Override
    protected boolean isConfigEnable() {
        return false;
    }
}
