package net.flectone.pulse.module.message.contact;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.message.contact.knock.KnockModule;
import net.flectone.pulse.module.message.contact.mark.MarkModule;
import net.flectone.pulse.module.message.contact.rightclick.RightclickModule;
import net.flectone.pulse.module.message.contact.spit.SpitModule;

@Singleton
public class BukkitContactModule extends ContactModule {

    @Inject
    public BukkitContactModule(FileManager fileManager) {
        super(fileManager);
    }

    @Override
    public void reload() {
        super.reload();

        addChildren(KnockModule.class);
        addChildren(MarkModule.class);
        addChildren(RightclickModule.class);
        addChildren(SpitModule.class);
    }
}
