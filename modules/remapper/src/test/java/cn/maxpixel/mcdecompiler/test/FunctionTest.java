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

import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassifiedMapping;
import cn.maxpixel.mcdecompiler.mapping.component.Descriptor;
import cn.maxpixel.mcdecompiler.mapping.remapper.ClassifiedMappingRemapper;
import cn.maxpixel.mcdecompiler.mapping.util.MappingUtil;
import cn.maxpixel.rewh.logging.LogManager;
import cn.maxpixel.rewh.logging.Logger;
import org.jetbrains.annotations.NotNull;

public class FunctionTest {
    private static final Logger LOGGER = LogManager.getLogger();

//    public static void srg2mcp() throws Throwable {
//        ClassifiedMapping<PairedMapping> srg = MappingFormats.SRG.read(Files.newBufferedReader(Path.of("downloads/1.8.9/joined.srg")));
//        UniqueMapping<PairedMapping> mcp = MCP.INSTANCE.read(Files.newBufferedReader(Path.of("downloads/1.9.4/methods.csv")), Files.newBufferedReader(Path.of("downloads/1.9.4/fields.csv")));
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
//        try (var os = Files.newOutputStream(Path.of("output/1.9.4-srg2mcp.tsrg"), StandardOpenOption.CREATE)) {
//            MappingFormats.TSRG_V1.write(out, os);
//        }
//    }

//    public static void moj2srg() throws Throwable {
//        ClassifiedMapping<PairedMapping> srg = MappingFormats.TSRG_V1.read(Files.newBufferedReader(Path.of("downloads/1.16.5/old_srg.tsrg")));
//        ClassifiedMapping<PairedMapping> moj = MappingFormats.PROGUARD.read(Files.newBufferedReader(Path.of("downloads/1.16.5/client_mappings.txt")));
//        ClassifiedMappingRemapper remapper = new ClassifiedMappingRemapper(moj);
//        var mappings = ClassifiedMappingRemapper.genMappingsByUnmappedNameMap(moj.classes);
//        ClassifiedMapping<PairedMapping> out = new ClassifiedMapping<>();
//        srg.classes.forEach(mapping -> {
//            ClassMapping<PairedMapping> cm = mappings.get(mapping.mapping.unmappedName);
//            mapping.mapping.unmappedName = cm.mapping.mappedName;
//            var methods = cm.getMethods().stream().collect(Collectors.toMap(m -> m.unmappedName + remapper
//                    .unmapMethodDesc(m.getComponent(Descriptor.Mapped.class).mappedDescriptor), Function.identity()));
//            mapping.getMethods().forEach(m -> {
//                var mm = methods.get(m.unmappedName + m.getComponent(Descriptor.class).unmappedDescriptor);
//                m.unmappedName = mm.mappedName;
//                m.getComponent(Descriptor.class).setUnmappedDescriptor(mm.getComponent(Descriptor.Mapped.class).mappedDescriptor);
//            });
//            var fields = cm.getFields().stream().collect(Collectors.toMap(PairedMapping::getUnmappedName, Function.identity()));
//            mapping.getFields().forEach(m -> {
//                var fm = fields.get(m.unmappedName);
//                m.unmappedName = fm.mappedName;
//            });
//            out.classes.add(mapping);
//        });
//        try (var os = Files.newOutputStream(Path.of("output/1.16.5-moj2srg.tsrg"), StandardOpenOption.CREATE)) {
//            MappingFormats.TSRG_V1.write(out, os);
//        }
//    }

