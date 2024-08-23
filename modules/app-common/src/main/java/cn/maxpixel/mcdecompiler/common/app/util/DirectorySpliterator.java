package cn.maxpixel.mcdecompiler.common.app.util;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

public class DirectorySpliterator implements Spliterator<Path> {// TODO: implement this later
    private final DirectoryStream<Path> stream;
    private Iterator<Path> it;

    public DirectorySpliterator(Path dir) throws IOException {
        this.stream = Files.newDirectoryStream(dir);
        this.it = stream.iterator();
    }

    @Override
    public boolean tryAdvance(Consumer<? super Path> action) {
        if (it.hasNext()) {
            action.accept(it.next());
            return true;
        }
        try {
            stream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    @Override
    public void forEachRemaining(Consumer<? super Path> action) {
    }

    @Override
    public Spliterator<Path> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        return Long.MAX_VALUE;
    }

    @Override
    public int characteristics() {
        return DISTINCT | NONNULL;
    }
}
