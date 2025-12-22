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

package cn.maxpixel.mcdecompiler.mapping.generator;

import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.UniqueMapping;
import cn.maxpixel.mcdecompiler.mapping.component.Documented;
import cn.maxpixel.mcdecompiler.mapping.component.SideSpecific;
import cn.maxpixel.mcdecompiler.mapping.format.MCPMappingFormat;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormat;
import cn.maxpixel.mcdecompiler.mapping.remapper.MappingRemapper;
import cn.maxpixel.mcdecompiler.mapping.util.OutputCollection;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Writer;

public enum MCPMappingGenerator implements MappingGenerator.Unique<PairedMapping> {
    INSTANCE;
    private static final String MEMBER_HEADER = "searge,name,side,desc\n";
    private static final String PARAM_HEADER = "param,name,side\n";

    @Override
    public MappingFormat<PairedMapping, UniqueMapping<PairedMapping>> getFormat() {
        return MCPMappingFormat.INSTANCE;
    }

    @Override
    public ObjectList<String> generate(UniqueMapping<PairedMapping> mappings, @Nullable MappingRemapper remapper) {
        throw new IllegalStateException("Call generateAndWrite");
    }

    @Override
    public void generateAndWrite(UniqueMapping<PairedMapping> mappings, OutputCollection out, @Nullable MappingRemapper remapper) throws IOException {
        try (var mw = out.getOutput(MCPMappingFormat.METHODS_CSV)) {
            if (mw != null) {
                mw.write(MEMBER_HEADER);
                writeMember(mw, mappings.methods);
            }
        }
        try (var fw = out.getOutput(MCPMappingFormat.FIELDS_CSV)) {
            if (fw != null) {
                fw.write(MEMBER_HEADER);
                writeMember(fw, mappings.fields);
            }
        }
        try (var pw = out.getOutput(MCPMappingFormat.PARAMS_CSV)) {
            if (pw != null) {
                pw.write(PARAM_HEADER);
                for (PairedMapping member : mappings.params) {
                    pw.append(member.unmappedName).append(',')
                            .append(member.mappedName).append(',')
                            .append(String.valueOf(member.getComponentOptional(SideSpecific.class)
                                    .orElse(SideSpecific.BOTH).ordinal())).append('\n');
                }
            }
        }
    }

    private void writeMember(Writer w, ObjectArrayList<PairedMapping> list) throws IOException {
        for (PairedMapping member : list) {
            String comment = member.getComponentOptional(Documented.class).map(d ->
                    d.contents.isEmpty() ? null : d.contents.get(0)).orElse("");
            if (comment.contains(",") || comment.contains("\"")) {
                comment = '"' + comment.replace("\"", "\"\"") + '"';
            }
            w.append(member.unmappedName).append(',')
                    .append(member.mappedName).append(',')
                    .append(String.valueOf(member.getComponentOptional(SideSpecific.class)
                            .orElse(SideSpecific.BOTH).ordinal())).append(',')
                    .append(comment).append('\n');
        }
    }
}