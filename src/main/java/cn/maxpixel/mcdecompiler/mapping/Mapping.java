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
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

/**
 * Base class of all mappings
 *
 * @implNote This class should only be extended, so it is abstract
 */
public abstract class Mapping implements NameGetter {
    private final Object2ObjectOpenHashMap<Class<? extends Component>, Component> components = new Object2ObjectOpenHashMap<>();

    /**
     * Constructor
     * @param components Components add to this mapping
     */
    protected Mapping(@NotNull Component @NotNull ... components) {
        for(@NotNull Component component : components) {
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
     *
     * @param component Given component type. Cannot be null
     * @return The component if exists, or {@code null}
     */
    @SuppressWarnings("unchecked")
    public final <C extends Component> @Nullable C getComponent(@NotNull Class<? super C> component) {
        return (C) components.get(component);
    }

    /**
     * Gets the component of given type.
     * <p>
     * For the {@link Owned} component, it is recommended to use {@link #getOwned()} instead of this method
     *
     * @param component Given component type. Cannot be null
     * @return The component
     */
    public final <C extends Component> @NotNull Optional<C> getComponentOptional(@NotNull Class<? super C> component) {
        return Optional.ofNullable(getComponent(component));
    }

    /**
     * Gets the {@link Owned} component if it is present
     *
     * @return The {@link Owned} component if it exists, or null
     */
    protected Owned<? extends Mapping> getOwned() {
        return getComponent(Owned.class);
    }

    /**
     * Checks if a component of given class exists.
     *
     * @param component The class of the component
     * @return True if the component exists, false otherwise
     */
    public final boolean hasComponent(@NotNull Class<? extends Component> component) {
        return components.containsKey(component);
    }

    /**
     * Gets all the components of this mapping.
     *
     * @return All the components of this mapping
     */
    public final @NotNull ObjectCollection<? extends Component> getComponents() {
        return components.values();
    }

    /**
     * Adds a component to this mapping.
     *
     * @implNote If a component of the same class exists, replaces that component.
     * @param component The component to add or replace with
     */
    public final void addComponent(@NotNull Component component) {
        this.components.put(component.getClass(), component);
    }

    /**
     * Removes a component from this mapping.
     *
     * @implNote Do nothing if the component does not exist.
     * @param component The class of the component to remove
     */
    public final void removeComponent(@NotNull Class<? extends Component> component) {
        components.remove(Objects.requireNonNull(component));
    }

    /* Auto-generated equals, hashCode and toString methods */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Mapping mapping)) return false;
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