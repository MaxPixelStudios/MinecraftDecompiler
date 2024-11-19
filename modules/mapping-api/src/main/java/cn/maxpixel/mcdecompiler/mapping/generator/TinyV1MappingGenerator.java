package cn.maxpixel.mcdecompiler.mapping.generator;

import cn.maxpixel.mcdecompiler.common.util.NamingUtil;
import cn.maxpixel.mcdecompiler.mapping.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassifiedMapping;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormat;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormats;
import cn.maxpixel.mcdecompiler.mapping.remapper.ClassifiedMappingRemapper;
import cn.maxpixel.mcdecompiler.mapping.trait.NamespacedTrait;
import cn.maxpixel.mcdecompiler.mapping.util.MappingUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

public enum TinyV1MappingGenerator implements MappingGenerator.Classified<NamespacedMapping> {
    INSTANCE;

    @Override
    public MappingFormat<NamespacedMapping, ClassifiedMapping<NamespacedMapping>> getFormat() {
        return MappingFormats.TINY_V1;
    }

    @Override
    public ObjectList<String> generate(ClassifiedMapping<NamespacedMapping> mappings, ClassifiedMappingRemapper remapper) {
        ObjectArrayList<String> lines = new ObjectArrayList<>();
        if (mappings.classes.isEmpty()) return lines;
        var namespaces = mappings.getTrait(NamespacedTrait.class).namespaces;
        String namespace0 = namespaces.first();
        lines.add("v1\t" + String.join("\t", namespaces));
        mappings.classes.parallelStream().forEach(cls -> {
            NamespacedMapping classMapping = cls.mapping;
            synchronized (lines) {
                lines.add("CLASS\t" + NamingUtil.concatNamespaces(namespaces, classMapping::getName, "\t"));
            }
            cls.getFields().parallelStream().forEach(field -> {
                String desc = MappingUtil.Namespaced.checkTiny(namespace0, cls, field);
                synchronized (lines) {
                    lines.add("FIELD\t" + classMapping.getName(namespace0) + '\t' + desc + '\t' +
                            NamingUtil.concatNamespaces(namespaces, field::getName, "\t"));
                }
            });
            cls.getMethods().parallelStream().forEach(method -> {
                String desc = MappingUtil.Namespaced.checkTiny(namespace0, cls, method);
                synchronized (lines) {
                    lines.add("METHOD\t" + classMapping.getName(namespace0) + '\t' + desc + '\t' +
                            NamingUtil.concatNamespaces(namespaces, method::getName, "\t"));
                }
            });
        });
        return lines;
    }
}