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

import cn.maxpixel.mcdecompiler.mapping1.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping1.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping1.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping1.component.Descriptor;
import cn.maxpixel.mcdecompiler.reader.ClassifiedMappingReader;
import cn.maxpixel.mcdecompiler.util.Logging;
import cn.maxpixel.mcdecompiler.util.MappingUtil;
import cn.maxpixel.mcdecompiler.util.NamingUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Remapper;

import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ClassifiedMappingRemapper extends Remapper {
    private static final Logger LOGGER = Logging.getLogger("Remapper");
    private final ExtraClassesInformation extraClassesInformation;
    private final Object2ObjectOpenHashMap<String, Object2ObjectOpenHashMap<String, PairedMapping>> fieldByUnm;
    private final Object2ObjectOpenHashMap<String, ClassMapping<PairedMapping>> mappingByUnm;
    private final Object2ObjectOpenHashMap<String, ClassMapping<PairedMapping>> mappingByMap;

    public ClassifiedMappingRemapper(ClassifiedMappingReader<PairedMapping> mappingReader) {
        this(mappingReader, null);
    }

    public ClassifiedMappingRemapper(ClassifiedMappingReader<PairedMapping> mappingReader, ExtraClassesInformation extraClassesInformation) {
        this.extraClassesInformation = extraClassesInformation;
        this.fieldByUnm = ClassMapping.genFieldsByUnmappedNameMap(mappingReader.mappings);
        this.mappingByUnm = ClassMapping.genMappingsByUnmappedNameMap(mappingReader.mappings);
        this.mappingByMap = ClassMapping.genMappingsByMappedNameMap(mappingReader.mappings);
    }

    public ClassifiedMappingRemapper(ClassifiedMappingReader<NamespacedMapping> mappingReader, String targetNamespace) {
        this(mappingReader, NamingUtil.findSourceNamespace(mappingReader), targetNamespace);
    }

    public ClassifiedMappingRemapper(ClassifiedMappingReader<NamespacedMapping> mappingReader, String sourceNamespace, String targetNamespace) {
        this(mappingReader, null, sourceNamespace, targetNamespace);
    }

    public ClassifiedMappingRemapper(ClassifiedMappingReader<NamespacedMapping> mappingReader, ExtraClassesInformation extraClassesInformation, String targetNamespace) {
        this(mappingReader, extraClassesInformation, NamingUtil.findSourceNamespace(mappingReader), targetNamespace);
    }

    public ClassifiedMappingRemapper(ClassifiedMappingReader<NamespacedMapping> mappingReader, ExtraClassesInformation extraClassesInformation, String sourceNamespace, String targetNamespace) {
        this.extraClassesInformation = extraClassesInformation;
        ObjectArrayList<ClassMapping<PairedMapping>> mappings = mappingReader.mappings.parallelStream()
                .map(m -> asPaired(m, sourceNamespace, targetNamespace))
                .collect(Collectors.toCollection(ObjectArrayList::new));
        this.fieldByUnm = ClassMapping.genFieldsByUnmappedNameMap(mappings);
        this.mappingByUnm = ClassMapping.genMappingsByUnmappedNameMap(mappings);
        this.mappingByMap = ClassMapping.genMappingsByMappedNameMap(mappings);
    }

    private static ClassMapping<PairedMapping> asPaired(ClassMapping<NamespacedMapping> old, String sourceNamespace, String targetNamespace) {
        ClassMapping<PairedMapping> cm = new ClassMapping<>(new PairedMapping(old.mapping.getName(sourceNamespace),
                old.mapping.getName(targetNamespace)));
        old.getFields().forEach(m -> cm.addField(MappingUtil.Paired.o(m.getName(sourceNamespace), m.getName(targetNamespace))));
        old.getMethods().forEach(m -> {
            if(!m.getComponent(Descriptor.Namespaced.class).getDescriptorNamespace().equals(sourceNamespace))
                throw new IllegalArgumentException();
            cm.addMethod(MappingUtil.Paired.duo(m.getName(sourceNamespace), m.getName(targetNamespace),
                    m.getComponent(Descriptor.Namespaced.class).getUnmappedDescriptor()));
        });
        return cm;
    }

    @Override
    public String map(String internalName) {
        ClassMapping<PairedMapping> classMapping = mappingByUnm.get(internalName);
        if(classMapping != null) return classMapping.mapping.mappedName;
        return internalName;
    }

    public String mapToUnmapped(final Type mappedType) {
        switch (mappedType.getSort()) {
            case Type.ARRAY:
                return "[".repeat(mappedType.getDimensions()) + mapToUnmapped(mappedType.getElementType());
            case Type.OBJECT:
                ClassMapping<PairedMapping> cm = mappingByMap.get(mappedType.getInternalName());
                return cm != null ? Type.getObjectType(cm.mapping.unmappedName).getDescriptor() : mappedType.getDescriptor();
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
                ClassMapping<PairedMapping> cm = mappingByUnm.get(unmappedType.getInternalName());
                return cm != null ? Type.getObjectType(cm.mapping.mappedName).getDescriptor() : unmappedType.getDescriptor();
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

    private String getUnmappedDesc(PairedMapping mapping) {
        if(mapping.hasComponent(Descriptor.class)) return mapping.getComponent(Descriptor.class).getUnmappedDescriptor();
        else if(mapping.hasComponent(Descriptor.Mapped.class))
            return getUnmappedDescByMappedDesc(mapping.getComponent(Descriptor.Mapped.class).getMappedDescriptor());
        else throw new IllegalArgumentException("Mapping for methods must support at least one of Descriptor or Descriptor.Mapped");
    }

    @Override
    public String mapMethodName(String owner, String name, String descriptor) {
        if(!(name.equals("<init>") || name.equals("<clinit>"))) {
            return Optional.ofNullable(mappingByUnm.get(owner))
                    .flatMap(cm -> cm.getMethods().parallelStream()
                            .filter(m -> m.unmappedName.equals(name) && getUnmappedDesc(m).equals(descriptor))
                            .reduce((left, right) -> {throw new IllegalArgumentException("Method duplicated... This should not happen!");})
                    )
                    .or(() -> processSuperMethod(owner, name, descriptor))
                    .map(m -> m.mappedName).orElse(name);
        }
        return name;
    }

    private Optional<PairedMapping> processSuperMethod(String owner, String name, String descriptor) {
        if(extraClassesInformation == null) throw new UnsupportedOperationException("Constructor ClassifiedMappingRemapper(AbstractMappingReader) is only " +
                "for reversing mapping. For remapping, please use ClassifiedMappingRemapper(AbstractMappingReader, ExtraClassesInformation)");
        return Optional.ofNullable(extraClassesInformation.getSuperNames(owner))
                .flatMap(superNames -> superNames.parallelStream()
                        .map(mappingByUnm::get)
                        .filter(Objects::nonNull)
                        .flatMap(cm -> cm.getMethods().stream())
                        .filter(m -> m.unmappedName.equals(name) && getUnmappedDesc(m).equals(descriptor))
                        .reduce(this::reduceMethod)
                        .or(() -> superNames.parallelStream()
                                .map(mappingByUnm::get)
                                .filter(Objects::nonNull)
                                .map(cm -> processSuperMethod(cm.mapping.unmappedName, name, descriptor))
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .reduce(this::reduceMethod)
                        )
                );
    }

    private PairedMapping reduceMethod(PairedMapping left, PairedMapping right) {
        if(nameAndDescEquals(left, right)) return left;
        // 0b111 = Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED | Opcodes.ACC_PRIVATE
        int leftAcc = extraClassesInformation.getAccessFlags(left.getOwned().getOwner().mapping.unmappedName,
                left.unmappedName.concat(getUnmappedDesc(left)), Opcodes.ACC_PUBLIC) & 0b111;
        int rightAcc = extraClassesInformation.getAccessFlags(right.getOwned().getOwner().mapping.unmappedName,
                right.unmappedName.concat(getUnmappedDesc(right)), Opcodes.ACC_PUBLIC) & 0b111;
        // 0b101 = Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED
        if((leftAcc & 0b101) != 0) return left;
        else if((rightAcc & 0b101) != 0) return right;
        else if(leftAcc == Opcodes.ACC_PRIVATE || rightAcc == Opcodes.ACC_PRIVATE)
            throw new IllegalArgumentException("This can't happen!");
        else throw new IllegalArgumentException("Method duplicated... This should not happen!");
    }

    private static boolean nameAndDescEquals(PairedMapping left, PairedMapping right) {
        boolean b = left.unmappedName.equals(right.unmappedName) && left.mappedName.equals(right.mappedName);
        if(left.hasComponent(Descriptor.class)) {
            if(!right.hasComponent(Descriptor.class)) return false;
            b &= left.getComponent(Descriptor.class).getUnmappedDescriptor().equals(right.getComponent(Descriptor.class).getUnmappedDescriptor());
        }
        if(left.hasComponent(Descriptor.Mapped.class)) {
            if(!right.hasComponent(Descriptor.Mapped.class)) return false;
            b &= left.getComponent(Descriptor.Mapped.class).mappedDescriptor.equals(right.getComponent(Descriptor.Mapped.class).mappedDescriptor);
        }
        return b;
    }

    @Override
    public String mapRecordComponentName(String owner, String name, String descriptor) {
        return mapFieldName(owner, name, descriptor);
    }

    @Override
    public String mapFieldName(String owner, String name, String descriptor) {
        return Optional.ofNullable(fieldByUnm.get(owner))
                .map(map -> map.get(name))
                .or(() -> processSuperField(owner, name))
                .map(m -> m.mappedName).orElse(name);
    }

    private Optional<PairedMapping> processSuperField(String owner, String name) {
        if(extraClassesInformation == null) throw new UnsupportedOperationException("Constructor ClassifiedMappingRemapper(AbstractMappingReader) is only " +
                "for reversing mapping. For remapping, please use ClassifiedMappingRemapper(AbstractMappingReader, ExtraClassesInformation)");
        return Optional.ofNullable(extraClassesInformation.getSuperNames(owner))
                .flatMap(superNames -> superNames.parallelStream()
                        .map(fieldByUnm::get)
                        .filter(Objects::nonNull)
                        .map(map -> map.get(name))
                        .filter(Objects::nonNull)
                        .reduce(this::reduceField)
                        .or(() -> superNames.parallelStream()
                                .map(mappingByUnm::get)
                                .filter(Objects::nonNull)
                                .map(cm -> processSuperField(cm.mapping.unmappedName, name))
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .reduce(this::reduceField)
                        )
                );
    }

    private PairedMapping reduceField(PairedMapping left, PairedMapping right) {
        // 0b111 = Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED | Opcodes.ACC_PRIVATE
        int leftAcc = extraClassesInformation.getAccessFlags(left.getOwned().getOwner().mapping.unmappedName,
                left.unmappedName, Opcodes.ACC_PUBLIC) & 0b111;
        int rightAcc = extraClassesInformation.getAccessFlags(right.getOwned().getOwner().mapping.unmappedName,
                right.unmappedName, Opcodes.ACC_PUBLIC) & 0b111;
        // 0b101 = Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED
        if((leftAcc & 0b101) != 0) return left;
        else if((rightAcc & 0b101) != 0) return right;
        else if(leftAcc == Opcodes.ACC_PRIVATE || rightAcc == Opcodes.ACC_PRIVATE)
            throw new IllegalArgumentException("This can't happen!");
        throw new IllegalArgumentException("Field duplicated... This should not happen!");
    }
}