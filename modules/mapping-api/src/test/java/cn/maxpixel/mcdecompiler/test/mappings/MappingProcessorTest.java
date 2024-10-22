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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassifiedMapping;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormats;

public class MappingProcessorTest {
    private static InputStream getResourceAsStream(String name) {
        return MappingProcessorTest.class.getClassLoader().getResourceAsStream(name);
    }

    public void testSrg() {
        MappingFormats.SRG.read(getResourceAsStream("1.17.1.srg"));
    }

    public void testCsrg() {
        MappingFormats.CSRG.read(getResourceAsStream("1.17.1.csrg"));
    }

    public void testTsrg() {
        MappingFormats.TSRG_V1.read(getResourceAsStream("1.17.1.tsrg"));
    }

    public void testTsrg2() {
        MappingFormats.TSRG_V2.read(getResourceAsStream("1.17.1-v2.tsrg"));
    }

    public void testProguard() {
        MappingFormats.PROGUARD.read(getResourceAsStream("1.17.1.txt"));
    }

    public void testTiny1() {
        MappingFormats.TINY_V1.read(getResourceAsStream("1.17.1.tiny"));
    }

    public void testTiny2() {
        MappingFormats.TINY_V2.read(getResourceAsStream("1.17.1-v2.tiny"));
    }
    
    public void testPDME() {
    	MappingFormats.PDME.read(getResourceAsStream("1.17.1.pdme"));
    }
}
