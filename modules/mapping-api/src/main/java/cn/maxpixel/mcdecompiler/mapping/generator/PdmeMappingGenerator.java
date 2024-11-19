package cn.maxpixel.mcdecompiler.mapping.generator;

import cn.maxpixel.mcdecompiler.common.util.Utils;
import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassifiedMapping;
import cn.maxpixel.mcdecompiler.mapping.component.Descriptor;
import cn.maxpixel.mcdecompiler.mapping.component.Documented;
import cn.maxpixel.mcdecompiler.mapping.component.LocalVariableTable;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormat;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormats;
import cn.maxpixel.mcdecompiler.mapping.remapper.ClassifiedMappingRemapper;
import cn.maxpixel.mcdecompiler.mapping.trait.AccessTransformationTrait;
import cn.maxpixel.mcdecompiler.mapping.trait.InheritanceTrait;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

import java.util.Locale;

public enum PdmeMappingGenerator implements MappingGenerator.Classified<PairedMapping> {
    INSTANCE;

    private static final String PARA = "¶";
    private static final String NIL = "nil";

    @Override
    public MappingFormat<PairedMapping, ClassifiedMapping<PairedMapping>> getFormat() {
        return MappingFormats.PDME;
    }

    @Override
    public ObjectList<String> generate(ClassifiedMapping<PairedMapping> mappings, ClassifiedMappingRemapper remapper) {
        ObjectArrayList<String> lines = new ObjectArrayList<>();
        if (mappings.classes.isEmpty()) return lines;
        mappings.classes.parallelStream().forEach(classMapping -> {
            PairedMapping cm = classMapping.mapping;
            String unmapped = cm.getUnmappedName().replace('/', '.');
            String mapped = cm.getMappedName().replace('/', '.');
            String clsDoc = getDoc(cm);
            synchronized (lines) {
                lines.add(String.join(PARA, "Class", unmapped, mapped, NIL, NIL, clsDoc));
            }
            classMapping.getFields().parallelStream().forEach(field -> {
                String desc = field.getComponent(Descriptor.class).unmappedDescriptor;
                String unmappedName = unmapped + '.' + field.getUnmappedName() + ':' + desc;
                String doc = getDoc(field);
                synchronized (lines) {
                    lines.add(String.join("Var", unmappedName, field.mappedName, NIL, NIL, doc));
                }
            });
            classMapping.getMethods().parallelStream().forEach(method -> {
                String desc = method.getComponent(Descriptor.class).unmappedDescriptor;
                String unmappedName = unmapped + '.' + method.getUnmappedName() + desc;
                String doc = getDoc(method);
                synchronized (lines) {
                    lines.add(String.join(PARA, "Def", unmappedName, method.mappedName, NIL, NIL, doc));
                }
                if (method.hasComponent(LocalVariableTable.Paired.class)) {
                    LocalVariableTable.Paired lvt = method.getComponent(LocalVariableTable.Paired.class);
                    IntIterator it = lvt.getLocalVariableIndexes().iterator();
                    while (it.hasNext()) {
                        int index = it.nextInt();
                        PairedMapping loc = lvt.getLocalVariable(index);
                        String line = String.join(
                                PARA,
                                "Param",
                                nilWhenBlank(loc.unmappedName),
                                nilWhenBlank(loc.mappedName),
                                unmappedName,
                                String.valueOf(index),
                                getDoc(loc)
                        );
                        synchronized (lines) {
                            lines.add(line);
                        }
                    }
                }
            });
        });
        if (mappings.hasTrait(InheritanceTrait.class)) {
            mappings.getTrait(InheritanceTrait.class).getMap().forEach((k, v) -> {
                if (!v.isEmpty()) lines.add(String.join(PARA, "Include", k.replace('/', '.'),
                        String.join(",", Utils.mapArray(v.toArray(), String[]::new,
                                s -> ((String) s).replace('/', '.'))), NIL, NIL, ""));
            });
        }
        if (mappings.hasTrait(AccessTransformationTrait.class)) {
            var map = mappings.getTrait(AccessTransformationTrait.class).getMap();
            map.object2IntEntrySet().fastForEach(e -> lines.add(String.join(PARA, "AccessFlag",
                    e.getKey().replace('/', '.'), formatHex(e.getIntValue()), NIL, NIL, "")));
        }
        return lines;
    }

    private static String nilWhenBlank(String s) {
        return s == null || s.isBlank() ? NIL : s;
    }

    private static String getDoc(PairedMapping m) {
        return m.getComponentOptional(Documented.class).map(Documented::getContentString).orElse("");
    }

    private static String formatHex(int value) {
        String s = Integer.toHexString(value).toUpperCase(Locale.ENGLISH);
        return "0x" + "0".repeat(4 - s.length()) + s;
    }
}