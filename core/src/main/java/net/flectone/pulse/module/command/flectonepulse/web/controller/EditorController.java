package net.flectone.pulse.module.command.flectonepulse.web.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.FileSerializable;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.module.command.flectonepulse.web.service.UrlService;
import net.flectone.pulse.processing.resolver.FileResolver;
import spark.Request;
import spark.Response;
import spark.Service;

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

    public void setupRoutes(Service spark) {
        spark.get("/", this::handleRoot);
        spark.get("/editor/:token", this::serveEditor);
        spark.get("/editor/file/:token/:fileType/:fileName", this::serveFile);
        spark.post("/editor/save/:token/:fileType/:fileName", this::handleSave);
        spark.post("/logout/:token", this::handleLogout);
    }

    private String handleRoot(Request req, Response res) {
        res.status(401);
        return loadTemplate("logout.html");
    }

    private String serveEditor(Request req, Response res) {
        String token = req.params("token");
        if (!urlService.validateToken(token)) {
            res.redirect("/");
            return null;
        }

        return renderEditor();
    }

    private String serveFile(Request req, Response res) {
        String token = req.params("token");
        String fileType = req.params("fileType");
        String fileName = req.params("fileName");

        if (!urlService.validateToken(token)) {
            res.redirect("/");
            return null;
        }

        try {
            String yamlContent = getFileContent(fileType, fileName);
            res.type("text/yaml; charset=utf-8");
            return yamlContent;
        } catch (Exception e) {
            res.status(500);
            return "Failed to load file: " + e.getMessage();
        }
    }

    private String handleSave(Request req, Response res) {
        String token = req.params("token");
        String fileType = req.params("fileType");
        String fileName = req.params("fileName");

        if (!urlService.validateToken(token)) {
            res.redirect("/");
            return null;
        }

        try {
            String yamlContent = req.body();
            saveFileContent(fileType, fileName, yamlContent);
            res.type("application/json");
            return "{\"success\": true}";
        } catch (Exception e) {
            res.status(500);
            res.type("application/json");
            return "{\"error\": \"Failed to save file: " + e.getMessage() + "\"}";
        }
    }

    private Object handleLogout(Request req, Response res) {
        String token = req.params("token");
        if (!urlService.validateToken(token)) {
            res.redirect("/");
            return null;
        }

        urlService.resetToken();
        res.type("application/json");
        return "{\"success\": true, \"redirect\": \"/?message=logged_out\"}";
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