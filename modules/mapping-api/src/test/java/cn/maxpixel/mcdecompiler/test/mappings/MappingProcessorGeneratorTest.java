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

import cn.maxpixel.mcdecompiler.mapping.Mapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassifiedMapping;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormat;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormats;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
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

    private static <M extends Mapping, C extends ClassifiedMapping<M>> void test(Path tmp, String fileName, MappingFormat<M, C> format) throws IOException {
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
}