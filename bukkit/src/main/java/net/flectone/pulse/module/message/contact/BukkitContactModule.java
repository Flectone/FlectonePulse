package net.flectone.pulse.module.message.contact;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.message.contact.knock.KnockModule;
import net.flectone.pulse.module.message.contact.mark.MarkModule;
import net.flectone.pulse.module.message.contact.rightclick.RightclickModule;
import net.flectone.pulse.module.message.contact.sign.SignModule;
import net.flectone.pulse.module.message.contact.spit.SpitModule;
import net.flectone.pulse.module.message.contact.unsign.UnsignModule;

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
        addChildren(SignModule.class);
        addChildren(SpitModule.class);
        addChildren(UnsignModule.class);
    }
}
