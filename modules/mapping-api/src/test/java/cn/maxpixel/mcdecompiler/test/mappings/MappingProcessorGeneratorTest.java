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

package cn.maxpixel.mcdecompiler.test.mappings;

import cn.maxpixel.mcdecompiler.common.app.util.FileUtil;
import cn.maxpixel.mcdecompiler.common.app.util.JarUtil;
import cn.maxpixel.mcdecompiler.mapping.Mapping;
import cn.maxpixel.mcdecompiler.mapping.collection.MappingCollection;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormat;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormats;
import cn.maxpixel.mcdecompiler.mapping.util.InputCollection;
import cn.maxpixel.mcdecompiler.mapping.util.OutputCollection;
import cn.maxpixel.mcdecompiler.utils.LambdaUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MappingProcessorGeneratorTest {
    @Test
    void testSrg(@TempDir(cleanup = CleanupMode.ON_SUCCESS) Path tmp) throws IOException {
        test(tmp, "1.17.1.srg", MappingFormats.SRG);
    }

    @Test
    void testCsrg(@TempDir(cleanup = CleanupMode.ON_SUCCESS) Path tmp) throws IOException {
        test(tmp, "1.17.1.csrg", MappingFormats.CSRG);
    }

    @Test
    void testTsrg(@TempDir(cleanup = CleanupMode.ON_SUCCESS) Path tmp) throws IOException {
        test(tmp, "1.17.1.tsrg", MappingFormats.TSRG_V1);
    }

    @Test
    void testTsrg2(@TempDir(cleanup = CleanupMode.ON_SUCCESS) Path tmp) throws IOException {
        test(tmp, "1.17.1-v2.tsrg", MappingFormats.TSRG_V2);
    }

    @Test
    void testProguard(@TempDir(cleanup = CleanupMode.ON_SUCCESS) Path tmp) throws IOException {
        test(tmp, "1.17.1.txt", MappingFormats.PROGUARD);
    }

    @Test
    void testTiny1(@TempDir(cleanup = CleanupMode.ON_SUCCESS) Path tmp) throws IOException {
        test(tmp, "1.17.1.tiny", MappingFormats.TINY_V1);
    }

    @Test
    void testTiny2(@TempDir(cleanup = CleanupMode.ON_SUCCESS) Path tmp) throws IOException {
        test(tmp, "1.17.1-v2.tiny", MappingFormats.TINY_V2);
    }

    @Test
    void testPDME(@TempDir(cleanup = CleanupMode.ON_SUCCESS) Path tmp) throws IOException {
        test(tmp, "1.17.1.pdme", MappingFormats.PDME);
    }

    @Test
    void testMCP(@TempDir(cleanup = CleanupMode.ON_SUCCESS) Path tmp) throws IOException, URISyntaxException {
        testZip(tmp, "mcp_stable-12-1.7.10.zip", MappingFormats.MCP);
    }

    private static <M extends Mapping, C extends MappingCollection<M>> void test(Path tmp, String fileName, MappingFormat<M, C> format) throws IOException {
        var is = MappingProcessorGeneratorTest.class.getClassLoader().getResourceAsStream(fileName);
        assertNotNull(is);
        var c1 = format.read(is);
        Path path = tmp.resolve(fileName);
        try (var os = Files.newOutputStream(path)) {
            format.write(c1, os);
        }
        try (var reader = Files.newBufferedReader(path)) {
            var c2 = format.read(reader);
            assertEquals(c1, c2);
        }
    }

    private static <M extends Mapping, C extends MappingCollection<M>> void testZip(Path tmp, String fileName, MappingFormat<M, C> format) throws IOException, URISyntaxException {
        var zipURL = MappingProcessorGeneratorTest.class.getClassLoader().getResource(fileName);
        assertNotNull(zipURL);
        try (var in = JarUtil.createZipFs(Path.of(zipURL.toURI()));
             var out = JarUtil.createZipFs(tmp.resolve(fileName), true);
             var s1 = FileUtil.iterateFiles(in.getPath(""))) {
            var paths = s1.toList();
            var c1 = format.read(InputCollection.ofLazy(paths.iterator(), LambdaUtil.unwrap(p ->
                    new InputCollection.Entry(Files.newBufferedReader(p), p.toString()))
            ));
//            format.write(c1, OutputCollection.of(c -> {
//                for (var p : paths) {
//                    String name = p.toString();
//                    c.putNamedOutput(new OutputCollection.Entry(Files.newBufferedWriter(
//                            FileUtil.makeParentDirs(out.getPath(name))), name));
//                }
//            }));
            format.write(c1, OutputCollection.ofLazy(LambdaUtil.unwrap(n -> new OutputCollection.Entry(
                    Files.newBufferedWriter(FileUtil.makeParentDirs(out.getPath(n))), n))));
            try (var s2 = FileUtil.iterateFiles(out.getPath(""))) {
                var c2 = format.read(InputCollection.ofLazy(s2.iterator(), LambdaUtil.unwrap(p ->
                        new InputCollection.Entry(Files.newBufferedReader(p), p.toString()))
                ));
                assertEquals(c1, c2);
            }
        }
    }
}