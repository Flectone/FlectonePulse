package net.flectone.pulse.module.integration.deepl;

import com.deepl.api.DeepLClient;
import com.deepl.api.DeepLClientOptions;
import com.deepl.api.DeepLException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.util.logging.FLogger;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.module.integration.FIntegration;

@Singleton
public class DeeplIntegration implements FIntegration {

    private final Integration.Deepl integration;
    private final FLogger fLogger;

    private DeepLClient client;

    @Inject
    public DeeplIntegration(FileResolver fileResolver,
                            FLogger fLogger) {
        integration = fileResolver.getIntegration().getDeepl();

        this.fLogger = fLogger;
    }

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
            client = new DeepLClient(integration.getAuthKey(), new DeepLClientOptions());

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
