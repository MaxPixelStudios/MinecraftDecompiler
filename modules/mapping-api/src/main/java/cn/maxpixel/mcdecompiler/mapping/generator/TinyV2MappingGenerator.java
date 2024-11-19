package cn.maxpixel.mcdecompiler.mapping.generator;

import cn.maxpixel.mcdecompiler.common.util.NamingUtil;
import cn.maxpixel.mcdecompiler.mapping.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassifiedMapping;
import cn.maxpixel.mcdecompiler.mapping.component.Documented;
import cn.maxpixel.mcdecompiler.mapping.component.LocalVariableTable;
import cn.maxpixel.mcdecompiler.mapping.component.StaticIdentifiable;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormat;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormats;
import cn.maxpixel.mcdecompiler.mapping.remapper.ClassifiedMappingRemapper;
import cn.maxpixel.mcdecompiler.mapping.trait.NamespacedTrait;
import cn.maxpixel.mcdecompiler.mapping.util.MappingUtil;
import cn.maxpixel.mcdecompiler.mapping.util.TinyUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

public enum TinyV2MappingGenerator implements MappingGenerator.Classified<NamespacedMapping> {
    INSTANCE;

    @Override
    public MappingFormat<NamespacedMapping, ClassifiedMapping<NamespacedMapping>> getFormat() {
        return MappingFormats.TINY_V2;
    }

    @Override
    public ObjectList<String> generate(ClassifiedMapping<NamespacedMapping> mappings, ClassifiedMappingRemapper remapper) {
        ObjectArrayList<String> lines = new ObjectArrayList<>();
        if (mappings.classes.isEmpty()) return lines;
        var namespaces = mappings.getTrait(NamespacedTrait.class).namespaces;
        String namespace0 = namespaces.first();
        lines.add("tiny\t2\t0\t" + String.join("\t", namespaces));
        for (ClassMapping<NamespacedMapping> cls : mappings.classes) {
            lines.add("c\t" + NamingUtil.concatNamespaces(namespaces, cls.mapping::getName, "\t"));
            cls.getFields().parallelStream().forEach(field -> {
                String desc = MappingUtil.Namespaced.checkTiny(namespace0, cls, field);
                synchronized (lines) {
                    lines.add("\tf\t" + desc + '\t' + NamingUtil.concatNamespaces(namespaces, field::getName, "\t"));
                    if (field.hasComponent(Documented.class)) {
                        String doc = field.getComponent(Documented.class).getContentString();
                        if (!doc.isBlank()) lines.add("\t\tc\t" + TinyUtil.escape(doc));
                    }
                }
            });
            cls.getMethods().parallelStream().forEach(method -> {
                String desc = MappingUtil.Namespaced.checkTiny(namespace0, cls, method);
                synchronized (lines) {
                    lines.add("\tm\t" + desc + '\t' + NamingUtil.concatNamespaces(namespaces, method::getName, "\t"));
                    if (method.hasComponent(Documented.class)) {
                        String doc = method.getComponent(Documented.class).getContentString();
                        if (!doc.isBlank()) lines.add("\t\tc\t" + TinyUtil.escape(doc));
                    }
                    if (method.hasComponent(LocalVariableTable.Namespaced.class)) {
                        LocalVariableTable.Namespaced lvt = method.getComponent(LocalVariableTable.Namespaced.class);
                        boolean omittedThis = method.hasComponent(StaticIdentifiable.class) &&
                                !method.getComponent(StaticIdentifiable.class).isStatic;
                        lvt.getLocalVariableIndexes().forEach(index -> {
                            NamespacedMapping localVariable = lvt.getLocalVariable(omittedThis ? index + 1 : index);
                            String names = NamingUtil.concatNamespaces(namespaces, namespace -> {
                                String name = localVariable.getName(namespace);
                                if(name == null || name.isBlank()) return "";
                                return name;
                            }, "\t");
                            lines.add("\t\tp\t" + index + '\t' + names);
                            if (localVariable.hasComponent(Documented.class)) {
                                String doc = localVariable.getComponent(Documented.class).getContentString();
                                if (!doc.isBlank()) lines.add("\t\t\tc\t" + TinyUtil.escape(doc));
                            }
                        });
                    }
                }
            });
        }
        return lines;
    }
}