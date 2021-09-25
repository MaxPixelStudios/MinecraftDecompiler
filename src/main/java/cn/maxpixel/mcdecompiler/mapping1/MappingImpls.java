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
import cn.maxpixel.mcdecompiler.util.LambdaUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

class PairedImpl extends PairedMapping implements Descriptor, Descriptor.Mapped, LineNumber, Owned<PairedMapping> {
    private String unmappedDescriptor;
    private final boolean sU = isSupported(Descriptor.class); // support unmapped descriptor, etc.

    private String mappedDescriptor;
    private final boolean sM = isSupported(Descriptor.Mapped.class);

    private final int[] lineNums = new int[2];
    private final boolean sL = isSupported(LineNumber.class);

    private ClassMapping<PairedMapping> owner;
    private final boolean sO = isSupported(Owned.class);

    PairedImpl(String unmappedName, String mappedName, Class<? extends Component>... components) {
        super(unmappedName, mappedName, components);
    }

    PairedImpl(Class<? extends Component>... components) {
        super(components);
    }

    @Override
    public String getUnmappedDescriptor() {
        if(!sU) throw new UnsupportedOperationException();
        return unmappedDescriptor;
    }

    @Override
    public void setUnmappedDescriptor(String unmappedDescriptor) {
        if(!sU) throw new UnsupportedOperationException();
        this.unmappedDescriptor = unmappedDescriptor;
    }

    @Override
    public String getMappedDescriptor() {
        if(!sM) throw new UnsupportedOperationException();
        return mappedDescriptor;
    }

    @Override
    public void setMappedDescriptor(String mappedDescriptor) {
        if(!sM) throw new UnsupportedOperationException();
        this.mappedDescriptor = mappedDescriptor;
    }

    @Override
    public int getStartLineNumber() {
        if(!sL) throw new UnsupportedOperationException();
        return lineNums[0];
    }

    @Override
    public int getEndLineNumber() {
        if(!sL) throw new UnsupportedOperationException();
        return lineNums[1];
    }

    @Override
    public void setStartLineNumber(int ns) {
        if(!sL) throw new UnsupportedOperationException();
        lineNums[0] = ns;
    }

    @Override
    public void setEndLineNumber(int ne) {
        if(!sL) throw new UnsupportedOperationException();
        lineNums[1] = ne;
    }

    @Override
    public ClassMapping<PairedMapping> getOwner() {
        if(!sO) throw new UnsupportedOperationException();
        return owner;
    }

    @Override
    public void setOwner(ClassMapping<PairedMapping> owner) {
        if(!sO) throw new UnsupportedOperationException();
        this.owner = owner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!super.equals(o)) return false;
        if(o instanceof PairedImpl impl) {
            return sU == impl.sU && sM == impl.sM && sL == impl.sL && sO == impl.sO &&
                    Objects.equals(unmappedDescriptor, impl.unmappedDescriptor) &&
                    Objects.equals(mappedDescriptor, impl.mappedDescriptor) &&
                    Arrays.equals(lineNums, impl.lineNums) &&
                    Objects.equals(LambdaUtil.safeCall(owner, owner -> owner.mapping),
                            LambdaUtil.safeCall(impl.owner, owner -> owner.mapping));
        }
        PairedMapping paired = (PairedMapping) o;
        try {
            boolean result = true;
            if(sU) result = Objects.equals(unmappedDescriptor, ((Descriptor) paired).getUnmappedDescriptor());
            if(sM) result &= Objects.equals(mappedDescriptor, ((Descriptor.Mapped) paired).getMappedDescriptor());
            if(sL) {
                result &= lineNums[0] == ((LineNumber) paired).getStartLineNumber();
                result &= lineNums[1] == ((LineNumber) paired).getEndLineNumber();
            }
            if(sO) {
                result &= Objects.equals(LambdaUtil.safeCall(owner, owner -> owner.mapping),
                        LambdaUtil.safeCall(((Owned<PairedMapping>) paired).getOwner(), owner -> owner.mapping));
            }
            return result;
        } catch(ClassCastException cce) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        if(sU) result = 31 * result + Objects.hashCode(unmappedDescriptor);
        if(sM) result = 31 * result + Objects.hashCode(mappedDescriptor);
        if(sL) result = 31 * result + Arrays.hashCode(lineNums);
        if(sO) result = 31 * result + (owner == null ? 0 : Objects.hashCode(owner.mapping));
        return result;
    }

    @Override
    public String toString() {
        return "PairedImpl{" +
                "unmappedDescriptor='" + unmappedDescriptor + '\'' +
                ", sU=" + sU +
                ", mappedDescriptor='" + mappedDescriptor + '\'' +
                ", sM=" + sM +
                ", lineNums=" + Arrays.toString(lineNums) +
                ", sL=" + sL +
                ", owner=" + LambdaUtil.safeCall(owner, owner -> owner.mapping) +
                ", sO=" + sO +
                "} " + super.toString();
    }
}

