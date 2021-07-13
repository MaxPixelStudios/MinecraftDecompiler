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

import cn.maxpixel.mcdecompiler.reader.TsrgMappingReader;
import cn.maxpixel.mcdecompiler.writer.CsrgMappingWriter;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@Fork(1)
@Threads(1)
@BenchmarkMode(Mode.SingleShotTime)
@State(Scope.Benchmark)
@Measurement(iterations = 40)
@Warmup(iterations = 20)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class PerformanceTest {
    private TsrgMappingReader reader;
    private CsrgMappingWriter writer;

    @Setup
    public void init() throws Throwable {
        reader = new TsrgMappingReader(getClass().getClassLoader().getResourceAsStream("1.16.5.tsrg"));
        reader.getMappings();
    }

    @Setup(Level.Invocation)
    public void reset() {
        writer = new CsrgMappingWriter();
    }

    public void test() throws RunnerException {
        Options options = new OptionsBuilder()
                .include(PerformanceTest.class.getName())
                .build();
//        new Runner(options).run();
    }

    @Benchmark
    public void write() {
        writer.writeMappings(reader.getMappings());
    }
}