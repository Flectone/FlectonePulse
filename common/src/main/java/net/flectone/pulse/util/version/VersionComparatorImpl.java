package net.flectone.pulse.util.version;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class VersionComparatorImpl implements VersionComparator {

    @Override
    public boolean isSnapshot(String version) {
        return version.endsWith("-SNAPSHOT");
    }

    @Override
    public boolean isOlderThan(String first, String second) {
        return isOlderThan(first, second, true);
    }

    @Override
    public boolean isOlderThan(String first, String second, boolean checkSnapshot) {
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

        return checkSnapshot && isSnapshot(first) && !isSnapshot(second);
    }

    private String[] parseVersionNumbers(String string) {
        int endIndex = string.indexOf('-');
        return (endIndex == -1 ? string : string.substring(0, endIndex)).split("\\.");
    }

}