class NamespacedImpl extends NamespacedMapping implements Descriptor.Namespaced, LocalVariableTable.Namespaced, Documented, Documented.LocalVariable, Owned<NamespacedMapping> {
    private String unmappedDescriptor;
    private String descriptorNamespace;
    private final boolean sU = isSupported(Descriptor.Namespaced.class); // support unmapped descriptor, etc.

    private String doc;
    private final boolean sD = isSupported(Documented.class);

    private final Int2ObjectOpenHashMap<String> lvtDoc = new Int2ObjectOpenHashMap<>();
    private final boolean sDL = isSupported(Documented.LocalVariable.class);

    private final Int2ObjectOpenHashMap<Object2ObjectMap<String, String>> lvt = new Int2ObjectOpenHashMap<>();
    private final boolean sL = isSupported(LocalVariableTable.Namespaced.class);

    private ClassMapping<NamespacedMapping> owner;
    private final boolean sO = isSupported(Owned.class);

    @Override
    public String getUnmappedDescriptor() {
        if(!sU) throw new UnsupportedOperationException();
        return unmappedDescriptor;
    }

    @Override
    public void setUnmappedDescriptor(String unmappedDescriptor) {
        if(!sU) throw new UnsupportedOperationException();
        this.unmappedDescriptor = unmappedDescriptor;
    }

    @Override
    public String getDescriptorNamespace() {
        if(!sU) throw new UnsupportedOperationException();
        return descriptorNamespace;
    }

    @Override
    public void setDescriptorNamespace(String namespace) {
        if(!sU) throw new UnsupportedOperationException();
        if(!containsNamespace(namespace)) throw new IllegalArgumentException();
        this.descriptorNamespace = namespace;
    }

    @Override
    public void setDoc(String doc) {
        if(!sD) throw new UnsupportedOperationException();
        this.doc = doc;
    }

    @Override
    public String getDoc() {
        if(!sD) throw new UnsupportedOperationException();
        return doc;
    }

    @Override
    public void setLocalVariableDoc(int index, String doc) {
        if(!sDL) throw new UnsupportedOperationException();
        lvtDoc.put(index, doc);
    }

    @Override
    public String getLocalVariableDoc(int index) {
        if(!sDL) throw new UnsupportedOperationException();
        return lvtDoc.get(index);
    }

    @Override
    public String getLocalVariableName(int index, String namespace) {
        if(!sL) throw new UnsupportedOperationException();
        return lvt.getOrDefault(index, Object2ObjectMaps.emptyMap()).get(namespace);
    }

    @Override
    public Object2ObjectMap<String, String> getLocalVariableNames(int index) {
        if(!sL) throw new UnsupportedOperationException();
        return Object2ObjectMaps.unmodifiable(lvt.get(index));
    }

    @Override
    public void setLocalVariableName(int index, Map<String, String> names) {
        if(!sL) throw new UnsupportedOperationException();
        if(Objects.requireNonNull(names).containsKey(null)) throw new IllegalArgumentException();
        lvt.computeIfAbsent(index, Object2ObjectOpenHashMap::new).putAll(names);
    }

    @Override
    public void setLocalVariableName(int index, String namespace, String name) {
        if(!sL) throw new UnsupportedOperationException();
        lvt.computeIfAbsent(index, Object2ObjectOpenHashMap::new).put(Objects.requireNonNull(namespace), name);
    }

    @Override
    public void setLocalVariableName(int index, String[] namespaces, String[] names) {
        if(!sL) throw new UnsupportedOperationException();
        if(namespaces.length != names.length) throw new IllegalArgumentException();
        Object2ObjectMap<String, String> map = lvt.computeIfAbsent(index, Object2ObjectOpenHashMap::new);
        for(int i = 0; i < namespaces.length; i++) {
            map.put(Objects.requireNonNull(namespaces[i]), names[i]);
        }
    }

