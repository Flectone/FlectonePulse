package net.flectone.pulse.util;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.logger.FLogger;

@Singleton
public class FabricFileUtil extends FileUtil {

    @Inject
    public FabricFileUtil(FLogger fLogger) {
        super(fLogger);
    }

    @Override
    public void saveResource(String path) {

    }
}
