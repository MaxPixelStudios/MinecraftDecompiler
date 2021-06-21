/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile Minecraft.
 * Copyright (C) 2019-2021  MaxPixelStudios
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cn.maxpixel.mcdecompiler.asm;

import cn.maxpixel.mcdecompiler.mapping.paired.PairedClassMapping;
import cn.maxpixel.mcdecompiler.mapping.paired.PairedFieldMapping;
import cn.maxpixel.mcdecompiler.mapping.paired.PairedMethodMapping;
import cn.maxpixel.mcdecompiler.reader.AbstractMappingReader;
import cn.maxpixel.mcdecompiler.util.Utils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Remapper;

import java.util.Objects;
import java.util.Optional;

public class MappingRemapper extends Remapper {
    private static final Logger LOGGER = LogManager.getLogger("Remapper");
    private final SuperClassMapping superClassMapping;
    private final Object2ObjectOpenHashMap<String, ? extends PairedClassMapping> mappingByUnm;
    private final Object2ObjectOpenHashMap<String, ? extends PairedClassMapping> mappingByMap;

    public MappingRemapper(AbstractMappingReader mappingReader) {
        this(mappingReader, null);
    }

    public MappingRemapper(AbstractMappingReader mappingReader, String fromNamespace, String toNamespace) {
        this(mappingReader, null, fromNamespace, toNamespace);
    }

    public MappingRemapper(AbstractMappingReader mappingReader, SuperClassMapping superClassMapping) {
        this.superClassMapping = superClassMapping;
        this.mappingByUnm = mappingReader.getMappingsByUnmappedNameMap();
        this.mappingByMap = mappingReader.getMappingsByMappedNameMap();
    }

    public MappingRemapper(AbstractMappingReader mappingReader, SuperClassMapping superClassMapping, String fromNamespace, String toNamespace) {
        this.superClassMapping = superClassMapping;
        this.mappingByUnm = mappingReader.getMappingsByNamespaceMap(fromNamespace, fromNamespace, toNamespace);
        this.mappingByMap = mappingReader.getMappingsByNamespaceMap(toNamespace, fromNamespace, toNamespace);
    }

    @Override
    public String map(String internalName) {
        PairedClassMapping classMapping = mappingByUnm.get(internalName);
        if(classMapping != null) return classMapping.getMappedName();
        return internalName;
    }

    public String mapToUnmapped(final Type mappedType) {
        switch (mappedType.getSort()) {
            case Type.ARRAY:
                return "[".repeat(mappedType.getDimensions()) + mapToUnmapped(mappedType.getElementType());
            case Type.OBJECT:
                PairedClassMapping cm = mappingByMap.get(mappedType.getInternalName());
                return cm != null ? Type.getObjectType(cm.getUnmappedName()).getDescriptor() : mappedType.getDescriptor();
            default:
                return mappedType.getDescriptor();
        }
    }

    public String getUnmappedDescByMappedDesc(String mappedDescriptor) {
        if ("()V".equals(mappedDescriptor)) {
            return mappedDescriptor;
        }
        StringBuilder stringBuilder = new StringBuilder("(");
        if(mappedDescriptor.charAt(1) != ')') for(Type argumentType : Type.getArgumentTypes(mappedDescriptor)) {
            stringBuilder.append(mapToUnmapped(argumentType));
        }
        return stringBuilder.append(')').append(mapToUnmapped(Type.getReturnType(mappedDescriptor))).toString();
    }

    public String mapToMapped(final Type unmappedType) {
        switch (unmappedType.getSort()) {
            case Type.ARRAY:
                return "[".repeat(Math.max(0, unmappedType.getDimensions())) + mapToUnmapped(unmappedType.getElementType());
            case Type.OBJECT:
                PairedClassMapping cm = mappingByUnm.get(unmappedType.getInternalName());
                return cm != null ? Type.getObjectType(cm.getMappedName()).getDescriptor() : unmappedType.getDescriptor();
            default:
                return unmappedType.getDescriptor();
        }
    }

    public String getMappedDescByUnmappedDesc(String unmappedDescriptor) {
        if ("()V".equals(unmappedDescriptor)) {
            return unmappedDescriptor;
        }
        StringBuilder stringBuilder = new StringBuilder("(");
        if(unmappedDescriptor.charAt(1) != ')') for(Type argumentType : Type.getArgumentTypes(unmappedDescriptor)) {
            stringBuilder.append(mapToMapped(argumentType));
        }
        return stringBuilder.append(')').append(mapToMapped(Type.getReturnType(unmappedDescriptor))).toString();
    }

