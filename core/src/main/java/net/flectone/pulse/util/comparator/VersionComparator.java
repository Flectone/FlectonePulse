package net.flectone.pulse.util.comparator;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class VersionComparator {

    public boolean isSnapshot(String version) {
        return version.endsWith("-SNAPSHOT");
    }

    public boolean isOlderThan(String first, String second) {
        String[] subFirst = parseVersionNumbers(first);
        if (subFirst.length != 3) return false;

        String[] subSecond = parseVersionNumbers(second);
        if (subSecond.length != 3) return true;

        for (int i = 0; i < 3; i++) {
            int intFirst = Integer.parseInt(subFirst[i]);
            int intSecond = Integer.parseInt(subSecond[i]);

            if (intFirst < intSecond) {
                return true;
            }

            if (intFirst > intSecond) {
                return false;
            }
        }

        return isSnapshot(first) && !isSnapshot(second);
    }

    private String[] parseVersionNumbers(String string) {
        int endIndex = string.indexOf('-');
        return (endIndex == -1 ? string : string.substring(0, endIndex)).split("\\.");
    }

}
