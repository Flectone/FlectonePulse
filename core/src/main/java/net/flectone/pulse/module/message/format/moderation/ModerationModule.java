package net.flectone.pulse.module.message.format.moderation;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.module.message.format.moderation.caps.CapsModule;
import net.flectone.pulse.module.message.format.moderation.delete.DeleteModule;
import net.flectone.pulse.module.message.format.moderation.flood.FloodModule;
import net.flectone.pulse.module.message.format.moderation.newbie.NewbieModule;
import net.flectone.pulse.module.message.format.moderation.swear.SwearModule;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import org.jspecify.annotations.NonNull;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ModerationModule implements ModuleSimple {

    private final FileFacade fileFacade;

    @Override
    public ImmutableList.Builder<@NonNull Class<? extends ModuleSimple>> childrenBuilder() {
        return ModuleSimple.super.childrenBuilder().add(
                CapsModule.class,
                DeleteModule.class,
                NewbieModule.class,
                FloodModule.class,
                SwearModule.class
        );
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_FORMAT_MODERATION;
    }

    @Override
    public Message.Format.Moderation config() {
        return fileFacade.message().format().moderation();
    }

    @Override
    public Permission.Message.Format.Moderation permission() {
        return fileFacade.permission().message().format().moderation();
    }

}
