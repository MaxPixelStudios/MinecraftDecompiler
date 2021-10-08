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

import cn.maxpixel.mcdecompiler.mapping1.component.Component;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

/**
 * Every mapping's base class
 * <p>This class should only be extended, so it is abstract</p>
 */
public abstract class Mapping {
    private final ReferenceArrayList<Class<? extends Component>> supportedComponents = new ReferenceArrayList<>();

    /**
     * Constructor
     * @param components Components supported by this mapping
     */
    @SafeVarargs
    protected Mapping(Class<? extends Component>... components) {
        supportedComponents.addElements(0, components);
        if(!Arrays.asList(getClass().getInterfaces()).containsAll(supportedComponents))
            throw new UnsupportedOperationException();
    }

    /**
     * No-arg constructor
     */
    protected Mapping() {}

    /**
     * Indicates whether this mapping supports the given component
     * @param component Component to test
     * @return true if this mapping supports the given component
     * @see #isSupported(Class[])
     * @see #isSupported(Collection)
     */
    public final boolean isSupported(Class<? extends Component> component) {
        return supportedComponents.contains(component);
    }

    /**
     * Indicates whether this mapping supports the given components
     * @param components Components to test
     * @return true if this mapping supports all the given components
     * @see #isSupported(Class)
     * @see #isSupported(Collection)
     */
    @SafeVarargs
    public final boolean isSupported(Class<? extends Component>... components) {
        return isSupported(Arrays.asList(components));
    }

    /**
     * Indicates whether this mapping supports the given components
     * @param components Components to test
     * @return true if this mapping supports all the given components
     * @see #isSupported(Class)
     * @see #isSupported(Class[])
     */
    public final boolean isSupported(Collection<Class<? extends Component>> components) {
        return supportedComponents.containsAll(components);
    }

    public final boolean isPairedMapping() {
        return this instanceof PairedMapping;
    }
    public final boolean isNamespacedMapping() {
        return this instanceof NamespacedMapping;
    }

    public final PairedMapping asPairedMapping() {
        return (PairedMapping) this;
    }
    public final NamespacedMapping asNamespacedMapping() {
        return (NamespacedMapping) this;
    }

    /* Auto-generated equals, hashCode and toString methods */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Mapping)) return false;
        Mapping mapping = (Mapping) o;
        return supportedComponents.equals(mapping.supportedComponents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(supportedComponents);
    }

    @Override
    public String toString() {
        return "Mapping{" +
                "supportedComponents=" + supportedComponents +
                '}';
    }
}