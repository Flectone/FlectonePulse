package net.flectone.pulse.module.integration.yandex;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import yandex.cloud.api.ai.translate.v2.TranslationServiceGrpc;
import yandex.cloud.api.ai.translate.v2.TranslationServiceOuterClass;
import yandex.cloud.sdk.ServiceFactory;
import yandex.cloud.sdk.auth.Auth;

import java.time.Duration;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class YandexIntegration implements FIntegration {

    private final FileFacade fileFacade;
    @Getter private final FLogger fLogger;

    private ServiceFactory factory;

    public String translate(String source, String target, String text) {
        TranslationServiceGrpc.TranslationServiceBlockingStub translationService = factory.create(TranslationServiceGrpc.TranslationServiceBlockingStub.class, TranslationServiceGrpc::newBlockingStub);

        TranslationServiceOuterClass.TranslateResponse response = translationService.translate(TranslationServiceOuterClass.TranslateRequest.newBuilder()
                .setSourceLanguageCode(source)
                .setTargetLanguageCode(target)
                .setFormat(TranslationServiceOuterClass.TranslateRequest.Format.PLAIN_TEXT)
                .addTexts(text)
                .setFolderId(fileFacade.integration().yandex().folderId())
                .build()
        );

        return response.getTranslations(0).getText();
    }

    @Override
    public String getIntegrationName() {
        return "Yandex";
    }

    @Override
    public void hook() {
        try {
            String iamToken = fileFacade.integration().yandex().iamToken();
            String oauthToken = fileFacade.integration().yandex().token();

            ServiceFactory.ServiceFactoryBuilder builder = ServiceFactory.builder()
                    .requestTimeout(Duration.ofMinutes(1));

            if (iamToken != null && !iamToken.isBlank()) {
                // Modern path: IAM token (Yandex Cloud "yc iam create-token", 12h TTL).
                // Recommended — OAuth is deprecated and stops working end of 2026.
                builder.credentialProvider(Auth.iamTokenBuilder().token(iamToken));
                fLogger.info("Yandex integration: using IAM token credentials");
            } else if (oauthToken != null && !oauthToken.isBlank()) {
                // Legacy path: OAuth token. Deprecated by Yandex — kept for backward compat.
                builder.credentialProvider(Auth.oauthTokenBuilder().oauth(oauthToken));
                fLogger.warning("Yandex integration: using deprecated OAuth token. "
                        + "Switch to iamToken in config/integration.yml — run 'yc iam create-token' to get one.");
            } else {
                fLogger.warning("Yandex integration: no credentials configured (need iamToken or token in config/integration.yml)");
                return;
            }

            factory = builder.build();
            logHook();
        } catch (Exception e) {
            fLogger.warning(e);
        }
    }

}
