package net.flectone.pulse.module.message.format.moderation;

import java.util.LinkedHashSet;
import java.util.Collections;
import java.util.Set;
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
public class ModerationModuleImpl implements ModerationModule {

    private final FileFacade fileFacade;

    @Override
    public Set<@NonNull Class<? extends ModuleSimple>> children() {
        Set<@NonNull Class<? extends ModuleSimple>> children = new LinkedHashSet<>(ModerationModule.super.children());
        children.add(CapsModule.class);
        children.add(DeleteModule.class);
        children.add(NewbieModule.class);
        children.add(FloodModule.class);
        children.add(SwearModule.class);
        return Collections.unmodifiableSet(children);
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
