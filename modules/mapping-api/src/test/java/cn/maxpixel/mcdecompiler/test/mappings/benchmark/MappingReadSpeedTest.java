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

package cn.maxpixel.mcdecompiler.test.mappings.benchmark;

import cn.maxpixel.mcdecompiler.mapping.format.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.profile.JavaFlightRecorderProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.io.StringReader;
import java.util.concurrent.TimeUnit;

@Fork(1)
@Threads(1)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3)
@Measurement(iterations = 10)
@BenchmarkMode(Mode.AverageTime)
@State(Scope.Benchmark)
public class MappingReadSpeedTest {
    private static final String srg = read("1.17.1.srg");
    private static final String csrg = read("1.17.1.csrg");
    private static final String tsrg = read("1.17.1.tsrg");
    private static final String tsrg2 = read("1.17.1-v2.tsrg");
    private static final String proguard = read("1.17.1.txt");
    private static final String tiny1 = read("1.17.1.tiny");
    private static final String tiny2 = read("1.17.1-v2.tiny");

    private static String read(String n) {
        try (var is = MappingReadSpeedTest.class.getClassLoader().getResourceAsStream(n)) {
            return new String(is.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//    @Test
    public void test() throws RunnerException {
        Options options = new OptionsBuilder()
                .include(MappingReadSpeedTest.class.getSimpleName())
                .addProfiler(JavaFlightRecorderProfiler.class, "-dir=jfr")
                .build();
        new Runner(options).run();
    }

    @Benchmark
    public void readSrg(Blackhole bh) {
        bh.consume(SrgMappingFormat.INSTANCE.read(new StringReader(srg)));
    }

    @Benchmark
    public void readCsrg(Blackhole bh) {
        bh.consume(CsrgMappingFormat.INSTANCE.read(new StringReader(csrg)));
    }

    @Benchmark
    public void readTsrg(Blackhole bh) {
        bh.consume(TsrgV1MappingFormat.INSTANCE.read(new StringReader(tsrg)));
    }

    @Benchmark
    public void readTsrgV2(Blackhole bh) {
        bh.consume(TsrgV2MappingFormat.INSTANCE.read(new StringReader(tsrg2)));
    }

    @Benchmark
    public void readProguard(Blackhole bh) {
        bh.consume(ProguardMappingFormat.INSTANCE.read(new StringReader(proguard)));
    }

    @Benchmark
    public void readTinyV1(Blackhole bh) {
        bh.consume(TinyV1MappingFormat.INSTANCE.read(new StringReader(tiny1)));
    }

    @Benchmark
    public void readTinyV2(Blackhole bh) {
        bh.consume(TinyV2MappingFormat.INSTANCE.read(new StringReader(tiny2)));
    }
}
