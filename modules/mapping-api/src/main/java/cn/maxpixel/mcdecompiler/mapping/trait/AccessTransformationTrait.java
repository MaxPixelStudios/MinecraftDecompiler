package cn.maxpixel.mcdecompiler.mapping.trait;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class AccessTransformationTrait implements MappingTrait {
    private final Object2IntOpenHashMap<String> map = new Object2IntOpenHashMap<>();

    @Override
    public String getName() {
        return "access-transformation";
    }

    public Object2IntOpenHashMap<String> getMap() {
        return map;
    }

    public void add(String name, int flag) {
        map.mergeInt(name, flag, (a, b) -> a | b);
    }
}