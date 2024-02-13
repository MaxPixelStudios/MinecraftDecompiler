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

import cn.maxpixel.mcdecompiler.asm.ClassifiedMappingRemapper;
import cn.maxpixel.mcdecompiler.mapping.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassifiedMapping;
import cn.maxpixel.mcdecompiler.mapping.type.MappingTypes;
import cn.maxpixel.mcdecompiler.reader.ClassifiedMappingReader;
import cn.maxpixel.mcdecompiler.util.FileUtil;
import cn.maxpixel.mcdecompiler.util.Logging;
import cn.maxpixel.mcdecompiler.writer.ClassifiedMappingWriter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

public class FunctionTest {
    private static final Logger LOGGER = Logging.getLogger();

//    public void test() throws Throwable {
//        ClassifiedMappingReader<PairedMapping> srg = new ClassifiedMappingReader<>(MappingTypes.SRG, "downloads/1.8.9/joined.srg");
//        UniqueMappingReader<PairedMapping> mcp = new UniqueMappingReader<>(new MCP(), "downloads/1.8.9/methods.csv",
//                "downloads/1.8.9/fields.csv");
//        Map<String, String> fields = mcp.mappings.fields.stream().collect(Collectors.toMap(PairedMapping::getUnmappedName, PairedMapping::getMappedName));
//        Map<String, String> methods = mcp.mappings.methods.stream().collect(Collectors.toMap(PairedMapping::getUnmappedName, PairedMapping::getMappedName));
//        ClassifiedMappingWriter<PairedMapping> writer = new ClassifiedMappingWriter<>(MappingTypes.TSRG_V1);
//        srg.mappings.forEach(mapping -> {
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
//            writer.addMapping(mapping);
//        });
//        writer.writeTo(Files.newOutputStream(Path.of("output/1.8.9-srg2mcp.tsrg"), StandardOpenOption.CREATE));
//    }

    public static void main(String[] args) throws Throwable {
        ClassifiedMapping<NamespacedMapping> mcpconfig = new ClassifiedMappingReader<>(MappingTypes.TSRG_V2).read("downloads/1.19.3/joined.tsrg");
        ClassifiedMapping<PairedMapping> official = new ClassifiedMappingReader<>(MappingTypes.PROGUARD).read("downloads/1.19.3/client_mappings.txt");
        var mappings = ClassifiedMappingRemapper.genMappingsByUnmappedNameMap(official.classes);
        for (ClassMapping<NamespacedMapping> cm : mcpconfig.classes) {
            NamespacedMapping mapping = cm.mapping;
            mapping.setName("srg", mappings.get(mapping.getName("obf")).mapping.mappedName);
        }
        ClassifiedMappingWriter<NamespacedMapping> writer = new ClassifiedMappingWriter<>(MappingTypes.TSRG_V2);
        writer.write(mcpconfig, Files.newBufferedWriter(FileUtil.ensureFileExist(Path.of("downloads/1.19.3/obf2srg.tsrg"))));
    }

//    private static class MCP implements MappingType.Unique<PairedMapping> {
//        @Override
//        public MappingProcessor.Unique<PairedMapping> getProcessor() {
//            return new MappingProcessor.Unique<>() {
//                @Override
//                public MappingType<PairedMapping, UniqueMapping<PairedMapping>> getType() {
//                    return MCP.this;
//                }
//
//                @Override
//                public Pair<UniqueMapping<PairedMapping>, ObjectList<PairedMapping>> process(ObjectList<String> content) {
//                    ObjectObjectImmutablePair<UniqueMapping<PairedMapping>, ObjectList<PairedMapping>> ret = new ObjectObjectImmutablePair<>(new UniqueMapping<>(), null);
//                    for(String s : content) {
//                        String[] sa = s.split(",");
//                        if(s.startsWith("field")) {
//                            ret.left().fields.add(new PairedMapping(sa[0], sa[1]));
//                        } else if(s.startsWith("func")) {
//                            ret.left().methods.add(new PairedMapping(sa[0], sa[1]));
//                        } else if(s.startsWith("p_")) {
//                            ret.left().params.add(new PairedMapping(sa[0], sa[1]));
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