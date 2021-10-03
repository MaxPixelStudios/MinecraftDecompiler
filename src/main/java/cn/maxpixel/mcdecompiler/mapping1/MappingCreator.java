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

import cn.maxpixel.mcdecompiler.mapping1.component.*;

import java.util.Map;

/**
 * Create supported field and method mappings easily
 */
public final class MappingCreator {
    public static final class Paired {
        public static PairedMapping newOwned() {
            return new PairedImpl(Owned.class);
        }

        public static PairedMapping newOwned(String unmappedName, String mappedName) {
            return new PairedImpl(unmappedName, mappedName, Owned.class);
        }

        public static PairedMapping newDescriptorOwned() {
            return new PairedImpl(Owned.class, Descriptor.class);
        }

        public static PairedMapping newDescriptorOwned(String unmappedName, String mappedName) {
            return new PairedImpl(unmappedName, mappedName, Owned.class, Descriptor.class);
        }

        public static PairedMapping newDescriptorOwned(String unmappedName, String mappedName, String unmappedDescriptor) {
            PairedImpl m = new PairedImpl(unmappedName, mappedName, Owned.class, Descriptor.class);
            m.setUnmappedDescriptor(unmappedDescriptor);
            return m;
        }

        public static PairedMapping newMappedDescriptorOwned() {
            return new PairedImpl(Owned.class, Descriptor.Mapped.class);
        }

        public static PairedMapping newMappedDescriptorOwned(String unmappedName, String mappedName) {
            return new PairedImpl(unmappedName, mappedName, Owned.class, Descriptor.Mapped.class);
        }

        public static PairedMapping newMappedDescriptorOwned(String unmappedName, String mappedName, String mappedDescriptor) {
            PairedImpl m = new PairedImpl(unmappedName, mappedName, Owned.class, Descriptor.Mapped.class);
            m.setMappedDescriptor(mappedDescriptor);
            return m;
        }

        public static PairedMapping newLineNumberMappedDescriptorOwned(String unmappedName, String mappedName, String mappedDescriptor) {
            PairedImpl m = new PairedImpl(unmappedName, mappedName, Owned.class, Descriptor.Mapped.class, LineNumber.class);
            m.setMappedDescriptor(mappedDescriptor);
            return m;
        }

        public static PairedMapping newLineNumberMappedDescriptorOwned(String unmappedName, String mappedName, String mappedDescriptor, int startLineNumber, int endLineNumber) {
            PairedImpl m = new PairedImpl(unmappedName, mappedName, Owned.class, Descriptor.Mapped.class, LineNumber.class);
            m.setMappedDescriptor(mappedDescriptor);
            m.setStartLineNumber(startLineNumber);
            m.setEndLineNumber(endLineNumber);
            return m;
        }

        public static PairedMapping newDescriptorsOwned() {
            return new PairedImpl(Owned.class, Descriptor.class, Descriptor.Mapped.class);
        }

        public static PairedMapping newDescriptorsOwned(String unmappedName, String mappedName) {
            return new PairedImpl(unmappedName, mappedName, Owned.class, Descriptor.class, Descriptor.Mapped.class);
        }

        public static PairedMapping newDescriptorsOwned(String unmappedName, String mappedName, String unmappedDescriptor, String mappedDescriptor) {
            PairedImpl m = new PairedImpl(unmappedName, mappedName, Owned.class, Descriptor.class, Descriptor.Mapped.class);
            m.setUnmappedDescriptor(unmappedDescriptor);
            m.setMappedDescriptor(mappedDescriptor);
            return m;
        }
    }

    public static final class Namespaced {
        public static NamespacedMapping newDocumented() {
            return new NamespacedImpl(Documented.class);
        }

        public static NamespacedMapping newDocumented(String namespace, String name) {
            return new NamespacedImpl(namespace, name, Documented.class);
        }

        public static NamespacedMapping newDocumented(String[] namespaces, String[] names) {
            return new NamespacedImpl(namespaces, names, Documented.class);
        }