    public static void main(String[] args) throws Throwable {
//        ClassifiedMapping<NamespacedMapping> obf2srg = MappingFormats.TSRG_V2.read(new FileInputStream("downloads/1.19/joined.tsrg"));
//        obf2srg.getTrait(NamespacedTrait.class).setMappedNamespace("srg");
//        obf2srg.updateCollection();
//        ClassifiedMapping<PairedMapping> official = MappingFormats.PROGUARD.read(new FileInputStream("downloads/1.19/client_mappings.txt"));
//        var obf2off = new ClassifiedMappingRemapper(official);
//        var out = new ClassifiedMapping<PairedMapping>();
//        for (ClassMapping<NamespacedMapping> cm : obf2srg.classes) {
//            String obfClassName = cm.mapping.getName("obf");
//            ClassMapping<PairedMapping> ncm = new ClassMapping<>(new PairedMapping(obfClassName, obf2off.mapClass(obfClassName)));
//            for (NamespacedMapping field : cm.getFields()) {
//                ncm.addField(MappingUtil.Paired.o(field.getUnmappedName(), field.getMappedName()));
//            }
//            for (NamespacedMapping method : cm.getMethods()) {
//                ncm.addMethod(MappingUtil.Paired.duo(method.getUnmappedName(), method.getMappedName(), method.getComponent(Descriptor.Namespaced.class).unmappedDescriptor));
//            }
//            out.classes.add(ncm);
//        }
//        try (var writer = Files.newBufferedWriter(Path.of("downloads/1.19/obf2srg.tsrg"))) {
//            MappingFormats.TSRG_V1.write(out, writer);
//        }

//        for (ClassMapping<NamespacedMapping> cm : obf2srg.classes) {
//            String obfClassName = cm.mapping.getUnmappedName();
//            ClassMapping<PairedMapping> ncm = new ClassMapping<>(new PairedMapping(obf2off.mapClass(obfClassName), obf2off.mapClass(obfClassName)));
//            for (NamespacedMapping field : cm.getFields()) {
//                ncm.addField(MappingUtil.Paired.o(field.getMappedName(), obf2off.mapField(obfClassName, field.getUnmappedName())));
//            }
//            for (NamespacedMapping method : cm.getMethods()) {
//                String obfDesc = method.getComponent(Descriptor.Namespaced.class).unmappedDescriptor;
//                ncm.addMethod(MappingUtil.Paired.duo(method.getMappedName(), obf2off.mapMethod(obfClassName, method.getUnmappedName(), obfDesc), obf2off.mapMethodDesc(obfDesc)));
//            }
//            out.classes.add(ncm);
//        }
//        try (var writer = Files.newBufferedWriter(Path.of("downloads/1.19/srg2moj.tsrg"))) {
//            MappingFormats.TSRG_V1.write(out, writer);
//        }

//        srg2mcp();
//        moj2srg();

//        ClassifiedMapping<PairedMapping> srcMixin = MappingFormats.TSRG_V1.read(new FileReader("downloads/CSL/mixin.tsrg"));// mcp -> int
//        ClassifiedMapping<PairedMapping> srcFabric = MappingFormats.TSRG_V1.read(new FileReader("downloads/CSL/Fabric.tsrg"));
//        ClassifiedMapping<PairedMapping> mMoj2srg = MappingFormats.TSRG_V1.read(new FileReader("output/1.16.5-moj2srg.tsrg"));
//        ClassifiedMapping<PairedMapping> mObf2srg = MappingFormats.TSRG_V1.read(new FileReader("downloads/1.16.5/old_srg.tsrg"));
//        ClassifiedMapping<NamespacedMapping> srcIntermediary = MappingFormats.TINY_V1.read(new FileReader("downloads/1.16.5/intermediary.tiny"));
//        ClassifiedMappingRemapper int2obf = new ClassifiedMappingRemapper(srcIntermediary, "intermediary", true);
//        ClassifiedMappingRemapper obf2srg = new ClassifiedMappingRemapper(mObf2srg);
//        ClassifiedMappingRemapper srg2moj = new ClassifiedMappingRemapper(mMoj2srg, true);
//        ClassifiedMapping<PairedMapping> outMixin = new ClassifiedMapping<>();// moj -> srg
//        ClassifiedMapping<PairedMapping> outFabric = new ClassifiedMapping<>();
//        process(srcMixin, int2obf, obf2srg, srg2moj, outMixin, "mixin.tsrg");
//        process(srcFabric, int2obf, obf2srg, srg2moj, outFabric, "Fabric.tsrg");
//        try (var osMixin = Files.newOutputStream(Path.of("output/mixin-moj2srg.tsrg"), CREATE, TRUNCATE_EXISTING);
//            var osFabric = Files.newOutputStream(Path.of("output/Fabric-moj2srg.tsrg"), CREATE, TRUNCATE_EXISTING)) {
//            MappingFormats.TSRG_V1.write(outMixin, osMixin);
//            MappingFormats.TSRG_V1.write(outFabric, osFabric);
//        }
//        try (var in = Files.newBufferedReader(Path.of("in"));
//            var out = Files.newBufferedWriter(Path.of("out"))) {
//            MappingFormats.SRG.write(MappingFormats.TSRG_V1.read(in), out);
//        }
    }

