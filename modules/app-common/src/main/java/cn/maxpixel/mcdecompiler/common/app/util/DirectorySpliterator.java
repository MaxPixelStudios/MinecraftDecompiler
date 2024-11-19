/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile jars.
 * Copyright (C) 2019-2024 MaxPixelStudios(XiaoPangxie732)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
