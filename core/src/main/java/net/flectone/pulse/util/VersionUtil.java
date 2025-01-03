package net.flectone.pulse.util;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class VersionUtil {

    @Inject
    public VersionUtil() {}

    public boolean isOlderThan(String first, String second) {
        String[] subFirst = first.split("\\.");
        if (subFirst.length != 3) return false;

        String[] subSecond = second.split("\\.");
        if (subSecond.length != 3) return true;

        for (int i = 0; i < 3; i++) {
            int intFirst = Integer.parseInt(subFirst[i]);
            int intSecond = Integer.parseInt(subSecond[i]);

            if (intFirst < intSecond) {
                return true;
            }
        }

        return false;
    }
}
