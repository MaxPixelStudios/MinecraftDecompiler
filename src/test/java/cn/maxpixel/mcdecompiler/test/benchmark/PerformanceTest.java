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
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@Fork(1)
@Threads(8)
@BenchmarkMode(Mode.SingleShotTime)
@State(Scope.Benchmark)
public class PerformanceTest {
    public void test() throws RunnerException {
        Options options = new OptionsBuilder()
                .include(".*" + PerformanceTest.class.getSimpleName() + ".*")
                .shouldDoGC(true)
                .timeUnit(TimeUnit.MICROSECONDS)
                .measurementIterations(50)
                .warmupIterations(6)
                .build();
//        new Runner(options).run();
    }

//    @Benchmark
//    public void javaNio(Blackhole bh) throws IOException {
//        bh.consume(Files.readAllBytes(p));
//    }

//    @Benchmark
//    public void javaBAO(Blackhole bh) throws IOException {
//        try (InputStream inputStream = Files.newInputStream(p);
//             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
//            byte[] data = new byte[4096];
//            int bytesRead;
//            while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
//                outputStream.write(data, 0, bytesRead);
//            }
//            outputStream.flush();
//            bh.consume(outputStream.toByteArray());
//        }
//    }
}