/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile jars.
 * Copyright (C) 2019-2025 MaxPixelStudios(XiaoPangxie732)
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

package cn.maxpixel.mcdecompiler.mapping.processor;

import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.UniqueMapping;
import cn.maxpixel.mcdecompiler.mapping.component.Documented;
import cn.maxpixel.mcdecompiler.mapping.component.SideSpecific;
import cn.maxpixel.mcdecompiler.mapping.format.MCPMappingFormat;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormat;
import cn.maxpixel.mcdecompiler.mapping.util.ContentList;
import cn.maxpixel.mcdecompiler.mapping.util.MappingUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;

public enum MCPMappingProcessor implements MappingProcessor.Unique<PairedMapping> {
    INSTANCE;
    private static final String[] MEMBER_HEADER = new String[] {"searge", "name", "side", "desc"};
    private static final String[] PARAM_HEADER = new String[] {"param", "name", "side"};

    @Override
    public MappingFormat<PairedMapping, UniqueMapping<PairedMapping>> getFormat() {
        return MCPMappingFormat.INSTANCE;
    }

    @Override
    public UniqueMapping<PairedMapping> process(ContentList contents) throws IOException {
        UniqueMapping<PairedMapping> ret = new UniqueMapping<>();
        for (var content : contents) {
            try (var reader = new CSVReader(content.asBufferedReader())) {
                String name = content.name();
                if (name == null) error();
                if (Arrays.equals(reader.header, MEMBER_HEADER)) {
                    String[] row;
                    ObjectArrayList<PairedMapping> list = name.endsWith(MCPMappingFormat.METHODS_CSV) ? ret.methods :
                            name.endsWith(MCPMappingFormat.FIELDS_CSV) ? ret.fields : error();
                    while ((row = reader.readRow()) != null) list.add(create(row[0], row[1], row[2], row[3]));
                } else if (Arrays.equals(reader.header, PARAM_HEADER) && name.endsWith(MCPMappingFormat.PARAMS_CSV)) {
                    String[] row;
                    while ((row = reader.readRow()) != null) ret.params.add(create(row[0], row[1], row[2], null));
                } else error();
            }
        }
        return ret;
    }

    private static PairedMapping create(String unmapped, String mapped, String side, String desc) {
        SideSpecific ss = switch (side) {
            case "0" -> SideSpecific.CLIENT;
            case "1" -> SideSpecific.SERVER;
            case "2" -> SideSpecific.BOTH;
            default -> error();
        };
        if (desc != null) {
            var doc = new Documented();
            doc.contents.add(desc);
            return new PairedMapping(unmapped, mapped, ss, doc);
        }
        return new PairedMapping(unmapped, mapped, ss);
    }

    private static <T> T error() {
        throw new IllegalArgumentException("Is this MCP mapping format?");
    }

    /*
     * Not a standard CSV reader
     * Assumptions:
     * 1. There's a header and there are no " or line breaks in the header
     * 2. There are no line breaks in a column
     * 3. " only exists in the last column of a row
     */
    private static class CSVReader implements Closeable {
        public final String[] header;
        private final int columnCount;
        private final BufferedReader in;

        public CSVReader(@NotNull BufferedReader in) throws IOException {
            this.in = in;
            this.header = MappingUtils.split(in.readLine(), ',');
            this.columnCount = header.length;
        }

        public String[] readRow() throws IOException {
            String s = in.readLine();
            if (MappingUtils.isStringNotBlank(s)) {
                String[] ret = MappingUtils.splitExact(s, ',', columnCount);
                String lastColumn = ret[columnCount - 1];
                int i = lastColumn.indexOf('"');
                if (i < 0) return ret;
                lastColumn = lastColumn.substring(1, lastColumn.length() - 1);
                i = lastColumn.indexOf('"');
                if (i < 0) {
                    ret[columnCount - 1] = lastColumn;
                    return ret;
                }
                StringBuilder sb = new StringBuilder().append(lastColumn);
                for (int j = i; j >= 0; j = sb.indexOf("\"", j)) {
                    if (sb.charAt(++j) != '"') error();
                    sb.deleteCharAt(j);
                }
                ret[columnCount - 1] = sb.toString();
                return ret;
            }
            return null;
        }

        @Override
        public void close() throws IOException {
            in.close();
        }
    }
}