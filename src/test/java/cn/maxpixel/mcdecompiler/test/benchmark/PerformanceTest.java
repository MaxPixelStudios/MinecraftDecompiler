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

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/*
 * This class is for internal test
 */
@Fork(1)
@Threads(8)
@BenchmarkMode(Mode.SingleShotTime)
@State(Scope.Benchmark)
public class PerformanceTest {
    private Path temp;
    private Path f;
    private Path t;
    @Setup(Level.Trial)
    public void setUp() {
        temp = Paths.get("temp");
        f = Paths.get("temp").resolve("f/a/b/d/d/ds/dfsg/g/fd");
        t = Paths.get("temp").resolve("t/e/s/a/a/q/q/f/sa/a");
    }
    public void test() throws RunnerException {
        Options options = new OptionsBuilder()
                .include(".*" + PerformanceTest.class.getSimpleName() + ".*")
                .shouldDoGC(true)
                .timeUnit(TimeUnit.MILLISECONDS)
                .measurementIterations(10)
                .warmupIterations(3)
                .build();
        new Runner(options).run();
    }
    @Benchmark
    public void a(Blackhole blackhole) {
        for(int i = 0; i < 5000; i++) {
            blackhole.consume(temp.relativize(f));
            blackhole.consume(temp.relativize(t));
        }
    }
}