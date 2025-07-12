package net.flectone.pulse.module.message.format.moderation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.format.moderation.caps.CapsModule;
import net.flectone.pulse.module.message.format.moderation.flood.FloodModule;
import net.flectone.pulse.module.message.format.moderation.newbie.NewbieModule;
import net.flectone.pulse.module.message.format.moderation.swear.SwearModule;

@Singleton
public class ModerationModule extends AbstractModule {

    private final Message.Format.Moderation message;
    private final Permission.Message.Format.Moderation permission;

    @Inject
    public ModerationModule(FileResolver fileResolver) {
        message = fileResolver.getMessage().getFormat().getModeration();
        permission = fileResolver.getPermission().getMessage().getFormat().getModeration();
    @Override
    public void onEnable() {
        registerModulePermission(permission);

        addChildren(CapsModule.class);
        addChildren(NewbieModule.class);
        addChildren(FloodModule.class);
        addChildren(SwearModule.class);
    }

    @Override
    public void reload() {
        registerModulePermission(permission);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

}
