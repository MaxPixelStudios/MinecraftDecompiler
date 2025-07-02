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
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A readonly content list.
 * <p>
 * This class is not reusable, which means that you can only read from each content once
 * @apiNote If no additional information is given, the default charset is UTF-8, instead of {@link Charset#defaultCharset()}
 */
public interface ContentList extends Iterable<ContentList.ContentStream> {
    /**
     * A content stream
     * @param reader The reader
     * @param name The name, may be null
     * @apiNote The one who calls {@link #asBufferedReader()} or {@link #lines()}(usually being the
     *          {@link cn.maxpixel.mcdecompiler.mapping.processor.MappingProcessor MappingProcessor}) should close
     *          the corresponding {@link BufferedReader} or {@link Stream}, and the one who consumes the {@link #reader}
     *          should close it
     */
    record ContentStream(@NotNull Reader reader, @Nullable String name) implements ContentList {
        public ContentStream {
            Objects.requireNonNull(reader);
        }

        public ContentStream(@NotNull Reader reader) {
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
        public @NotNull Iterator<ContentStream> iterator() {
            return ObjectIterators.singleton(this);
        }

        @Override
        public ContentStream getAsSingle() {
            return this;
        }
    }

    default ContentStream getAsSingle() {
        throw new UnsupportedOperationException("The content list contains multiple contents, " +
                "but a single content is required");
    }

    static Eager of() {
        return new Eager();
    }

    static Lazy ofLazy(Supplier<Iterator<ContentStream>> iterator) {
        return new Lazy(iterator);
    }

    class Eager implements ContentList {
        private final ObjectArrayList<ContentStream> streams = new ObjectArrayList<>();

        public void add(@NotNull ContentStream c) {
            streams.add(c);
        }

        @Override
        public @NotNull Iterator<ContentStream> iterator() {
            return streams.iterator();
        }
    }

    class Lazy implements ContentList {
        private final Eager supplied = new Eager();
        private final Supplier<Iterator<ContentStream>> supplier;
        private boolean computed;

        public Lazy(Supplier<Iterator<ContentStream>> supplier) {
            this.supplier = supplier;
        }

        @Override
        public @NotNull Iterator<ContentStream> iterator() {
            if (computed) return supplied.iterator();
            computed = true;
            return new Iterator<>() {
                private final Iterator<ContentStream> it = supplier.get();

                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }

                @Override
                public ContentStream next() {
                    var s = it.next();
                    supplied.add(s);
                    return s;
                }
            };
        }
    }
}