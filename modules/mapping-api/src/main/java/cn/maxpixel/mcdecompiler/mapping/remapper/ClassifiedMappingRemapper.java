package cn.maxpixel.mcdecompiler.mapping.remapper;

import cn.maxpixel.mcdecompiler.common.util.Utils;
import cn.maxpixel.mcdecompiler.mapping.Mapping;
import cn.maxpixel.mcdecompiler.mapping.NameGetter;
import cn.maxpixel.mcdecompiler.mapping.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassifiedMapping;
import cn.maxpixel.mcdecompiler.mapping.component.Descriptor;
import cn.maxpixel.mcdecompiler.mapping.component.StaticIdentifiable;
import cn.maxpixel.mcdecompiler.mapping.trait.NamespacedTrait;
import cn.maxpixel.mcdecompiler.mapping.util.DescriptorRemapper;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.stream.Collectors;

public class ClassifiedMappingRemapper implements MappingRemapper {
    private final Object2ObjectOpenHashMap<String, ? extends Object2ObjectOpenHashMap<String, ? extends Mapping>> fieldByUnm;
    private final Object2ObjectOpenHashMap<String, ? extends Object2ObjectOpenHashMap<String,
            ? extends Object2ObjectOpenHashMap<String, ? extends Mapping>>> methodsByUnm;
    private final Object2ObjectOpenHashMap<String, ? extends ClassMapping<? extends Mapping>> mappingByUnm;
    private final Object2ObjectOpenHashMap<String, ? extends ClassMapping<? extends Mapping>> mappingByMap;
    private final DescriptorRemapper descriptorRemapper;
    private boolean methodStaticIdentifiable;

    public ClassifiedMappingRemapper(ClassifiedMapping<PairedMapping> mappings) {
        this(mappings, false);
    }

    public ClassifiedMappingRemapper(ClassifiedMapping<PairedMapping> mappings, boolean reverse) {
        if (reverse) mappings.reverse();
        this.fieldByUnm = genFieldsByUnmappedNameMap(mappings.classes);
        this.mappingByUnm = genMappingsByUnmappedNameMap(mappings.classes);
        this.mappingByMap = genMappingsByMappedNameMap(mappings.classes);
        this.methodsByUnm = mappings.classes.parallelStream().collect(Collectors.toMap(cm -> cm.mapping.unmappedName, cm -> {
            Object2ObjectOpenHashMap<String, Object2ObjectOpenHashMap<String, PairedMapping>> map =
                    new Object2ObjectOpenHashMap<>();
            for (PairedMapping mm : cm.getMethods()) {
                var m = map.computeIfAbsent(mm.unmappedName, k -> new Object2ObjectOpenHashMap<>());
                if (!methodStaticIdentifiable && mm.hasComponent(StaticIdentifiable.class)) methodStaticIdentifiable = true;
                if (m.putIfAbsent(getUnmappedDesc(mm), mm) != null) {
                    throw new IllegalArgumentException("Method duplicated... This should not happen!");
                }
            }
            return map;
        }, Utils::onKeyDuplicate, Object2ObjectOpenHashMap::new));
        this.descriptorRemapper = new DescriptorRemapper(mappingByUnm, mappingByMap);
    }

    public ClassifiedMappingRemapper(ClassifiedMapping<NamespacedMapping> mappings, String targetNamespace) {
        this(mappings, targetNamespace, false);
    }

    public ClassifiedMappingRemapper(ClassifiedMapping<NamespacedMapping> mappings, String targetNamespace, String fallbackNamespace) {
        this(mappings, targetNamespace, fallbackNamespace, false);
    }

    public ClassifiedMappingRemapper(ClassifiedMapping<NamespacedMapping> mappings, String targetNamespace, boolean reverse) {
        this(mappings, targetNamespace, mappings.getSourceNamespace(), reverse);
    }

    public ClassifiedMappingRemapper(ClassifiedMapping<NamespacedMapping> mappings, String targetNamespace, String fallbackNamespace, boolean reverse) {
        if (reverse) mappings.swap(targetNamespace);
        mappings.getTrait(NamespacedTrait.class).setMappedNamespace(targetNamespace);
        mappings.getTrait(NamespacedTrait.class).setFallbackNamespace(fallbackNamespace);
        mappings.updateCollection();
        this.fieldByUnm = genFieldsByUnmappedNameMap(mappings.classes);
        this.mappingByUnm = genMappingsByUnmappedNameMap(mappings.classes);
        this.mappingByMap = genMappingsByMappedNameMap(mappings.classes);
        this.methodsByUnm = mappings.classes.parallelStream().collect(Collectors.toMap(cm -> cm.mapping.getUnmappedName(), cm -> {
            Object2ObjectOpenHashMap<String, Object2ObjectOpenHashMap<String, NamespacedMapping>> map =
                    new Object2ObjectOpenHashMap<>();
            for (NamespacedMapping mm : cm.getMethods()) {
                var m = map.computeIfAbsent(mm.getUnmappedName(), k -> new Object2ObjectOpenHashMap<>());
                if (!methodStaticIdentifiable && mm.hasComponent(StaticIdentifiable.class)) methodStaticIdentifiable = true;
                if (m.putIfAbsent(getUnmappedDesc(mm), mm) != null) {
                    throw new IllegalArgumentException("Method duplicated... This should not happen!");
                }
            }
            return map;
        }, Utils::onKeyDuplicate, Object2ObjectOpenHashMap::new));
        this.descriptorRemapper = new DescriptorRemapper(mappingByUnm, mappingByMap);
    }

