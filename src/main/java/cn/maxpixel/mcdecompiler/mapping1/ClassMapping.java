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

package cn.maxpixel.mcdecompiler.mapping1;

import cn.maxpixel.mcdecompiler.asm.ClassifiedMappingRemapper;
import cn.maxpixel.mcdecompiler.mapping1.component.Descriptor;
import cn.maxpixel.mcdecompiler.mapping1.component.LocalVariableTable;
import cn.maxpixel.mcdecompiler.mapping1.component.Owned;
import cn.maxpixel.mcdecompiler.reader.ClassifiedMappingReader;
import cn.maxpixel.mcdecompiler.util.Utils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A mapping could contain members. Represents a class and its members' mappings
 * @param <T> The type of this class mapping
 */
public final class ClassMapping<T extends Mapping> {
    /**
     * The mapping for this class
     */
    public T mapping;
    private final ObjectArrayList<T> methods = new ObjectArrayList<>();
    private final ObjectArrayList<T> fields = new ObjectArrayList<>();

    /**
     * Constructor
     * @param mapping The mapping for this class
     */
    public ClassMapping(T mapping) {
        this.mapping = Objects.requireNonNull(mapping);
    }

    /**
     * Add fields to this class mapping
     * @param fields Fields to add
     * @return this class mapping
     */
    public ClassMapping<T> addFields(T... fields) {
        for(T field : fields) addField(field);
        return this;
    }

    /**
     * Add fields to this class mapping
     * @param fields Fields to add
     * @return this class mapping
     */
    public ClassMapping<T> addFields(Collection<? extends T> fields) {
        fields.forEach(this::addField);
        return this;
    }

    /**
     * Add methods to this class mapping
     * @param methods Methods to add
     * @return this class mapping
     */
    public ClassMapping<T> addMethods(T... methods) {
        for(T method : methods) addMethod(method);
        return this;
    }

    /**
     * Add methods to this class mapping
     * @param methods Methods to add
     * @return this class mapping
     */
    public ClassMapping<T> addMethods(Collection<? extends T> methods) {
        methods.forEach(this::addMethod);
        return this;
    }

    /**
     * Add a field to this class mapping
     * @param field Field to add
     * @return this class mapping
     */
    @SuppressWarnings("unchecked")
    public ClassMapping<T> addField(T field) {
        if(!field.isSupported(Owned.class)) throw new UnsupportedOperationException();
        ((Owned<T>) field).setOwner(this);
        fields.add(field);
        return this;
    }

    /**
     * Add a method to this class mapping
     * @param method Method to add
     * @return this class mapping
     */
    @SuppressWarnings("unchecked")
    public ClassMapping<T> addMethod(T method) {
        if(!method.isSupported(Owned.class)) throw new UnsupportedOperationException();
        if(!method.isSupported(Descriptor.class) && !method.isSupported(Descriptor.Mapped.class) &&
                !method.isSupported(Descriptor.Namespaced.class)) throw new UnsupportedOperationException();
        ((Owned<T>) method).setOwner(this);
        methods.add(method);
        return this;
    }

    /**
     * Gets the methods this class mapping currently has<br>
     * <b>NOTE: Adding methods through this list is unsafe</b>
     * @return The methods
     */
    public ObjectList<T> getMethods() {
        return methods;
    }

    /**
     * Gets the fields this class mapping currently has<br>
     * <b>NOTE: Adding fields through this list is unsafe</b>
     * @return The fields
     */
    public ObjectList<T> getFields() {
        return fields;
    }

    /**
     * Reverse the given class mapping<br>
     * <b>WARNING: INTERNAL METHOD. DO NOT CALL. USE {@link cn.maxpixel.mcdecompiler.reader.ClassifiedMappingReader#reverse(ClassifiedMappingReader)}</b>
     * @param mapping Mapping to reverse
     * @param remapper Remapper to remap descriptors
     * @return The given class mapping
     */
    public static ClassMapping<PairedMapping> reverse(ClassMapping<PairedMapping> mapping, ClassifiedMappingRemapper remapper) {
        mapping.mapping.reverse();
        mapping.getMethods().forEach(m -> reverse(m, remapper));
        mapping.getFields().forEach(m -> reverse(m, remapper));
        return mapping;
    }

