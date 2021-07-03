/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile Minecraft.
 * Copyright (C) 2019-2021  MaxPixelStudios
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cn.maxpixel.mcdecompiler.test.benchmark;

import cn.maxpixel.mcdecompiler.util.FileUtil;
import cn.maxpixel.mcdecompiler.util.JarUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Fork(1)
@Threads(1)
@BenchmarkMode(Mode.SingleShotTime)
@State(Scope.Benchmark)
@Measurement(iterations = 40)
@Warmup(iterations = 20)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class PerformanceTest {
    public void test() throws RunnerException {
        Options options = new OptionsBuilder()
                .include(".*" + PerformanceTest.class.getSimpleName() + ".*")
                .build();
//        new Runner(options).run();
    }

    private static final FileSystem fs;

    static {
        try {
            fs = JarUtil.getJarFileSystemProvider().newFileSystem(Path.of("D:\\intelliJ_idea-workspace\\MinecraftDecompiler\\downloads\\1.17.1-pre3", "client.jar"), Object2ObjectMaps.emptyMap());
        } catch (IOException e) {
            throw new ExceptionInInitializerError();
        }
    }

    @Benchmark
    public void walk(Blackhole bh) throws IOException {
        try(Stream<Path> stream = Files.walk(fs.getPath("/")).filter(Files::isRegularFile).parallel()) {
            stream.forEach(bh::consume);
        }
    }

    @Benchmark
    public void iter(Blackhole bh) {
        try(Stream<Path> stream = FileUtil.iterateFiles(fs.getPath("/"))) {
            stream.forEach(bh::consume);
        }
    }
}