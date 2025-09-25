package net.flectone.pulse.module.integration.yandex;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.logging.FLogger;
import yandex.cloud.api.ai.translate.v2.TranslationServiceGrpc;
import yandex.cloud.api.ai.translate.v2.TranslationServiceOuterClass;
import yandex.cloud.sdk.ServiceFactory;
import yandex.cloud.sdk.auth.Auth;

import java.time.Duration;

@Singleton
public class YandexIntegration implements FIntegration {

    private final FileResolver fileResolver;
    private final FLogger fLogger;

    private ServiceFactory factory;

    @Inject
    public YandexIntegration(FileResolver fileResolver,
                             FLogger fLogger) {
        this.fileResolver = fileResolver;
        this.fLogger = fLogger;
    }

    public String translate(String source, String target, String text) {
        TranslationServiceGrpc.TranslationServiceBlockingStub translationService = factory.create(TranslationServiceGrpc.TranslationServiceBlockingStub.class, TranslationServiceGrpc::newBlockingStub);

        TranslationServiceOuterClass.TranslateResponse response = translationService.translate(TranslationServiceOuterClass.TranslateRequest.newBuilder()
                .setSourceLanguageCode(source)
                .setTargetLanguageCode(target)
                .setFormat(TranslationServiceOuterClass.TranslateRequest.Format.PLAIN_TEXT)
                .addTexts(text)
                .setFolderId(fileResolver.getIntegration().getYandex().getFolderId())
                .build()
        );

        return response.getTranslations(0).getText();
    }

    @Override
    public void hook() {
        try {
            factory = ServiceFactory.builder()
                    .credentialProvider(Auth.oauthTokenBuilder().oauth(fileResolver.getIntegration().getYandex().getToken()))
                    .requestTimeout(Duration.ofMinutes(1))
                    .build();

            fLogger.info("✔ Yandex integration enabled");
        } catch (Exception e) {
            fLogger.warning(e);
        }
    }

    @Override
    public void unhook() {
        fLogger.info("✖ Yandex integration disabled");
    }
}
