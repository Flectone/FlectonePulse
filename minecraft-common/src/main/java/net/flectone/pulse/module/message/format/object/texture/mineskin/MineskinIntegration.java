package net.flectone.pulse.module.message.format.object.texture.mineskin;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.module.message.format.object.texture.model.Frame;
import net.flectone.pulse.processing.resolver.SystemVariableResolver;
import net.flectone.pulse.util.WebUtil;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import org.apache.commons.lang3.StringUtils;
import org.mineskin.JsoupRequestHandler;
import org.mineskin.MineSkinClient;
import org.mineskin.exception.MineSkinRequestException;
import org.mineskin.request.GenerateRequest;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CopyOnWriteArrayList;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MineskinIntegration implements FIntegration {

    private static final int MAX_ATTEMPTS = 3;
    private final List<CompletableFuture<?>> pendingFutures = new CopyOnWriteArrayList<>();

    private final FileFacade fileFacade;
    private final SystemVariableResolver systemVariableResolver;
    private final TaskScheduler taskScheduler;
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
                .userAgent(WebUtil.USER_AGENT)
                .apiKey(apiKey)
                .build();

        logHook();
    }

    @Override
    public void unhook() {
        if (client == null) return;

        pendingFutures.forEach(completableFuture -> completableFuture.cancel(true));
        pendingFutures.clear();
        client = null;

        logUnhook();
    }

    public boolean isHooked() {
        return client != null;
    }

    public CompletableFuture<Frame> loadTexture(int x, int y, BufferedImage skinImage, String texture) {
        // create future
        CompletableFuture<Frame> completableFuture = loadWithRetry(x, y, skinImage, texture, 0);

        // save to pending
        pendingFutures.add(completableFuture);

        // remove from pending
        completableFuture.whenComplete((_, _) -> pendingFutures.remove(completableFuture));

        // return
        return completableFuture;
    }

    private CompletableFuture<Frame> loadWithRetry(int x, int y, BufferedImage skinImage, String texture, int attempt) {
        return client.queue()
                .submit(GenerateRequest.upload(skinImage))
                .thenCompose(queueResponse -> queueResponse.getJob().waitForCompletion(client))
                .thenCompose(jobResponse -> jobResponse.getOrLoadSkin(client))
                .thenApply(skin -> new Frame(x, y, skin.texture().data().value()))
                .exceptionallyCompose(throwable -> {
                    Throwable cause = throwable instanceof CompletionException ? throwable.getCause() : throwable;
                    if (attempt < MAX_ATTEMPTS
                            && cause instanceof MineSkinRequestException
                            && cause.getMessage().contains("rate limit")) {
                        long delay = 200L * (attempt + 1);
                        fLogger.warning("Texture %s | Frame [x=%d, y=%d] rate limited, retry in %ds...", texture, x, y, delay / 20L);

                        CompletableFuture<Frame> result = new CompletableFuture<>();
                        taskScheduler.runAsyncLater(() -> loadWithRetry(x, y, skinImage, texture, attempt + 1)
                                .whenComplete((frame, err) -> {
                                    if (err != null) {
                                        result.completeExceptionally(err);
                                    } else {
                                        result.complete(frame);
                                    }}), delay);

                        return result;
                    }

                    return CompletableFuture.failedFuture(throwable);
                });
    }

}