    private static void reverse(PairedMapping m, ClassifiedMappingRemapper remapper) {
        m.reverse();
        boolean supportDesc = m.isSupported(Descriptor.class);
        boolean supportDescMapped = m.isSupported(Descriptor.Mapped.class);
        if(supportDesc && supportDescMapped) {
            String unmapped = ((Descriptor) m).getUnmappedDescriptor();
            ((Descriptor) m).setUnmappedDescriptor(((Descriptor.Mapped) m).getMappedDescriptor());
            ((Descriptor.Mapped) m).setMappedDescriptor(unmapped);
        } else if(supportDesc) ((Descriptor) m).reverseUnmapped(remapper);
        else if(supportDescMapped) ((Descriptor.Mapped) m).reverseMapped(remapper);
    }

    /**
     * Swap the given class mapping<br>
     * <b>WARNING: INTERNAL METHOD. DO NOT CALL. USE {@link cn.maxpixel.mcdecompiler.reader.ClassifiedMappingReader#swap(ClassifiedMappingReader, String)}}</b>
     * @param mapping Mapping to swap
     * @param remapper Remapper to remap descriptors
     * @param fromNamespace Namespace to swap from
     * @param toNamespace Namespace to swap to
     * @return The given class mapping
     */
    public static ClassMapping<NamespacedMapping> swap(ClassMapping<NamespacedMapping> mapping, ClassifiedMappingRemapper remapper,
                                                       String fromNamespace, String toNamespace) {
        mapping.mapping.swap(fromNamespace, toNamespace);
        mapping.getMethods().forEach(m -> swap(m, remapper, fromNamespace, toNamespace));
        mapping.getFields().forEach(m -> swap(m, remapper, fromNamespace, toNamespace));
        return mapping;
    }

    private static void swap(NamespacedMapping m, ClassifiedMappingRemapper remapper, String fromNamespace, String toNamespace) {
        m.swap(fromNamespace, toNamespace);
        if(m.isSupported(Descriptor.Namespaced.class)) {
            Descriptor.Namespaced n = ((Descriptor.Namespaced) m);
            if(!n.getDescriptorNamespace().equals(fromNamespace)) throw new IllegalArgumentException();
            n.reverseUnmapped(remapper);
        }
        if(m.isSupported(LocalVariableTable.Namespaced.class)) ((LocalVariableTable.Namespaced) m).swapAll(fromNamespace, toNamespace);
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
        return mapping.parallelStream().collect(Collectors.toMap(cm -> cm.mapping.unmappedName,
                Function.identity(), Utils::onKeyDuplicate, Object2ObjectOpenHashMap::new));
    }

    public static Object2ObjectOpenHashMap<String, ClassMapping<NamespacedMapping>> genMappingsByNamespaceMap(
            ObjectList<ClassMapping<NamespacedMapping>> mapping, String namespace) {
        return mapping.parallelStream().collect(Collectors.toMap(m -> m.mapping.getName(namespace),
                Function.identity(), Utils::onKeyDuplicate, Object2ObjectOpenHashMap::new));
    }

    /* Auto-generated equals, hashCode and toString methods */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClassMapping)) return false;
        ClassMapping<?> that = (ClassMapping<?>) o;
        return mapping.equals(that.mapping) && methods.equals(that.methods) && fields.equals(that.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mapping, methods, fields);
    }

    @Override
    public String toString() {
        return "ClassMapping{" +
                "mapping=" + mapping +
                ", methods=" + methods +
                ", fields=" + fields +
                '}';
    }
}