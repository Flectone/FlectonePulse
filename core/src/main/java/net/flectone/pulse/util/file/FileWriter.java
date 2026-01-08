package net.flectone.pulse.util.file;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.exception.FileWriteException;
import net.flectone.pulse.model.file.FilePack;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FileWriter {

    public static final long LAST_MODIFIED_TIME = System.currentTimeMillis();

    private static final String HEADER =
            """
            #  ___       ___  __  ___  __        ___
            # |__  |    |__  /  `  |  /  \\ |\\ | |__
            # |    |___ |___ \\__,  |  \\__/ | \\| |___
            #  __             __   ___
            # |__) |  | |    /__` |__
            # |    \\__/ |___ .__/ |___   /\\
            #                           /  \\
            # __/\\___  ____/\\_____  ___/    \\______
            #        \\/           \\/
            #
            """;

    private final ObjectMapper yamlMapper;
    private final FilePathProvider filePathProvider;

    public void save(FilePack files, boolean checkExist) {
        Path commandPath = filePathProvider.get(files.command());
        if (!checkExist || !Files.exists(commandPath)) {
            save(commandPath, files.command());
        }

        Path configPath = filePathProvider.get(files.config());
        if (!checkExist || !Files.exists(configPath)) {
            save(configPath, files.config());
        }

        Path integrationPath = filePathProvider.get(files.integration());
        if (!checkExist || !Files.exists(integrationPath)) {
            save(integrationPath, files.integration());
        }

        Path messagePath = filePathProvider.get(files.message());
        if (!checkExist || !Files.exists(messagePath)) {
            save(messagePath, files.message());
        }

        Path permissionPath = filePathProvider.get(files.permission());
        if (!checkExist || !Files.exists(permissionPath)) {
            save(permissionPath, files.permission());
        }

        files.localizations().values()
                .forEach( localization -> {
                    Path localizationPath = filePathProvider.get(localization);
                    if (!checkExist || !Files.exists(localizationPath)) {
                        save(localizationPath, localization);
                    }
                }
        );
    }

    public void save(Path pathToFile, Object fileResource) {
        if (pathToFile.toFile().lastModified() == LAST_MODIFIED_TIME) return;

        Map<String, String> comments = new LinkedHashMap<>();
        collectDescriptions(fileResource.getClass(), "", comments, new HashSet<>());

        try {
            String yaml = yamlMapper.writeValueAsString(fileResource);
            String yamlWithComments = HEADER + addCommentsToYaml(yaml, comments);

            Files.createDirectories(pathToFile.getParent());
            Files.writeString(pathToFile, yamlWithComments);
            pathToFile.toFile().setLastModified(LAST_MODIFIED_TIME);
        } catch (IOException e) {
            throw new FileWriteException(pathToFile.toFile().getName(), e);
        }
    }

    private void collectDescriptions(Class<?> clazz, String basePath, Map<String, String> out, Set<Class<?>> visited) {
        if (clazz == null || visited.contains(clazz)) return;
        visited.add(clazz);

        for (Field field : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) continue;

            String propName = field.getName();
            JsonProperty jsonProperty = field.getAnnotation(JsonProperty.class);
            if (jsonProperty != null && jsonProperty.value() != null && !jsonProperty.value().isEmpty()) propName = jsonProperty.value();

            String path = basePath.isEmpty() ? propName : basePath + "." + propName;

            JsonPropertyDescription propertyDescription = field.getAnnotation(JsonPropertyDescription.class);
            if (propertyDescription != null && propertyDescription.value() != null && !propertyDescription.value().isEmpty()) {
                String comment = propertyDescription.value().trim();
                out.put(path, comment);
            }

            Class<?> classField = field.getType();
            if (isUserType(classField)) {
                collectDescriptions(classField, path, out, visited);
            }
        }
    }

    private boolean isUserType(Class<?> t) {
        if (t.isPrimitive()) return false;
        if (t.isEnum()) return false;
        if (t.getName().startsWith("java.")) return false;
        if (Collection.class.isAssignableFrom(t)) return false;
        if (Map.class.isAssignableFrom(t)) return false;
        return !t.isArray();
    }

    private String addCommentsToYaml(String yaml, Map<String, String> comments) {
        String[] lines = yaml.split("\n", -1);
        List<String> out = new ArrayList<>(lines.length * 2);

        int indentUnit = detectIndentUnit(lines);
        if (indentUnit <= 0) indentUnit = 2;

        Map<Integer, String> pathAtDepth = new HashMap<>();
        Set<String> alreadyInserted = new HashSet<>();

        for (String line : lines) {
            String trimmed = line.trim();

            if (trimmed.isEmpty()) {
                out.add(line);
                continue;
            }

            // skip list items entirely
            if (trimmed.charAt(0) == '-') {
                out.add(line);
                continue;
            }

            int leading = countLeadingSpaces(line);
            String withoutLeading = line.substring(leading);

            int colonIndex = withoutLeading.indexOf(':');
            if (colonIndex == -1) {
                out.add(line);
                continue;
            }

            String keyPart = withoutLeading.substring(0, colonIndex).trim();

            // strip surrounding quotes if present
            if ((keyPart.startsWith("\"") && keyPart.endsWith("\"")) ||
                    (keyPart.startsWith("'") && keyPart.endsWith("'"))) {
                if (keyPart.length() >= 2) {
                    keyPart = keyPart.substring(1, keyPart.length() - 1);
                }
            }

            int depth = Math.max(0, leading / indentUnit);
            pathAtDepth.put(depth, keyPart);

            // clear deeper depths
            pathAtDepth.keySet().removeIf(d -> d > depth);

            // build dotted path
            List<String> parts = new ArrayList<>();
            for (int d = 0; d <= depth; d++) {
                String p = pathAtDepth.get(d);
                if (p != null && !p.isEmpty()) parts.add(p);
            }

            String path = String.join(".", parts);

            // insert comment only if present and not already inserted for this path
            if (comments.containsKey(path) && !alreadyInserted.contains(path)) {
                String comment = comments.get(path);
                for (String cLine : comment.split("\n")) {
                    out.add(repeat(' ', leading) + "# " + cLine.trim());
                }

                alreadyInserted.add(path);
            }

            out.add(line);
        }

        return String.join("\n", out);
    }

    private int detectIndentUnit(String[] lines) {
        List<Integer> numbers = new ArrayList<>();
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            if (!trimmed.contains(":")) continue;

            int leading = countLeadingSpaces(line);
            if (leading > 0) {
                numbers.add(leading);
            }
        }

        if (numbers.isEmpty()) return -1;

        int g = numbers.getFirst();
        for (int n : numbers) {
            g = gcd(g, n);
        }

        return g;
    }

    private int gcd(int a, int b) {
        if (b == 0) return a;
        return gcd(b, a % b);
    }

    private int countLeadingSpaces(String string) {
        int i = 0;
        while (i < string.length() && string.charAt(i) == ' ') {
            i++;
        }

        return i;
    }

    private String repeat(char c, int n) {
        if (n <= 0) return "";

        char[] arr = new char[n];
        Arrays.fill(arr, c);

        return new String(arr);
    }

}
