package net.flectone.pulse.module.message.format;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.util.ItemUtil;
import net.flectone.pulse.util.PermissionUtil;
import net.flectone.pulse.util.platformServerAdapter;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

@Singleton
public class FabricFormatModule extends FormatModule {

    @Inject
    public FabricFormatModule(FileManager fileManager,
                              platformServerAdapter platformServerAdapter,
                              FPlayerManager fPlayerManager,
                              PermissionUtil permissionUtil,
                              ItemUtil itemUtil) {
        super(fileManager, platformServerAdapter, fPlayerManager, permissionUtil, itemUtil);
    }

    @Override
    public TagResolver coordsTag(FEntity sender, FEntity fReceiver) {
        return TagResolver.empty();
    }

    @Override
    public TagResolver statsTag(FEntity sender, FEntity fReceiver) {
        return TagResolver.empty();
    }

}