    private static void process(ClassifiedMapping<PairedMapping> src, ClassifiedMappingRemapper int2obf, ClassifiedMappingRemapper obf2srg,
                                ClassifiedMappingRemapper srg2moj, ClassifiedMapping<PairedMapping> out, String fileName) {
        for (var cm : src.classes) {
            String intClassName = cm.mapping.mappedName;
            String obfClassName = int2obf.mapClass(intClassName);
            if (obfClassName == null) {
                LOGGER.warn("[{}] missing intermediary entry {}", fileName, intClassName);
                obfClassName = intClassName;
            }
            String srgClassName = obf2srg.mapClassOrDefault(obfClassName);
            ClassMapping<PairedMapping> outClass = new ClassMapping<>(
                    new PairedMapping(srg2moj.mapClassOrDefault(srgClassName), srgClassName)
            );
            for (@NotNull PairedMapping field : cm.getFields()) {
                String obfFieldName = int2obf.mapField(intClassName, field.mappedName);
                if (obfFieldName == null) {
                    LOGGER.warn("[{}] missing intermediary entry {}", fileName, field.mappedName);
                    obfFieldName = field.mappedName;
                }
                String srgFieldName = obf2srg.mapFieldOrDefault(obfClassName, obfFieldName);
                outClass.addField(MappingUtil.Paired.o(srg2moj.mapFieldOrDefault(srgClassName, srgFieldName), srgFieldName));
            }
            for (@NotNull PairedMapping method : cm.getMethods()) {
                String srgMethodDesc = method.getComponent(Descriptor.class).unmappedDescriptor;
                String obfMethodDesc = obf2srg.unmapMethodDesc(srgMethodDesc);
                String intMethodDesc = int2obf.unmapMethodDesc(obfMethodDesc);
                String obfMethodName = int2obf.mapMethod(intClassName, method.mappedName, intMethodDesc);
                if (obfMethodName == null) {
                    LOGGER.warn("[{}] missing intermediary entry {}", fileName, method.mappedName);
                    obfMethodName = method.mappedName;
                }
                String srgMethodName = obf2srg.mapMethodOrDefault(obfClassName, obfMethodName, obfMethodDesc);
                outClass.addMethod(MappingUtil.Paired.duo(srg2moj.mapMethodOrDefault(srgClassName, srgMethodName, srgMethodDesc),
                        srgMethodName, srg2moj.mapMethodDesc(srgMethodDesc)));
            }
            out.classes.add(outClass);
        }
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
//                        if (s.startsWith("field")) {
//                            ret.fields.add(new PairedMapping(sa[0], sa[1]));
//                        } else if (s.startsWith("func")) {
//                            ret.methods.add(new PairedMapping(sa[0], sa[1]));
//                        } else if (s.startsWith("p_")) {
//                            ret.params.add(new PairedMapping(sa[0], sa[1]));
//                        }
//                    }
//                    return ret;
//                }
//
//                @Override
//                public UniqueMapping<PairedMapping> process(ObjectList<String>... contents) {
//                    UniqueMapping<PairedMapping> result = new UniqueMapping<>();
//                    for (ObjectList<String> content : contents) {
//                        for (String s : content) {
//                            String[] sa = s.split(",");
//                            if (s.startsWith("field")) {
//                                result.fields.add(new PairedMapping(sa[0], sa[1]));
//                            } else if (s.startsWith("func")) {
//                                result.methods.add(new PairedMapping(sa[0], sa[1]));
//                            } else if (s.startsWith("p_")) {
//                                result.params.add(new PairedMapping(sa[0], sa[1]));
//                            }
//                        }
//                    }
//                    return result;
//                }
//            };
//        }
//        @Override
//        public MappingGenerator.Unique<PairedMapping> getGenerator() {
//            return null;
//        }
//    }
}