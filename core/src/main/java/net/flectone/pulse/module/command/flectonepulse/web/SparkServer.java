package net.flectone.pulse.module.command.flectonepulse.web;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Config;
import net.flectone.pulse.module.command.flectonepulse.web.controller.EditorController;
import net.flectone.pulse.resolver.FileResolver;
import spark.Service;

@Singleton
public class SparkServer {

    private final Config.Editor config;
    private final EditorController controller;
    private Service sparkService;

    @Inject
    public SparkServer(FileResolver fileResolver,
                       EditorController controller) {
        this.config = fileResolver.getConfig().getEditor();
        this.controller = controller;
    }

    public boolean isEnable() {
        return sparkService != null;
    }

    public void onEnable() {
        if (sparkService != null) {
            sparkService.stop();
        }

        sparkService = Service.ignite();
        sparkService.port(config.getPort());
        sparkService.staticFiles.location("/");

        sparkService.before((req, res) -> res.type("text/html; charset=utf-8"));

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