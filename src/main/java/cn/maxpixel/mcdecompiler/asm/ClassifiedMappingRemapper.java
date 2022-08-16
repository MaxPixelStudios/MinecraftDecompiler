/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile Minecraft.
 * Copyright (C) 2019-2022  MaxPixelStudios
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

import cn.maxpixel.mcdecompiler.Info;
import cn.maxpixel.mcdecompiler.mapping.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.component.Descriptor;
import cn.maxpixel.mcdecompiler.util.Logging;
import cn.maxpixel.mcdecompiler.util.MappingUtil;
import cn.maxpixel.mcdecompiler.util.NamingUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.intellij.lang.annotations.Pattern;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Remapper;

import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

public class ClassifiedMappingRemapper extends Remapper {
    private static final Logger LOGGER = Logging.getLogger("Remapper");
    private ExtraClassesInformation extraClassesInformation;
    private final Object2ObjectOpenHashMap<String, Object2ObjectOpenHashMap<String, PairedMapping>> fieldByUnm;
    private final Object2ObjectOpenHashMap<String, ClassMapping<PairedMapping>> mappingByUnm;
    private final Object2ObjectOpenHashMap<String, ClassMapping<PairedMapping>> mappingByMap;

    public ClassifiedMappingRemapper(ObjectList<ClassMapping<PairedMapping>> mappings) {
        this.fieldByUnm = ClassMapping.genFieldsByUnmappedNameMap(mappings);
        this.mappingByUnm = ClassMapping.genMappingsByUnmappedNameMap(mappings);
        this.mappingByMap = ClassMapping.genMappingsByMappedNameMap(mappings);
    }

    public ClassifiedMappingRemapper(ObjectList<ClassMapping<NamespacedMapping>> mappings, String targetNamespace) {
        this(mappings, NamingUtil.findSourceNamespace(mappings), targetNamespace);
    }

    public ClassifiedMappingRemapper(ObjectList<ClassMapping<NamespacedMapping>> mappings, String sourceNamespace, String targetNamespace) {
        this(mappings.parallelStream()
                .map(old -> {
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
                }).collect(ObjectArrayList.toList()));
    }

    public ClassifiedMappingRemapper setExtraClassesInformation(ExtraClassesInformation extraClassesInformation) {
        this.extraClassesInformation = Objects.requireNonNull(extraClassesInformation);
        return this;
    }

    @Override
    public String map(String internalName) {
        ClassMapping<PairedMapping> classMapping = mappingByUnm.get(internalName);
        if(classMapping != null) return classMapping.mapping.mappedName;
        return internalName;
    }

    public String mapToUnmapped(@NotNull final Type mappedType) {
        switch(mappedType.getSort()) {
            case Type.ARRAY:
                return "[".repeat(mappedType.getDimensions()) + mapToUnmapped(mappedType.getElementType());
            case Type.OBJECT:
                ClassMapping<PairedMapping> cm = mappingByMap.get(mappedType.getInternalName());
                return cm != null ? Type.getObjectType(cm.mapping.unmappedName).getDescriptor() : mappedType.getDescriptor();
            default:
                return mappedType.getDescriptor();
        }
    }

    public String getUnmappedDescByMappedDesc(@Subst("()V") @NotNull @Pattern(Info.METHOD_DESC_PATTERN) String mappedDescriptor) {
        if(mappedDescriptor.startsWith("()") && mappedDescriptor.charAt(2) != 'L' && mappedDescriptor.charAt(2) != '[') {
            return mappedDescriptor;
        }
        StringBuilder stringBuilder = new StringBuilder("(");
        if(mappedDescriptor.charAt(1) != ')') for(Type argumentType : Type.getArgumentTypes(mappedDescriptor)) {
            stringBuilder.append(mapToUnmapped(argumentType));
        }
        return stringBuilder.append(')').append(mapToUnmapped(Type.getReturnType(mappedDescriptor))).toString();
    }

    public String mapToMapped(@NotNull final Type unmappedType) {
        switch(unmappedType.getSort()) {
            case Type.ARRAY:
                return "[".repeat(Math.max(0, unmappedType.getDimensions())) + mapToMapped(unmappedType.getElementType());
            case Type.OBJECT:
                ClassMapping<PairedMapping> cm = mappingByUnm.get(unmappedType.getInternalName());
                return cm != null ? Type.getObjectType(cm.mapping.mappedName).getDescriptor() : unmappedType.getDescriptor();
            default:
                return unmappedType.getDescriptor();
        }
    }

    public String getMappedDescByUnmappedDesc(@Subst("()V") @NotNull @Pattern(Info.METHOD_DESC_PATTERN) String unmappedDescriptor) {
        if (unmappedDescriptor.startsWith("()") && unmappedDescriptor.charAt(2) != 'L' && unmappedDescriptor.charAt(2) != '[') {
            return unmappedDescriptor;
        }
        StringBuilder stringBuilder = new StringBuilder("(");
        if(unmappedDescriptor.charAt(1) != ')') for(Type argumentType : Type.getArgumentTypes(unmappedDescriptor)) {
            stringBuilder.append(mapToMapped(argumentType));
        }
        return stringBuilder.append(')').append(mapToMapped(Type.getReturnType(unmappedDescriptor))).toString();
    }

