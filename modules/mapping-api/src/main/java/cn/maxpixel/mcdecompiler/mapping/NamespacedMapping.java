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
import cn.maxpixel.mcdecompiler.mapping.util.MappingUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

/**
 * A mapping with names under namespaces
 */
public class NamespacedMapping extends Mapping implements NameGetter.Namespace {
    // Forge
    public static final String OBF = "obf";
    public static final String SRG = "srg";

    // Fabric
    public static final String OFFICIAL = "official";
    public static final String INTERMEDIARY = "intermediary";
    public static final String YARN = "named";

    private String unmappedNamespace;
    private String mappedNamespace;
    private String fallbackNamespace;
    private final Object2ObjectLinkedOpenHashMap<String, String> names = new Object2ObjectLinkedOpenHashMap<>();

    /**
     * Constructor
     *
     * @param names A map keyed with namespace and valued with name
     */
    public NamespacedMapping(Map<String, String> names) {
        if (names.containsKey(null)) throw new IllegalArgumentException();
        this.names.putAll(names);
    }

    /**
     * Constructor
     *
     * @param namespace The namespace
     * @param name The name
     */
    public NamespacedMapping(String namespace, String name) {
        this.names.put(Objects.requireNonNull(namespace), name);
    }

    /**
     * Constructor
     *
     * @param namespaces The namespaces
     * @param names The names
     */
    public NamespacedMapping(String[] namespaces, String[] names) {
        if (namespaces.length != names.length) throw new IllegalArgumentException();
        for (int i = 0; i < namespaces.length; i++) {
            this.names.put(Objects.requireNonNull(namespaces[i]), names[i]);
        }
    }

    /**
     * No-arg constructor
     */
    public NamespacedMapping() {}

    /**
     * Helper constructor. Internally used by mapping readers
     *
     * @param namespaces The namespaces
     * @param names The names
     * @param nameStart To put the names start from the index
     */
    public NamespacedMapping(String[] namespaces, String[] names, int nameStart) {
        // namespaces.length != (names.length - Objects.checkIndex(nameStart, names.length))
        if (names.length - namespaces.length != Objects.checkIndex(nameStart, names.length))
            throw new IllegalArgumentException();
        for (int i = 0; i < namespaces.length; i++) {
            this.names.put(Objects.requireNonNull(namespaces[i]), names[i + nameStart]);
        }
    }

    /**
     * Helper constructor. Internally used by mapping readers
     *
     * @param namespaces The namespaces
     * @param name The name
     */
    public NamespacedMapping(String[] namespaces, String name) {
        for (String namespace : namespaces) {
            this.names.put(Objects.requireNonNull(namespace), name);
        }
    }

    /**
     * Constructor
     *
     * @param names A map keyed with namespace and valued with name
     * @param components Components to add to this mapping
     */
    public NamespacedMapping(Map<String, String> names, Component... components) {
        super(components);
        if (names.containsKey(null)) throw new IllegalArgumentException();
        this.names.putAll(names);
    }

    /**
     * Constructor
     *
     * @param namespace The namespace
     * @param name The name
     * @param components Components to add to this mapping
     */
    public NamespacedMapping(String namespace, String name, Component... components) {
        super(components);
        this.names.put(Objects.requireNonNull(namespace), name);
    }

    /**
     * Constructor
     *
     * @param namespaces The namespaces
     * @param names The names
     * @param components Components to add to this mapping
     */
    public NamespacedMapping(String[] namespaces, String[] names, Component... components) {
        super(components);
        if (namespaces.length != names.length) throw new IllegalArgumentException();
        for (int i = 0; i < namespaces.length; i++) {
            this.names.put(Objects.requireNonNull(namespaces[i]), names[i]);
        }
    }

    /**
     * Constructor
     *
     * @param components Components to add to this mapping
     */
    public NamespacedMapping(Component... components) {
        super(components);
    }

    /**
     * Helper constructor. Internally used by mapping readers
     *
     * @param namespaces The namespaces
     * @param names The names
     * @param nameStart To put the names start from the index
     * @param components Components to add to this mapping
     */
    public NamespacedMapping(String[] namespaces, String[] names, int nameStart, Component... components) {
        super(components);
        // namespaces.length != (names.length - Objects.checkIndex(nameStart, names.length))
        if (names.length - namespaces.length != Objects.checkIndex(nameStart, names.length))
            throw new IllegalArgumentException();
        for (int i = 0; i < namespaces.length; i++) {
            this.names.put(Objects.requireNonNull(namespaces[i]), names[i + nameStart]);
        }
    }

