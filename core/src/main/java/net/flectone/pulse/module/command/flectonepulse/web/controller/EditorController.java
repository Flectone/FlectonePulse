package net.flectone.pulse.module.command.flectonepulse.web.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.javalin.Javalin;
import io.javalin.http.Context;
import net.flectone.pulse.configuration.FileSerializable;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.module.command.flectonepulse.web.service.UrlService;
import net.flectone.pulse.resolver.FileResolver;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class EditorController {

    private final UrlService urlService;
    private final FileResolver fileResolver;
    private final Map<String, FileSerializable> configFiles = new LinkedHashMap<>();
    private final Map<String, List<LocalizationFile>> localizationFiles = new HashMap<>();

    @Inject
    public EditorController(UrlService urlService,
                            FileResolver fileResolver) {
        this.urlService = urlService;
        this.fileResolver = fileResolver;
        initConfigFiles();
    }

    public void initConfigFiles() {
        configFiles.clear();
        localizationFiles.clear();

        configFiles.put("config.yml", fileResolver.getConfig());
        configFiles.put("commands.yml", fileResolver.getCommand());
        configFiles.put("integration.yml", fileResolver.getIntegration());
        configFiles.put("messages.yml", fileResolver.getMessage());
        configFiles.put("permissions.yml", fileResolver.getPermission());

        Map<String, Localization> localizationMap = fileResolver.getLocalizationMap();
        localizationFiles.put("localizations", new ArrayList<>());

        for (Map.Entry<String, Localization> entry : localizationMap.entrySet()) {
            String lang = entry.getKey();
            Localization localization = entry.getValue();
            localizationFiles.get("localizations").add(new LocalizationFile(lang + ".yml", localization));
        }
    }

    public void setupRoutes(Javalin app) {
        app.get("/", this::handleRoot);
        app.get("/editor/{token}", this::serveEditor);
        app.get("/editor/file/{token}/{fileType}/{fileName}", this::serveFile);
        app.post("/editor/save/{token}/{fileType}/{fileName}", this::handleSave);
        app.post("/logout/{token}", this::handleLogout);
    }

    private void serveEditor(Context ctx) {
        String token = ctx.pathParam("token");
        if (!urlService.validateToken(token)) {
            ctx.status(403).redirect("/");
            return;
        }

        ctx.html(renderEditor());
    }

    private void serveFile(Context ctx) {
        String token = ctx.pathParam("token");
        String fileType = ctx.pathParam("fileType");
        String fileName = ctx.pathParam("fileName");

        if (!urlService.validateToken(token)) {
            ctx.status(403).redirect("/");
            return;
        }

        try {
            String yamlContent = getFileContent(fileType, fileName);
            ctx.result(yamlContent).contentType("text/yaml; charset=utf-8");
        } catch (Exception e) {
            ctx.status(500).result("Failed to load file: " + e.getMessage());
        }
    }

    private void handleSave(Context ctx) {
        String token = ctx.pathParam("token");
        String fileType = ctx.pathParam("fileType");
        String fileName = ctx.pathParam("fileName");

        if (!urlService.validateToken(token)) {
            ctx.status(403).redirect("/");
            return;
        }

        try {
            String yamlContent = ctx.body();
            saveFileContent(fileType, fileName, yamlContent);
            ctx.json(Map.of("success", true));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", "Failed to save file: " + e.getMessage()));
        }
    }

    private void handleRoot(Context ctx) {
        String html = loadTemplate("logout.html");
        ctx.html(html).status(401);
    }

    private void handleLogout(Context ctx) {
        String token = ctx.pathParam("token");
        if (!urlService.validateToken(token)) {
            ctx.status(403).redirect("/");
            return;
        }

        urlService.resetToken();
        ctx.json(Map.of(
                "success", true,
                "redirect", "/?message=logged_out"
        ));
    }

    private String getFileContent(String fileType, String fileName) throws IOException {
        if ("main".equals(fileType)) {
            FileSerializable config = configFiles.get(fileName);
            if (config != null) {
                return Files.readString(config.getPath());
            }

        } else if ("localizations".equals(fileType)) {
            List<LocalizationFile> files = localizationFiles.get(fileType);
            for (LocalizationFile file : files) {
                if (file.fileName.equals(fileName)) {
                    return Files.readString(file.localization.getPath());
                }
            }
        }

        throw new FileNotFoundException("File not found: " + fileName);
    }

    private void saveFileContent(String fileType, String fileName, String content) throws IOException {
        if ("main".equals(fileType)) {
            FileSerializable config = configFiles.get(fileName);
            if (config != null) {
                Files.writeString(config.getPath(), content);
                config.reload(config.getPath());
                return;
            }

        } else if ("localizations".equals(fileType)) {
            List<LocalizationFile> files = localizationFiles.get(fileType);
            for (LocalizationFile file : files) {
                if (file.fileName.equals(fileName)) {
                    Files.writeString(file.localization.getPath(), content);
                    file.localization.reload(file.localization.getPath());
                    return;
                }
            }
        }

        throw new FileNotFoundException("File not found: " + fileName);
    }

    private String renderEditor() {
        StringBuilder mainFiles = new StringBuilder();
        for (String fileName : configFiles.keySet()) {
            mainFiles.append(String.format(
                    "<div class=\"file\" data-type=\"main\" data-name=\"%s\">%s</div>",
                    fileName, fileName
            ));
        }

        StringBuilder localeFiles = new StringBuilder();
        List<LocalizationFile> localizations = localizationFiles.get("localizations");
        for (LocalizationFile file : localizations) {
            localeFiles.append(String.format(
                    "<div class=\"file\" data-type=\"localizations\" data-name=\"%s\">%s</div>",
                    file.fileName, file.fileName
            ));
        }

        String template = loadTemplate("editor.html");

        return template
                .replace("{{mainFiles}}", mainFiles.toString())
                .replace("{{localeFiles}}", localeFiles.toString());
    }

    private String loadTemplate(String name) {
        try (InputStream is = getClass().getResourceAsStream("/web/" + name);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException | NullPointerException e) {
            throw new RuntimeException("Failed to load editor template", e);
        }
    }

    private record LocalizationFile(String fileName, Localization localization) {
    }
}