        public static NamespacedMapping newDocumented(String[] namespaces, String[] names, int nameStart) {
            return new NamespacedImpl(namespaces, names, nameStart, Documented.class);
        }

        public static NamespacedMapping newDocumented(Map<String, String> names) {
            return new NamespacedImpl(names, Documented.class);
        }

        public static NamespacedMapping newOwned() {
            return new NamespacedImpl(Owned.class);
        }

        public static NamespacedMapping newOwned(String namespace, String name) {
            return new NamespacedImpl(namespace, name, Owned.class);
        }

        public static NamespacedMapping newOwned(String[] namespaces, String[] names) {
            return new NamespacedImpl(namespaces, names, Owned.class);
        }

        public static NamespacedMapping newOwned(String[] namespaces, String[] names, int nameStart) {
            return new NamespacedImpl(namespaces, names, nameStart, Owned.class);
        }

        public static NamespacedMapping newOwned(Map<String, String> names) {
            return new NamespacedImpl(names, Owned.class);
        }

        public static NamespacedMapping newDescriptorOwned() {
            return new NamespacedImpl(Owned.class, Descriptor.Namespaced.class);
        }

        public static NamespacedMapping newDescriptorOwned(String namespace, String name) {
            return new NamespacedImpl(namespace, name, Owned.class, Descriptor.Namespaced.class);
        }

        public static NamespacedMapping newDescriptorOwned(String[] namespaces, String[] names) {
            return new NamespacedImpl(namespaces, names, Owned.class, Descriptor.Namespaced.class);
        }

        public static NamespacedMapping newDescriptorOwned(String[] namespaces, String[] names, int nameStart) {
            return new NamespacedImpl(namespaces, names, nameStart, Owned.class, Descriptor.Namespaced.class);
        }

        public static NamespacedMapping newDescriptorOwned(Map<String, String> names) {
            return new NamespacedImpl(names, Owned.class, Descriptor.Namespaced.class);
        }

        public static NamespacedMapping newDescriptorOwned(String namespace, String name, String descriptorNamespace, String descriptor) {
            NamespacedImpl m = new NamespacedImpl(namespace, name, Owned.class, Descriptor.Namespaced.class);
            m.setDescriptorNamespace(descriptorNamespace);
            m.setUnmappedDescriptor(descriptor);
            return m;
        }

        public static NamespacedMapping newDescriptorOwned(String[] namespaces, String[] names, String descriptorNamespace, String descriptor) {
            NamespacedImpl m = new NamespacedImpl(namespaces, names, Owned.class, Descriptor.Namespaced.class);
            m.setDescriptorNamespace(descriptorNamespace);
            m.setUnmappedDescriptor(descriptor);
            return m;
        }

        public static NamespacedMapping newDescriptorOwned(String[] namespaces, String[] names, int nameStart, String descriptorNamespace, String descriptor) {
            NamespacedImpl m = new NamespacedImpl(namespaces, names, nameStart, Owned.class, Descriptor.Namespaced.class);
            m.setDescriptorNamespace(descriptorNamespace);
            m.setUnmappedDescriptor(descriptor);
            return m;
        }

        public static NamespacedMapping newDescriptorOwned(Map<String, String> names, String descriptorNamespace, String descriptor) {
            NamespacedImpl m = new NamespacedImpl(names, Owned.class, Descriptor.Namespaced.class);
            m.setDescriptorNamespace(descriptorNamespace);
            m.setUnmappedDescriptor(descriptor);
            return m;
        }

        public static NamespacedMapping newDocumentedDescriptorOwned() {
            return new NamespacedImpl(Owned.class, Descriptor.Namespaced.class, Documented.class);
        }

        public static NamespacedMapping newDocumentedDescriptorOwned(String namespace, String name) {
            return new NamespacedImpl(namespace, name, Owned.class, Descriptor.Namespaced.class, Documented.class);
        }

