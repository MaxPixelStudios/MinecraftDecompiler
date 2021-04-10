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

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Fork(1)
@Threads(1)
@BenchmarkMode(Mode.SingleShotTime)
@State(Scope.Benchmark)
@Measurement(iterations = 100)
@Warmup(iterations = 50)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class PerformanceTest {
    private static final int size = 53;
    private static final ObjectArrayList<String> libs = new ObjectArrayList<>(size);
    private static final ObjectArrayList<String> args = new ObjectArrayList<>(size + 9);

    @Setup
    public void setup() throws Throwable {
        Stream.generate(() -> UUID.randomUUID().toString()).limit(size).forEach(libs::add);
        args.addElements(0, new String[] { "java", "-jar", "awawawawawawa", "-rsy=1", "-dgs=1", "-asc=1", "-bsm=1", "-iec=1", "-log=TRACE" });
    }

    public void test() throws RunnerException {
        Options options = new OptionsBuilder()
                .include(".*" + PerformanceTest.class.getSimpleName() + ".*")
                .build();
//        new Runner(options).run();
    }

    @Benchmark
    public void language(Blackhole bh) {
        ObjectArrayList<String> args = PerformanceTest.args.clone();
        for(int i = 0; i < libs.size(); i++) args.add("-e=" + libs.get(i));
        bh.consume(args);
    }

    @Benchmark
    public void languageConcat(Blackhole bh) {
        ObjectArrayList<String> args = PerformanceTest.args.clone();
        for(int i = 0; i < libs.size(); i++) args.add("-e=".concat(libs.get(i)));
        bh.consume(args);
    }

    @Benchmark
    public void languageEnhanced(Blackhole bh) {
        ObjectArrayList<String> args = PerformanceTest.args.clone();
        for (String lib : libs) args.add("-e=" + lib);
        bh.consume(args);
    }


    @Benchmark
    public void languageEnhancedConcat(Blackhole bh) {
        ObjectArrayList<String> args = PerformanceTest.args.clone();
        for (String lib : libs) args.add("-e=".concat(lib));
        bh.consume(args);
    }

    @Benchmark
    public void method(Blackhole bh) {
        ObjectArrayList<String> args = PerformanceTest.args.clone();
        libs.forEach(lib -> args.add("-e=" + lib));
        bh.consume(args);
    }

    @Benchmark
    public void methodConcat(Blackhole bh) {
        ObjectArrayList<String> args = PerformanceTest.args.clone();
        libs.forEach(lib -> args.add("-e=".concat(lib)));
        bh.consume(args);
    }

    @Benchmark
    public void stream(Blackhole bh) {
        ObjectArrayList<String> args = PerformanceTest.args.clone();
        libs.stream().map(lib -> "-e=" + lib).forEach(args::add);
        bh.consume(args);
    }

    @Benchmark
    public void streamConcat(Blackhole bh) {
        ObjectArrayList<String> args = PerformanceTest.args.clone();
        libs.stream().map("-e="::concat).forEach(args::add);
        bh.consume(args);
    }
}