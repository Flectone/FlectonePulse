package net.flectone.pulse.logging.filter;

import java.util.List;

/**
 * The log filter.
 * Idea taken from here <a href="https://github.com/Whitescan/ConsoleFilter/blob/master/src/main/java/dev/whitescan/consolefilter/share/LogFilter.java">...</a>
 *
 * @author Whitescan
 * @since 1.0.0
 */
public interface LogFilter {

    /**
     * Replaces the list of substrings that suppress a console line.
     *
     * @param filters the substrings to suppress
     */
    void setFilters(List<String> filters);

}