    private String getUnmappedDesc(PairedMapping mapping) {
        if(mapping.hasComponent(Descriptor.class)) return mapping.getComponent(Descriptor.class).unmappedDescriptor;
        else if(mapping.hasComponent(Descriptor.Mapped.class))
            return getUnmappedDescByMappedDesc(mapping.getComponent(Descriptor.Mapped.class).mappedDescriptor);
        else throw new IllegalArgumentException("Mapping for methods must support at least one of Descriptor or Descriptor.Mapped");
    }

    @Override
    public String mapMethodName(String owner, String name, String descriptor) {
        if(name.charAt(0) != '<') { // equivalent to !(name.equals("<init>") || name.equals("<clinit>"))
            return Optional.ofNullable(mappingByUnm.get(owner))
                    .flatMap(cm -> cm.getMethods().parallelStream()
                            .filter(m -> m.unmappedName.equals(name) && getUnmappedDesc(m).equals(descriptor))
                            .reduce((left, right) -> {throw new IllegalArgumentException("Method duplicated... This should not happen!");})
                    ).or(() -> processSuperMethod(owner, name, descriptor))
                    .map(m -> m.mappedName).orElse(name);
        }
        return name;
    }

    private Optional<PairedMapping> processSuperMethod(String owner, String name, String descriptor) {
        if(extraClassesInformation == null) throw new UnsupportedOperationException("Constructor ClassifiedMappingRemapper(AbstractMappingReader) is only " +
                "for reversing mapping. For remapping, please use ClassifiedMappingRemapper(AbstractMappingReader, ExtraClassesInformation)");
        return Optional.ofNullable(extraClassesInformation.getSuperNames(owner))
                .flatMap(superNames -> {
                    ObjectArrayList<ClassMapping<PairedMapping>> cms = superNames.stream()
                            .map(mappingByUnm::get)
                            .filter(Objects::nonNull)
                            .collect(ObjectArrayList.toList());
                    return cms.parallelStream()
                            .flatMap(cm -> cm.getMethods().stream())
                            .filter(m -> m.unmappedName.equals(name) && getUnmappedDesc(m).equals(descriptor))
                            .reduce(this::reduceMethod)
                            .or(() -> cms.parallelStream()
                                    .map(cm -> processSuperMethod(cm.mapping.unmappedName, name, descriptor))
                                    .filter(Optional::isPresent)
                                    .map(Optional::get)
                                    .reduce(this::reduceMethod)
                            );
                });
    }

    private PairedMapping reduceMethod(@NotNull PairedMapping left, @NotNull PairedMapping right) {
        if(left == right || nameAndDescEquals(left, right)) return left;
        int leftAcc = extraClassesInformation.getAccessFlags(left.getOwned().getOwner().mapping.unmappedName,
                left.unmappedName.concat(getUnmappedDesc(left)));
        int rightAcc = extraClassesInformation.getAccessFlags(right.getOwned().getOwner().mapping.unmappedName,
                right.unmappedName.concat(getUnmappedDesc(right)));
        if((leftAcc & (Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED)) != 0) {
            if((rightAcc & (Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED)) != 0) throw new IllegalArgumentException("This can't happen!");
            return left;
        } else if((rightAcc & (Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED)) != 0) return right;
        else if(Modifier.isPrivate(leftAcc) || Modifier.isPrivate(rightAcc)) throw new IllegalArgumentException("This can't happen!");
        else throw new IllegalArgumentException("Method duplicated... This should not happen!");
    }

    private static boolean nameAndDescEquals(@NotNull PairedMapping left, @NotNull PairedMapping right) {
        if(left.unmappedName.equals(right.unmappedName) && left.mappedName.equals(right.mappedName)) {
            // Will return false if they have no descriptor components
            boolean leftD = left.hasComponent(Descriptor.class), leftDM = left.hasComponent(Descriptor.Mapped.class),
                    rightD = right.hasComponent(Descriptor.class), rightDM = right.hasComponent(Descriptor.Mapped.class);
            if(leftD && rightD) {
                boolean b = left.getComponent(Descriptor.class).equals(right.getComponent(Descriptor.class));
                if(leftDM && rightDM) {
                    return b && left.getComponent(Descriptor.Mapped.class).equals(right.getComponent(Descriptor.Mapped.class));
                } else if(leftDM || rightDM) return false;
                return b;
            } else if(leftD || rightD) return false;
            else if(leftDM && rightDM) return left.getComponent(Descriptor.Mapped.class).equals(right.getComponent(Descriptor.Mapped.class));
        }
        return false;
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
                .map(PairedMapping::getMappedName).orElse(name);
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
        int leftAcc = extraClassesInformation.getAccessFlags(left.getOwned().owner.mapping.unmappedName,
                left.unmappedName) & (Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED | Opcodes.ACC_PRIVATE);
        int rightAcc = extraClassesInformation.getAccessFlags(right.getOwned().owner.mapping.unmappedName,
                right.unmappedName) & (Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED | Opcodes.ACC_PRIVATE);
        if((leftAcc & (Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED)) != 0) {
            if((rightAcc & (Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED)) != 0) throw new IllegalArgumentException("This can't happen!");
            return left;
        } else if((rightAcc & (Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED)) != 0) return right;
        else if(Modifier.isPrivate(leftAcc) || Modifier.isPrivate(rightAcc))
            throw new IllegalArgumentException("This can't happen!");
        throw new IllegalArgumentException("Field duplicated... This should not happen!");
    }
}