package cn.maxpixel.mcdecompiler.mapping.trait;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;

/**
 * A trait that stores inheritance information statically
 */
public class InheritanceTrait implements MappingTrait {
    private final Object2ObjectOpenHashMap<String, List<String>> map = new Object2ObjectOpenHashMap<>();

    @Override
    public String getName() {
        return "inheritance";
    }

    public Object2ObjectOpenHashMap<String, List<String>> getMap() {
        return map;
    }

    public void put(String parent, String[] children) {
        map.put(parent, ObjectArrayList.wrap(children));
    }
}