    @Override
    public boolean hasClassMapping(String name) {
        return mappingByUnm.containsKey(name);
    }

    @Override
    public boolean isMethodStaticIdentifiable() {
        return methodStaticIdentifiable;
    }

    @Override
    public @Nullable("When no corresponding mapping found") String mapClass(@NotNull String name) {
        ClassMapping<? extends Mapping> classMapping = mappingByUnm.get(name);
        if (classMapping != null) return classMapping.mapping.getMappedName();
        return null;
    }

    @Override
    public @Nullable("When no corresponding mapping found") String unmapClass(@NotNull String name) {
        ClassMapping<? extends Mapping> classMapping = mappingByMap.get(name);
        if (classMapping != null) return classMapping.mapping.getUnmappedName();
        return null;
    }

    @Override
    public @Nullable("When no corresponding mapping found") String mapField(@NotNull String owner, @NotNull String name) {
        var fields = fieldByUnm.get(owner);
        if (fields != null) {
            var mapping = fields.get(name);
            if (mapping != null) return mapping.getMappedName();
        }
        return null;
    }

    @Override
    public @Nullable("When no corresponding mapping found") String mapMethod(@NotNull String owner, @NotNull String name,
                                     @Nullable("When desc doesn't matter") String desc) {
        var methods = methodsByUnm.get(owner);
        if (methods != null) {
            var mappings = methods.get(name);
            if (mappings != null) {
                if (desc == null) {
                    if (!mappings.isEmpty()) return mappings.values().iterator().next().getMappedName();
                } else {
                    var mapping = mappings.get(desc);
                    if (mapping != null) return mapping.getMappedName();
                }
            }
        }
        return null;
    }

    @Override
    public DescriptorRemapper getDescriptorRemapper() {
        return descriptorRemapper;
    }

    public ClassMapping<? extends Mapping> getClassMappingUnmapped(@NotNull String name) {
        return mappingByUnm.get(name);
    }

    public String getUnmappedDesc(Mapping mapping) {
        if (mapping.hasComponent(Descriptor.class)) return mapping.getComponent(Descriptor.class).unmappedDescriptor;
        else if (mapping.hasComponent(Descriptor.Mapped.class))
            return unmapMethodDesc(mapping.getComponent(Descriptor.Mapped.class).mappedDescriptor);
        else if (mapping.hasComponent(Descriptor.Namespaced.class))
            return mapping.getComponent(Descriptor.Namespaced.class).unmappedDescriptor;
        else throw new IllegalArgumentException("Mapping for methods must support at least one of the descriptor components");
    }

    public static <T extends Mapping> Object2ObjectOpenHashMap<String, Object2ObjectOpenHashMap<String, T>> genFieldsByUnmappedNameMap(
            ObjectList<ClassMapping<T>> mapping) {
        return mapping.parallelStream().collect(Collectors.toMap(
                cm -> cm.mapping.getUnmappedName(),
                cm -> cm.getFields().parallelStream().collect(Collectors.toMap(NameGetter::getUnmappedName, Function.identity(),
                        Utils::onKeyDuplicate, Object2ObjectOpenHashMap::new)),
                Utils::onKeyDuplicate, Object2ObjectOpenHashMap::new));
    }

    public static <T extends Mapping> Object2ObjectOpenHashMap<String, ClassMapping<T>> genMappingsByUnmappedNameMap(
            ObjectList<ClassMapping<T>> mapping) {
        return mapping.parallelStream().collect(Collectors.toMap(cm -> cm.mapping.getUnmappedName(),
                Function.identity(), Utils::onKeyDuplicate, Object2ObjectOpenHashMap::new));
    }

    public static <T extends Mapping> Object2ObjectOpenHashMap<String, ClassMapping<T>> genMappingsByMappedNameMap(
            ObjectList<ClassMapping<T>> mapping) {
        return mapping.parallelStream().collect(Collectors.toMap(cm -> cm.mapping.getMappedName(),
                Function.identity(), Utils::onKeyDuplicate, Object2ObjectOpenHashMap::new));
    }

    public static Object2ObjectOpenHashMap<String, ClassMapping<NamespacedMapping>> genMappingsByNamespaceMap(
            ObjectList<ClassMapping<NamespacedMapping>> mapping, String namespace) {
        return mapping.parallelStream().collect(Collectors.toMap(m -> m.mapping.getName(namespace),
                Function.identity(), Utils::onKeyDuplicate, Object2ObjectOpenHashMap::new));
    }
}