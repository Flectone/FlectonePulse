package net.flectone.pulse.module.message.contact.afk;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.message.contact.ContactModule;

@Singleton
public class FabricContactModule extends ContactModule {

    @Inject
    public FabricContactModule(FileManager fileManager) {
        super(fileManager);
    }

}
