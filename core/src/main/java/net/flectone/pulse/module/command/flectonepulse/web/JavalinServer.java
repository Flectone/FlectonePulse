package net.flectone.pulse.module.command.flectonepulse.web;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import net.flectone.pulse.configuration.Config;
import net.flectone.pulse.module.command.flectonepulse.web.controller.EditorController;
import net.flectone.pulse.resolver.FileResolver;
import org.apache.commons.io.output.NullPrintStream;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import java.io.PrintStream;

@Singleton
public class JavalinServer {

    private final Config.Editor config;
    private final EditorController controller;
    private Javalin app;

    @Inject
    public JavalinServer(FileResolver fileResolver,
                         EditorController controller) {
        this.config = fileResolver.getConfig().getEditor();
        this.controller = controller;
    }

    public boolean isEnable() {
        return app != null;
    }

    public void onEnable() {
        if (app != null) {
            app.stop();
        }

        // disable first jetty message
        PrintStream originalErr = System.err;

        System.setErr(NullPrintStream.INSTANCE);

        app = Javalin.create(javalinConfig -> {
            javalinConfig.startupWatcherEnabled = false;
            javalinConfig.showJavalinBanner = false;

            QueuedThreadPool threadPool = new QueuedThreadPool();
            threadPool.setMaxThreads(3);
            threadPool.setMinThreads(1);
            threadPool.setIdleTimeout(60000);
            threadPool.setReservedThreads(0);

            javalinConfig.staticFiles.add(staticFiles -> {
                staticFiles.directory = "/web";
                staticFiles.hostedPath = "/web";
                staticFiles.location = Location.CLASSPATH;
            });

            javalinConfig.jetty.threadPool = threadPool;
        }).start(config.getPort());

        controller.initConfigFiles();
        controller.setupRoutes(app);

        System.setErr(originalErr);
    }

    public void onDisable() {
        if (app != null) {
            app.stop();
            app = null;
        }
    }

}