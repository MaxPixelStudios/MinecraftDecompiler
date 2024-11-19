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

package cn.maxpixel.mcdecompiler.mapping.parchment;

public record FormatVersion(int major, int minor, int patch) {
    public static FormatVersion CURRENT = new FormatVersion(1, 1, 0);

    public FormatVersion {
        if (major < 0) throw new IllegalArgumentException("Major version " + major + "must not be negative");
        if (minor < 0) throw new IllegalArgumentException("Minor version " + major + "must not be negative");
        if (patch < 0) throw new IllegalArgumentException("Patch version " + major + "must not be negative");
    }

    public static FormatVersion from(String version) {
        String[] parts = version.split("\\.");
        if (parts.length < 2) throw new IllegalArgumentException("Expected at least 2 tokens for version " + version);
        if (parts.length > 3) throw new IllegalArgumentException("Expected at most 3 tokens for version " + version);
        return new FormatVersion(
                Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1]),
                parts.length == 3 ? Integer.parseInt(parts[2]) : 0
        );
    }

    public boolean compatibleWith(FormatVersion v) {
        return major == v.major;
    }

    @Override
    public String toString() {
        return major + "." + minor + '.' + patch;// need at least 1 string to make sure this is string concatenation
    }
}
