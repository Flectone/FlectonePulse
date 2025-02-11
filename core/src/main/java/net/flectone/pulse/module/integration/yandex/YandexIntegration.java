package net.flectone.pulse.module.integration.yandex;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.util.logging.FLogger;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.integration.FIntegration;
import yandex.cloud.api.ai.translate.v2.TranslationServiceGrpc;
import yandex.cloud.api.ai.translate.v2.TranslationServiceOuterClass;
import yandex.cloud.sdk.ServiceFactory;
import yandex.cloud.sdk.auth.Auth;

import java.time.Duration;

@Singleton
public class YandexIntegration implements FIntegration {

    private final Integration.Yandex integration;
    private final FLogger fLogger;

    private ServiceFactory factory;

    @Inject
    public YandexIntegration(FileManager fileManager,
                             FLogger fLogger) {
        integration = fileManager.getIntegration().getYandex();

        this.fLogger = fLogger;
    }

    public String translate(String source, String target, String text) {
        TranslationServiceGrpc.TranslationServiceBlockingStub translationService = factory.create(TranslationServiceGrpc.TranslationServiceBlockingStub.class, TranslationServiceGrpc::newBlockingStub);

        TranslationServiceOuterClass.TranslateResponse response = translationService.translate(TranslationServiceOuterClass.TranslateRequest.newBuilder()
                .setSourceLanguageCode(source)
                .setTargetLanguageCode(target)
                .setFormat(TranslationServiceOuterClass.TranslateRequest.Format.PLAIN_TEXT)
                .addTexts(text)
                .setFolderId(integration.getFolderId())
                .build()
        );

        return response.getTranslations(0).getText();
    }

    @Override
    public void hook() {
        try {
            factory = ServiceFactory.builder()
                    .credentialProvider(Auth.oauthTokenBuilder().oauth(integration.getToken()))
                    .requestTimeout(Duration.ofMinutes(1))
                    .build();

            fLogger.info("Yandex integration enabled");
        } catch (Exception e) {
            fLogger.warning(e);
        }
    }
}
