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

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.profile.JavaFlightRecorderProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Fork(1)
@Threads(1)
@BenchmarkMode(Mode.AverageTime)
@State(Scope.Benchmark)
@Measurement(iterations = 10)
@Warmup(iterations = 5)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class PerformanceTest {
    private static final ObjectArrayList<String> srg = new BufferedReader(new InputStreamReader(
            PerformanceTest.class.getClassLoader().getResourceAsStream("1.17.1.srg"))).lines().map(s -> {
        if(s.startsWith("#") || s.isEmpty() || s.replaceAll("\\s+", "").isEmpty()) return null;

        int index = s.indexOf('#');
        if (index > 0) return s.substring(0, index);
        else if (index == 0) return null;

        return s;
    }).filter(Objects::nonNull).collect(ObjectArrayList.toList());

//    @Test
    public void benchmark() throws RunnerException {
        Options options = new OptionsBuilder()
                .include(PerformanceTest.class.getName())
                .addProfiler(JavaFlightRecorderProfiler.class, "-dir=jfr")
                .build();
        new Runner(options).run();
    }
    /*
     * Benchmark                             Mode  Cnt   Score   Error  Units
     * PerformanceTest.mappingSplit          avgt   10  16.566 ± 0.049  ms/op
     * PerformanceTest.mappingSplit2_16      avgt   10  13.610 ± 0.074  ms/op
     * PerformanceTest.mappingSplit2_8       avgt   10  14.290 ± 0.071  ms/op
     * PerformanceTest.mappingSplit3_16      avgt   10  13.744 ± 0.074  ms/op
     * PerformanceTest.mappingSplit3_8       avgt   10  13.262 ± 0.549  ms/op
     * PerformanceTest.stringSplit           avgt   10  14.718 ± 0.094  ms/op
     */
    /*
     * Benchmark                             Mode  Cnt   Score   Error  Units
     * PerformanceTest.mappingSplit          avgt   10  16.352 ± 0.154  ms/op
     * PerformanceTest.mappingSplit2_16      avgt   10  14.486 ± 0.384  ms/op
     * PerformanceTest.mappingSplit2_8       avgt   10  14.522 ± 0.107  ms/op
     * PerformanceTest.mappingSplit3_16      avgt   10  13.206 ± 0.046  ms/op
     * PerformanceTest.mappingSplit3_8       avgt   10  13.908 ± 0.312  ms/op
     * PerformanceTest.stringSplit           avgt   10  14.393 ± 0.157  ms/op
     */
    /*
     * Benchmark                             Mode  Cnt   Score   Error  Units
     * PerformanceTest.mappingSplit2_16      avgt   10  14.213 ± 0.057  ms/op
     * PerformanceTest.mappingSplit2_8       avgt   10  13.779 ± 0.078  ms/op
     */

    @Benchmark
    public void stringSplit(Blackhole bh) {
        for (String s : srg) {
            bh.consume(s.split(" "));
        }
    }

    @Benchmark
    public void mappingSplit(Blackhole bh) {
        for (String s : srg) {
            bh.consume(split(s, ' '));
        }
    }

    @Benchmark
    public void mappingSplit2_8(Blackhole bh) {
        for (String s : srg) {
            bh.consume(split2(s, ' ', 8));
        }
    }

    @Benchmark
    public void mappingSplit2_16(Blackhole bh) {
        for (String s : srg) {
            bh.consume(split2(s, ' ', 16));
        }
    }

    @Benchmark
    public void mappingSplit3_8(Blackhole bh) {
        for (String s : srg) {
            bh.consume(split3(s, ' ', 8));
        }
    }

    @Benchmark
    public void mappingSplit3_16(Blackhole bh) {
        for (String s : srg) {
            bh.consume(split3(s, ' ', 16));
        }
    }

    public static String[] split(String s, char c) {
        int n = 2;
        int i = s.indexOf(c);
        if (i == -1) return new String[] {s};
        while ((i = s.indexOf(c, i + 1)) != -1) n++;

        String[] ret = new String[n];
        int start = 0;
        for (int j = s.indexOf(c), p = 0; j != -1; j = s.indexOf(c, start)) {
            ret[p++] = s.substring(start, j);
            start = j + 1;
        }
        ret[n - 1] = s.substring(start);

        return ret;
    }

    public static String[] split2(String s, char c, int initLen) {
        int i = s.indexOf(c);
        if (i == -1) return new String[] {s};
        int[] indexes = new int[initLen];
        indexes[0] = i;
        int n = 0;
        while ((i = s.indexOf(c, i + 1)) != -1) {
            if (++n >= indexes.length) indexes = Arrays.copyOf(indexes, indexes.length << 1);
            indexes[n] = i;
        }

        String[] ret = new String[++n + 1];
        int start = 0;
        for (int j = 0; j < n; j++) {
            int index = indexes[j];
            ret[j] = s.substring(start, index);
            start = index + 1;
        }
        ret[n] = s.substring(start);

        return ret;
    }

    public static String[] split3(String s, char c, int initLen) {
        int i = s.indexOf(c);
        if (i == -1) return new String[] {s};

        String[] ret = new String[initLen];
        int start = 0;
        int p = 0;
        for (; i != -1; i = s.indexOf(c, start)) {
            ret[p++] = s.substring(start, i);
            if (p >= ret.length) ret = copyOf(ret, ret.length << 1);
            start = i + 1;
        }
        ret[p] = s.substring(start);

        return p == ret.length - 1 ? ret : copyOf(ret, p + 1);
    }

    private static String[] copyOf(String[] arr, int newLen) {
        String[] ret = new String[newLen];
        System.arraycopy(arr, 0, ret, 0, Math.min(newLen, arr.length));
        return ret;
    }
}