    private String getUnmappedDesc(PairedMethodMapping mapping) {
        if(mapping.isDescriptor()) return mapping.asDescriptor().getUnmappedDescriptor();
        else if(mapping.isMappedDescriptor()) return getUnmappedDescByMappedDesc(mapping.asMappedDescriptor().getMappedDescriptor());
        else throw new IllegalArgumentException("Impls of MethodMapping must implement at least one of Descriptor or Descriptor.Mapped");
    }

    @Override
    public String mapMethodName(String owner, String name, String descriptor) {
        if(!(name.contains("<init>") || name.contains("<clinit>"))) {
            return Optional.ofNullable(mappingByUnm.get(owner))
                    .map(cm -> {
                        LazyFinalValue<PairedMethodMapping> methodMapping = new LazyFinalValue<>("Method duplicated... This should not happen!");
                        cm.getMethods().parallelStream()
                                .filter(m -> m.getUnmappedName().equals(name) && getUnmappedDesc(m).equals(descriptor))
                                .forEach(methodMapping::set);
                        return methodMapping.value;
                    })
                    .or(() -> Optional.ofNullable(processSuperMethod(owner, name, descriptor)))
                    .map(PairedMethodMapping::getMappedName).orElse(name);
        }
        return name;
    }

    private PairedMethodMapping processSuperMethod(String owner, String name, String descriptor) {
        if(superClassMapping == null) throw new UnsupportedOperationException("Constructor MappingRemapper(AbstractMappingReader) is only " +
                "for reversing mapping. For remapping, please use MappingRemapper(AbstractMappingReader, SuperClassMapping)");
        ObjectArrayList<String> superNames = superClassMapping.MAP.get(owner);
        if(superNames != null) {
            LazyFinalValue<PairedMethodMapping> methodMapping = new LazyFinalValue<>("Method duplicated... This should not happen!");
            superNames.stream()
                    .map(mappingByUnm::get)
                    .filter(Objects::nonNull)
                    .flatMap(cm -> cm.getMethods().stream())
                    .filter(m -> {
                        String mapped = m.getMappedName();
                        return !mapped.startsWith("lambda$") && !mapped.startsWith("access$") &&
                                m.getUnmappedName().equals(name) && getUnmappedDesc(m).equals(descriptor);
                    })
                    .reduce((left, right) -> {
                        if(Utils.nameAndDescEquals(left, right)) return left;
                        throw new IllegalArgumentException();
                    }).ifPresent(methodMapping::set);
            if(methodMapping.value == null)
                superNames.stream()
                        .map(mappingByUnm::get)
                        .filter(Objects::nonNull)
                        .map(cm -> processSuperMethod(cm.getUnmappedName(), name, descriptor))
                        .filter(Objects::nonNull)
                        .reduce((left, right) -> {
                            if(Utils.nameAndDescEquals(left, right)) return left;
                            throw new IllegalArgumentException();
                        }).ifPresent(methodMapping::set);
            return methodMapping.value;
        }
        return null;
    }

    @Override
    public String mapFieldName(String owner, String name, String descriptor) {
        return Optional.ofNullable(mappingByUnm.get(owner))
                .map(classMapping -> classMapping.getField(name))
                .or(() -> Optional.ofNullable(processSuperField(owner, name)))
                .map(PairedFieldMapping::getMappedName).orElse(name);
    }

    private PairedFieldMapping processSuperField(String owner, String name) {
        if(superClassMapping == null) throw new UnsupportedOperationException("Constructor MappingRemapper(AbstractMappingReader) is only " +
                "for reversing mapping. For remapping, please use MappingRemapper(AbstractMappingReader, SuperClassMapping)");
        ObjectArrayList<String> superNames = superClassMapping.MAP.get(owner);
        if(superNames != null) {
            LazyFinalValue<PairedFieldMapping> fieldMapping = new LazyFinalValue<>("Field duplicated... This should not happen!");
            superNames.parallelStream()
                    .map(mappingByUnm::get)
                    .filter(Objects::nonNull)
                    .map(cm -> cm.getField(name))
                    .filter(Objects::nonNull)
                    .forEach(fieldMapping::set);
            if(fieldMapping.value == null)
                superNames.parallelStream()
                        .map(mappingByUnm::get)
                        .filter(Objects::nonNull)
                        .map(cm -> processSuperField(cm.getUnmappedName(), name))
                        .filter(Objects::nonNull)
                        .forEach(fieldMapping::set);
            return fieldMapping.value;
        }
        return null;
    }

    private static class LazyFinalValue<V> {
        private V value;
        private final String errorMessage;

        private LazyFinalValue(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public synchronized void set(V value) {
            if(this.value == null) this.value = Objects.requireNonNull(value);
            else throw new IllegalStateException(errorMessage);
        }
    }
}