    /**
     * Helper constructor. Internally used by mapping readers
     *
     * @param namespaces The namespaces
     * @param name The name
     * @param components Components to add to this mapping
     */
    public NamespacedMapping(String[] namespaces, String name, Component... components) {
        super(components);
        for (String namespace : namespaces) {
            this.names.put(Objects.requireNonNull(namespace), name);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Owned<NamespacedMapping> getOwned() {
        return getComponent(Owned.class);
    }

    /**
     * Gets the namespaces this mapping currently has
     *
     * @apiNote Removing values or clearing would also change the names map
     * @return The namespaces this mapping currently has
     */
    public @NotNull ObjectSet<String> getNamespaces() {
        return names.keySet();
    }

    /**
     * Sets the name under the given namespace
     *
     * @param namespace The namespace the name is under
     * @param name The name to set
     */
    public void setName(@NotNull String namespace, @Nullable String name) {
        names.put(Objects.requireNonNull(namespace), name);
    }

    /**
     * Gets the name under the given namespace
     *
     * @param namespace The namespace the name is under
     * @return The name under the given namespace
     */
    public String getName(@NotNull String namespace) {
        return names.get(Objects.requireNonNull(namespace));
    }

    /**
     * Gets the name under the given namespace
     *
     * @param namespace The namespace the name is under
     * @param defaultValue Return this value if there is no name under the given namespace
     * @return The name under the given namespace if exists, otherwise the given default value
     */
    public String getName(@NotNull String namespace, @Nullable String defaultValue) {
        return names.getOrDefault(Objects.requireNonNull(namespace), defaultValue);
    }

    /**
     * Swap the names under the given two namespaces of this mapping
     *
     * @param fromNamespace The first namespace
     * @param toNamespace The second namespace
     * @return this mapping
     */
    public NamespacedMapping swap(@NotNull String fromNamespace, @NotNull String toNamespace) {
        names.put(Objects.requireNonNull(toNamespace), names.put(Objects.requireNonNull(fromNamespace), names.get(toNamespace)));
        for (Component component : getComponents()) {
            if (component instanceof Component.Swappable s) s.swap(fromNamespace, toNamespace);
        }
        return this;
    }

    /**
     * Determines if this mapping contains the given namespace
     *
     * @param namespace the namespace to determine
     * @return if this mapping contains the given namespace
     */
    public boolean contains(@NotNull String namespace) {
        return names.containsKey(Objects.requireNonNull(namespace));
    }

    @Override
    public String getUnmappedName() {
        if (unmappedNamespace == null) throw new IllegalStateException("Set a namespace for unmapped name first");
        return names.get(unmappedNamespace);
    }

    @Override
    public String getMappedName() {
        if (mappedNamespace == null) throw new IllegalStateException("Set a namespace for mapped name first");
        var name = names.get(mappedNamespace);
        if (fallbackNamespace == null || MappingUtils.isStringNotBlank(name)) return name;
        return names.get(fallbackNamespace);
    }

    @Override
    public String getUnmappedNamespace() {
        return unmappedNamespace;
    }

    @Override
    public String getMappedNamespace() {
        return mappedNamespace;
    }

    @Override
    public void setUnmappedNamespace(@NotNull String namespace) {
        this.unmappedNamespace = Objects.requireNonNull(namespace);
        for (Component component : getComponents()) {
            if (component instanceof NameGetter.Namespace n) n.setUnmappedNamespace(namespace);
        }
    }

    @Override
    public void setMappedNamespace(@NotNull String namespace) {
        this.mappedNamespace = Objects.requireNonNull(namespace);
        for (Component component : getComponents()) {
            if (component instanceof NameGetter.Namespace n) n.setMappedNamespace(namespace);
        }
    }

    @Override
    public String getFallbackNamespace() {
        return fallbackNamespace;
    }

    @Override
    public void setFallbackNamespace(@NotNull String namespace) {
        this.fallbackNamespace = Objects.requireNonNull(namespace);
        for (Component component : getComponents()) {
            if (component instanceof NameGetter.Namespace n) n.setFallbackNamespace(namespace);
        }
    }

    /* Auto-generated equals, hashCode and toString methods */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NamespacedMapping that)) return false;
        if (!super.equals(o)) return false;
        return names.equals(that.names);
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + Objects.hashCode(names);
    }

    @Override
    public String toString() {
        return "NamespacedMapping{" +
                "names=" + names +
                "} " + super.toString();
    }
}