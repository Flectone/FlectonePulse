package net.flectone.pulse.module.message.format.object.texture.mineskin;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.module.message.format.object.texture.model.Frame;
import net.flectone.pulse.processing.resolver.SystemVariableResolver;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import org.apache.commons.lang3.StringUtils;
import org.mineskin.JsoupRequestHandler;
import org.mineskin.MineSkinClient;
import org.mineskin.data.ValueAndSignature;
import org.mineskin.request.GenerateRequest;

import java.awt.image.BufferedImage;
import java.util.concurrent.CompletableFuture;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MineskinIntegration implements FIntegration {

    private final FileFacade fileFacade;
    private final SystemVariableResolver systemVariableResolver;
    @Getter private final FLogger fLogger;

    private MineSkinClient client;

    @Override
    public String getIntegrationName() {
        return "MineSkin";
    }

    @Override
    public void hook() {
        String apiKey = systemVariableResolver.substituteEnvVars(fileFacade.message().format().object().textureTag().mineskinApiKey());
        if (StringUtils.isEmpty(apiKey)) return;

        client = MineSkinClient.builder()
                .requestHandler(JsoupRequestHandler::new)
                .userAgent("FlectonePulse/1.0")
                .apiKey(apiKey)
                .build();

        logHook();
    }

    @Override
    public void unhook() {
        if (client == null) return;

        client = null;

        logUnhook();
    }

    public boolean isHooked() {
        return client != null;
    }

    public CompletableFuture<Frame> loadTexture(int x, int y, BufferedImage skinImage) {
        return client.queue()
                .submit(GenerateRequest.upload(skinImage))
                .thenCompose(queueResponse -> queueResponse.getJob().waitForCompletion(client))
                .thenCompose(jobResponse -> jobResponse.getOrLoadSkin(client))
                .thenApply(skin -> {
                    ValueAndSignature valueAndSignature = skin.texture().data();
                    return new Frame(x, y, valueAndSignature.value());
                });
    }

}