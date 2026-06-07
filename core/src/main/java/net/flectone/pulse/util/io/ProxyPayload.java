package net.flectone.pulse.util.io;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

public class ProxyPayload implements Closeable {

    private final DataInputStream dataInputStream;

    public ProxyPayload(byte[] bytes) {
        this.dataInputStream = new DataInputStream(new ByteArrayInputStream(bytes));
    }

    public String readString() throws IOException {
        return dataInputStream.readUTF();
    }

    public int readInt() throws IOException {
        return dataInputStream.readInt();
    }

    public long readLong() throws IOException {
        return dataInputStream.readLong();
    }

    public boolean readBoolean() throws IOException {
        return dataInputStream.readBoolean();
    }

    public UUID readUUID() throws IOException {
        return UUID.fromString(dataInputStream.readUTF());
    }

    public byte[] readAllBytes() throws IOException {
        return dataInputStream.readAllBytes();
    }

    public Optional<FEntity> parseFEntity(Gson gson, JsonObject jsonObject) {
        if (jsonObject.has("name") && jsonObject.has("uuid") && jsonObject.has("type")) {
            boolean isPlayer = jsonObject.has("id");
            return Optional.of(gson.fromJson(jsonObject, isPlayer ? FPlayer.FPlayerImpl.class : FEntity.FEntityImpl.class));
        }

        return Optional.empty();
    }

    @Override
    public void close() throws IOException {
        dataInputStream.close();
    }

}
