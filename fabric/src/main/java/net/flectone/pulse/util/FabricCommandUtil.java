package net.flectone.pulse.util;

import java.util.Optional;

public class FabricCommandUtil extends CommandUtil {

    @Override
    public void unregister(String command) {

    }

    @Override
    public void dispatch(String command) {

    }

    @Override
    public Optional<Object> getOptional(int index, Object arguments) {
        return Optional.empty();
    }

    @Override
    public String getLiteral(int index, Object arguments) {
        return "";
    }

    @Override
    public String getString(int index, Object arguments) {
        return "";
    }

    @Override
    public String getText(int index, Object arguments) {
        return "";
    }

    @Override
    public String getFull(Object arguments) {
        return "";
    }

    @Override
    public Integer getInteger(int index, Object arguments) {
        return 0;
    }

    @Override
    public Boolean getBoolean(int index, Object arguments) {
        return false;
    }

    @Override
    public <T> T getByClassOrDefault(int index, Class<T> clazz, T defaultValue, Object arguments) {
        return null;
    }

}
