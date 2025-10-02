package net.flectone.pulse.processing.processor;

import com.fasterxml.jackson.annotation.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.YamlFile;
import net.flectone.pulse.config.localization.EnglishLocale;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.config.localization.RussianLocale;
import net.flectone.pulse.exception.YamlReadException;
import net.flectone.pulse.exception.YamlWriteException;
import org.apache.commons.lang3.Strings;
import org.snakeyaml.engine.v2.api.LoadSettings;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.*;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.exc.MismatchedInputException;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.dataformat.yaml.YAMLFactory;
import tools.jackson.dataformat.yaml.YAMLMapper;
import tools.jackson.dataformat.yaml.YAMLWriteFeature;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Singleton
public class YamlFileProcessor {

    private final ObjectMapper mapper = YAMLMapper.builder(
                    YAMLFactory.builder()
                            .loadSettings(LoadSettings.builder().setBufferSize(4096).build()) // increase string limit
                            .build()
            )
            .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES) // jackson 2.x value
            .disable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS) // jackson 2.x value
            .disable(EnumFeature.READ_ENUMS_USING_TO_STRING) // jackson 2.x value
            .disable(EnumFeature.WRITE_ENUMS_USING_TO_STRING) // jackson 2.x value
            .disable(MapperFeature.DETECT_PARAMETER_NAMES) // [databind#5314]
            .enable(SerializationFeature.INDENT_OUTPUT) // indent output for values
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS) // fix enum names
            .enable(MapperFeature.FIX_FIELD_NAME_UPPER_CASE_PREFIX) // fix field names
            .disable(YAMLWriteFeature.SPLIT_LINES) // fix split long values
            .disable(YAMLWriteFeature.WRITE_DOC_START_MARKER) // fix header
            .disable(YAMLWriteFeature.USE_NATIVE_TYPE_ID) // fix type id like !!java.util.Hashmap
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .changeDefaultPropertyInclusion(config -> JsonInclude.Value.construct(JsonInclude.Include.NON_NULL, JsonInclude.Include.NON_NULL)) // show only non-null values
            .changeDefaultNullHandling(config -> JsonSetter.Value.forValueNulls(Nulls.SKIP)) // skip null values deserialization
            .defaultMergeable(true)
            .addModule(new SimpleModule().addDeserializer(String.class, new ValueDeserializer<>() { // fix null strings

                @Override
                public String deserialize(JsonParser p, DeserializationContext ctxt) {
                    return p.currentToken() == JsonToken.VALUE_NULL ? "" : p.getString();
                }

                @Override
                public String getNullValue(DeserializationContext ctxt) {
                    return "";
                }

            }))
            .build();

    private final String header =
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

    private final EnglishLocale englishLocale;
    private final RussianLocale russianLocale;

    @Inject
    public YamlFileProcessor(EnglishLocale englishLocale,
                             RussianLocale russianLocale) {
        this.englishLocale = englishLocale;
        this.russianLocale = russianLocale;
    }

    public <T extends YamlFile> void reload(T yamlFile) throws IOException {
        if (yamlFile instanceof Localization localization) {
            initLocalization(localization);
        }

        load(yamlFile);
        save(yamlFile);
    }

    public void initLocalization(Localization localization) {
        if (!localization.isInitialized()) {
            if (localization.getLanguage().equals("ru_ru")) {
                russianLocale.init(localization);
            } else {
                englishLocale.init(localization);
            }

            localization.setInitialized(true);
        }
    }

    public <T extends YamlFile> void load(T yamlFile) throws IOException {
        if (Files.exists(yamlFile.getPathToFile())) {
            File file = yamlFile.getPathToFile().toFile();

            try {
                mapper.readerForUpdating(yamlFile).readValue(file);
            } catch (Exception e) {
                if (e instanceof MismatchedInputException mismatchedInputException
                        && mismatchedInputException.getMessage() != null
                        && mismatchedInputException.getMessage().contains("No content to map due to end-of-input")) {
                    save(yamlFile);
                    return;
                }

                throw new YamlReadException(file.getName(), e);
            }
        }
    }

    public <T extends YamlFile> void save(T yamlFile) throws IOException {
        Map<String, String> comments = new LinkedHashMap<>();

        boolean english = yamlFile instanceof Localization localization
                && !localization.getLanguage().equals("ru_ru");
        collectDescriptions(yamlFile.getClass(), "", comments, new HashSet<>(), english);

        Path pathToFile = yamlFile.getPathToFile();

        try {
            String yaml = mapper.writeValueAsString(yamlFile);
            String yamlWithComments = header + addCommentsToYaml(yaml, comments);

            Files.createDirectories(pathToFile.getParent());
            Files.writeString(pathToFile, yamlWithComments);
        } catch (IOException e) {
            throw new YamlWriteException(pathToFile.toFile().getName(), e);
        }
    }

    private void collectDescriptions(Class<?> clazz, String basePath, Map<String, String> out, Set<Class<?>> visited, boolean english) {
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
                if (english) {
                    comment = Strings.CS.replace(comment, "https://flectone.net/pulse/", "https://flectone.net/en/pulse/");
                }

                out.put(path, comment);
            }

            Class<?> classField = field.getType();
            if (isUserType(classField)) {
                collectDescriptions(classField, path, out, visited, english);
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
