package net.flectone.pulse.util;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class SafeDataOutputStream extends DataOutputStream {

    private final Gson gson;
    
    public SafeDataOutputStream(Gson gson, OutputStream out) {
        super(out);

        this.gson = gson;
    }

    public void writeAsJson(Object object) throws IOException {
        String json = gson.toJson(object);
        super.writeUTF(json);
    }
    
    public void writeString(@Nullable String string) throws IOException {
        super.writeUTF(StringUtils.defaultString(string));
    }
    
}
