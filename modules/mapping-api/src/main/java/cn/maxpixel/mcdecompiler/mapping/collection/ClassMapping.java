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
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
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
//    private final ObjectArrayList<@NotNull T> methods = new ObjectArrayList<>();// TODO: Remove this after passing the tests
//    private final ObjectArrayList<@NotNull T> fields = new ObjectArrayList<>();
    private final ObjectLinkedOpenHashSet<@NotNull T> methods = new ObjectLinkedOpenHashSet<>();
    private final ObjectLinkedOpenHashSet<@NotNull T> fields = new ObjectLinkedOpenHashSet<>();

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
//    public ObjectList<T> getMethods() {
    public ObjectSet<T> getMethods() {// TODO: Remove this after passing the tests
        return methods;
    }

    /**
     * Gets the fields this class mapping currently has<br>
     *
     * @apiNote You shouldn't add methods through this list
     * @return The fields
     */
//    public ObjectList<T> getFields() {
    public ObjectSet<T> getFields() {// TODO: Remove this after passing the tests
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

    /* Auto-generated equals, hashCode and toString methods */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClassMapping<?> that)) return false;
        boolean res = Objects.equals(mapping, that.mapping) && methods.equals(that.methods) && fields.equals(that.fields);
        if (!res && Objects.equals(mapping, that.mapping)) {
            System.err.println("Not equals(method=" + methods.equals(that.methods) + ",field=" + fields.equals(that.fields) + "): this: <" + this + ">                                       o: <" + o + ">");
            for (@NotNull T method : methods) {
                if (!that.methods.contains(method)) {
                    System.err.println("Not contains: a<" + method + ">");
                }
            }
            System.err.println(methods.first().equals(that.methods.first()));
            System.err.println(that.methods.first().equals(methods.first()));
            System.err.println(that.methods.contains(methods.first()));
            System.err.println(that.methods.contains(that.methods.first()));
            System.err.println(methods.contains(methods.first()));
            System.err.println(methods.contains(that.methods.first()));
//            that.methods.en
            System.err.println(methods.first().hashCode());
            System.err.println(that.methods.first().hashCode());
            System.err.println(methods.contains(methods.first()));
            System.err.println(that.methods.contains(that.methods.first()));
            ObjectLinkedOpenHashSet<Object> set = new ObjectLinkedOpenHashSet<>();
            for (@NotNull T method : methods) {
                set.add(method);
            }
            System.err.println(set.contains(that.methods.first()));
        }
        return res;
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
                "} \n";
    }
}