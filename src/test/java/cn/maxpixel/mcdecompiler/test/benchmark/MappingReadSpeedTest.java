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

import cn.maxpixel.mcdecompiler.reader.*;
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
public class MappingReadSpeedTest {
    public void test() throws RunnerException {
        Options options = new OptionsBuilder()
                .include(".*" + MappingReadSpeedTest.class.getSimpleName() + ".*")
                .shouldDoGC(true)
                .timeUnit(TimeUnit.MILLISECONDS)
                .measurementIterations(15)
                .warmupIterations(10)
                .build();
//        new Runner(options).run();
    }

    @Benchmark
    public void readSrg(Blackhole bh) {
        try(SrgMappingReader mappingReader = new SrgMappingReader(getClass().getClassLoader().getResourceAsStream("1.12.2.srg"))) {
            bh.consume(mappingReader.getMappings());
            bh.consume(mappingReader.getPackages());
        }
    }

    @Benchmark
    public void readCsrg(Blackhole bh) {
        try(CsrgMappingReader mappingReader = new CsrgMappingReader(getClass().getClassLoader().getResourceAsStream("1.12.2.csrg"))) {
            bh.consume(mappingReader.getMappings());
            bh.consume(mappingReader.getPackages());
        }
    }

    @Benchmark
    public void readTsrg(Blackhole bh) {
        try(TsrgMappingReader mappingReader = new TsrgMappingReader(getClass().getClassLoader().getResourceAsStream("1.16.5.tsrg"))) {
            bh.consume(mappingReader.getMappings());
            bh.consume(mappingReader.getPackages());
        }
    }

    @Benchmark
    public void readProguard(Blackhole bh) {
        try(ProguardMappingReader mappingReader = new ProguardMappingReader(getClass().getClassLoader().getResourceAsStream("1.16.5.txt"))) {
            bh.consume(mappingReader.getMappings());
        }
    }

    @Benchmark
    public void readTinyV1(Blackhole bh) {
        try(TinyMappingReader mappingReader = new TinyMappingReader(getClass().getClassLoader().getResourceAsStream("mappings-yarn-v1.tiny"))) {
            bh.consume(mappingReader.getMappings());
        }
    }

    @Benchmark
    public void readTinyV2(Blackhole bh) {
        try(TinyMappingReader mappingReader = new TinyMappingReader(getClass().getClassLoader().getResourceAsStream("mappings-merged-v2.tiny"))) {
            bh.consume(mappingReader.getMappings());
        }
    }
}
