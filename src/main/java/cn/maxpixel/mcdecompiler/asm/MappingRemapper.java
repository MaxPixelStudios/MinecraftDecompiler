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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Remapper;

import java.util.Objects;
import java.util.Optional;

public class MappingRemapper extends Remapper {
    private static final Logger LOGGER = LogManager.getLogger("Remapper");
    private final ExtraClassesInformation extraClassesInformation;
    private final Object2ObjectOpenHashMap<String, ? extends PairedClassMapping> mappingByUnm;
    private final Object2ObjectOpenHashMap<String, ? extends PairedClassMapping> mappingByMap;

    public MappingRemapper(AbstractMappingReader mappingReader) {
        this(mappingReader, null);
    }

    public MappingRemapper(AbstractMappingReader mappingReader, String fromNamespace, String toNamespace) {
        this(mappingReader, null, fromNamespace, toNamespace);
    }

    public MappingRemapper(AbstractMappingReader mappingReader, ExtraClassesInformation extraClassesInformation) {
        this.extraClassesInformation = extraClassesInformation;
        this.mappingByUnm = mappingReader.getMappingsByUnmappedNameMap();
        this.mappingByMap = mappingReader.getMappingsByMappedNameMap();
    }

    public MappingRemapper(AbstractMappingReader mappingReader, ExtraClassesInformation extraClassesInformation, String fromNamespace, String toNamespace) {
        this.extraClassesInformation = extraClassesInformation;
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
                    .flatMap(cm -> cm.getMethods().parallelStream()
                            .filter(m -> m.getUnmappedName().equals(name) && getUnmappedDesc(m).equals(descriptor))
                            .map(PairedMethodMapping.class::cast)
                            .reduce((left, right) -> {throw new IllegalArgumentException("Method duplicated... This should not happen!");})
                    )
                    .or(() -> processSuperMethod(owner, name, descriptor))
                    .map(PairedMethodMapping::getMappedName).orElse(name);
        }
        return name;
    }

    private Optional<PairedMethodMapping> processSuperMethod(String owner, String name, String descriptor) {
        if(extraClassesInformation == null) throw new UnsupportedOperationException("Constructor MappingRemapper(AbstractMappingReader) is only " +
                "for reversing mapping. For remapping, please use MappingRemapper(AbstractMappingReader, ExtraClassesInformation)");
        return Optional.ofNullable(extraClassesInformation.SUPER_NAMES.get(owner))
                .flatMap(superNames -> superNames.parallelStream()
                        .map(mappingByUnm::get)
                        .filter(Objects::nonNull)
                        .flatMap(cm -> cm.getMethods().stream())
                        .filter(m -> m.getUnmappedName().equals(name) && getUnmappedDesc(m).equals(descriptor))
                        .map(PairedMethodMapping.class::cast)
                        .reduce(this::reduceMethod)
                        .or(() -> superNames.parallelStream()
                                .map(mappingByUnm::get)
                                .filter(Objects::nonNull)
                                .map(cm -> processSuperMethod(cm.getUnmappedName(), name, descriptor))
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .reduce(this::reduceMethod)
                        )
                );
    }

    private PairedMethodMapping reduceMethod(PairedMethodMapping left, PairedMethodMapping right) {
        if(Utils.nameAndDescEquals(left, right)) return left;
        // 0b111 = Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED | Opcodes.ACC_PRIVATE
        int leftAcc = extraClassesInformation.ACCESS_MAP.get(left.getOwner().getUnmappedName())
                .getOrDefault(left.getUnmappedName().concat(getUnmappedDesc(left)), Opcodes.ACC_PUBLIC) & 0b111;
        int rightAcc = extraClassesInformation.ACCESS_MAP.get(right.getOwner().getUnmappedName())
                .getOrDefault(right.getUnmappedName().concat(getUnmappedDesc(right)), Opcodes.ACC_PUBLIC) & 0b111;
        // 0b101 = Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED
        if((leftAcc & 0b101) != 0) return left;
        else if((rightAcc & 0b101) != 0) return right;
        else if(leftAcc == Opcodes.ACC_PRIVATE || rightAcc == Opcodes.ACC_PRIVATE)
            throw new IllegalArgumentException("This can't happen!");
        throw new IllegalArgumentException("Method duplicated... This should not happen!");
    }

    @Override
    public String mapFieldName(String owner, String name, String descriptor) {
        return Optional.ofNullable(mappingByUnm.get(owner))
                .map(classMapping -> classMapping.getField(name))
                .or(() -> processSuperField(owner, name))
                .map(PairedFieldMapping::getMappedName).orElse(name);
    }

    private Optional<PairedFieldMapping> processSuperField(String owner, String name) {
        if(extraClassesInformation == null) throw new UnsupportedOperationException("Constructor MappingRemapper(AbstractMappingReader) is only " +
                "for reversing mapping. For remapping, please use MappingRemapper(AbstractMappingReader, ExtraClassesInformation)");
        return Optional.ofNullable(extraClassesInformation.SUPER_NAMES.get(owner))
                .flatMap(superNames -> superNames.parallelStream()
                        .map(mappingByUnm::get)
                        .filter(Objects::nonNull)
                        .map(cm -> cm.getField(name))
                        .filter(Objects::nonNull)
                        .reduce(this::reduceField)
                        .or(() -> superNames.parallelStream()
                                .map(mappingByUnm::get)
                                .filter(Objects::nonNull)
                                .map(cm -> processSuperField(cm.getUnmappedName(), name))
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .reduce(this::reduceField)
                        )
                );
    }

    private PairedFieldMapping reduceField(PairedFieldMapping left, PairedFieldMapping right) {
        // 0b111 = Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED | Opcodes.ACC_PRIVATE
        int leftAcc = extraClassesInformation.ACCESS_MAP.get(left.getOwner().getUnmappedName())
                .getOrDefault(left.getUnmappedName(), Opcodes.ACC_PUBLIC) & 0b111;
        int rightAcc = extraClassesInformation.ACCESS_MAP.get(right.getOwner().getUnmappedName())
                .getOrDefault(right.getUnmappedName(), Opcodes.ACC_PUBLIC) & 0b111;
        // 0b101 = Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED
        if((leftAcc & 0b101) != 0) return left;
        else if((rightAcc & 0b101) != 0) return right;
        else if(leftAcc == Opcodes.ACC_PRIVATE || rightAcc == Opcodes.ACC_PRIVATE)
            throw new IllegalArgumentException("This can't happen!");
        throw new IllegalArgumentException("Field duplicated... This should not happen!");
    }
}