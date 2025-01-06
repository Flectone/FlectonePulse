package net.flectone.pulse.util;

import com.google.gson.JsonElement;

public interface ServerUtil {
    String getMinecraftName(Object item);
    String getTPS();
    int getMax();
    int getOnlineCount();
    boolean hasProject(String projectName);

    JsonElement getMOTD();
}
