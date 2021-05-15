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

package cn.maxpixel.mcdecompiler.mapping.namespaced;

import cn.maxpixel.mcdecompiler.mapping.AbstractClassMapping;
import cn.maxpixel.mcdecompiler.mapping.paired.PairedClassMapping;
import cn.maxpixel.mcdecompiler.mapping.paired.PairedFieldMapping;
import cn.maxpixel.mcdecompiler.mapping.paired.UnmappedDescriptoredPairedMethodMapping;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

import java.util.Collection;
import java.util.Map;

public class NamespacedClassMapping extends NamespacedMapping implements AbstractClassMapping {
    private final ObjectArrayList<NamespacedMethodMapping> methods = new ObjectArrayList<>();
    private final ObjectArrayList<NamespacedFieldMapping> fields = new ObjectArrayList<>();

    public NamespacedClassMapping(Map<String, String> names) {
        super(names);
    }

    public NamespacedClassMapping(String namespace, String name) {
        super(namespace, name);
    }

    public NamespacedClassMapping(String[] namespaces, String[] names) {
        super(namespaces, names);
    }

    public NamespacedClassMapping(String[] namespaces, String[] names, int nameStart) {
        super(namespaces, names, nameStart);
    }
    public NamespacedClassMapping() {}

    public NamespacedClassMapping addField(Collection<? extends NamespacedFieldMapping> fields) {
        fields.forEach(this::addField);
        return this;
    }

    public NamespacedClassMapping addMethod(Collection<? extends NamespacedMethodMapping> methods) {
        methods.forEach(this::addMethod);
        return this;
    }

    public NamespacedClassMapping addField(NamespacedFieldMapping... fields) {
        for(NamespacedFieldMapping field : fields) addField(field);
        return this;
    }

    public NamespacedClassMapping addMethod(NamespacedMethodMapping... methods) {
        for(NamespacedMethodMapping method : methods) addMethod(method);
        return this;
    }

    public NamespacedClassMapping addField(NamespacedFieldMapping field) {
        this.fields.add(field.setOwner(this));
        return this;
    }

    public NamespacedClassMapping addMethod(NamespacedMethodMapping method) {
        this.methods.add(method.setOwner(this));
        return this;
    }

    public ObjectList<NamespacedMethodMapping> getMethods() {
        return methods;
    }

    public ObjectList<NamespacedFieldMapping> getFields() {
        return fields;
    }

    public PairedClassMapping getAsPaired(String fromNamespace, String toNamespace) {
        PairedClassMapping cm = new PairedClassMapping(getName(fromNamespace), getName(toNamespace));
        getFields().parallelStream().map(f -> new PairedFieldMapping(f.getName(fromNamespace), f.getName(toNamespace)))
                .forEach(cm::addField);
        getMethods().parallelStream().map(m -> new UnmappedDescriptoredPairedMethodMapping(m.getName(fromNamespace), m.getName(toNamespace),
                m.getUnmappedDescriptor())).forEach(cm::addMethod);
        return cm;
    }
}