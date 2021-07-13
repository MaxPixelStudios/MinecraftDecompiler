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

package cn.maxpixel.mcdecompiler.test;

import cn.maxpixel.mcdecompiler.reader.TsrgMappingReader;
import cn.maxpixel.mcdecompiler.writer.CsrgMappingWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FunctionTest {
    private static final Logger LOGGER = LogManager.getLogger();
    private TsrgMappingReader reader;

    public void setUp() throws Throwable {
        reader = new TsrgMappingReader(getClass().getClassLoader().getResourceAsStream("1.16.5.tsrg"));
        reader.getMappings();
    }

    public void test() throws Throwable {
        CsrgMappingWriter writer = new CsrgMappingWriter();
        writer.writeMappings(reader.getMappings());
//        try(FileChannel ch = FileChannel.open(Path.of("1.16.5.csrg"), WRITE, CREATE)) {
//            writer.writeTo(ch);
//        }
    }
}