package com.carnot.libclasses;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.List;

public class PrimitiveWrapper<T> extends AbstractList<T> {

    private final T[] data;

    private PrimitiveWrapper(T[] data) {
        this.data = data; // you can clone this array for preventing aliasing
    }

    public static <T> List<T> ofIntegers(int... data) {
        return new PrimitiveWrapper(toBoxedArray(Integer.class, data));
    }

    public static <T> List<T> ofCharacters(char... data) {
        return new PrimitiveWrapper(toBoxedArray(Character.class, data));
    }

    public static <T> List<T> ofDoubles(double... data) {
        return new PrimitiveWrapper(toBoxedArray(Double.class, data));
    }

    // ditto for byte, float, boolean, long

    private static <T> T[] toBoxedArray(Class<T> boxClass, Object components) {
        final int length = Array.getLength(components);
        Object res = Array.newInstance(boxClass, length);

        for (int i = 0; i < length; i++) {
            Array.set(res, i, Array.get(components, i));
        }

        return (T[]) res;
    }

    @Override
    public T get(int index) {
        return data[index];
    }

    @Override
    public int size() {
        return data.length;
    }
}