    @Override
    public void setLocalVariableName(int index, String[] namespaces, String[] names, int nameStart) {
        if(!sL) throw new UnsupportedOperationException();
        if(nameStart < 0 || nameStart > names.length || namespaces.length != names.length - nameStart) throw new IllegalArgumentException();
        Object2ObjectMap<String, String> map = lvt.computeIfAbsent(index, Object2ObjectOpenHashMap::new);
        for(int i = 0; i < namespaces.length; i++) {
            map.put(Objects.requireNonNull(namespaces[i]), names[i + nameStart]);
        }
    }

    @Override
    public IntSet getLocalVariableIndexes() {
        if(!sL) throw new UnsupportedOperationException();
        return lvt.keySet();
    }

    @Override
    public ClassMapping<NamespacedMapping> getOwner() {
        if(!sO) throw new UnsupportedOperationException();
        return owner;
    }

    @Override
    public void setOwner(ClassMapping<NamespacedMapping> owner) {
        if(!sO) throw new UnsupportedOperationException();
        this.owner = owner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!super.equals(o)) return false;
        if(o instanceof NamespacedImpl impl) {
            return sU == impl.sU && sD == impl.sD && sDL == impl.sDL && sL == impl.sL && sO == impl.sO &&
                    Objects.equals(unmappedDescriptor, impl.unmappedDescriptor) &&
                    Objects.equals(descriptorNamespace, impl.descriptorNamespace) &&
                    Objects.equals(doc, impl.doc) && lvtDoc.equals(impl.lvtDoc) && lvt.equals(impl.lvt) &&
                    Objects.equals(LambdaUtil.safeCall(owner, owner -> owner.mapping),
                            LambdaUtil.safeCall(impl.owner, owner -> owner.mapping));
        }
        NamespacedMapping that = (NamespacedMapping) o;
        try {
            boolean result = true;
            if(sU) result = Objects.equals(unmappedDescriptor, ((Descriptor.Namespaced) that).getUnmappedDescriptor()) &&
                    Objects.equals(descriptorNamespace, ((Descriptor.Namespaced) that).getDescriptorNamespace());
            if(sD) result &= Objects.equals(doc, ((Documented) that).getDoc());
            if(sDL) for(IntIterator it = lvtDoc.keySet().iterator(); it.hasNext(); ) {
                if(!result) break;
                int i = it.nextInt();
                result = Objects.equals(lvtDoc.get(i), ((LocalVariable) that).getLocalVariableDoc(i));
            }
            if(sL) for(IntIterator it = lvt.keySet().iterator(); it.hasNext(); ) {
                if(!result) break;
                int i = it.nextInt();
                result = Objects.equals(lvt.get(i), ((LocalVariableTable.Namespaced) that).getLocalVariableNames(i));
            }
            if(sO) result &= Objects.equals(LambdaUtil.safeCall(owner, owner -> owner.mapping),
                    LambdaUtil.safeCall(((Owned<NamespacedMapping>) that).getOwner(), owner -> owner.mapping));
            return result;
        } catch(ClassCastException cce) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        if(sU) {
            result = 31 * result + Objects.hashCode(unmappedDescriptor);
            result = 31 * result + Objects.hashCode(descriptorNamespace);
        }
        if(sD) result = 31 * result + Objects.hashCode(doc);
        if(sDL) result = 31 * result + lvtDoc.hashCode();
        if(sL) result = 31 * result + lvt.hashCode();
        if(sO) result = 31 * result + (owner == null ? 0 : Objects.hashCode(owner.mapping));
        return result;
    }

    @Override
    public String toString() {
        return "NamespacedImpl{" +
                "unmappedDescriptor='" + unmappedDescriptor + '\'' +
                ", descriptorNamespace='" + descriptorNamespace + '\'' +
                ", sU=" + sU +
                ", doc='" + doc + '\'' +
                ", sD=" + sD +
                ", lvtDoc=" + lvtDoc +
                ", sDL=" + sDL +
                ", lvt=" + lvt +
                ", sL=" + sL +
                ", owner=" + LambdaUtil.safeCall(owner, owner -> owner.mapping) +
                ", sO=" + sO +
                "} " + super.toString();
    }
}