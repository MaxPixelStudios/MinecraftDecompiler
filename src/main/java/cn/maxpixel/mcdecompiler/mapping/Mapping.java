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

package cn.maxpixel.mcdecompiler.mapping;

import cn.maxpixel.mcdecompiler.mapping.component.Component;
import cn.maxpixel.mcdecompiler.mapping.component.Owned;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectCollections;

/**
 * Every mapping's base class
 * <p>This class should only be extended, so it is abstract</p>
 */
public abstract class Mapping {
    private final Object2ObjectOpenHashMap<Class<? extends Component>, Component> components = new Object2ObjectOpenHashMap<>();

    /**
     * Constructor
     * @param components Components add to this mapping
     */
    protected Mapping(Component... components) {
        for(Component component : components) {
            this.components.put(component.getClass(), component);
        }
    }

    /**
     * No-arg constructor
     */
    protected Mapping() {}

    /**
     * Gets the component of given type if it is present.<br>
     * For the {@link Owned} component, it is recommended to use {@link #getOwned()} instead of this method
     * @param component Given component type. Cannot be null
     * @return The component if exists, or {@code null}
     */
    public final <C extends Component> C getComponent(Class<? extends C> component) {
        return (C) components.get(component);
    }

    /**
     * Gets the {@link Owned} component if it is present
     * @return The {@link Owned} component if it exists, or null
     */
    protected Owned<? extends Mapping> getOwned() {
        return (Owned<? extends Mapping>) components.get(Owned.class);
    }

    public final boolean hasComponent(Class<? extends Component> component) {
        return components.containsKey(component);
    }

    public final ObjectCollection<? extends Component> getComponents() {
        return ObjectCollections.unmodifiable(components.values());
    }

    public final boolean isPaired() {
        return this instanceof PairedMapping;
    }
    public final boolean isNamespaced() {
        return this instanceof NamespacedMapping;
    }

    public final PairedMapping asPaired() {
        return (PairedMapping) this;
    }
    public final NamespacedMapping asNamespaced() {
        return (NamespacedMapping) this;
    }

    /* Auto-generated equals, hashCode and toString methods */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Mapping)) return false;
        Mapping mapping = (Mapping) o;
        return components.equals(mapping.components);
    }

    @Override
    public int hashCode() {
        return components.hashCode();
    }

    @Override
    public String toString() {
        return "Mapping{" +
                "components=" + components +
                '}';
    }
}