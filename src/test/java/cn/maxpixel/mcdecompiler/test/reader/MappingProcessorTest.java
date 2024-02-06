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

package cn.maxpixel.mcdecompiler.test.reader;

import cn.maxpixel.mcdecompiler.mapping.type.MappingTypes;
import cn.maxpixel.mcdecompiler.reader.ClassifiedMappingReader;

public class MappingProcessorTest {
    public void testSrg() {
        new ClassifiedMappingReader<>(MappingTypes.SRG, getClass().getClassLoader().getResourceAsStream("1.17.1.srg"));
    }

    public void testCsrg() {
        new ClassifiedMappingReader<>(MappingTypes.CSRG, getClass().getClassLoader().getResourceAsStream("1.17.1.csrg"));
    }

    public void testTsrg() {
        new ClassifiedMappingReader<>(MappingTypes.TSRG_V1, getClass().getClassLoader().getResourceAsStream("1.17.1.tsrg"));
    }

    public void testTsrg2() {
        new ClassifiedMappingReader<>(MappingTypes.TSRG_V2, getClass().getClassLoader().getResourceAsStream("1.17.1-v2.tsrg"));
    }

    public void testProguard() {
        new ClassifiedMappingReader<>(MappingTypes.PROGUARD, getClass().getClassLoader().getResourceAsStream("1.17.1.txt"));
    }

    public void testTiny1() {
        new ClassifiedMappingReader<>(MappingTypes.TINY_V1, getClass().getClassLoader().getResourceAsStream("1.17.1.tiny"));
    }

    public void testTiny2() {
        new ClassifiedMappingReader<>(MappingTypes.TINY_V2, getClass().getClassLoader().getResourceAsStream("1.17.1-v2.tiny"));
    }
}