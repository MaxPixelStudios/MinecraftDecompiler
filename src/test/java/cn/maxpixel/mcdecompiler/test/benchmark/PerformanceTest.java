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

import cn.maxpixel.mcdecompiler.Info;
import cn.maxpixel.mcdecompiler.InfoProviders;
import cn.maxpixel.mcdecompiler.util.FileUtil;
import cn.maxpixel.mcdecompiler.util.Utils;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Stream;

/*
 * This class is for internal test
 */
@Fork(1)
@Threads(1)
@BenchmarkMode(Mode.SingleShotTime)
@State(Scope.Benchmark)
public class PerformanceTest {
    private static final Path f = InfoProviders.get().getTempPath().resolve("unmappedClasses");
    private static final Path t = InfoProviders.get().getTempPath().resolve("tar");
    public void test() throws RunnerException {
        Options options = new OptionsBuilder()
                .include(".*" + PerformanceTest.class.getSimpleName() + ".*")
                .shouldDoGC(true)
                .measurementIterations(1)
                .warmupIterations(0)
                .build();
        new Runner(options).run();
    }
//    @TearDown(Level.Iteration)
    public void setUp() {
        FileUtil.deleteDirectory(t);
    }
    @Benchmark
    public String newM() {
        return copyOthers(f, t);
    }
//    @Benchmark
    public String oldM() throws IOException {
        return copyOthersOld(f, t);
    }
    private String copyOthers(Path from, Path to) {
        try(Stream<Path> paths = Files.list(from).parallel().filter(p -> !p.toString().endsWith(".class") && !p.endsWith("net"));
            InputStream is = Files.newInputStream(from.resolve("META-INF").resolve("MANIFEST.MF"))) {
            paths.forEach(childPath -> {
                if(Files.isDirectory(childPath) && childPath.endsWith("com")) {
                    try(Stream<Path> s = Files.walk(childPath, 2);
                        Stream<Path> s1 = Files.list(childPath)) {
                        // Unmapped client only has com.mojang.blaze3d package
                        // If unmapped jar doesn't have this package, it will be treated as unmapped server
                        // In unmapped server jar, packages in com.mojang are its libraries, so we directly copy them
                        if(s.anyMatch(p -> p.endsWith("blaze3d")))
                            s1.filter(p -> !p.endsWith("mojang")).forEach(p -> FileUtil.copyDirectory(p, to.resolve("com")));
                        else FileUtil.copyDirectory(childPath, to);
                    } catch (IOException e) {
                        throw Utils.wrapInRuntime(e);
                    }
                } else FileUtil.copy(childPath, to);
            });

            Manifest man = new Manifest(is);
            return man.getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private String copyOthersOld(Path from, Path to) throws IOException {
        Files.list(from).forEach(childPath -> {
            if(Files.isRegularFile(childPath) && !childPath.toString().endsWith(".class")) {
                FileUtil.copyFile(childPath, to);
            } else if(Files.isDirectory(childPath) && !childPath.toAbsolutePath().toString().contains("net")
                    && !childPath.toAbsolutePath().toString().contains("com" + Info.FILE_SEPARATOR + "mojang")) {
                FileUtil.copyDirectory(childPath, to);
            }
        });
        try(InputStream is = Files.newInputStream(from.resolve("META-INF").resolve("MANIFEST.MF"))) {
            Manifest man = new Manifest(is);
            return man.getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
        }
    }
}