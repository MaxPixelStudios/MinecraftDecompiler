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

import cn.maxpixel.mcdecompiler.mapping.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.TinyClassMapping;
import cn.maxpixel.mcdecompiler.mapping.base.BaseFieldMapping;
import cn.maxpixel.mcdecompiler.mapping.base.BaseMethodMapping;
import cn.maxpixel.mcdecompiler.reader.AbstractMappingReader;
import cn.maxpixel.mcdecompiler.util.NamingUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Remapper;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MappingRemapper extends Remapper {
    private final Object2ObjectOpenHashMap<String, ? extends ClassMapping> mappingByUnm;
    private final Object2ObjectOpenHashMap<String, ? extends ClassMapping> mappingByMap;
    private final SuperClassMapping superClassMapping;
    private static final Logger LOGGER = LogManager.getLogger("Remapper");

    public MappingRemapper(AbstractMappingReader mappingReader) {
        this.mappingByUnm = mappingReader.getMappingsByUnmappedNameMap();
        this.mappingByMap = mappingReader.getMappingsByMappedNameMap();
        this.superClassMapping = null;
    }

    public MappingRemapper(AbstractMappingReader mappingReader, SuperClassMapping superClassMapping) {
        this.mappingByUnm = mappingReader.getMappingsByUnmappedNameMap();
        this.mappingByMap = mappingReader.getMappingsByMappedNameMap();
        this.superClassMapping = superClassMapping;
    }

    public MappingRemapper(AbstractMappingReader mappingReader, SuperClassMapping superClassMapping, String fromNamespace, String toNamespace) {
        this.mappingByUnm = ((List<TinyClassMapping>) mappingReader.getMappings()).stream()
                .collect(Collectors.toMap(cm -> cm.getName(fromNamespace), Function.identity(), (cm1, cm2) -> {throw new IllegalArgumentException("Key \"" + cm1 + "\" and \"" + cm2 + "\" duplicated!");}, Object2ObjectOpenHashMap::new));
        this.mappingByMap = ((List<TinyClassMapping>) mappingReader.getMappings()).stream()
                .collect(Collectors.toMap(cm -> cm.getName(toNamespace), Function.identity(), (cm1, cm2) -> {throw new IllegalArgumentException("Key \"" + cm1 + "\" and \"" + cm2 + "\" duplicated!");}, Object2ObjectOpenHashMap::new));
        this.superClassMapping = superClassMapping;
    }

    @Override
    public String map(String internalName) {
        ClassMapping classMapping = mappingByUnm.get(NamingUtil.asJavaName(internalName));
        if(classMapping != null) return NamingUtil.asNativeName(classMapping.getMappedName());
        else return internalName;
    }

    private String mapToUnmapped(final Type mappedType) {
        switch (mappedType.getSort()) {
            case Type.ARRAY:
                StringBuilder remappedDescriptor = new StringBuilder();
                for (int i = 0; i < mappedType.getDimensions(); ++i) {
                    remappedDescriptor.append('[');
                }
                remappedDescriptor.append(mapToUnmapped(mappedType.getElementType()));
                return remappedDescriptor.toString();
            case Type.OBJECT:
                ClassMapping cm = mappingByMap.get(mappedType.getClassName());
                return cm != null ? NamingUtil.asDescriptor(cm.getUnmappedName()) : mappedType.getDescriptor();
            default:
                return mappedType.getDescriptor();
        }
    }

    public String getUnmappedDescByMappedDesc(String mappedDescriptor) {
        if ("()V".equals(mappedDescriptor)) {
            return mappedDescriptor;
        }
        StringBuilder stringBuilder = new StringBuilder("(");
        for (Type argumentType : Type.getArgumentTypes(mappedDescriptor)) {
            stringBuilder.append(mapToUnmapped(argumentType));
        }
        Type returnType = Type.getReturnType(mappedDescriptor);
        if (returnType == Type.VOID_TYPE) {
            stringBuilder.append(")V");
        } else {
            stringBuilder.append(')').append(mapToUnmapped(returnType));
        }
        return stringBuilder.toString();
    }

    private String mapToMapped(final Type unmappedType) {
        switch (unmappedType.getSort()) {
            case Type.ARRAY:
                StringBuilder remappedDescriptor = new StringBuilder();
                for (int i = 0; i < unmappedType.getDimensions(); ++i) {
                    remappedDescriptor.append('[');
                }
                remappedDescriptor.append(mapToUnmapped(unmappedType.getElementType()));
                return remappedDescriptor.toString();
            case Type.OBJECT:
                ClassMapping cm = mappingByUnm.get(unmappedType.getClassName());
                return cm != null ? NamingUtil.asDescriptor(cm.getMappedName()) : unmappedType.getDescriptor();
            default:
                return unmappedType.getDescriptor();
        }
    }

    public String getMappedDescByUnmappedDesc(String unmappedDescriptor) {
        if ("()V".equals(unmappedDescriptor)) {
            return unmappedDescriptor;
        }
        StringBuilder stringBuilder = new StringBuilder("(");
        for (Type argumentType : Type.getArgumentTypes(unmappedDescriptor)) {
            stringBuilder.append(mapToMapped(argumentType));
        }
        Type returnType = Type.getReturnType(unmappedDescriptor);
        if (returnType == Type.VOID_TYPE) {
            stringBuilder.append(")V");
        } else {
            stringBuilder.append(')').append(mapToMapped(returnType));
        }
        return stringBuilder.toString();
    }

    private String getUnmappedDesc(BaseMethodMapping mapping) {
        if(mapping.isDescriptor()) return mapping.asDescriptor().getUnmappedDescriptor();
        else if(mapping.isMappedDescriptor()) return getUnmappedDescByMappedDesc(mapping.asMappedDescriptor().getMappedDescriptor());
        else throw new IllegalArgumentException("Impls of MethodMapping must implement at least one of Descriptor or Descriptor.Mapped");
    }

    @Override
    public String mapMethodName(String owner, String name, String descriptor) {
        if(!(name.contains("<init>") || name.contains("<clinit>"))) {
            ClassMapping cm = mappingByUnm.get(NamingUtil.asJavaName0(owner));
            if(cm != null) {
                AtomicReference<BaseMethodMapping> methodMapping = new AtomicReference<>();
                cm.getMethods().parallelStream().filter(m -> m.getUnmappedName().equals(name)).forEach(mapping -> {
                    if(methodMapping.get() == null && getUnmappedDesc(mapping).equals(descriptor) && !methodMapping.compareAndSet(null, mapping))
                        throw new RuntimeException("Method duplicated...This should not happen!");
                });
                if(methodMapping.get() != null) return methodMapping.get().getMappedName();
                else {
                    BaseMethodMapping result = processSuperMethod(owner, name, descriptor);
                    if(result != null) return result.getMappedName();
                }
            }
        }
        return name;
    }
    private BaseMethodMapping processSuperMethod(String owner, String name, String descriptor) {
        if(superClassMapping == null) throw new UnsupportedOperationException("Constructor MappingRemapper(AbstractMappingReader) is only " +
                "for reversing mapping. For remapping, please use MappingRemapper(AbstractMappingReader, SuperClassMapping)");
        ObjectArrayList<String> superNames = superClassMapping.getMap().get(NamingUtil.asJavaName0(owner));
        if(superNames != null) {
            AtomicReference<BaseMethodMapping> methodMapping = new AtomicReference<>();
            superNames.parallelStream().map(mappingByUnm::get).filter(Objects::nonNull).flatMap(cm -> cm.getMethods().stream()).filter(m -> {
                String mapped = m.getMappedName();
                return !mapped.startsWith("lambda$") && !mapped.startsWith("access$") && m.getUnmappedName().equals(name) &&
                        getUnmappedDesc(m).equals(descriptor);
            }).distinct().findAny().ifPresent(mapping -> {
                if(!methodMapping.compareAndSet(null, mapping)) throw new RuntimeException("Method duplicated... This should not happen!");
            });
            if(methodMapping.get() == null) superNames.parallelStream().map(mappingByUnm::get).filter(Objects::nonNull)
                .map(cm -> processSuperMethod(cm.getUnmappedName(), name, descriptor)).filter(Objects::nonNull).distinct().findAny().ifPresent(result -> {
                    if(!methodMapping.compareAndSet(null, result)) throw new RuntimeException("Method duplicated... This should not happen!");
                });
            if(methodMapping.get() != null) return methodMapping.get();
        }
        return null;
    }
    @Override
    public String mapFieldName(String owner, String name, String descriptor) {
        ClassMapping classMapping = mappingByUnm.get(NamingUtil.asJavaName0(owner));
        if(classMapping != null) {
            BaseFieldMapping fieldMapping = classMapping.getField(name);
            if(fieldMapping == null) fieldMapping = processSuperField(owner, name);
            if(fieldMapping != null) return fieldMapping.getMappedName();
        }
        return name;
    }
    private BaseFieldMapping processSuperField(String owner, String name) {
        if(superClassMapping == null) throw new UnsupportedOperationException("Constructor MappingRemapper(AbstractMappingReader) is only " +
                "for reversing mapping. For remapping, please use MappingRemapper(AbstractMappingReader, SuperClassMapping)");
        ObjectArrayList<String> superNames = superClassMapping.getMap().get(NamingUtil.asJavaName0(owner));
        if(superNames != null) {
            AtomicReference<BaseFieldMapping> fieldMapping = new AtomicReference<>();
            superNames.parallelStream().map(mappingByUnm::get).filter(Objects::nonNull).map(cm -> cm.getField(name)).filter(Objects::nonNull)
                .findAny().ifPresent(fm -> {
                if(!fieldMapping.compareAndSet(null, fm)) throw new RuntimeException("Field duplicated... This should not happen!");
            });
            if(fieldMapping.get() == null) superNames.parallelStream().map(mappingByUnm::get).filter(Objects::nonNull)
                .map(cm -> processSuperField(cm.getUnmappedName(), name)).filter(Objects::nonNull).forEach(fm -> {
                    if(!fieldMapping.compareAndSet(null, fm)) throw new RuntimeException("Field duplicated... This should not happen!");
                });
            if(fieldMapping.get() != null) return fieldMapping.get();
        }
        return null;
    }
}