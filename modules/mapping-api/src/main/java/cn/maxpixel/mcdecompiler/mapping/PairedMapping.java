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

package cn.maxpixel.mcdecompiler.mapping;

import cn.maxpixel.mcdecompiler.mapping.component.Component;
import cn.maxpixel.mcdecompiler.mapping.component.Owned;
import cn.maxpixel.mcdecompiler.mapping.util.Validation;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.Objects;

/**
 * A mapping with an unmapped name and a mapped name
 */
public class PairedMapping extends Mapping {
    /**
     * Unmapped name of this mapping
     */
    public String unmappedName;

    /**
     * Mapped name of this mapping
     */
    public String mappedName;

    /**
     * Constructor
     *
     * @param unmappedName The unmapped name
     * @param mappedName The mapped name
     * @param components Components add to this mapping
     */
    public PairedMapping(String unmappedName, String mappedName, Component... components) {
        super(components);
        this.unmappedName = unmappedName;
        this.mappedName = mappedName;
    }

    /**
     * Constructor
     *
     * @param unmappedName The unmapped name
     * @param mappedName The mapped name
     */
    public PairedMapping(String unmappedName, String mappedName) {
        this.unmappedName = unmappedName;
        this.mappedName = mappedName;
    }

    /**
     * Constructor
     *
     * @param name The name
     * @param components Components add to this mapping
     */
    public PairedMapping(String name, Component... components) {
        super(components);
        this.unmappedName = name;
        this.mappedName = name;
    }

    /**
     * Constructor
     *
     * @param name The name
     */
    public PairedMapping(String name) {
        this.unmappedName = name;
        this.mappedName = name;
    }

    /**
     * Constructor
     *
     * @param components Components add to this mapping
     */
    public PairedMapping(Component... components) {
        super(components);
    }

    /**
     * No-arg constructor
     */
    public PairedMapping() {}

    @SuppressWarnings("unchecked")
    @Override
    public Owned<PairedMapping> getOwned() {
        return getComponent(Owned.class);
    }

    /**
     * Reverse this mapping
     *
     * @return this mapping
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public PairedMapping reverse() {
        String temp = unmappedName;
        unmappedName = mappedName;
        mappedName = temp;
        ObjectOpenHashSet<Class<? extends Component>> skipped = new ObjectOpenHashSet<>();
        Object2ObjectOpenHashMap<Class<? extends Component>, Component> toAdd = new Object2ObjectOpenHashMap<>();
        var it = getComponents().iterator();
        while (it.hasNext()) {
            Component component = it.next();
            if (skipped.contains(component.getClass())) continue;
            if (component instanceof Component.Reversible r) r.reverse();
            if (component instanceof Component.ConvertingReversible c) {
                var targetClass = c.getTarget();
                var targetComponent = getComponent(targetClass);
                if (targetComponent != null) {
                    skipped.add(targetClass);
                    c.reverse(targetComponent);
                } else {
                    var converted = c.convert();
                    toAdd.put(converted.getClass(), converted);
                    it.remove();
                }
            }
        }
        components.putAll(toAdd);
        return this;
    }

    @Override
    public String getUnmappedName() {
        return unmappedName;
    }

    public void setUnmappedName(String unmappedName) {
        this.unmappedName = unmappedName;
    }

    @Override
    public String getMappedName() {
        return mappedName;
    }

    public void setMappedName(String mappedName) {
        this.mappedName = mappedName;
    }

    @Override
    public void validate() throws IllegalStateException {
        Validation.requireNonNull(unmappedName, "unmappedName");
        Validation.requireNonNull(mappedName, "mappedName");
        super.validate();
    }

    /* Auto-generated equals, hashCode and toString methods */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PairedMapping that)) return false;
        if (!super.equals(o)) return false;
        return unmappedName.equals(that.unmappedName) && mappedName.equals(that.mappedName);
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + Objects.hash(unmappedName, mappedName);
    }

    @Override
    public String toString() {
        return "PairedMapping{" +
                "unmappedName='" + unmappedName + '\'' +
                ", mappedName='" + mappedName + '\'' +
                "} " + super.toString();
    }
}