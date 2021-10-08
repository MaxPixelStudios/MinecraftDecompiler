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

import cn.maxpixel.mcdecompiler.reader.ClassifiedMappingReader;
import cn.maxpixel.mcdecompiler.reader.MappingProcessors;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@Fork(1)
@Threads(8)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 1)
@Measurement(iterations = 1)
@BenchmarkMode(Mode.SingleShotTime)
@State(Scope.Benchmark)
public class MappingReadSpeedTest {
    public void test() throws RunnerException {
        Options options = new OptionsBuilder()
                .include(".*" + MappingReadSpeedTest.class.getSimpleName() + ".*")
                .build();
        new Runner(options).run();
    }

//    @Benchmark
    public void readSrg(Blackhole bh) {
        bh.consume(new ClassifiedMappingReader<>(MappingProcessors.SRG, getClass().getClassLoader().getResourceAsStream("1.17.1.srg")));

    }

//    @Benchmark
    public void readCsrg(Blackhole bh) {
        bh.consume(new ClassifiedMappingReader<>(MappingProcessors.CSRG, getClass().getClassLoader().getResourceAsStream("1.17.1.csrg")));
    }

//    @Benchmark
    public void readTsrg(Blackhole bh) {
        bh.consume(new ClassifiedMappingReader<>(MappingProcessors.TSRG_V1, getClass().getClassLoader().getResourceAsStream("1.17.1.tsrg")));
    }

    @Benchmark
    public void readTsrgV2(Blackhole bh) {
        bh.consume(new ClassifiedMappingReader<>(MappingProcessors.TSRG_V2, getClass().getClassLoader().getResourceAsStream("1.17.1-v2.tsrg")));
    }

//    @Benchmark
    public void readProguard(Blackhole bh) {
        bh.consume(new ClassifiedMappingReader<>(MappingProcessors.PROGUARD, getClass().getClassLoader().getResourceAsStream("1.17.1.txt")));
    }

//    @Benchmark
    public void readTinyV1(Blackhole bh) {
        bh.consume(new ClassifiedMappingReader<>(MappingProcessors.TINY_V1, getClass().getClassLoader().getResourceAsStream("1.17.1.tiny")));
    }

//    @Benchmark
    public void readTinyV2(Blackhole bh) {
        bh.consume(new ClassifiedMappingReader<>(MappingProcessors.TINY_V2, getClass().getClassLoader().getResourceAsStream("1.17.1-v2.tiny")));

    }
}
