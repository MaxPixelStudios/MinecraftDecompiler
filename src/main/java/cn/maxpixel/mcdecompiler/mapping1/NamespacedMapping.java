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
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;

import java.util.Map;
import java.util.Objects;

/**
 * A mapping with names under namespaces
 */
public class NamespacedMapping extends Mapping {
    // Forge
    public static final String OBF = "obf";
    public static final String SRG = "srg";

    // Fabric
    public static final String OFFICIAL = "official";
    public static final String INTERMEDIARY = "intermediary";
    public static final String YARN = "named";

    private final Object2ObjectOpenHashMap<String, String> names = new Object2ObjectOpenHashMap<>();

    /**
     * Constructor
     * @param names A map keyed with namespace and valued with name
     */
    public NamespacedMapping(Map<String, String> names) {
        if(names.containsKey(null)) throw new IllegalArgumentException();
        this.names.putAll(names);
    }

    /**
     * Constructor
     * @param namespace The namespace
     * @param name The name
     */
    public NamespacedMapping(String namespace, String name) {
        this.names.put(Objects.requireNonNull(namespace), name);
    }

    /**
     * Constructor
     * @param namespaces The namespaces
     * @param names The names
     */
    public NamespacedMapping(String[] namespaces, String[] names) {
        if(namespaces.length != names.length) throw new IllegalArgumentException();
        for(int i = 0; i < namespaces.length; i++) {
            this.names.put(Objects.requireNonNull(namespaces[i]), names[i]);
        }
    }

    /**
     * No-arg constructor
     */
    public NamespacedMapping() {}

    /**
     * Helper constructor. Internally used by mapping readers
     * @param namespaces The namespaces
     * @param names The names
     * @param nameStart To put the names start from the index
     */
    public NamespacedMapping(String[] namespaces, String[] names, int nameStart) {
        if(nameStart < 0 || nameStart >= names.length || namespaces.length != (names.length - nameStart)) throw new IllegalArgumentException();
        for(int i = 0; i < namespaces.length; i++) {
            this.names.put(Objects.requireNonNull(namespaces[i]), names[i + nameStart]);
        }
    }

    /**
     * Constructor
     * @param names A map keyed with namespace and valued with name
     * @param components Components supported by this mapping
     */
    protected NamespacedMapping(Map<String, String> names, Class<? extends Component>... components) {
        super(components);
        if(names.containsKey(null)) throw new IllegalArgumentException();
        this.names.putAll(names);
    }

    /**
     * Constructor
     * @param namespace The namespace
     * @param name The name
     * @param components Components supported by this mapping
     */
    protected NamespacedMapping(String namespace, String name, Class<? extends Component>... components) {
        super(components);
        this.names.put(Objects.requireNonNull(namespace), name);
    }

    /**
     * Constructor
     * @param namespaces The namespaces
     * @param names The names
     * @param components Components supported by this mapping
     */
    protected NamespacedMapping(String[] namespaces, String[] names, Class<? extends Component>... components) {
        super(components);
        if(namespaces.length != names.length) throw new IllegalArgumentException();
        for(int i = 0; i < namespaces.length; i++) {
            this.names.put(Objects.requireNonNull(namespaces[i]), names[i]);
        }
    }

    /**
     * Constructor
     * @param components Components supported by this mapping
     */
    protected NamespacedMapping(Class<? extends Component>... components) {
        super(components);
    }

    /**
     * Helper constructor. Internally used by mapping readers
     * @param namespaces The namespaces
     * @param names The names
     * @param nameStart To put the names start from the index
     * @param components Components supported by this mapping
     */
    protected NamespacedMapping(String[] namespaces, String[] names, int nameStart, Class<? extends Component>... components) {
        super(components);
        if(nameStart < 0 || nameStart >= names.length || namespaces.length != (names.length - nameStart)) throw new IllegalArgumentException();
        for(int i = 0; i < namespaces.length; i++) {
            this.names.put(Objects.requireNonNull(namespaces[i]), names[i + nameStart]);
        }
    }

    /**
     * Gets the namespaces this mapping currently has
     * <p>Note: Removing values or clearing would also change the names map</p>
     * @return The namespaces this mapping currently has
     */
    public ObjectSet<String> getNamespaces() {
        return names.keySet();
    }

    /**
     * Sets the name under the given namespace
     * @param namespace The namespace the name is under
     * @param name The name to set
     */
    public void setName(String namespace, String name) {
        names.put(Objects.requireNonNull(namespace), name);
    }

    /**
     * Gets the name under the given namespace
     * @param namespace The namespace the name is under
     * @return The name under the given namespace
     */
    public String getName(String namespace) {
        return names.get(Objects.requireNonNull(namespace));
    }

    /**
     * Gets the name under the given namespace
     * @param namespace The namespace the name is under
     * @param defaultValue Return this value if there is no name under the given namespace
     * @return The name under the given namespace if exists, otherwise the given default value
     */
    public String getName(String namespace, String defaultValue) {
        return names.getOrDefault(Objects.requireNonNull(namespace), defaultValue);
    }

    /**
     * Swap the names under the given two namespaces of this mapping
     * @param fromNamespace The first namespace
     * @param toNamespace The second namespace
     * @return this mapping
     */
    public NamespacedMapping swap(String fromNamespace, String toNamespace) {
        names.put(Objects.requireNonNull(toNamespace), names.put(Objects.requireNonNull(fromNamespace), names.get(toNamespace)));
        return this;
    }

    /**
     * Determines if this mapping contains the given namespace
     * @param namespace the namespace to determine
     * @return if this mapping contains the given namespace
     */
    public boolean containsNamespace(String namespace) {
        return names.containsKey(Objects.requireNonNull(namespace));
    }

    /* Auto-generated equals, hashCode and toString methods */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NamespacedMapping)) return false;
        if (!super.equals(o)) return false;
        NamespacedMapping that = (NamespacedMapping) o;
        return names.equals(that.names);
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + Objects.hash(names);
    }

    @Override
    public String toString() {
        return "NamespacedMapping{" +
                "names=" + names +
                "} " + super.toString();
    }
}