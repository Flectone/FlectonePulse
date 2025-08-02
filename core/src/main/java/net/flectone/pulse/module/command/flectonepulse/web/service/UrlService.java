package net.flectone.pulse.module.command.flectonepulse.web.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.module.command.flectonepulse.web.exception.EmptyHostException;
import net.flectone.pulse.processing.resolver.FileResolver;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Singleton
public class UrlService {

    private final Config.Editor config;
    private final AtomicReference<UUID> currentToken = new AtomicReference<>(UUID.randomUUID());

    @Inject
    public UrlService(FileResolver fileResolver) {
        this.config = fileResolver.getConfig().getEditor();
    }

    public String generateUrl() {
        String url = "http";
        if (config.isHttps()) {
            url += "s";
        }

        return url + "://" + getLocalIp() + ":" + config.getPort() + "/editor/" + currentToken.get();
    }

    public boolean validateToken(String token) {
        return token.equalsIgnoreCase(currentToken.get().toString());
    }


    public void resetToken() {
        currentToken.set(UUID.randomUUID());
    }

    private String getLocalIp() throws EmptyHostException {
        String host = config.getHost();
        if (host.isEmpty()) {
            throw new EmptyHostException();
        }

        return host;
    }
}