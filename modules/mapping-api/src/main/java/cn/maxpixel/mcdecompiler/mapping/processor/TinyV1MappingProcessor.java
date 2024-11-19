package cn.maxpixel.mcdecompiler.mapping.processor;

import cn.maxpixel.mcdecompiler.mapping.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassifiedMapping;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormat;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormats;
import cn.maxpixel.mcdecompiler.mapping.trait.NamespacedTrait;
import cn.maxpixel.mcdecompiler.mapping.util.MappingUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectList;

import java.util.Arrays;
import java.util.function.Function;

public enum TinyV1MappingProcessor implements MappingProcessor.Classified<NamespacedMapping> {
    INSTANCE;

    private static final Function<String[], Function<String, ClassMapping<NamespacedMapping>>> MAPPING_FUNC = (namespaces) ->
            key -> new ClassMapping<>(new NamespacedMapping(namespaces, copy(key, namespaces.length)).setUnmappedNamespace(namespaces[0]));

    private static String[] copy(String s, int count) {
        String[] ret = new String[count];
        Arrays.fill(ret, s);
        return ret;
    }

    @Override
    public MappingFormat<NamespacedMapping, ClassifiedMapping<NamespacedMapping>> getFormat() {
        return MappingFormats.TINY_V1;
    }

    @Override
    public ClassifiedMapping<NamespacedMapping> process(ObjectList<String> content) {// TODO: Support properties
        if (!content.get(0).startsWith("v1")) error();
        String[] namespaces = MappingUtil.split(content.get(0), '\t', 3);
        ClassifiedMapping<NamespacedMapping> mappings = new ClassifiedMapping<>(new NamespacedTrait(namespaces));
        Object2ObjectOpenHashMap<String, ClassMapping<NamespacedMapping>> classes = new Object2ObjectOpenHashMap<>(); // k: the first namespace, usually unmapped name
        String k = namespaces[0];
        content.parallelStream().skip(1).forEach(s -> {
            String[] sa = MappingUtil.split(s, '\t');
            if (s.startsWith("CLASS")) {
                ClassMapping<NamespacedMapping> classMapping = new ClassMapping<>(new NamespacedMapping(namespaces, sa, 1)
                        .setUnmappedNamespace(k));
                synchronized (classes) {
                    classes.merge(sa[1], classMapping, (o, n) -> n.addFields(o.getFields()).addMethods(o.getMethods()));
                }
            } else if (s.startsWith("FIELD")) {
                NamespacedMapping fieldMapping = MappingUtil.Namespaced.duo(namespaces, sa, 3, k, sa[2]);
                synchronized (classes) {
                    classes.computeIfAbsent(sa[1], MAPPING_FUNC.apply(namespaces))
                            .addField(fieldMapping);
                }
            } else if (s.startsWith("METHOD")) {
                NamespacedMapping methodMapping = MappingUtil.Namespaced.duo(namespaces, sa, 3, k, sa[2]);
                synchronized (classes) {
                    classes.computeIfAbsent(sa[1], MAPPING_FUNC.apply(namespaces))
                            .addMethod(methodMapping);
                }
            } else error();
        });
        mappings.classes.addAll(classes.values());
        return mappings;
    }

    private static void error() {
        throw new IllegalArgumentException("Is this a Tiny v1 mapping file?");
    }
}