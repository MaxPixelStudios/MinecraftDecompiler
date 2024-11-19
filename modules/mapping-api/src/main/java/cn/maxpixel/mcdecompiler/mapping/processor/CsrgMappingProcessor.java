package cn.maxpixel.mcdecompiler.mapping.processor;

import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassifiedMapping;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormat;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormats;
import cn.maxpixel.mcdecompiler.mapping.util.MappingUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectList;

public enum CsrgMappingProcessor implements MappingProcessor.Classified<PairedMapping> {
    INSTANCE;

    @Override
    public MappingFormat<PairedMapping, ClassifiedMapping<PairedMapping>> getFormat() {
        return MappingFormats.CSRG;
    }

    @Override
    public ClassifiedMapping<PairedMapping> process(ObjectList<String> content) {
        ClassifiedMapping<PairedMapping> mappings = new ClassifiedMapping<>();
        Object2ObjectOpenHashMap<String, ClassMapping<PairedMapping>> classes = new Object2ObjectOpenHashMap<>(); // k: unmapped name
        content.parallelStream().forEach(s -> {
            String[] sa = MappingUtil.split(s, ' ');
            switch (sa.length) {
                case 2 -> { // Class / Package
                    if (sa[0].charAt(sa[0].length() - 1) == '/') synchronized (mappings.packages) {
                        mappings.packages.add(new PairedMapping(sa[0].substring(0, sa[0].length() - 1),
                                sa[1].substring(0, sa[1].length() - 1)));
                    } else {
                        ClassMapping<PairedMapping> classMapping = new ClassMapping<>(new PairedMapping(sa[0], sa[1]));
                        synchronized (classes) {
                            classes.merge(classMapping.mapping.unmappedName, classMapping, (o, n) -> {
                                n.addFields(o.getFields());
                                n.addMethods(o.getMethods());
                                return n;
                            });
                        }
                    }
                }
                case 3 -> { // Field
                    PairedMapping fieldMapping = MappingUtil.Paired.o(sa[1], sa[2]);
                    synchronized (classes) {
                        classes.computeIfAbsent(sa[0], MappingUtil.Paired.COMPUTE_DEFAULT_CLASS).addField(fieldMapping);
                    }
                }
                case 4 -> { // Method
                    PairedMapping methodMapping = MappingUtil.Paired.duo(sa[1], sa[3], sa[2]);
                    synchronized (classes) {
                        classes.computeIfAbsent(sa[0], MappingUtil.Paired.COMPUTE_DEFAULT_CLASS).addMethod(methodMapping);
                    }
                }
                default -> throw new IllegalArgumentException("Is this a CSRG mapping file?");
            }
        });
        mappings.classes.addAll(classes.values());
        return mappings;
    }
}