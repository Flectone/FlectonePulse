package net.flectone.pulse.module.command.flectonepulse.web.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.module.command.flectonepulse.web.exception.EmptyHostException;
import net.flectone.pulse.util.file.FileFacade;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class UrlService {

    private final AtomicReference<UUID> currentToken = new AtomicReference<>(UUID.randomUUID());

    private final FileFacade fileFacade;

    public Config.Editor config() {
        return fileFacade.config().editor();
    }

    public String generateUrl() {
        String url = "http";
        if (config().https()) {
            url += "s";
        }

        return url + "://" + getLocalIp() + ":" + config().port() + "/editor/" + currentToken.get();
    }

    public boolean validateToken(String token) {
        return token.equalsIgnoreCase(currentToken.get().toString());
    }

    public void resetToken() {
        currentToken.set(UUID.randomUUID());
    }

    private String getLocalIp() throws EmptyHostException {
        String host = config().host();
        if (host.isEmpty()) {
            throw new EmptyHostException();
        }

        return host;
    }
}