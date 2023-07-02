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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base class of all mappings
 * <p>This class should only be extended, so it is abstract</p>
 */
public abstract class Mapping implements NameGetter {
    private final Object2ObjectOpenHashMap<Class<? extends Component>, Component> components = new Object2ObjectOpenHashMap<>();

    /**
     * Constructor
     * @param components Components add to this mapping
     */
    protected Mapping(@NotNull Component... components) {
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
     * @param component Given component type. Cannot be null
     * @return The component if exists, or {@code null}
     */
    @SuppressWarnings("unchecked")
    public final <C extends Component> @Nullable C getComponent(@NotNull Class<? super C> component) {
        return (C) components.get(component);
    }

    /**
     * Gets the {@link Owned} component if it is present
     * @return The {@link Owned} component if it exists, or null
     */
    protected @Nullable Owned<? extends Mapping> getOwned() {
        return getComponent(Owned.class);
    }

    public final boolean hasComponent(@NotNull Class<? extends Component> component) {
        return components.containsKey(component);
    }

    public final @NotNull ObjectCollection<? extends Component> getComponents() {
        return ObjectCollections.unmodifiable(components.values());
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