package net.flectone.pulse.util.version;

/**
 * Compares plugin version strings, so the update check can tell a newer release from an older one.
 * @author TheFaser
 */
public interface VersionComparator {

    /**
     * Whether a version is a snapshot rather than a release.
     *
     * @param version the version
     * @return true if it is a snapshot
     */
    boolean isSnapshot(String version);

    /**
     * Whether one version predates another.
     *
     * @param first the version to test
     * @param second the version to compare against
     * @return true if the first is older
     */
    boolean isOlderThan(String first, String second);

    /**
     * Whether one version predates another, optionally treating a snapshot as older than the release it leads to.
     *
     * @param first the version to test
     * @param second the version to compare against
     * @param checkSnapshot whether the snapshot marker is taken into account
     * @return true if the first is older
     */
    boolean isOlderThan(String first, String second, boolean checkSnapshot);

}
