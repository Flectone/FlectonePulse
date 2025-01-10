package net.flectone.pulse.module.command.symbol;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.util.CommandUtil;

@Singleton
public class FabricSymbolModule extends SymbolModule {

    @Inject
    public FabricSymbolModule(FileManager fileManager,
                              CommandUtil commandUtil) {
        super(fileManager, commandUtil);
    }

    @Override
    public void createCommand() {

    }
}
