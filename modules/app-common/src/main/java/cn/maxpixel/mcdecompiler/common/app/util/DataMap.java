package cn.maxpixel.mcdecompiler.common.app.util;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Objects;

public final class DataMap {// TODO
    private final Object2ObjectOpenHashMap<Key<?>, Object> data = new Object2ObjectOpenHashMap<>();

    public <T> void put(Key<T> key, T value) {
        data.put(key, value);
    }

    public <T> T get(Key<T> key) {
        return (T) data.get(key);
    }

    public static final class Key<T> {
        private final String name;

        public Key(String name) {
            this.name = Objects.requireNonNull(name);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            return name.equals(((Key<?>) o).name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }
}