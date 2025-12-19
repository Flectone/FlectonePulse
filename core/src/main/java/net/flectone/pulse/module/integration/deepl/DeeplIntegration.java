package net.flectone.pulse.module.integration.deepl;

import com.deepl.api.DeepLClient;
import com.deepl.api.DeepLClientOptions;
import com.deepl.api.DeepLException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DeeplIntegration implements FIntegration {

    private final FileFacade fileFacade;
    private final FLogger fLogger;

    private DeepLClient client;

    public String translate(String source, String target, String text) {
        try {
            return client.translateText(text, source.equals("auto") ? null : source, target).getText();
        } catch (InterruptedException | DeepLException e) {
            fLogger.warning(e);
            return text;
        }
    }

    @Override
    public void hook() {
        try {
            client = new DeepLClient(fileFacade.integration().deepl().authKey(), new DeepLClientOptions());

            fLogger.info("✔ Deepl integration enabled");
        } catch (Exception e) {
            fLogger.warning(e);
        }
    }

    @Override
    public void unhook() {
        fLogger.info("✖ Deepl integration disabled");
    }

}
