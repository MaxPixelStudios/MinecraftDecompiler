/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile jars.
 * Copyright (C) 2019-2024 MaxPixelStudios(XiaoPangxie732)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cn.maxpixel.mcdecompiler.mapping.collection;

import cn.maxpixel.mcdecompiler.mapping.Mapping;
import cn.maxpixel.mcdecompiler.mapping.NameGetter;
import cn.maxpixel.mcdecompiler.mapping.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.component.Descriptor;
import cn.maxpixel.mcdecompiler.mapping.component.Owned;
import cn.maxpixel.mcdecompiler.mapping.util.DescriptorRemapper;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

/**
 * A {@link ClassMapping} represents a hierarchical mapping structure of a class,
 * including fields, methods, parameters, etc.
 *
 * @param <T> The type of this class mapping
 */
public class ClassMapping<T extends Mapping> {
    /**
     * The mapping for this class
     */
    public T mapping;
    private final ObjectArrayList<@NotNull T> methods = new ObjectArrayList<>();
    private final ObjectArrayList<@NotNull T> fields = new ObjectArrayList<>();
    private final ObjectOpenHashSet<@NotNull T> memberSet = new ObjectOpenHashSet<>();

    /**
     * No-arg constructor
     */
    public ClassMapping() {}

    /**
     * Constructor
     *
     * @param mapping The mapping for this class
     */
    public ClassMapping(T mapping) {
        this.mapping = Objects.requireNonNull(mapping);
    }

    /**
     * Add fields to this class mapping
     *
     * @param fields Fields to add
     * @return this class mapping
     */
    public ClassMapping<T> addFields(T... fields) {
        for (T field : fields) addField(field);
        return this;
    }

    /**
     * Add fields to this class mapping
     *
     * @param fields Fields to add
     * @return this class mapping
     */
    public ClassMapping<T> addFields(Collection<? extends T> fields) {
        fields.forEach(this::addField);
        return this;
    }

    /**
     * Add methods to this class mapping
     *
     * @param methods Methods to add
     * @return this class mapping
     */
    public ClassMapping<T> addMethods(T... methods) {
        for (T method : methods) addMethod(method);
        return this;
    }

    /**
     * Add methods to this class mapping
     *
     * @param methods Methods to add
     * @return this class mapping
     */
    public ClassMapping<T> addMethods(Collection<? extends T> methods) {
        methods.forEach(this::addMethod);
        return this;
    }

    /**
     * Add a field to this class mapping
     *
     * @param field Field to add
     * @return this class mapping
     */
    @SuppressWarnings("unchecked")
    public ClassMapping<T> addField(T field) {
        if (!field.hasComponent(Owned.class)) field.addComponent(new Owned<>(this));
        else field.getComponent(Owned.class).setOwner(this);
        fields.add(field);
        return this;
    }

    /**
     * Add a method to this class mapping
     *
     * @param method Method to add
     * @return this class mapping
     */
    @SuppressWarnings("unchecked")
    public ClassMapping<T> addMethod(T method) {
        if (!method.hasComponent(Descriptor.Unmapped.class) && !method.hasComponent(Descriptor.Mapped.class) &&
                !method.hasComponent(Descriptor.Namespaced.class)) throw new UnsupportedOperationException();
        if (!method.hasComponent(Owned.class)) method.addComponent(new Owned<>(this));
        else method.getComponent(Owned.class).setOwner(this);
        methods.add(method);
        return this;
    }

    /**
     * Gets the methods this class mapping currently has<br>
     *
     * @apiNote You shouldn't add methods through this list
     * @return The methods
     */
    public ObjectList<T> getMethods() {
        return methods;
    }

    /**
     * Gets the fields this class mapping currently has<br>
     *
     * @apiNote You shouldn't add methods through this list
     * @return The fields
     */
    public ObjectList<T> getFields() {
        return fields;
    }

    /**
     * Reverse the given class mapping
     *
     * @param mapping Mapping to reverse
     * @see ClassifiedMapping#reverse()
     * @see ClassifiedMapping#reverse(ClassifiedMapping)
     */
    public static void reverse(ClassMapping<PairedMapping> mapping) {
        mapping.mapping.reverse();
        mapping.getMethods().forEach(PairedMapping::reverse);
        mapping.getFields().forEach(PairedMapping::reverse);
    }

    /**
     * Swap the given class mapping<br>
     * <b>INTERNAL METHOD. DO NOT CALL. USE METHODS LISTED BELOW</b>
     *
     * @param mapping         Mapping to swap
     * @param remapper        Remapper to remap descriptors
     * @param sourceNamespace Namespace to swap from
     * @param targetNamespace Namespace to swap to
     * @see ClassifiedMapping#swap(ClassifiedMapping, String)
     * @see ClassifiedMapping#swap(ClassifiedMapping, String, String)
     * @see ClassifiedMapping#swap(String)
     * @see ClassifiedMapping#swap(String, String)
     */
    @ApiStatus.Internal
    public static void swap(ClassMapping<NamespacedMapping> mapping, DescriptorRemapper remapper,
                            String sourceNamespace, String targetNamespace) {
        mapping.mapping.swap(remapper, sourceNamespace, targetNamespace);
        mapping.getFields().forEach(m -> m.swap(remapper, sourceNamespace, targetNamespace));
        mapping.getMethods().forEach(m -> m.swap(remapper, sourceNamespace, targetNamespace));
    }

    public static void setMappedNamespace(ClassMapping<? extends NameGetter.Namespace> cm, String namespace) {
        cm.mapping.setMappedNamespace(namespace);
        cm.fields.forEach(m -> m.setMappedNamespace(namespace));
        cm.methods.forEach(m -> m.setMappedNamespace(namespace));
    }

    public static void setFallbackNamespace(ClassMapping<? extends NameGetter.Namespace> cm, String namespace) {
        cm.mapping.setFallbackNamespace(namespace);
        cm.fields.forEach(m -> m.setFallbackNamespace(namespace));
        cm.methods.forEach(m -> m.setFallbackNamespace(namespace));
    }

    public boolean mappingEquals(ClassMapping<?> that) {
        if (this == that) return true;
        if (that == null) return false;
        return Objects.equals(mapping, that.mapping);
    }

    public static boolean mappingEquals(ClassMapping<?> a, ClassMapping<?> b) {
        return (a == b) || (a != null && b != null && Objects.equals(a.mapping, b.mapping));
    }

    private ObjectOpenHashSet<@NotNull T> populateMemberSet() {
        if (!memberSet.containsAll(methods) || !memberSet.containsAll(fields)) {// TODO: Profile to see whether this condition is needed
            memberSet.clear();
            memberSet.addAll(methods);
            memberSet.addAll(fields);
        }
        return memberSet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClassMapping<?> that)) return false;
        return Objects.equals(mapping, that.mapping) && methods.size() == that.methods.size() &&
                fields.size() == that.fields.size() && populateMemberSet().containsAll(that.fields) &&
                memberSet.containsAll(that.methods);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mapping, populateMemberSet());
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