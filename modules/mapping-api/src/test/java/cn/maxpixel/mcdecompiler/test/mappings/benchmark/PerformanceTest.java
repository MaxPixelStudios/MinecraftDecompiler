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

import cn.maxpixel.mcdecompiler.mapping.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassifiedMapping;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormats;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.profile.JavaFlightRecorderProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@Fork(1)
@Threads(1)
@BenchmarkMode(Mode.AverageTime)
@State(Scope.Benchmark)
@Measurement(iterations = 10)
@Warmup(iterations = 5)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class PerformanceTest {
    private static final ClassifiedMapping<NamespacedMapping> tiny1 = MappingFormats.TINY_V2.read(PerformanceTest.class.getClassLoader().getResourceAsStream("1.17.1-v2.tiny"));
    private static final ClassifiedMapping<NamespacedMapping> tiny2 = MappingFormats.TINY_V2.read(PerformanceTest.class.getClassLoader().getResourceAsStream("1.17.1-v2.tiny"));

//    @Test
    public void benchmark() throws RunnerException {
        Options options = new OptionsBuilder()
                .include(PerformanceTest.class.getName())
                .addProfiler(JavaFlightRecorderProfiler.class, "-dir=jfr")
                .build();
        new Runner(options).run();
    }

    /*
     * Benchmark                              Mode  Cnt    Score   Error  Units
     * PerformanceTest.testEqualsTiny2_1      avgt   10  161.856 ± 5.299  ms/op
     * PerformanceTest.testEqualsTiny2_1:jfr  avgt           NaN            ---
     * PerformanceTest.testEqualsTiny2_1      avgt   10  162.691 ± 5.395  ms/op
     * PerformanceTest.testEqualsTiny2_1:jfr  avgt           NaN            ---
     * PerformanceTest.testEqualsTiny2_2      avgt   10  163.996 ± 3.845  ms/op
     * PerformanceTest.testEqualsTiny2_2:jfr  avgt           NaN            ---
     */

    @Benchmark
    public boolean testEqualsTiny2_1() {
        return tiny1.equals(tiny2);
    }
}