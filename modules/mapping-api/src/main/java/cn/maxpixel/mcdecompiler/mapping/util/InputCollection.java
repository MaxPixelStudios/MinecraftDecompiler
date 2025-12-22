package cn.maxpixel.mcdecompiler.mapping.util;

import cn.maxpixel.mcdecompiler.utils.LambdaUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterators;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A readonly collection for input.
 * <p>
 * This interface is not reusable, which means that you can only read from each entry once
 * @apiNote If no additional information is given, the default charset is UTF-8, instead of {@link Charset#defaultCharset()}
 */
public interface InputCollection extends Iterable<InputCollection.Entry> {
    /**
     * An entry of the input
     * @param reader The reader
     * @param name The name, may be null
     * @apiNote The one who calls {@link #asBufferedReader()} or {@link #lines()}(usually being the
     *          {@link cn.maxpixel.mcdecompiler.mapping.processor.MappingProcessor MappingProcessor}) should close
     *          the corresponding {@link BufferedReader} or {@link Stream}, and the one who consumes the {@link #reader}
     *          should close it as well
     */
    record Entry(@NotNull Reader reader, @Nullable String name) implements InputCollection {
        public Entry {
            Objects.requireNonNull(reader);
        }

        public Entry(@NotNull Reader reader) {
            this(reader, null);
        }

        public BufferedReader asBufferedReader() {
            return MappingUtils.asBufferedReader(reader);
        }

        public Stream<String> lines() {
            var br = asBufferedReader();
            return br.lines().onClose(LambdaUtil.unwrap(br::close));
        }

        @Override
        public @NotNull Iterator<Entry> iterator() {
            return ObjectIterators.singleton(this);
        }

        @Override
        public Entry getAsSingle() {
            return this;
        }
    }

    default Entry getAsSingle() {
        throw new UnsupportedOperationException("The input contains multiple entries, " +
                "but a single entry is required");
    }

    static Eager of() {
        return new Eager();
    }

    static <E extends Exception> Eager of(LambdaUtil.Consumer_WithThrowable<Eager, E> init) throws E {
        var c = new Eager();
        init.accept(c);
        return c;
    }

    static <E> Lazy<E> ofLazy(Iterator<E> it, Function<E, Entry> converter) {
        return new Lazy<>(it, converter);
    }

    class Eager implements InputCollection {
        private final ObjectArrayList<Entry> entries = new ObjectArrayList<>();

        public void add(@NotNull InputCollection.Entry c) {
            entries.add(c);
        }

        @Override
        public @NotNull Iterator<Entry> iterator() {
            return entries.iterator();
        }
    }

    /**
     * A lazy input collection
     * @param <E> Intermediate entry that can be used to create and identify an {@link Entry}
     */
    class Lazy<E> implements InputCollection {
        private final Iterator<E> it;
        private final Function<E, Entry> converter;
        private final Eager elements = new Eager();
        private boolean computed;

        public Lazy(Iterator<E> it, Function<E, Entry> converter) {
            this.it = Objects.requireNonNull(it);
            this.converter = Objects.requireNonNull(converter);
        }

        @Override
        public @NotNull Iterator<Entry> iterator() {
            if (computed) return elements.iterator();
            computed = true;
            return new Iterator<>() {
                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }

                @Override
                public Entry next() {
                    Entry e = Objects.requireNonNull(converter.apply(it.next()));
                    elements.add(e);
                    return e;
                }
            };
        }
    }
}