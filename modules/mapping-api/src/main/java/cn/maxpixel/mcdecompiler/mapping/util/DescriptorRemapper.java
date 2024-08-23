package cn.maxpixel.mcdecompiler.mapping.util;

import cn.maxpixel.mcdecompiler.common.Constants;
import cn.maxpixel.mcdecompiler.common.util.DescriptorUtil;
import cn.maxpixel.mcdecompiler.mapping.Mapping;
import cn.maxpixel.mcdecompiler.mapping.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassifiedMapping;
import cn.maxpixel.mcdecompiler.mapping.remapper.ClassifiedMappingRemapper;
import cn.maxpixel.mcdecompiler.mapping.trait.NamespacedTrait;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.intellij.lang.annotations.Pattern;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

/**
 * Lightweight remapper for descriptors in place of the general heavyweight {@link cn.maxpixel.mcdecompiler.mapping.remapper.MappingRemapper}s.
 */
public class DescriptorRemapper {
    private final Object2ObjectOpenHashMap<String, ? extends ClassMapping<? extends Mapping>> mappingByUnm;
    private final Object2ObjectOpenHashMap<String, ? extends ClassMapping<? extends Mapping>> mappingByMap;

    public DescriptorRemapper(Object2ObjectOpenHashMap<String, ? extends ClassMapping<? extends Mapping>> mappingByUnm,
                              Object2ObjectOpenHashMap<String, ? extends ClassMapping<? extends Mapping>> mappingByMap) {
        this.mappingByUnm = mappingByUnm;
        this.mappingByMap = mappingByMap;
    }

    public DescriptorRemapper(ClassifiedMapping<?> collection) {
        this(ClassifiedMappingRemapper.genMappingsByUnmappedNameMap(collection.classes),
                ClassifiedMappingRemapper.genMappingsByMappedNameMap(collection.classes));
    }

    public DescriptorRemapper(ClassifiedMapping<NamespacedMapping> collection, String targetNamespace) {
        this(setup(collection, targetNamespace));
    }

    private static ClassifiedMapping<NamespacedMapping> setup(ClassifiedMapping<NamespacedMapping> collection, String targetNamespace) {
        var trait = collection.getTrait(NamespacedTrait.class);
        trait.setMappedNamespace(targetNamespace);
        trait.setFallbackNamespace(trait.getUnmappedNamespace());
        collection.updateCollection();
        return collection;
    }

    private String mapClass(@NotNull String name) {
        ClassMapping<? extends Mapping> classMapping = mappingByUnm.get(name);
        if (classMapping != null) return classMapping.mapping.getMappedName();
        return name;
    }

    private String unmapClass(@NotNull String name) {
        ClassMapping<? extends Mapping> classMapping = mappingByMap.get(name);
        if (classMapping != null) return classMapping.mapping.getUnmappedName();
        return name;
    }

    @Subst("I")
    public @Pattern(Constants.FIELD_DESC_PATTERN) String mapDesc(@Pattern(Constants.FIELD_DESC_PATTERN) String unmappedDesc) {
        return mapDesc(unmappedDesc, true);
    }

    @Subst("()V")
    public @Pattern(Constants.METHOD_DESC_PATTERN) String mapMethodDesc(@Pattern(Constants.METHOD_DESC_PATTERN) String unmappedDesc) {
        return mapMethodDesc(unmappedDesc, true);
    }

    @Subst("I")
    public @Pattern(Constants.FIELD_DESC_PATTERN) String unmapDesc(@Pattern(Constants.FIELD_DESC_PATTERN) String mappedDesc) {
        return mapDesc(mappedDesc, false);
    }

    @Subst("()V")
    public @Pattern(Constants.METHOD_DESC_PATTERN) String unmapMethodDesc(@Pattern(Constants.METHOD_DESC_PATTERN) String mappedDesc) {
        return mapMethodDesc(mappedDesc, false);
    }

    @Subst("I")
    private String mapDesc(@Pattern(Constants.FIELD_DESC_PATTERN) String desc, boolean map) {
        int i = 0;
        if (desc.charAt(0) == '[') while (desc.charAt(++i) == '[');
        return switch (desc.charAt(i)) {
            case 'Z', 'B', 'C', 'D', 'F', 'I', 'J', 'S' -> desc;
            case 'L' -> {
                StringBuilder ret = new StringBuilder(desc.length()).append(desc, 0, ++i);
                int j = desc.indexOf(';', i + 1);// skip 'L' and the first char
                if (j < 0) DescriptorUtil.throwInvalid(true);
                yield ret.append(map ? mapClass(desc.substring(i, j)) : unmapClass(desc.substring(i, j)))
                        .append(desc, j, desc.length()).toString();
            }
            default -> DescriptorUtil.throwInvalid(true);
        };
    }

    @Subst("()V")
    private String mapMethodDesc(@Pattern(Constants.METHOD_DESC_PATTERN) String desc, boolean map) {
        if (desc.length() == 3 || desc.indexOf('L') < 0) return desc;// no need to map
        StringBuilder ret = new StringBuilder(desc.length());
        int start = 0;
        for (int i = 1; i < desc.length(); i++) {
            switch (desc.charAt(i)) {
                case 'Z', 'B', 'C', 'D', 'F', 'I', 'J', 'S', 'V', '[', ')' -> {} // no op
                case 'L' -> {
                    ret.append(desc, start, ++i);
                    start = desc.indexOf(';', i + 1);// skip 'L'(++i) and the first char
                    if (start < 0) DescriptorUtil.throwInvalid(true);
                    ret.append(map ? mapClass(desc.substring(i, start)) : unmapClass(desc.substring(i, start)));
                    i = start;// will do i++, so don't assign `start + 1` here
                }
                default -> DescriptorUtil.throwInvalid(true);
            }
        }
        return ret.append(desc, start, desc.length()).toString();
    }
}