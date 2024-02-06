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

package cn.maxpixel.mcdecompiler.test.benchmark;

import cn.maxpixel.mcdecompiler.reader.MappingProcessors;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Fork(1)
@Threads(1)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3)
@Measurement(iterations = 10)
@BenchmarkMode(Mode.AverageTime)
@State(Scope.Benchmark)
public class MappingReadSpeed {
    private static final ObjectArrayList<String> srg = read("1.17.1.srg");
    private static final ObjectArrayList<String> csrg = read("1.17.1.csrg");
    private static final ObjectArrayList<String> tsrg = read("1.17.1.tsrg");
    private static final ObjectArrayList<String> tsrg2 = read("1.17.1-v2.tsrg");
    private static final ObjectArrayList<String> proguard = read("1.17.1.txt");
    private static final ObjectArrayList<String> tiny1 = read("1.17.1.tiny");
    private static final ObjectArrayList<String> tiny2 = read("1.17.1-v2.tiny");

    private static ObjectArrayList<String> read(String n) {
        return new BufferedReader(new InputStreamReader(MappingReadSpeed.class.getClassLoader().getResourceAsStream(n))).lines().map(s -> {
            if(s.startsWith("#") || s.isEmpty() || s.replaceAll("\\s+", "").isEmpty()) return null;

            int index = s.indexOf('#');
            if(index > 0) return s.substring(0, index);
            else if(index == 0) return null;

            return s;
        }).filter(Objects::nonNull).collect(ObjectArrayList.toList());
    }

    @Benchmark
    public void readSrg(Blackhole bh) {
        bh.consume(MappingProcessors.SRG.process(srg));
    }

    @Benchmark
    public void readCsrg(Blackhole bh) {
        bh.consume(MappingProcessors.CSRG.process(csrg));
    }

    @Benchmark
    public void readTsrg(Blackhole bh) {
        bh.consume(MappingProcessors.TSRG_V1.process(tsrg));
    }

    @Benchmark
    public void readTsrgV2(Blackhole bh) {
        bh.consume(MappingProcessors.TSRG_V2.process(tsrg2));
    }

    @Benchmark
    public void readProguard(Blackhole bh) {
        bh.consume(MappingProcessors.PROGUARD.process(proguard));
    }

    @Benchmark
    public void readTinyV1(Blackhole bh) {
        bh.consume(MappingProcessors.TINY_V1.process(tiny1));
    }

    @Benchmark
    public void readTinyV2(Blackhole bh) {
        bh.consume(MappingProcessors.TINY_V2.process(tiny2));
    }
}
