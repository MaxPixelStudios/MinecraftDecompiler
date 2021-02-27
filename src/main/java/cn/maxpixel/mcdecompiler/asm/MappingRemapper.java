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
import cn.maxpixel.mcdecompiler.mapping.base.BaseFieldMapping;
import cn.maxpixel.mcdecompiler.mapping.base.BaseMethodMapping;
import cn.maxpixel.mcdecompiler.reader.AbstractMappingReader;
import cn.maxpixel.mcdecompiler.util.NamingUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Remapper;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class MappingRemapper extends Remapper {
    private final Object2ObjectOpenHashMap<String, ? extends ClassMapping> mappingByUnm;
    private final Object2ObjectOpenHashMap<String, ? extends ClassMapping> mappingByMap;
    private final SuperClassMapping superClassMapping;
    private static final Logger LOGGER = LogManager.getLogger("Remapper");
    public MappingRemapper(AbstractMappingReader mappingReader, SuperClassMapping superClassMapping) {
        this.mappingByUnm = mappingReader.getMappingsByUnmappedNameMap();
        this.mappingByMap = mappingReader.getMappingsByMappedNameMap();
        this.superClassMapping = superClassMapping;
    }
    @Override
    public String map(String internalName) {
        ClassMapping classMapping = mappingByUnm.get(NamingUtil.asJavaName0(internalName));
        if(classMapping != null) return NamingUtil.asNativeName(classMapping.getMappedName());
        else return internalName;
    }

    private String mapType(final Type type) {
        switch (type.getSort()) {
            case Type.ARRAY:
                StringBuilder remappedDescriptor = new StringBuilder();
                for (int i = 0; i < type.getDimensions(); ++i) {
                    remappedDescriptor.append('[');
                }
                remappedDescriptor.append(mapType(type.getElementType()));
                return remappedDescriptor.toString();
            case Type.OBJECT:
                ClassMapping cm = mappingByMap.get(type.getClassName());
                return cm != null ? NamingUtil.asDescriptor(cm.getUnmappedName()) : type.getDescriptor();
            default:
                return type.getDescriptor();
        }
    }
    private String getUnmappedDescByMappedDesc(String originalDescriptor) {
        if ("()V".equals(originalDescriptor)) {
            return originalDescriptor;
        }
        StringBuilder stringBuilder = new StringBuilder("(");
        for (Type argumentType : Type.getArgumentTypes(originalDescriptor)) {
            stringBuilder.append(mapType(argumentType));
        }
        Type returnType = Type.getReturnType(originalDescriptor);
        if (returnType == Type.VOID_TYPE) {
            stringBuilder.append(")V");
        } else {
            stringBuilder.append(')').append(mapType(returnType));
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
            ClassMapping cm = mappingByUnm.get(NamingUtil.asJavaName(owner));
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
        List<String> superNames = superClassMapping.getMap().get(NamingUtil.asJavaName(owner));
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
        ClassMapping classMapping = mappingByUnm.get(NamingUtil.asJavaName(owner));
        if(classMapping != null) {
            BaseFieldMapping fieldMapping = classMapping.getField(name);
            if(fieldMapping == null) fieldMapping = processSuperField(owner, name);
            if(fieldMapping != null) return fieldMapping.getMappedName();
        }
        return name;
    }
    private BaseFieldMapping processSuperField(String owner, String name) {
        List<String> superNames = superClassMapping.getMap().get(NamingUtil.asJavaName(owner));
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