package net.flectone.pulse.module.command.flectonepulse.web;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.module.command.flectonepulse.web.controller.EditorController;
import net.flectone.pulse.processing.resolver.FileResolver;
import spark.Service;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SparkServer {

    private final FileResolver fileResolver;
    private final EditorController controller;

    private Service sparkService;

    public boolean isEnable() {
        return sparkService != null;
    }

    public void onEnable() {
        if (sparkService != null) {
            sparkService.stop();
        }

        sparkService = Service.ignite();
        sparkService.port(fileResolver.getConfig().getEditor().getPort());
        sparkService.staticFiles.location("/");

        sparkService.before((req, res) -> res.type("text/html; charset=utf-8"));

        controller.initConfigFiles();
        controller.setupRoutes(sparkService);
        sparkService.init();
    }

    public void onDisable() {
        if (sparkService != null) {
            sparkService.stop();
            sparkService = null;
        }
    }

}