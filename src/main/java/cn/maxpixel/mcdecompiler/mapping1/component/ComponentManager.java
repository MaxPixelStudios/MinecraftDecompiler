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

package cn.maxpixel.mcdecompiler.mapping1.component;

import cn.maxpixel.mcdecompiler.mapping1.component.impl.DescriptorImpl;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import java.util.function.Supplier;

public final class ComponentManager {
    static {
        register(Descriptor.class, DescriptorImpl::new);
        register(Descriptor.Mapped.class, DescriptorImpl.MappedImpl::new);
        register(Descriptor.Namespaced.class, DescriptorImpl.NamespacedImpl::new);
    }

    private static final Reference2ObjectOpenHashMap<Class<? extends Component>, Supplier<? extends Component>> COMPONENTS = new Reference2ObjectOpenHashMap<>();

    public static <T extends Component> void register(Class<T> componentClass, Supplier<T> constructor) {
        if(COMPONENTS.putIfAbsent(componentClass, constructor) != null) {
            throw new IllegalArgumentException("Already registered");
        }
    }
}