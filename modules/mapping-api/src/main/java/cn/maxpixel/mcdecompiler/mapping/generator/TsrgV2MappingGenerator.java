package cn.maxpixel.mcdecompiler.mapping.generator;

import cn.maxpixel.mcdecompiler.common.util.NamingUtil;
import cn.maxpixel.mcdecompiler.mapping.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassifiedMapping;
import cn.maxpixel.mcdecompiler.mapping.component.Descriptor;
import cn.maxpixel.mcdecompiler.mapping.component.LocalVariableTable;
import cn.maxpixel.mcdecompiler.mapping.component.StaticIdentifiable;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormat;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormats;
import cn.maxpixel.mcdecompiler.mapping.remapper.ClassifiedMappingRemapper;
import cn.maxpixel.mcdecompiler.mapping.trait.NamespacedTrait;
import cn.maxpixel.mcdecompiler.mapping.util.MappingUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

public enum TsrgV2MappingGenerator implements MappingGenerator.Classified<NamespacedMapping> {
    INSTANCE;

    @Override
    public MappingFormat<NamespacedMapping, ClassifiedMapping<NamespacedMapping>> getFormat() {
        return MappingFormats.TSRG_V2;
    }

    @Override
    public ObjectList<String> generate(ClassifiedMapping<NamespacedMapping> mappings, ClassifiedMappingRemapper remapper) {
        ObjectArrayList<String> lines = new ObjectArrayList<>();
        if (mappings.classes.isEmpty() && mappings.packages.isEmpty()) return lines;
        var namespaces = mappings.getTrait(NamespacedTrait.class).namespaces;
        String namespace0 = namespaces.first();
        lines.add("tsrg2 " + String.join(" ", namespaces));
        for (ClassMapping<NamespacedMapping> cls : mappings.classes) {
            lines.add(NamingUtil.concatNamespaces(namespaces, cls.mapping::getName, " "));
            cls.getFields().parallelStream().forEach(field -> {
                MappingUtil.checkOwner(field.getOwned(), cls);
                String names = NamingUtil.concatNamespaces(namespaces, field::getName, " ");
                if (field.hasComponent(Descriptor.Namespaced.class)) synchronized (lines) {
                    genDescriptorLine(lines, namespace0, field, names);
                } else synchronized (lines) {
                    lines.add('\t' + names);
                }
            });
            cls.getMethods().parallelStream().forEach(method -> {
                if (!method.hasComponent(Descriptor.Namespaced.class)) throw new UnsupportedOperationException();
                MappingUtil.checkOwner(method.getOwned(), cls);
                synchronized (lines) {
                    genDescriptorLine(lines, namespace0, method, NamingUtil.concatNamespaces(namespaces,
                            method::getName, " "));
                    var si = method.getComponent(StaticIdentifiable.class);
                    if (si != null && si.isStatic) lines.add("\t\tstatic");
                    if (method.hasComponent(LocalVariableTable.Namespaced.class)) {
                        LocalVariableTable.Namespaced lvt = method.getComponent(LocalVariableTable.Namespaced.class);
                        lvt.getLocalVariableIndexes().forEach(index -> {
                            String names = NamingUtil.concatNamespaces(namespaces, namespace -> {
                                String name = lvt.getLocalVariable(index).getName(namespace);
                                if(name != null && name.isBlank()) return "o";
                                return name;
                            }, " ");
                            lines.add("\t\t" + index + ' ' + names);
                        });
                    }
                }
            });
        }
        mappings.packages.parallelStream().forEach(pkg -> {
            synchronized (lines) {
                lines.add(NamingUtil.concatNamespaces(namespaces, pkg::getName, " "));
            }
        });
        return lines;
    }

    private static void genDescriptorLine(ObjectArrayList<String> lines, String namespace0, NamespacedMapping method, String names) {
        Descriptor.Namespaced desc = method.getComponent(Descriptor.Namespaced.class);
        if (!namespace0.equals(desc.descriptorNamespace)) throw new IllegalArgumentException();
        int i = names.indexOf(' ');
        lines.add('\t' + names.substring(0, i + 1) + desc.unmappedDescriptor + names.substring(i));
    }
}