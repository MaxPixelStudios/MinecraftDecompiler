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

import cn.maxpixel.mcdecompiler.asm.MappingRemapper;
import cn.maxpixel.mcdecompiler.mapping.paired.PairedClassMapping;
import cn.maxpixel.mcdecompiler.reader.ProguardMappingReader;
import cn.maxpixel.mcdecompiler.writer.CsrgMappingWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;

public class FunctionTest {
    private static final Logger LOGGER = LogManager.getLogger();

    public void test() throws Throwable {
        ProguardMappingReader reader = new ProguardMappingReader(getClass().getClassLoader().getResourceAsStream("1.17.1.txt"));
        CsrgMappingWriter writer = new CsrgMappingWriter(new MappingRemapper(reader));
        writer.writePairedMappings((Collection<PairedClassMapping>) reader.getMappings());
//        writer.writePairedMappings((Collection<NamespacedClassMapping>) reader.getMappings(), "obf", "srg");
//        try(FileChannel ch = FileChannel.open(Path.of("1.17.1.csrg"), WRITE, CREATE, TRUNCATE_EXISTING)) {
//            writer.writeTo(ch);
//        }
    }
}