        public static NamespacedMapping newDocumentedDescriptorOwned(String[] namespaces, String[] names) {
            return new NamespacedImpl(namespaces, names, Owned.class, Descriptor.Namespaced.class, Documented.class);
        }

        public static NamespacedMapping newDocumentedDescriptorOwned(String[] namespaces, String[] names, int nameStart) {
            return new NamespacedImpl(namespaces, names, nameStart, Owned.class, Descriptor.Namespaced.class, Documented.class);
        }

        public static NamespacedMapping newDocumentedDescriptorOwned(Map<String, String> names) {
            return new NamespacedImpl(names, Owned.class, Descriptor.Namespaced.class, Documented.class);
        }

        public static NamespacedMapping newDocumentedDescriptorOwned(String namespace, String name, String descriptorNamespace, String descriptor) {
            NamespacedImpl m = new NamespacedImpl(namespace, name, Owned.class, Descriptor.Namespaced.class, Documented.class);
            m.setDescriptorNamespace(descriptorNamespace);
            m.setUnmappedDescriptor(descriptor);
            return m;
        }

        public static NamespacedMapping newDocumentedDescriptorOwned(String[] namespaces, String[] names, String descriptorNamespace, String descriptor) {
            NamespacedImpl m = new NamespacedImpl(namespaces, names, Owned.class, Descriptor.Namespaced.class, Documented.class);
            m.setDescriptorNamespace(descriptorNamespace);
            m.setUnmappedDescriptor(descriptor);
            return m;
        }

        public static NamespacedMapping newDocumentedDescriptorOwned(String[] namespaces, String[] names, int nameStart, String descriptorNamespace, String descriptor) {
            NamespacedImpl m = new NamespacedImpl(namespaces, names, nameStart, Owned.class, Descriptor.Namespaced.class, Documented.class);
            m.setDescriptorNamespace(descriptorNamespace);
            m.setUnmappedDescriptor(descriptor);
            return m;
        }

        public static NamespacedMapping newDocumentedDescriptorOwned(Map<String, String> names, String descriptorNamespace, String descriptor) {
            NamespacedImpl m = new NamespacedImpl(names, Owned.class, Descriptor.Namespaced.class, Documented.class);
            m.setDescriptorNamespace(descriptorNamespace);
            m.setUnmappedDescriptor(descriptor);
            return m;
        }

        public static NamespacedMapping newLvtDescriptorOwned() {
            return new NamespacedImpl(Owned.class, Descriptor.Namespaced.class, LocalVariableTable.Namespaced.class);
        }

        public static NamespacedMapping newLvtDescriptorOwned(String namespace, String name) {
            return new NamespacedImpl(namespace, name, Owned.class, Descriptor.Namespaced.class, LocalVariableTable.Namespaced.class);
        }

        public static NamespacedMapping newLvtDescriptorOwned(String[] namespaces, String[] names) {
            return new NamespacedImpl(namespaces, names, Owned.class, Descriptor.Namespaced.class, LocalVariableTable.Namespaced.class);
        }

        public static NamespacedMapping newLvtDescriptorOwned(String[] namespaces, String[] names, int nameStart) {
            return new NamespacedImpl(namespaces, names, nameStart, Owned.class, Descriptor.Namespaced.class, LocalVariableTable.Namespaced.class);
        }

        public static NamespacedMapping newLvtDescriptorOwned(Map<String, String> names) {
            return new NamespacedImpl(names, Owned.class, Descriptor.Namespaced.class, LocalVariableTable.Namespaced.class);
        }

        public static NamespacedMapping newLvtDescriptorOwned(String namespace, String name, String descriptorNamespace, String descriptor) {
            NamespacedImpl m = new NamespacedImpl(namespace, name, Owned.class, Descriptor.Namespaced.class, LocalVariableTable.Namespaced.class);
            m.setDescriptorNamespace(descriptorNamespace);
            m.setUnmappedDescriptor(descriptor);
            return m;
        }

