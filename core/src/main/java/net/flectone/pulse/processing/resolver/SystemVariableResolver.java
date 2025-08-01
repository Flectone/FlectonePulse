package net.flectone.pulse.processing.resolver;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class SystemVariableResolver {

    private final Pattern envVarPattern = Pattern.compile("\\$\\{([A-Za-z0-9_.-]+)(?::([^}]*))?}");

    @Inject
    public SystemVariableResolver() {
    }

    public String substituteEnvVars(String text) {
        String variable = process(text);
        if (variable.isBlank()) return text;

        return variable;
    }

    private String process(String text) {
        StringBuilder stringBuilder = new StringBuilder();
        Matcher matcher = envVarPattern.matcher(text);

        int index = 0;
        while (matcher.find()) {
            stringBuilder.append(text, index, matcher.start());

            String variable = matcher.group(1);
            Object obj =  System.getenv(variable);

            String value;
            if (obj != null) {
                value = String.valueOf(obj);
            } else {
                value = matcher.group(2);
                if (value == null) {
                    value = "";
                }
            }

            stringBuilder.append(value);
            index = matcher.end();
        }

        stringBuilder.append(text, index, text.length());
        return stringBuilder.toString();
    }
}
