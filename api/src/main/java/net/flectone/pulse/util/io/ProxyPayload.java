package net.flectone.pulse.util.io;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Reads the extra data attached to a proxy message, in the same order the sender wrote it.
 * @author TheFaser
 */
public class ProxyPayload implements Closeable {

    private final DataInputStream dataInputStream;

    /**
     * Opens a payload for reading.
     *
     * @param bytes the raw payload
     */
    public ProxyPayload(byte[] bytes) {
        this.dataInputStream = new DataInputStream(new ByteArrayInputStream(bytes));
    }

    /**
     * Reads a string.
     *
     * @return the value
     * @throws IOException if the payload ends early
     */
    public String readString() throws IOException {
        return dataInputStream.readUTF();
    }

    /**
     * Reads an int.
     *
     * @return the value
     * @throws IOException if the payload ends early
     */
    public int readInt() throws IOException {
        return dataInputStream.readInt();
    }

    /**
     * Reads a long.
     *
     * @return the value
     * @throws IOException if the payload ends early
     */
    public long readLong() throws IOException {
        return dataInputStream.readLong();
    }

    /**
     * Reads a boolean.
     *
     * @return the value
     * @throws IOException if the payload ends early
     */
    public boolean readBoolean() throws IOException {
        return dataInputStream.readBoolean();
    }

    /**
     * Reads a uuid.
     *
     * @return the value
     * @throws IOException if the payload ends early
     */
    public UUID readUUID() throws IOException {
        return UUID.fromString(dataInputStream.readUTF());
    }

    /**
     * Reads whatever is left of the payload.
     *
     * @return the remaining bytes
     * @throws IOException if reading fails
     */
    public byte[] readAllBytes() throws IOException {
        return dataInputStream.readAllBytes();
    }

    @Override
    public void close() throws IOException {
        dataInputStream.close();
    }

}
