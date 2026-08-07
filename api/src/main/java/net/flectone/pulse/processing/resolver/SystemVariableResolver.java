package net.flectone.pulse.processing.resolver;

/**
 * Substitutes environment variables into config values, so secrets such as tokens and
 * database passwords need not be written into the files.
 * @author TheFaser
 */
public interface SystemVariableResolver {

    /**
     * Replaces every environment variable reference in the text with its value.
     *
     * @param text the raw config value
     * @return the text with variables substituted
     */
    String substituteEnvVars(String text);

}