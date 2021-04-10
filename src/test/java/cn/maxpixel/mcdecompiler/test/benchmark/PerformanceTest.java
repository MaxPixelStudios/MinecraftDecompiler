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

import org.objectweb.asm.Opcodes;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@Fork(1)
@Threads(8)
@BenchmarkMode(Mode.SingleShotTime)
@State(Scope.Benchmark)
public class PerformanceTest {
    private static final int i = 318226432;
    private static final String s = new String("(Ljava/lang/String;I");

//    public void regen() {
//        i = new Random().nextInt(2) == 0 ? 318226432 : new Random().nextInt(10000);
//        s = new Random().nextInt(2) == 0 ? "(Ljava/lang/String;I" : UUID.randomUUID().toString();
//    }

    public void test() throws RunnerException {
        Options options = new OptionsBuilder()
                .include(".*" + PerformanceTest.class.getSimpleName() + ".*")
                .timeUnit(TimeUnit.MICROSECONDS)
                .measurementIterations(2000)
                .warmupIterations(10000)
                .build();
//        new Runner(options).run();
    }

    @Benchmark
    public void calculate(Blackhole bh) {
        bh.consume((i & Opcodes.ACC_ENUM) != 0);
    }

    @Benchmark
    public void equal(Blackhole bh) {
        bh.consume(s.equals("(Ljava/lang/String;I"));
    }
}