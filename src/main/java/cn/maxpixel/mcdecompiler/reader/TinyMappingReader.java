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

package cn.maxpixel.mcdecompiler.reader;

import cn.maxpixel.mcdecompiler.mapping.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.base.BaseFieldMapping;
import cn.maxpixel.mcdecompiler.mapping.base.BaseMethodMapping;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.apache.logging.log4j.LogManager;

import java.io.*;
import java.util.stream.Stream;

public class TinyMappingReader extends AbstractMappingReader {
    public TinyMappingReader(BufferedReader reader) {
        super(reader);
    }
    public TinyMappingReader(Reader rd) {
        super(rd);
    }
    public TinyMappingReader(InputStream is) {
        super(is);
    }
    public TinyMappingReader(String path) throws FileNotFoundException, NullPointerException {
        super(path);
    }
    private static final V1TinyMappingProcessor V1_PROCESSOR = new V1TinyMappingProcessor();
    private static final V2TinyMappingProcessor V2_PROCESSOR = new V2TinyMappingProcessor();
    @Override
    protected AbstractNonPackageMappingProcessor getProcessor() {
        try {
            reader.mark(50);
            String s = reader.readLine();
            reader.reset();
            if(s.startsWith("tiny\t2\t0\t")) return V2_PROCESSOR;
            else if(s.startsWith("v1\t")) return V1_PROCESSOR;
        } catch(IOException e) {
            LogManager.getLogger().catching(e);
        }
        return V2_PROCESSOR;
    }

    public static class V1TinyMappingProcessor extends AbstractNonPackageMappingProcessor {
        @Override
        public ObjectList<ClassMapping> process(Stream<String> lines) {
            return null;
        }
        @Override
        protected ClassMapping processClass(String line) {
            return null;
        }
        @Override
        protected BaseMethodMapping processMethod(String line) {
            return null;
        }
        @Override
        protected BaseFieldMapping processField(String line) {
            return null;
        }
    }
    public static class V2TinyMappingProcessor extends AbstractNonPackageMappingProcessor {
        @Override
        public ObjectList<ClassMapping> process(Stream<String> lines) {
            return null;
        }
        @Override
        protected ClassMapping processClass(String line) {
            return null;
        }
        @Override
        protected BaseMethodMapping processMethod(String line) {
            return null;
        }
        @Override
        protected BaseFieldMapping processField(String line) {
            return null;
        }
    }
}