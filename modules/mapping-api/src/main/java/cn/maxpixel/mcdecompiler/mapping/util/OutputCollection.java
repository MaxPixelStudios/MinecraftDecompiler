package cn.maxpixel.mcdecompiler.mapping.util;

import cn.maxpixel.mcdecompiler.utils.LambdaUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Writer;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public interface OutputCollection {
    /**
     * An entry of the output
     * @param writer The writer
     * @param name The name, may be null
     * @apiNote The one who calls {@link #getUnnamedOutput()} or {@link #getOutput(String)}(usually being the
     *          {@link cn.maxpixel.mcdecompiler.mapping.generator.MappingGenerator MappingGenerator}) should close
     *          the corresponding {@link Writer}, and the one who consumes the {@link #writer}
     *          should close it as well
     */
    record Entry(@NotNull Writer writer, @Nullable String name) implements OutputCollection {
        public Entry {
            Objects.requireNonNull(writer);
        }

        public Entry(@NotNull Writer writer) {
            this(writer, null);
        }

        @Override
        public Writer getUnnamedOutput() {
            return writer;
        }
    }

    Writer getUnnamedOutput();

    default @Nullable Writer getOutput(String name) {
        throw new UnsupportedOperationException("The collection has no named output, " +
                "but named output is required");
    }

    static Entry ofUnnamed(Writer w) {
        return new Entry(w);
    }

    static Eager of() {
        return new Eager();
    }

    static <E extends Exception> Eager of(LambdaUtil.Consumer_WithThrowable<Eager, E> init) throws E {
        var named = new Eager();
        init.accept(named);
        return named;
    }

    static Lazy ofLazy(Supplier<Entry> unnamed, Function<String, Entry> named) {
        return new Lazy(unnamed, named);
    }

    static Lazy ofLazy(Function<String, Entry> named) {
        return new Lazy(null, named);
    }

    class Eager implements OutputCollection {
        private Entry unnamed;
        private final Object2ObjectOpenHashMap<String, Entry> named = new Object2ObjectOpenHashMap<>();

        public void setUnnamedOutput(Entry e) {
            this.unnamed = e;
        }

        public void putNamedOutput(Entry e) {
            named.put(Objects.requireNonNull(e.name), e);
        }

        @Override
        public Writer getUnnamedOutput() {
            return unnamed != null ? unnamed.writer : null;
        }

        @Override
        public @Nullable Writer getOutput(String name) {
            var e = named.get(name);
            return e != null ? e.writer : null;
        }
    }

    class Lazy implements OutputCollection {
        private final Eager elements = new Eager();
        private final Supplier<Entry> unnamed;
        private final Function<String, Entry> named;
        private boolean unnamedComputed;

        public Lazy(Supplier<Entry> unnamed, Function<String, Entry> named) {
            this.unnamed = unnamed == null ? () -> null : unnamed;
            this.named = Objects.requireNonNull(named);
        }

        @Override
        public Writer getUnnamedOutput() {
            if (!unnamedComputed) {
                elements.setUnnamedOutput(unnamed.get());
                unnamedComputed = true;
            }
            return elements.getUnnamedOutput();
        }

        @Override
        public @Nullable Writer getOutput(String name) {
            var o = elements.getOutput(name);
            if (o == null) {
                elements.putNamedOutput(named.apply(name));
                return elements.getOutput(name);
            }
            return o;
        }
    }
}