        public static NamespacedMapping newLvtDescriptorOwned(String[] namespaces, String[] names, String descriptorNamespace, String descriptor) {
            NamespacedImpl m = new NamespacedImpl(namespaces, names, Owned.class, Descriptor.Namespaced.class, LocalVariableTable.Namespaced.class);
            m.setDescriptorNamespace(descriptorNamespace);
            m.setUnmappedDescriptor(descriptor);
            return m;
        }

        public static NamespacedMapping newLvtDescriptorOwned(String[] namespaces, String[] names, int nameStart, String descriptorNamespace, String descriptor) {
            NamespacedImpl m = new NamespacedImpl(namespaces, names, nameStart, Owned.class, Descriptor.Namespaced.class, LocalVariableTable.Namespaced.class);
            m.setDescriptorNamespace(descriptorNamespace);
            m.setUnmappedDescriptor(descriptor);
            return m;
        }

        public static NamespacedMapping newLvtDescriptorOwned(Map<String, String> names, String descriptorNamespace, String descriptor) {
            NamespacedImpl m = new NamespacedImpl(names, Owned.class, Descriptor.Namespaced.class, LocalVariableTable.Namespaced.class);
            m.setDescriptorNamespace(descriptorNamespace);
            m.setUnmappedDescriptor(descriptor);
            return m;
        }

        public static NamespacedMapping newDocumentedLvtDescriptorOwned() {
            return new NamespacedImpl(Owned.class, Descriptor.Namespaced.class, LocalVariableTable.Namespaced.class, Documented.class, Documented.LocalVariable.class);
        }

        public static NamespacedMapping newDocumentedLvtDescriptorOwned(String namespace, String name) {
            return new NamespacedImpl(namespace, name, Owned.class, Descriptor.Namespaced.class, LocalVariableTable.Namespaced.class, Documented.class, Documented.LocalVariable.class);
        }

        public static NamespacedMapping newDocumentedLvtDescriptorOwned(String[] namespaces, String[] names) {
            return new NamespacedImpl(namespaces, names, Owned.class, Descriptor.Namespaced.class, LocalVariableTable.Namespaced.class, Documented.class, Documented.LocalVariable.class);
        }

        public static NamespacedMapping newDocumentedLvtDescriptorOwned(String[] namespaces, String[] names, int nameStart) {
            return new NamespacedImpl(namespaces, names, nameStart, Owned.class, Descriptor.Namespaced.class, LocalVariableTable.Namespaced.class, Documented.class, Documented.LocalVariable.class);
        }

        public static NamespacedMapping newDocumentedLvtDescriptorOwned(Map<String, String> names) {
            return new NamespacedImpl(names, Owned.class, Descriptor.Namespaced.class, LocalVariableTable.Namespaced.class, Documented.class, Documented.LocalVariable.class);
        }

        public static NamespacedMapping newDocumentedLvtDescriptorOwned(String namespace, String name, String descriptorNamespace, String descriptor) {
            NamespacedImpl m = new NamespacedImpl(namespace, name, Owned.class, Descriptor.Namespaced.class, LocalVariableTable.Namespaced.class, Documented.class, Documented.LocalVariable.class);
            m.setDescriptorNamespace(descriptorNamespace);
            m.setUnmappedDescriptor(descriptor);
            return m;
        }

        public static NamespacedMapping newDocumentedLvtDescriptorOwned(String[] namespaces, String[] names, String descriptorNamespace, String descriptor) {
            NamespacedImpl m = new NamespacedImpl(namespaces, names, Owned.class, Descriptor.Namespaced.class, LocalVariableTable.Namespaced.class, Documented.class, Documented.LocalVariable.class);
            m.setDescriptorNamespace(descriptorNamespace);
            m.setUnmappedDescriptor(descriptor);
            return m;
        }

        public static NamespacedMapping newDocumentedLvtDescriptorOwned(String[] namespaces, String[] names, int nameStart, String descriptorNamespace, String descriptor) {
            NamespacedImpl m = new NamespacedImpl(namespaces, names, nameStart, Owned.class, Descriptor.Namespaced.class, LocalVariableTable.Namespaced.class, Documented.class, Documented.LocalVariable.class);
            m.setDescriptorNamespace(descriptorNamespace);
            m.setUnmappedDescriptor(descriptor);
            return m;
        }

