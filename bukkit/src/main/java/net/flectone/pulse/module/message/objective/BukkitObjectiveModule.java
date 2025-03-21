package net.flectone.pulse.module.message.objective;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.message.objective.belowname.BelownameModule;
import net.flectone.pulse.module.message.objective.tabname.TabnameModule;

@Singleton
public class BukkitObjectiveModule extends ObjectiveModule {

    @Inject
    public BukkitObjectiveModule(FileManager fileManager) {
        super(fileManager);

        addChildren(BelownameModule.class);
        addChildren(TabnameModule.class);
    }

    @Override
    public void reload() {
        super.reload();
    }
}
