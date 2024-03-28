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

package cn.maxpixel.mcdecompiler.test;

import cn.maxpixel.mcdecompiler.common.app.util.FileUtil;
import cn.maxpixel.mcdecompiler.mapping.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassifiedMapping;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormats;
import cn.maxpixel.mcdecompiler.mapping.remapper.ClassifiedMappingRemapper;
import cn.maxpixel.rewh.logging.LogManager;
import cn.maxpixel.rewh.logging.Logger;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FunctionTest {
    private static final Logger LOGGER = LogManager.getLogger();

//    public void test() throws Throwable {
//        ClassifiedMapping<PairedMapping> srg = MappingFormats.SRG.read(Files.newBufferedReader(Path.of("downloads/1.8.9/joined.srg")));
//        UniqueMapping<PairedMapping> mcp =  MCP.INSTANCE.read(Files.newBufferedReader(Path.of("downloads/1.8.9/methods.csv")), Files.newBufferedReader(Path.of("downloads/1.8.9/fields.csv")));
//        Map<String, String> fields = mcp.fields.stream().collect(Collectors.toMap(PairedMapping::getUnmappedName, PairedMapping::getMappedName));
//        Map<String, String> methods = mcp.methods.stream().collect(Collectors.toMap(PairedMapping::getUnmappedName, PairedMapping::getMappedName));
//        ClassifiedMapping<PairedMapping> out = new ClassifiedMapping<>();
//        srg.classes.forEach(mapping -> {
//            mapping.mapping.unmappedName = mapping.mapping.mappedName;
//            mapping.getMethods().forEach(m -> {
//                m.unmappedName = m.mappedName;
//                m.getComponent(Descriptor.class).setUnmappedDescriptor(m.getComponent(Descriptor.Mapped.class).mappedDescriptor);
//                m.mappedName = methods.getOrDefault(m.mappedName, m.mappedName);
//            });
//            mapping.getFields().forEach(m -> {
//                m.unmappedName = m.mappedName;
//                m.mappedName = fields.getOrDefault(m.mappedName, m.mappedName);
//            });
//            out.classes.add(mapping);
//        });
//        try (var os = Files.newOutputStream(Path.of("output/1.8.9-srg2mcp.tsrg"), StandardOpenOption.CREATE)) {
//            MappingFormats.TSRG_V1.write(out, os);
//        }
//    }

    public static void main(String[] args) throws Throwable {
        ClassifiedMapping<NamespacedMapping> mcpconfig = MappingFormats.TSRG_V2.read(new FileInputStream("downloads/1.19.3/joined.tsrg"));
        ClassifiedMapping<PairedMapping> official = MappingFormats.PROGUARD.read(new FileInputStream("downloads/1.19.3/client_mappings.txt"));
        var mappings = ClassifiedMappingRemapper.genMappingsByUnmappedNameMap(official.classes);
        for (ClassMapping<NamespacedMapping> cm : mcpconfig.classes) {
            NamespacedMapping mapping = cm.mapping;
            mapping.setName("srg", mappings.get(mapping.getName("obf")).mapping.mappedName);
        }
        MappingFormats.TSRG_V2.write(mcpconfig, Files.newBufferedWriter(FileUtil.ensureFileExist(Path.of("downloads/1.19.3/obf2srg.tsrg"))));
    }

//    private enum MCP implements MappingFormat.Unique<PairedMapping> {
//        INSTANCE;
//        @Override
//        public @NotNull String getName() {
//            return "mcp";
//        }
//
//        @Override
//        public MappingProcessor.Unique<PairedMapping> getProcessor() {
//            return new MappingProcessor.Unique<>() {
//                @Override
//                public MappingFormat<PairedMapping, UniqueMapping<PairedMapping>> getFormat() {
//                    return MCP.this;
//                }
//
//                @Override
//                public UniqueMapping<PairedMapping> process(ObjectList<String> content) {
//                    UniqueMapping<PairedMapping> ret = new UniqueMapping<>();
//                    for (String s : content) {
//                        String[] sa = s.split(",");
//                        if(s.startsWith("field")) {
//                            ret.fields.add(new PairedMapping(sa[0], sa[1]));
//                        } else if(s.startsWith("func")) {
//                            ret.methods.add(new PairedMapping(sa[0], sa[1]));
//                        } else if(s.startsWith("p_")) {
//                            ret.params.add(new PairedMapping(sa[0], sa[1]));
//                        }
//                    }
//                    return ret;
//                }
//            };
//        }
//        @Override
//        public MappingGenerator.Unique<PairedMapping> getGenerator() {
//            return null;
//        }
//    }
}