package net.flectone.pulse.resolver;

import com.alessiodp.libby.Library;
import com.alessiodp.libby.classloader.IsolatedClassLoader;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Downloads the libraries the plugin does not bundle and loads them at runtime, relocating
 * their packages so they cannot clash with anything the server already has.
 * @author TheFaser
 */
public interface LibraryResolver {

    /**
     * The adventure artifacts fetched when the platform does not supply them.
     *
     * @return the artifact ids
     */
    default List<String> getAdventureArtifactIds() {
        return List.of(
                "adventure-nbt",
                "adventure-key",
                "adventure-text-serializer-ansi",
                "adventure-text-serializer-plain",
                "adventure-text-serializer-legacy",
                "adventure-text-serializer-json-legacy-impl",
                "adventure-text-serializer-gson"
        );
    }

    /**
     * The PacketEvents artifacts fetched when the platform does not supply them.
     *
     * @return the artifact ids
     */
    default List<String> getPacketEventsArtifactIds() {
        return List.of(
                "packetevents-api",
                "packetevents-netty-common"
        );
    }

    /**
     * Downloads and loads a single library.
     *
     * @param library the library to load
     */
    void loadLibrary(Library library);

    /**
     * Downloads and loads several libraries.
     *
     * @param libraries the libraries to load
     */
    void loadLibraries(List<Library> libraries);

    /**
     * Downloads and loads every library the current platform
     */
    void loadLibraries();

    /**
     * The class loader holding one isolated group of libraries, so versions that would otherwise
     * conflict can live side by side.
     *
     * @param loaderId the group id
     * @return the class loader for that group
     */
    IsolatedClassLoader getIsolatedClassLoaderById(@NotNull String loaderId);

    /**
     * Registers the repositories libraries are downloaded from.
     */
    void resolveRepositories();

}
