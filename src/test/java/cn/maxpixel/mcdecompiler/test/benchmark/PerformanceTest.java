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

import cn.maxpixel.mcdecompiler.InfoProviders;
import cn.maxpixel.mcdecompiler.util.Utils;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectBigArrayBigList;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/*
 * This class is for internal test
 */
@Fork(1)
@Threads(8)
@BenchmarkMode(Mode.SingleShotTime)
@State(Scope.Benchmark)
public class PerformanceTest {
    private static final Path f = InfoProviders.get().getTempUnmappedClassesPath();
    private static final Path t = InfoProviders.get().getTempPath().resolve("tar");
    public void test() throws RunnerException {
        Options options = new OptionsBuilder()
                .include(".*" + PerformanceTest.class.getSimpleName() + ".*")
                .shouldDoGC(true)
                .measurementIterations(5)
                .warmupIterations(3)
                .build();
//        new Runner(options).run();
    }
    public void setUp() {
    }
    private void fill(Map<String, String> map) {
        Stream.generate(() -> 1).limit((long) Math.pow(2, 16)).parallel().forEach(i -> {
            try {
                byte[] bytes = new byte[2048];
                Random r = new Random();
                r.nextBytes(bytes);
                byte[] out = MessageDigest.getInstance("SHA-512").digest(bytes);
                StringBuilder builder = new StringBuilder();
                for(byte b : out) {
                    String s = Integer.toHexString(b);
                    if(s.length() == 1) s = s.concat("0");
                    builder.append(s);
                }
                map.put(builder.toString(), Base64.getEncoder().encodeToString(out));
            } catch (Throwable e) {
                throw Utils.wrapInRuntime(e);
            }
        });
    }
    private void consume(Map<String, String> map, Blackhole blackhole) {
        ObjectBigArrayBigList<String> list = new ObjectBigArrayBigList<>();
        map.forEach((k, v) -> list.add(k.concat("_").concat(v)));
        blackhole.consume(list);
    }
    @Benchmark
    public void a(Blackhole blackhole) throws Throwable {
        Object2ObjectMap<String, String> map = Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<>());
        CompletableFuture<Void> task = CompletableFuture.allOf(CompletableFuture.runAsync(() -> fill(map)),
                CompletableFuture.runAsync(() -> consume(map, blackhole)));
        blackhole.consume(task.join());
    }
    @Benchmark
    public void b(Blackhole blackhole) throws Throwable {
        Map<String, String> map = new ConcurrentHashMap<>();
        CompletableFuture<Void> task = CompletableFuture.allOf(CompletableFuture.runAsync(() -> fill(map)),
                CompletableFuture.runAsync(() -> consume(map, blackhole)));
        blackhole.consume(task.join());
    }
    @Benchmark
    public void c(Blackhole blackhole) throws Throwable {
        Map<String, String> map = Collections.synchronizedMap(new Object2ObjectOpenHashMap<>());
        CompletableFuture<Void> task = CompletableFuture.allOf(CompletableFuture.runAsync(() -> fill(map)),
                CompletableFuture.runAsync(() -> consume(map, blackhole)));
        blackhole.consume(task.join());
    }
    @Benchmark
    public void d(Blackhole blackhole) throws Throwable {
        Map<String, String> map = Collections.synchronizedMap(new HashMap<>());
        CompletableFuture<Void> task = CompletableFuture.allOf(CompletableFuture.runAsync(() -> fill(map)),
                CompletableFuture.runAsync(() -> consume(map, blackhole)));
        blackhole.consume(task.join());
    }
}