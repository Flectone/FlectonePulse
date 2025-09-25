package net.flectone.pulse.module.message.format.moderation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.format.moderation.caps.CapsModule;
import net.flectone.pulse.module.message.format.moderation.delete.DeleteModule;
import net.flectone.pulse.module.message.format.moderation.flood.FloodModule;
import net.flectone.pulse.module.message.format.moderation.newbie.NewbieModule;
import net.flectone.pulse.module.message.format.moderation.swear.SwearModule;
import net.flectone.pulse.processing.resolver.FileResolver;

@Singleton
public class ModerationModule extends AbstractModule {

    private final FileResolver fileResolver;

    @Inject
    public ModerationModule(FileResolver fileResolver) {
        this.fileResolver = fileResolver;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission());

        addChildren(CapsModule.class);
        addChildren(DeleteModule.class);
        addChildren(NewbieModule.class);
        addChildren(FloodModule.class);
        addChildren(SwearModule.class);
    }

    @Override
    public Message.Format.Moderation config() {
        return fileResolver.getMessage().getFormat().getModeration();
    }

    @Override
    public Permission.Message.Format.Moderation permission() {
        return fileResolver.getPermission().getMessage().getFormat().getModeration();
    }

}
