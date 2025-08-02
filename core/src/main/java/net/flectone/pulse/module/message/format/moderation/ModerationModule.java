package net.flectone.pulse.module.message.format.moderation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.format.moderation.caps.CapsModule;
import net.flectone.pulse.module.message.format.moderation.flood.FloodModule;
import net.flectone.pulse.module.message.format.moderation.newbie.NewbieModule;
import net.flectone.pulse.module.message.format.moderation.swear.SwearModule;
import net.flectone.pulse.processing.resolver.FileResolver;

@Singleton
public class ModerationModule extends AbstractModule {

    private final Message.Format.Moderation message;
    private final Permission.Message.Format.Moderation permission;

    @Inject
    public ModerationModule(FileResolver fileResolver) {
        this.message = fileResolver.getMessage().getFormat().getModeration();
        this.permission = fileResolver.getPermission().getMessage().getFormat().getModeration();
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        addChildren(CapsModule.class);
        addChildren(NewbieModule.class);
        addChildren(FloodModule.class);
        addChildren(SwearModule.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

}