        public static NamespacedMapping newDocumentedLvtDescriptorOwned(Map<String, String> names, String descriptorNamespace, String descriptor) {
            NamespacedImpl m = new NamespacedImpl(names, Owned.class, Descriptor.Namespaced.class, LocalVariableTable.Namespaced.class, Documented.class, Documented.LocalVariable.class);
            m.setDescriptorNamespace(descriptorNamespace);
            m.setUnmappedDescriptor(descriptor);
            return m;
        }

        public static NamespacedMapping newSiLvtDescriptorOwned() {
            return new NamespacedImpl(Owned.class, Descriptor.Namespaced.class, LocalVariableTable.Namespaced.class, StaticIdentifiable.class);
        }

        public static NamespacedMapping newSiLvtDescriptorOwned(String namespace, String name) {
            return new NamespacedImpl(namespace, name, Owned.class, Descriptor.Namespaced.class, LocalVariableTable.Namespaced.class, StaticIdentifiable.class);
        }

        public static NamespacedMapping newSiLvtDescriptorOwned(String[] namespaces, String[] names) {
            return new NamespacedImpl(namespaces, names, Owned.class, Descriptor.Namespaced.class, LocalVariableTable.Namespaced.class, StaticIdentifiable.class);
        }

        public static NamespacedMapping newSiLvtDescriptorOwned(String[] namespaces, String[] names, int nameStart) {
            return new NamespacedImpl(namespaces, names, nameStart, Owned.class, Descriptor.Namespaced.class, LocalVariableTable.Namespaced.class, StaticIdentifiable.class);
        }

        public static NamespacedMapping newSiLvtDescriptorOwned(Map<String, String> names) {
            return new NamespacedImpl(names, Owned.class, Descriptor.Namespaced.class, LocalVariableTable.Namespaced.class, StaticIdentifiable.class);
        }

        public static NamespacedMapping newSiLvtDescriptorOwned(String namespace, String name, String descriptorNamespace, String descriptor) {
            NamespacedImpl m = new NamespacedImpl(namespace, name, Owned.class, Descriptor.Namespaced.class, LocalVariableTable.Namespaced.class, StaticIdentifiable.class);
            m.setDescriptorNamespace(descriptorNamespace);
            m.setUnmappedDescriptor(descriptor);
            return m;
        }

        public static NamespacedMapping newSiLvtDescriptorOwned(String[] namespaces, String[] names, String descriptorNamespace, String descriptor) {
            NamespacedImpl m = new NamespacedImpl(namespaces, names, Owned.class, Descriptor.Namespaced.class, LocalVariableTable.Namespaced.class, StaticIdentifiable.class);
            m.setDescriptorNamespace(descriptorNamespace);
            m.setUnmappedDescriptor(descriptor);
            return m;
        }

        public static NamespacedMapping newSiLvtDescriptorOwned(String[] namespaces, String[] names, int nameStart, String descriptorNamespace, String descriptor) {
            NamespacedImpl m = new NamespacedImpl(namespaces, names, nameStart, Owned.class, Descriptor.Namespaced.class, LocalVariableTable.Namespaced.class, StaticIdentifiable.class);
            m.setDescriptorNamespace(descriptorNamespace);
            m.setUnmappedDescriptor(descriptor);
            return m;
        }

        public static NamespacedMapping newSiLvtDescriptorOwned(Map<String, String> names, String descriptorNamespace, String descriptor) {
            NamespacedImpl m = new NamespacedImpl(names, Owned.class, Descriptor.Namespaced.class, LocalVariableTable.Namespaced.class, StaticIdentifiable.class);
            m.setDescriptorNamespace(descriptorNamespace);
            m.setUnmappedDescriptor(descriptor);
            return m;
        }
    }
}