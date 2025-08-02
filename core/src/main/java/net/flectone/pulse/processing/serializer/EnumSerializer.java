package net.flectone.pulse.processing.serializer;

import net.elytrium.serializer.custom.ClassSerializer;

import java.util.Arrays;

public class EnumSerializer<E extends Enum<E>> extends ClassSerializer<E, String> {
    private final Class<E> enumClass;

    @SuppressWarnings("unchecked")
    public EnumSerializer(Class<?> enumClass) {
        super((Class<E>) enumClass, String.class);
        this.enumClass = (Class<E>) enumClass;
    }

    @Override
    public String serialize(E enumValue) {
        return enumValue.name();
    }

    @Override
    public E deserialize(String string) {
        return Arrays.stream(enumClass.getEnumConstants())
                .filter(enumValue -> enumValue.name().equalsIgnoreCase(string))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown enum value: " + string + " for enum " + enumClass.getName()));
    }
}
