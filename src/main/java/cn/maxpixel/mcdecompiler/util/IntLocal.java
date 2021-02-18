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

package cn.maxpixel.mcdecompiler.util;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.UUID;

public class IntLocal {
    private int defaultValue = 0;
    private final Object2IntOpenHashMap<UUID> map = new Object2IntOpenHashMap<>();
    public IntLocal() {}
    public IntLocal(int defaultValue) {
        this.defaultValue = defaultValue;
    }
    public UUID requestNew() {
        UUID uuid = UUID.randomUUID();
        map.put(uuid, defaultValue);
        return uuid;
    }
    public void delete(UUID uuid) {
        check(uuid);
        map.removeInt(uuid);
    }
    private void check(UUID uuid) {
        if(!map.containsKey(uuid))
            throw new IllegalArgumentException("Invalid identifier");
    }
    public void set(UUID uuid, int value) {
        check(uuid);
        map.put(uuid, value);
    }
    public int get(UUID uuid) {
        check(uuid);
        return map.getInt(uuid);
    }
    public void increment(UUID uuid) {
        check(uuid);
        map.computeInt(uuid, (u, i) -> i + 1);
    }
}