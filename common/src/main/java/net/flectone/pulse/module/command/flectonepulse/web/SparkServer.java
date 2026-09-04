package net.flectone.pulse.module.command.flectonepulse.web;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.logging.FLogger;
import net.flectone.pulse.module.command.flectonepulse.web.controller.EditorController;
import spark.Service;

import java.util.concurrent.atomic.AtomicReference;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SparkServer {

    private final FileFacade fileFacade;
    private final EditorController controller;
    private final FLogger fLogger;

    private Service sparkService;

    public boolean isEnable() {
        return sparkService != null;
    }

    public void onEnable() {
        onDisable();

        int port = fileFacade.command().flectonepulse().editor().port();

        Service service = Service.ignite();

        AtomicReference<Exception> initException = new AtomicReference<>();
        service.initExceptionHandler(initException::set);

        service.port(port);
        service.staticFiles.location("/");

        service.before((_, res) -> res.type("text/html; charset=utf-8"));

        controller.initConfigFiles();
        controller.setupRoutes(service);

        service.init();
        service.awaitInitialization();

        Exception exception = initException.get();
        if (exception != null) {
            service.stop();

            fLogger.warning("Editor did not start on port %s", exception, port);
            return;
        }

        sparkService = service;
    }

    public void onDisable() {
        if (sparkService != null) {
            sparkService.stop();
            sparkService = null;
        }
    }

}
