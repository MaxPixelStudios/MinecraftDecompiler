package cn.maxpixel.mcdecompiler.mapping.remapper;

import cn.maxpixel.mcdecompiler.common.util.Utils;
import cn.maxpixel.mcdecompiler.mapping.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassifiedMapping;
import cn.maxpixel.mcdecompiler.mapping.component.Descriptor;
import cn.maxpixel.mcdecompiler.mapping.trait.NamespacedTrait;
import cn.maxpixel.mcdecompiler.mapping.util.MappingUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterators;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ClassifiedMappingRemapper implements MappingRemapper {
    private final Object2ObjectOpenHashMap<String, Object2ObjectOpenHashMap<String, PairedMapping>> fieldByUnm;
    private final Object2ObjectOpenHashMap<String, Object2ObjectOpenHashMap<String,
            Object2ObjectOpenHashMap<String, PairedMapping>>> methodsByUnm;
    private final Object2ObjectOpenHashMap<String, ClassMapping<PairedMapping>> mappingByUnm;
    private final Object2ObjectOpenHashMap<String, ClassMapping<PairedMapping>> mappingByMap;

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
            cm.getMethods().forEach(mm -> {
                Object2ObjectOpenHashMap<String, PairedMapping> m = map.computeIfAbsent(mm.unmappedName,
                        k -> new Object2ObjectOpenHashMap<>());
                if (m.putIfAbsent(getUnmappedDesc(mm), mm) != null) {
                    throw new IllegalArgumentException("Method duplicated... This should not happen!");
                }
            });
            return map;
        }, Utils::onKeyDuplicate, Object2ObjectOpenHashMap::new));
    }

    public ClassifiedMappingRemapper(ClassifiedMapping<NamespacedMapping> mappings, String targetNamespace) {
        this(mappings, targetNamespace, false);
    }

    public ClassifiedMappingRemapper(ClassifiedMapping<NamespacedMapping> mappings, String targetNamespace, boolean reverse) {
        this(toPaired(mappings, targetNamespace), reverse);
    }

    @Override
    public @NotNull String mapClass(@NotNull String name) {
        ClassMapping<PairedMapping> classMapping = mappingByUnm.get(name);
        if (classMapping != null) return classMapping.mapping.mappedName;
        return name;
    }

    @Override
    public @NotNull String unmapClass(@NotNull String name) {
        ClassMapping<PairedMapping> classMapping = mappingByMap.get(name);
        if (classMapping != null) return classMapping.mapping.unmappedName;
        return name;
    }

    @Override
    public @NotNull String mapField(@NotNull String owner, @NotNull String name) {
        var fields = fieldByUnm.get(owner);
        if (fields != null) {
            var mapping = fields.get(name);
            if (mapping != null) return mapping.mappedName;
        }
        return name;
    }

    @Override
    public @NotNull String mapMethod(@NotNull String owner, @NotNull String name,
                                     @Nullable("When desc doesn't matter") String desc) {
        var methods = methodsByUnm.get(owner);
        if (methods != null) {
            var mappings = methods.get(name);
            if (mappings != null) {
                if (desc == null) {
                    if (!mappings.isEmpty()) return mappings.values().iterator().next().mappedName;
                } else {
                    var mapping = mappings.get(desc);
                    if (mapping != null) return mapping.mappedName;
                }
            }
        }
        return name;
    }

    public String getUnmappedDesc(PairedMapping mapping) {
        if (mapping.hasComponent(Descriptor.class)) return mapping.getComponent(Descriptor.class).unmappedDescriptor;
        else if (mapping.hasComponent(Descriptor.Mapped.class))
            return unmapMethodDesc(mapping.getComponent(Descriptor.Mapped.class).mappedDescriptor);
        else throw new IllegalArgumentException("Mapping for methods must support at least one of Descriptor or Descriptor.Mapped");
    }

    public static Object2ObjectOpenHashMap<String, Object2ObjectOpenHashMap<String, PairedMapping>> genFieldsByUnmappedNameMap(
            ObjectList<ClassMapping<PairedMapping>> mapping) {
        return mapping.parallelStream().collect(Collectors.toMap(
                cm -> cm.mapping.unmappedName,
                cm -> cm.getFields().parallelStream().collect(Collectors.toMap(m -> m.unmappedName, Function.identity(),
                        Utils::onKeyDuplicate, Object2ObjectOpenHashMap::new)),
                Utils::onKeyDuplicate, Object2ObjectOpenHashMap::new));
    }

    public static Object2ObjectOpenHashMap<String, ClassMapping<PairedMapping>> genMappingsByUnmappedNameMap(
            ObjectList<ClassMapping<PairedMapping>> mapping) {
        return mapping.parallelStream().collect(Collectors.toMap(cm -> cm.mapping.unmappedName,
                Function.identity(), Utils::onKeyDuplicate, Object2ObjectOpenHashMap::new));
    }

    public static Object2ObjectOpenHashMap<String, ClassMapping<PairedMapping>> genMappingsByMappedNameMap(
            ObjectList<ClassMapping<PairedMapping>> mapping) {
        return mapping.parallelStream().collect(Collectors.toMap(cm -> cm.mapping.mappedName,
                Function.identity(), Utils::onKeyDuplicate, Object2ObjectOpenHashMap::new));
    }

    public static Object2ObjectOpenHashMap<String, ClassMapping<NamespacedMapping>> genMappingsByNamespaceMap(
            ObjectList<ClassMapping<NamespacedMapping>> mapping, String namespace) {
        return mapping.parallelStream().collect(Collectors.toMap(m -> m.mapping.getName(namespace),
                Function.identity(), Utils::onKeyDuplicate, Object2ObjectOpenHashMap::new));
    }

    private static ClassifiedMapping<PairedMapping> toPaired(ClassifiedMapping<NamespacedMapping> mappings, String targetNamespace) {
        ClassifiedMapping<PairedMapping> paired = new ClassifiedMapping<>();
        paired.classes.ensureCapacity(mappings.classes.size());
        var namespaces = mappings.getTrait(NamespacedTrait.class).namespaces;
        String sourceNamespace = namespaces.first();
        if (!namespaces.contains(targetNamespace)) {
            var it = namespaces.iterator();
            it.next();
            throw new IllegalArgumentException(String.format("Target namespace \"%s\" does not exist. Available namespaces: %s",
                    targetNamespace, Arrays.toString(ObjectIterators.unwrap(it))));
        }
        mappings.classes.parallelStream().forEach(old -> {
            ClassMapping<PairedMapping> cm = new ClassMapping<>(new PairedMapping(old.mapping.getName(sourceNamespace),
                    getName(old.mapping, targetNamespace, sourceNamespace)));
            for (NamespacedMapping f : old.getFields()) {
                cm.addField(MappingUtil.Paired.o(f.getName(sourceNamespace), getName(f, targetNamespace, sourceNamespace)));
            }
            for (NamespacedMapping m : old.getMethods()) {
                var desc = m.getComponent(Descriptor.Namespaced.class);
                if (!sourceNamespace.equals(desc.descriptorNamespace)) throw new IllegalArgumentException();
                cm.addMethod(MappingUtil.Paired.duo(m.getName(sourceNamespace), getName(m, targetNamespace, sourceNamespace),
                        desc.unmappedDescriptor));
            }
            synchronized (paired.classes) {
                paired.classes.add(cm);
            }
        });
        return paired;
    }

    private static String getName(NamespacedMapping mapping, String targetNamespace, String fallbackNamespace) {
        var name = mapping.getName(targetNamespace);// TODO: Fallback namespace
        return Utils.isStringNotBlank(name) ? name : mapping.getName(fallbackNamespace);
    }
}