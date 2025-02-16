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

package cn.maxpixel.mcdecompiler.test.mapping.parchment;

import cn.maxpixel.mcdecompiler.api.extension.ExtensionManager;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormats;
import cn.maxpixel.mcdecompiler.mapping.parchment.FormatVersion;
import cn.maxpixel.mcdecompiler.mapping.parchment.ParchmentMappingFormat;
import cn.maxpixel.mcdecompiler.mapping.parchment.ParchmentMappingProcessor;
import cn.maxpixel.mcdecompiler.mapping.util.Utils;
import cn.maxpixel.rewh.logging.LogManager;
import cn.maxpixel.rewh.logging.Logger;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ParchmentFormatTest {
    private static final Logger LOGGER = LogManager.getLogger();

    @Test
    void testFormatVersion() {
        assertEquals(FormatVersion.from("1.1.0"), FormatVersion.CURRENT);
        assertTrue(FormatVersion.CURRENT.compatibleWith(FormatVersion.from("1.0.0")));
        assertTrue(FormatVersion.CURRENT.compatibleWith(FormatVersion.from("1.5.0")));
    }

    @Test
    void testFormatExtension() {
        ExtensionManager.init();
        assertSame(ParchmentMappingFormat.INSTANCE, MappingFormats.get(ParchmentMappingFormat.NAME));
    }

    @Test
    void testProcessorAndGenerator(@TempDir(cleanup = CleanupMode.ON_SUCCESS) Path tmp) throws IOException {
//        LOGGER.info(tmp.toString());
        var is = getClass().getClassLoader().getResourceAsStream("parchment.json");
        assertNotNull(is);
        var c1 = ParchmentMappingFormat.INSTANCE.read(is);
        Path path = tmp.resolve("parchment.json");
        try (var os = Files.newOutputStream(path)) {
            ParchmentMappingFormat.INSTANCE.write(c1, os);
        }
        try (var reader = Files.newBufferedReader(path)) {
            var c2 = ParchmentMappingFormat.INSTANCE.read(reader);
            assertEquals(c1, c2);
        }
        try (var reader = Files.newBufferedReader(path)) {
            ObjectArrayList<String> lines = reader.lines().collect(ObjectArrayList.toList());
            var c3 = ParchmentMappingProcessor.INSTANCE.process(lines);
            assertEquals(c1, c3);
        } catch (IOException e) {
            throw Utils.wrapInRuntime(e);
        }
    }
}